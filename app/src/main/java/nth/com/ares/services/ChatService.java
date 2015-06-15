package nth.com.ares.services;

import android.app.ActivityManager;
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
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;
import nth.com.ares.LoginActivity;
import nth.com.ares.MainActivity;
import nth.com.ares.R;
import nth.com.ares.utils.Utils;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;

import java.util.List;

/**
 * Created by LUZ on 13-Jun-15.
 */
public class ChatService extends Service {

    private static final String TAG = "com.example.ServiceExample";
    Context context;

    String mUser;
    String mPass;

    boolean isOpen;

    int historyLength;

    AbstractXMPPConnection connection;
    MultiUserChatManager multiUserChatManager;
    MultiUserChat multiUserChat;

    NotificationCompat.Builder mBuilder;
    int mNotificationId = 001;
    private LocalBroadcastManager broadcaster;

    static final public String CHAT_RESULT = "nth.com.ares.ChatService.REQUEST_PROCESSED";
    static final public String CHAT_FROM = "nth.com.ares.ChatService.CHAT_FROM";
    static final public String CHAT_MESSAGE = "nth.com.ares.ChatService.CHAT_MSG";

    @Override
    public void onCreate() {
        Utils.log(TAG, "Service onCreate");
        broadcaster = LocalBroadcastManager.getInstance(this);
        context = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Utils.log(TAG, "Service onStartCommand " + startId);
        Toast.makeText(context, "Service onStartCommand", Toast.LENGTH_LONG).show();

        final int currentId = startId;

        final Handler toastMaker = new Handler() {
            @Override
            public void handleMessage(android.os.Message msg) {
                String mString = (String) msg.obj;
                Toast.makeText(context, mString, Toast.LENGTH_SHORT).show();
            }
        };

        Runnable r = new Runnable() {
            public void run() {
                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                String defaultUser = "";
                String defaultPass = "";
                mUser = sharedPref.getString(getString(R.string.saved_user), defaultUser);
                mPass = sharedPref.getString(getString(R.string.saved_pass), defaultPass);

                isOpen = isForeground("nth.com.ares");

                Utils.log("XMPP", "Trying to establish connection");
                if (connection == null) {
                    Utils.log("XMPP", "Connection null");
                    try {
                        Utils.log("XMPP", "Try");
                        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                                .setUsernameAndPassword(mUser, mPass)
                                .setServiceName(Utils.SERVICE_NAME)
                                .setHost(Utils.SERVER_HOST)
                                .setPort(Utils.SERVER_PORT)
                                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                                .build();
                        connection = new XMPPTCPConnection(config);
                        connection.connect();
                        connection.login();
                        Utils.log("XMPP", "LOGIN!! " + connection);
                        Utils.log("XMPP", "USER: " + mUser);

//                        if (isOpen) {
//                            Utils.log("SERVICE", "IS FOREGROUND");
//                            Intent intent = new Intent(context, MainActivity.class);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            startActivity(intent);
//                        } else {
//                            Utils.log("SERVICE", "IS NOOOOT FOREGROUND");
//                        }

                        multiUserChatManager = MultiUserChatManager.getInstanceFor(connection);

                        multiUserChat = multiUserChatManager.getMultiUserChat(Utils.getRoomName(context) + "@" +
                                Utils.getRoomService(context) + "." + Utils.SERVICE_NAME);
                        DiscussionHistory history = new DiscussionHistory();
                        history.setMaxStanzas(0);

                        try {
//                    Utils.log("LZM-MN-AC", "TRYING TO JOIN ROOM START");
                            if (!multiUserChat.isJoined()) {
//                        Utils.log("LZM-MN-AC", "NOT JOINED START");
                                historyLength = Utils.getHistoryLength(context);
                                Utils.log("XMPP", "Trying to join muc");
                                multiUserChat.join(mUser, mPass, history, SmackConfiguration.getDefaultPacketReplyTimeout());
//                                multiUserChat.join(mUser, mPass);

                                multiUserChat.addMessageListener(new MessageListener() {
                                    @Override
                                    public void processMessage(Message message) {
//                                Utils.log("LZM-MN-AC", "CHAT LISTENER START");
                                        String from = message.getFrom();
                                        String body = message.getBody();
                                        String[] parts = from.split("/");
                                        if (parts.length > 1) {
                                            from = parts[1];
                                        } else {
                                            from = "";
                                        }
//                                        Utils.log("MENSAJE", from + ": " + body);
//                                        Mensaje mensaje = new Mensaje(mUser, body, from, false);
//                                        if (mensaje.mostrar()) {
//                                    chatFragmentList.addMensaje(mensaje);
//                                            chatFragmentList.showMessage(mensaje);
//                                        sendResult(from, body);
//                                        }

//                                        android.os.Message msg = new android.os.Message();
//                                        msg.obj = from + ": " + body;
//                                        toastMaker.sendMessage(msg);
//                                        try {
//                                            Thread.sleep(100);
//                                        } catch (InterruptedException e) {
//                                            e.printStackTrace();
//                                        }
                                        if (body != null && !body.trim().equalsIgnoreCase("")) {
                                            if (!from.equalsIgnoreCase(mUser)) {

                                                // Start immediately, Vibrate for 200 milliseconds, Sleep for 500 milliseconds

                                                //Define sound URI
                                                //default sound
//                                    Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//                                    notification.defaults |= Notification.DEFAULT_SOUND;
                                                //default vibrate
//                                    Notification.DEFAULT_VIBRATE;
//                                    notification.defaults |= Notification.DEFAULT_VIBRATE;
                                                Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.popup_notification);

//                                            if (!isOpen) {
                                                long[] pattern = {0, 200, 500};
                                                mBuilder =
                                                        new NotificationCompat.Builder(context)
                                                                .setSmallIcon(R.drawable.notification_icon)
                                                                .setVibrate(pattern)
                                                                .setSound(soundUri, AudioManager.STREAM_NOTIFICATION)
                                                                .setLights(Color.BLUE, 500, 500)
                                                                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                                                .setContentTitle(from)
                                                                .setStyle(new NotificationCompat.InboxStyle())
                                                                .setContentText(body);

                                                // Creates an explicit intent for an Activity in your app
                                                Intent resultIntent = new Intent(context, MainActivity.class);
                                                // The stack builder object will contain an artificial back stack for the
                                                // started Activity.
                                                // This ensures that navigating backward from the Activity leads out of
                                                // your application to the Home screen.
                                                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                                                // Adds the back stack for the Intent (but not the Intent itself)
                                                stackBuilder.addParentStack(MainActivity.class);
                                                // Adds the Intent that starts the Activity to the top of the stack
                                                stackBuilder.addNextIntent(resultIntent);
                                                PendingIntent resultPendingIntent =
                                                        stackBuilder.getPendingIntent(
                                                                0,
                                                                PendingIntent.FLAG_UPDATE_CURRENT
                                                        );
                                                mBuilder.setContentIntent(resultPendingIntent);

                                                // Gets an instance of the NotificationManager service
                                                NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                                // Builds the notification and issues it.
                                                mNotifyMgr.notify(mNotificationId, mBuilder.build());
//                                            } else {
//
//                                            }
                                            }
                                        }

//                                }
//                                }

//                                Utils.log("LZM-MN-AC", "CHAT LISTENER END");
//                                chatFragmentList.showMessage(false, message.getFrom(), message.getBody());
                                    }
                                });
                                Utils.log("XMPP", "muc joined!!!");
//                        Utils.log("LZM-MN-AC", "NOT JOINED END");
                            }
//                    Utils.log("LZM-MN-AC", "TRYING TO JOIN ROOM END");
                        } catch (Exception e) {
//                    Utils.log("LZM-MN-AC", "ERROR JOINING ROOM START");
                            Utils.log("XMPP", "Error joining muc");
                            e.printStackTrace();
//                            Utils.toast(context, getString(R.string.error_joining_room));
                            stopSelf();
//                            logout();
//                    Utils.log("LZM-MN-AC", "ERROR JOINING ROOM END");
                        }
                        Utils.log("XMPP", "muc joined 2!!!");
//                Utils.log("LZM-MN-AC", "ON POST EXECUTE SUCCESS END");
                    } catch (Exception e) {
                        Utils.log("XMPP", "Catch");

                        if (isOpen) {
                            Utils.log("SERVICE", "IS FOREGROUND");

//                            android.os.Message msg = new android.os.Message();
//                            msg.obj = getString(R.string.login_failed);
//                            toastMaker.sendMessage(msg);

//                            Intent intent = new Intent(context, LoginActivity.class);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            startActivity(intent);
                        } else {
                            Utils.log("SERVICE", "IS NOOOOT FOREGROUND");
                        }

                        e.printStackTrace();
                        stopSelf();
                    }
                } else {
                    Utils.log("XMPP", "connection not null?");
                }

            } //end run
        };

        Thread t = new Thread(r);
        t.start();
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        Utils.log(TAG, "Service onBind");
        Toast.makeText(context, "Service onBind", Toast.LENGTH_LONG).show();
        return null;
    }

    @Override
    public void onDestroy() {
        Utils.log(TAG, "Service onDestroy");
        Toast.makeText(context, "Service onDestroy", Toast.LENGTH_LONG).show();
        if (connection != null) {
            connection.disconnect();
            connection = null;
        }
    }

    public boolean isForeground(String myPackage) {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        return componentInfo.getPackageName().equals(myPackage);
    }

//    public void sendResult(String from, String message) {
//        Intent intent = new Intent(CHAT_RESULT);
//        if (from != null)
//            intent.putExtra(CHAT_FROM, from);
//        if (message != null)
//            intent.putExtra(CHAT_MESSAGE, message);
//        broadcaster.sendBroadcast(intent);
//    }
}
