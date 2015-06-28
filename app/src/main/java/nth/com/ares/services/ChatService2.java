package nth.com.ares.services;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.delay.packet.DelayInformation;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import nth.com.ares.MainActivity;
import nth.com.ares.R;
import nth.com.ares.classes.XMPP;
import nth.com.ares.domains.Mensaje;
import nth.com.ares.utils.Utils;

/**
 * Created by svt on 19/06/2015.
 */
public class ChatService2 extends Service {
    Context context;
    public final static int SEND_M = 91;
    public final static int RECIBE_M = 92;
    public final static int LOGIN_M = 93;
    private NotificationManager nm;
    private static boolean isRunning = false;

    ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.
    int mValue = 0; // Holds last value set by a client.
    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_SET_INT_VALUE = 3;
    public static final int MSG_SET_STRING_VALUE = 4;
    public final int LOGIN_RESULT = 101;
    public final int SHOW_MESSAGE = 102;
    public static final int MSG_TEST = 109;
    final Messenger mMessenger = new Messenger(new IncomingHandler()); // Target we publish for clients to send messages to IncomingHandler.

    NotificationCompat.Builder mBuilder;
    int mNotificationId = 001;
    public AbstractXMPPConnection connection;
    public MultiUserChatManager multiUserChatManager;
    public MultiUserChat multiUserChat;
    public String mUser;
    public String mPass;
    public int isConected = 0;
    XMPP xmpp;
    private Timer timer = new Timer();

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    class IncomingHandler extends Handler { // Handler of incoming messages from clients.
        @Override
        public void handleMessage(Message msg) {
            Utils.log("ChatCom", "llego mensaje services " + msg.what);
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case MSG_TEST:
                    Utils.log("ChatCom", "EEEEEEEEEEEE   Entro test!!! " + msg.what);
                    if (!checkConnection())
                        xmpp.connect();
                    break;
                case MSG_SET_INT_VALUE:
                    if (msg.arg1 == LOGIN_M) {
                        Utils.log("ChatCom", "login " + isConected);
                        if (isConected == 0) {

                            SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                            String defaultUser = "";
                            String defaultPass = "";
                            mUser = sharedPref.getString(getString(R.string.saved_user), defaultUser);
                            mPass = sharedPref.getString(getString(R.string.saved_pass), defaultPass);
                            login();

                        }

                    }
                    break;
                case SEND_M:
                    try {
                        Utils.log("ChatCom", "service recibio " + msg.what + " " + msg.getData().getString("str1"));
                        org.jivesoftware.smack.packet.Message msn = new org.jivesoftware.smack.packet.Message();
                        msn.setType(org.jivesoftware.smack.packet.Message.Type.groupchat);
                        msn.setBody(msg.getData().getString("str1"));
                        msn.setTo(Utils.getRoomName(context));
                        multiUserChat.sendMessage(msn);

                        Utils.log("XMPP", "Message sent");
                    } catch (Exception e) {
                        Utils.log("XMPP", "error enviar mensaje");
                        e.printStackTrace();
                    }

                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    public void sendMessageToUI(int tipo, int intvaluetosend) {
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                // Send data as an Integer
                mClients.get(i).send(Message.obtain(null, tipo, intvaluetosend, 0));
                //Send data as a String
                Bundle b = new Bundle();
                b.putString("str1", "ab" + intvaluetosend + "cd");
                Message msg = Message.obtain(null, MSG_SET_STRING_VALUE);
                msg.setData(b);
                mClients.get(i).send(msg);
            } catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }

    private void sendMessage(String from, String date, String body) {
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {

                //Send data as a String
                Bundle b = new Bundle();
                b.putString("str1", from);
                b.putString("str2", date);
                b.putString("str3", body);
                Message msg = Message.obtain(null, SHOW_MESSAGE);
                msg.setData(b);
                mClients.get(i).send(msg);
            } catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("ChatCom", "Service Started.");
        showNotification();
        isRunning = true;
        context = this;
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String defaultUser = "";
        String defaultPass = "";
        mUser = sharedPref.getString(getString(R.string.saved_user), defaultUser);
        mPass = sharedPref.getString(getString(R.string.saved_pass), defaultPass);

    }

    private void showNotification() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("ChatCom", "Received start id " + startId + ": " + intent);
        login();
        return START_STICKY; // run until explicitly stopped.
    }

    public static boolean isRunning() {
        return isRunning;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        connection.disconnect();
        Log.i("ChatCom", "!!!OMG!!!!!!!!!!!! !!!!!!!!!!_-----------------Service Stopped.");
        isRunning = false;
        Intent intent = new Intent();
        intent.setAction("nth.com.ares.mybroadcast");
        sendBroadcast(intent);
        timer.cancel();
    }

