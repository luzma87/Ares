package nth.com.ares.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import nth.com.ares.MainActivity;
import nth.com.ares.R;

/**
 * Created by LUZ on 06-Jun-15.
 */
public class Utils {

    public final static String SERVER_HOST = "167.114.144.175";
    public final static int SERVER_PORT = 5222;
    public final static String SERVICE_NAME = "vps44751.vps.ovh.ca";

    public final static int CHAT_POS = 0;
    public final static int SETTINGS_POS = 1;
    public final static int LOGOUT_POS = 2;

    /*
    <div>Icons made by <a href="http://www.flaticon.com/authors/freepik" title="Freepik">Freepik</a> from <a href="http://www.flaticon.com" title="Flaticon">www.flaticon.com</a>             is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0">CC BY 3.0</a></div>
    <div>Icons made by <a href="http://www.flaticon.com/authors/nice-and-serious" title="Nice and Serious">Nice and Serious</a> from <a href="http://www.flaticon.com" title="Flaticon">www.flaticon.com</a>             is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0">CC BY 3.0</a></div>
    <div>Icons made by <a href="http://www.flaticon.com/authors/ocha" title="OCHA">OCHA</a> from <a href="http://www.flaticon.com" title="Flaticon">www.flaticon.com</a>             is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0">CC BY 3.0</a></div>

    icono
    <a href='http://www.freepik.com/free-vector/metallic-shield_794455.htm'>Designed by Freepik</a>
     */

    public static int getHistoryLength(MainActivity context) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        int defaultLength = 20;
        return sharedPref.getInt(context.getString(R.string.history_length), defaultLength);
    }

    public static String getRoomName(MainActivity context) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String defaultName = "Jipijapa";
        return sharedPref.getString(context.getString(R.string.room_name), defaultName);
    }

    public static String getRoomService(MainActivity context) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String defaultService = "conference";
        return sharedPref.getString(context.getString(R.string.room_service), defaultService);
    }

    public static void openFragment(MainActivity context, Fragment fragment, String title) {
        openFragment(context, fragment, title, null);
    }

    public static void openFragment(MainActivity context, Fragment fragment, String title, Bundle args) {
        openFragment(context, fragment, title, args, true);
    }

    public static void openFragment(MainActivity context, Fragment fragment, String title, Bundle args, boolean backstack) {
        context.setTitle(title);
//        FragmentManager fragmentManager = context.getFragmentManager();
        FragmentManager fragmentManager = context.getSupportFragmentManager();
        if (fragment != null) {
            if (args != null) {
                fragment.setArguments(args);
            }
//                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
            if (backstack) {
                fragmentManager.beginTransaction()
//                        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                        .replace(R.id.container, fragment)
                        .addToBackStack("")
                        .commit();
            } else {
                fragmentManager.beginTransaction()
//                        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                        .replace(R.id.container, fragment)
                        .commit();
            }
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
//            e.printStackTrace();
        }
//        try {
//            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
//            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
//        } catch (Exception e) {
////            e.printStackTrace();
//        }
    }

    public static void showSoftKeyboard(Activity activity, View view) {
        try {
            InputMethodManager keyboard = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
//            keyboard.showSoftInput(view, 0);
            activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
//            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    public static void toast(Context context, String s) {
        Toast.makeText(context, s, Toast.LENGTH_LONG).show();
    }

    public static void log(String cod, String text) {
        Log.i(cod, text);
    }

    public static void vibrate(Context context) {
        vibrate(100, context);
    }

    public static void vibrate(int length, Context context) {
        Vibrator v1 = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for [length] milliseconds
        v1.vibrate(length);

        // Output yes if can vibrate, no otherwise
//        if (v1.hasVibrator()) {
//            Log.v("Can Vibrate", "YES");
//        } else {
//            Log.v("Can Vibrate", "NO");
//        }
    }

    public static void vibrate(long[] pattern, Context context) {
        // Start without a delay
        // Vibrate for 100 milliseconds
        // Sleep for 1000 milliseconds
        // long[] pattern = {0, 100, 1000};

        // The first value indicates the number of milliseconds to wait before turning the vibrator ON: 0=Start without a delay
        // Each element then alternates between vibrate, sleep, vibrate, sleep...
        // long[] pattern = {0, 100, 1000, 300, 200, 100, 500, 200, 100};
        Vibrator v1 = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        // The '-1' here means to vibrate once, as '-1' is out of bounds in the pattern array
        //     '0' here means to repeat indefinitely
        //     '0' is actually the index at which the pattern keeps repeating from (the start)
        //     To repeat the pattern from any other point, you could increase the index, e.g. '1'
        v1.vibrate(pattern, -1);
    }

    public static void playSound(Context context, int sound) {
        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float percent = 0.7f;
        int seventyVolume = (int) (maxVolume * percent);
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, seventyVolume, 0);
        final MediaPlayer mp = MediaPlayer.create(context, sound);
        mp.start();
    }

    public static int pixels2dp(Context context, int pixels) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pixels * scale + 0.5f);
    }
}
