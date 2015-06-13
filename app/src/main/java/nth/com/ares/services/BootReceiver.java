package nth.com.ares.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import nth.com.ares.utils.Utils;

/**
 * Created by LUZ on 13-Jun-15.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent myIntent = new Intent(context, ChatService.class);
            context.startService(myIntent);
            Toast.makeText(context, "Service Started", Toast.LENGTH_LONG).show();
        }

        String action = intent.getAction();
        if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
            Toast.makeText(context, "Service_PowerUp Started", Toast.LENGTH_LONG).show();
        } else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
            Toast.makeText(context, "Service_PowerUp Stopped", Toast.LENGTH_LONG).show();
        }
    }
}