    public void login() {
        Utils.log("ChatCom", "------------------->>>>>><<<<< >>>  Trying to establish connection: user=" + mUser + "  pass=" + mPass);
        if (mUser != null && mUser != "") {
            isConected = 0;
            xmpp = new XMPP(mUser, mPass, this);
            xmpp.connect();
            timer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    onTimerTick();
                }
            }, 1000 * 60, 1000 * 60 * 3);
            isRunning = true;

        }

    }

    public void logout() {
        Utils.log("CAHT", "LOGGING OUT");
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.saved_user), "");
        editor.putString(getString(R.string.saved_pass), "");
        editor.apply();

        if (connection != null) {
            connection.disconnect();
        }

    }

    public void setListener() {
        Utils.log("ChatCom", "añade listener");

        multiUserChat.addMessageListener(new MessageListener() {
            @Override
            public void processMessage(org.jivesoftware.smack.packet.Message message) {
                PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
                wakeLock.acquire();
                DelayInformation inf = null;
                Date date;
                try {
                    inf = (DelayInformation) message.getExtension("urn:xmpp:delay");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (inf != null) {
                    date = inf.getStamp();
                    //println "stored "+date
                } else {
                    date = new Date();
                }
                String df = android.text.format.DateFormat.format("HH:mm:ss", date).toString();
                String from = message.getFrom();
                String body = message.getBody();
                String[] parts = from.split("/");
                if (parts.length > 1) {
                    from = parts[1];
                } else {
                    from = "";
                }
                Utils.log("ChatCom", "----------------- LLEGO MENSAJE!!!!! " + from + "(" + df + ")" + ": " + body);

                if (body != null && !body.trim().equalsIgnoreCase("")) {
                    Utils.log("ChatCom", "paso primer if body not null ni vacio");
                    Mensaje msn = new Mensaje(context);
                    msn.setMensajeDbHelper();
                    int res = msn.mensajeDbHelper.countMensajeesByFecha(df);
                    Utils.log("ChatCom", "res " + res);
                    if (res == 0) {

                        msn.setBody(body);
                        msn.setSender(from);
                        msn.setFecha(df);
                        msn.user = mUser;

                    }

                    Utils.log("ChatCom", "paso segundo if from ");
                    if (isForeground("nth.com.ares")) {
                        Utils.log("ChatCom", "++++++++++++++is foreground ");
                        msn.setVisto("S");
                        sendMessage(from, df, body);
                    } else {
                        Utils.log("ChatCom", "++++++++++++++ notificacion ");
                        msn.setVisto("N");
                        Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.popup_notification);
                        long[] pattern = {0, 200, 500};
                        mBuilder =
                                new NotificationCompat.Builder(context)
                                        .setSmallIcon(R.drawable.notification_icon)
                                        .setVibrate(pattern)
                                        .setSound(soundUri, AudioManager.STREAM_NOTIFICATION)
                                        .setLights(Color.BLUE, 500, 500)
                                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                        .setPriority(NotificationCompat.PRIORITY_MAX)
                                        .setContentTitle(from + " (" + df + ")")
                                        .setStyle(new NotificationCompat.InboxStyle())
                                        .setAutoCancel(true)
                                        .setContentText(body.substring(4));

                        Intent resultIntent = new Intent(context, MainActivity.class);
                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                        stackBuilder.addParentStack(MainActivity.class);
                        stackBuilder.addNextIntent(resultIntent);
                        PendingIntent resultPendingIntent =
                                stackBuilder.getPendingIntent(
                                        0,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                );
                        mBuilder.setContentIntent(resultPendingIntent);

                        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        mNotifyMgr.notify(mNotificationId, mBuilder.build());
                    }
                    msn.save();
                }
                wakeLock.release();
            }
        });
    }

    public boolean checkConnection() {
        Utils.log("ChatCom", "++++++++++++++ check connection ");
        if (connection == null)
            return false;
        try {
            Roster roster = Roster.getInstanceFor(connection);
            Presence availability = roster.getPresence(mUser);
            Presence p = new Presence(Presence.Type.available, "A", 42, Presence.Mode.available);
            connection.sendPacket(p);
            String xml = availability.toXML().toString();
            Utils.log("ChatCom", "++++++++++++++  xml  " + xml);
            Utils.log("ChatCom", "++++++++++++++ is connected  " + connection.isConnected());
            Utils.log("ChatCom", "++++++++++++++ is auth  " + connection.isAuthenticated());
            if (!connection.isConnected()) {
                return false;
            } else {
                if (!connection.isAuthenticated()) {
                    xmpp.login((XMPPTCPConnection) connection);
                    return false;
                }
            }

            return true;
        } catch (Throwable t) { //you should always ultimately catch all exceptions in timer tasks.
            Utils.log("ChatCom", "++++++++++++++not connected");
            return false;
        }
    }

    public boolean isForeground(String myPackage) {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        return componentInfo.getPackageName().equals(myPackage);
    }

    private void onTimerTick() {
        if (!checkConnection())
            xmpp.connect();
    }
}
