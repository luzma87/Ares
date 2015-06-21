package nth.com.ares.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.widget.Toast;

import nth.com.ares.utils.Utils;

/**
 * Created by svt on 19/06/2015.
 */
public class ChatReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager mgr = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        wakeLock.acquire();
        Intent intent2 = new Intent(context, ChatService2.class);
        context.startService(intent2);
        Utils.log("ChatReceiver", "recibed");
        wakeLock.release();
    }
}
