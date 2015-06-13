package nth.com.ares.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;
import nth.com.ares.utils.Utils;

/**
 * Created by LUZ on 13-Jun-15.
 */
public class ChatService extends Service {

    private static final String TAG = "com.example.ServiceExample";
    Context context;

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

        Runnable r = new Runnable() {
            public void run() {

                for (int i = 0; i < 3; i++) {
                    long endTime = System.currentTimeMillis() + 10 * 1000;

                    while (System.currentTimeMillis() < endTime) {
                        synchronized (this) {
                            try {
                                wait(endTime -
                                        System.currentTimeMillis());
                            } catch (Exception e) {
                            }
                        }
                    }
                    Utils.log(TAG, "Service running " + currentId);
                }
                stopSelf();
            }
        };

        Thread t = new Thread(r);
        t.start();
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        Utils.log(TAG, "Service onBind");
        return null;
    }

    @Override
    public void onDestroy() {
        Utils.log(TAG, "Service onDestroy");
    }
}
