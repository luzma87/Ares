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
import nth.com.ares.classes.XMPP;
import nth.com.ares.utils.Utils;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.delay.packet.DelayInformation;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;

import java.util.Date;
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

    AbstractXMPPConnection connection;
    MultiUserChat multiUserChat;

    NotificationCompat.Builder mBuilder;
    int mNotificationId = 001;

    XMPP xmpp;

    @Override
    public void onCreate() {
        Utils.log(TAG, "Service onCreate");
        context = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Utils.log(TAG, "Service onStartCommand " + startId);
        Toast.makeText(context, "Service onStartCommand", Toast.LENGTH_LONG).show();

        final int currentId = startId;

//        final Handler toastMaker = new Handler() {
//            @Override
//            public void handleMessage(android.os.Message msg) {
//                String mString = (String) msg.obj;
//                Toast.makeText(context, mString, Toast.LENGTH_SHORT).show();
//            }
//        };

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String defaultUser = "";
        String defaultPass = "";
        mUser = sharedPref.getString(getString(R.string.saved_user), defaultUser);
        mPass = sharedPref.getString(getString(R.string.saved_pass), defaultPass);

        xmpp = new XMPP(mUser, mPass, context);
        xmpp.connect();

        connection = xmpp.getConnection();
        multiUserChat = xmpp.getMultiUserChat();

        Runnable r = new Runnable() {
            public void run() {

                while (multiUserChat == null) {
                    try {
                        Thread.sleep(1000);
                        connection = xmpp.getConnection();
                        multiUserChat = xmpp.getMultiUserChat();
                        Utils.log("LZM", "dentro del while:   " + connection + "   " + multiUserChat);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                Utils.log("LZM", "dentro del runnable run");
                isOpen = isForeground("nth.com.ares");

                multiUserChat.addMessageListener(new MessageListener() {
                    @Override
                    public void processMessage(Message message) {

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
                        String df = android.text.format.DateFormat.format("dd-MM-yy HH:mm:ss", date).toString();

                        String from = message.getFrom();
                        String body = message.getBody();
                        String[] parts = from.split("/");
                        if (parts.length > 1) {
                            from = parts[1];
                        } else {
                            from = "";
                        }
                        Utils.log("PROCESS MESSAGE", from + "(" + df + ")" + ": " + body);
                        if (body != null && !body.trim().equalsIgnoreCase("")) {
                            if (!from.equalsIgnoreCase(mUser)) {
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
                                                .setContentText(body);

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
                        }
                    }
                });
                Utils.log("LZM", "fin del runnable run");
            } //end run
        };

        Utils.log("LZM", "UNO");
        Thread t = new Thread(r);
        t.start();
        Utils.log("LZM", "DOS");
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
}
