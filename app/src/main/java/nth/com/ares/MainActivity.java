package nth.com.ares;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.google.android.gms.common.api.GoogleApiClient;
import nth.com.ares.classes.MyLocation;
import nth.com.ares.domains.Mensaje;
import nth.com.ares.drawer.NavigationDrawerCallbacks;
import nth.com.ares.drawer.NavigationDrawerFragment;
import nth.com.ares.fragments.ChatFragment;
import nth.com.ares.services.ChatService;
import nth.com.ares.utils.Utils;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.delay.packet.DelayInformation;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;

import java.text.DateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity implements NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private Toolbar mToolbar;

    private UserLoginTask mAuthTask = null;

    public AbstractXMPPConnection connection;
    public MultiUserChatManager multiUserChatManager;
    public MultiUserChat multiUserChat;

    public boolean isDoneLoading = false;
    public String mUser;
    String mPass;

    private View mProgressView;

    private MainActivity context;

    ChatFragment chatFragmentList;

    View mLayoutMain;

    int historyLength;

    DrawerLayout drawerLayout;

    ProgressDialog progress;

    NotificationCompat.Builder mBuilder;
    int mNotificationId = 001;

    DateFormat dateFormat;
    boolean vaAlLogin = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        Utils.log("LZM-MN-AC", "ON CREATE START");

        context = this;
        vaAlLogin=false;
        setContentView(R.layout.activity_main);

        mProgressView = findViewById(R.id.login_progress);
        mLayoutMain = findViewById(R.id.container);

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String defaultUser = "";
        String defaultPass = "";
        mUser = sharedPref.getString(getString(R.string.saved_user), defaultUser);
        mPass = sharedPref.getString(getString(R.string.saved_pass), defaultPass);


        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);

        historyLength = Utils.getHistoryLength(context);

        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.fragment_drawer);

        // Set up the drawer.
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        mNavigationDrawerFragment.setup(R.id.fragment_drawer, drawerLayout, mToolbar);

        dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());

//        Utils.log("LZM-MN-AC", "ON CREATE END");
        Utils.log("LZM_ACTIVITY", "ON CREATE");


    }

    @Override
    protected void onStart() {
        super.onStart();
        Utils.log("LZM_ACTIVITY", "ON START");
        if (isMyServiceRunning(ChatService.class)) {
            Intent serviceIntent = new Intent(ChatService.class.getName());
            serviceIntent.setPackage("com.android.vending");
            stopService(serviceIntent);
        }
        if(mUser!=null && mUser!=""){

            mAuthTask = new UserLoginTask(mUser, mPass);
            mAuthTask.execute((Void) null);
        }else{
            Utils.log("LZM_ACTIVITY", "va al login");
            Intent intent = new Intent(context, LoginActivity.class);
            startActivity(intent);
            vaAlLogin = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.log("LZM_ACTIVITY", "ON RESUME");


    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.log("LZM_ACTIVITY", "ON PAUSE");
        if(connection!=null)
            connection.disconnect();
        if (!isMyServiceRunning(ChatService.class) && !vaAlLogin) {
            Intent intent = new Intent(this, ChatService.class);
            startService(intent);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Utils.log("LZM_ACTIVITY", "ON STOP");


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.log("LZM_ACTIVITY", "ON DESTROY");


    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
//        Utils.log("LZM-MN-AC", "ON NAVGATION DRAWER ITEM SELECTED START");
        switch (position) {
            case Utils.CHAT_POS:
                chatFragmentList = new ChatFragment();
                Utils.openFragment(context, chatFragmentList, getString(R.string.chat_title));
                break;
            case Utils.SETTINGS_POS:
                break;
            case Utils.LOGOUT_POS:
                logout();
                break;
        }
//        Utils.log("LZM-MN-AC", "ON NAVGATION DRAWER ITEM SELECTED END");
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
        vaAlLogin = true;
        Intent intent = new Intent(context, LoginActivity.class);
        startActivity(intent);

    }

    @Override
    public void onBackPressed() {
        if (mNavigationDrawerFragment.isDrawerOpen())
            mNavigationDrawerFragment.closeDrawer();
        else
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_login) {
//            mNavigationDrawerFragment.login();
//            return true;
//        } else if (id == R.id.action_logout) {
//            mNavigationDrawerFragment.logout();
//            return true;
//        }
        if (id == R.id.action_asalto) {
            chatFragmentList.setMessage("Asalto! Necesito ayuda!");
            sendMyLoc();
        } else if (id == R.id.action_ubicacion) {
            sendMyLoc();
        } else if (id == R.id.action_accidente) {
            chatFragmentList.setMessage("Accidente! Necesito ayuda!");
            sendMyLoc();
        } else if (id == R.id.action_ambulancia) {
            chatFragmentList.setMessage("Necesito una ambulancia!");
            sendMyLoc();
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendMyLoc() {
        progress = ProgressDialog.show(this, getString(R.string.espere), getString(R.string.calculando_ubicacion), true);
        MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
            @Override
            public void gotLocation(Location location) {
                //Got the location!
                chatFragmentList.setMessage("loc:" + location.getLatitude() + "," + location.getLongitude());
                progress.dismiss();
            }
        };
        MyLocation myLocation = new MyLocation();
        myLocation.getLocation(this, locationResult);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Utils.log("SERVICE", "RUNNING!!!!");
                return true;
            }
        }
        Utils.log("SERVICE", "STOPPED");
        return false;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLayoutMain.setVisibility(show ? View.GONE : View.VISIBLE);
            mLayoutMain.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLayoutMain.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLayoutMain.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUser;
        private final String mPassword;

        UserLoginTask(String user, String password) {
            mUser = user;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
//            Utils.log("LZM-MN-AC", "DO IN BACKGROUND START");
            // attempt authentication against a network service.
            Utils.log("XMPP", "Trying to establish connection: user=" + mUser + "  pass=" + mPassword);
            if (connection == null) {
                Utils.log("XMPP", "Connection not null");
                try {
//                    Utils.log("LZM-MN-AC", "TRYING TO CONNECT START");
                    Utils.log("XMPP", "Try");
                    XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                            .setUsernameAndPassword(mUser, mPassword)
                            .setServiceName(Utils.SERVICE_NAME)
                            .setHost(Utils.SERVER_HOST)
                            .setPort(Utils.SERVER_PORT)
                            .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                            .build();
                    connection = new XMPPTCPConnection(config);
                    connection.connect();
                    connection.login();
//                    Utils.log("LZM-MN-AC", "TRYING TO CONNECT END");
                    Utils.log("XMPP", "LOGIN!! " + connection);
                    return true;
                } catch (Exception e) {
//                    Utils.log("LZM-MN-AC", "ERROR CONNECTING");
                    Utils.log("XMPP", "Catch");
                    e.printStackTrace();
                    logout();
                    return false;
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            Utils.log("XMPP", "On post execute!!1 " + success);
//            Utils.log("LZM-MN-AC", "ON POST EXECUTE SUCCESS: " + success + " START");
            //mAuthTask = null;
            //showProgress(false);
            if (success) {
//                Utils.log("LZM-MN-AC", "ON POST EXECUTE SUCCESS START");
                // populate the navigation drawer
                Utils.log("XMPP !!! ", "USER: " + mUser);
                mNavigationDrawerFragment.setUserData(mUser, "", BitmapFactory.decodeResource(getResources(), R.drawable.avatar));

                multiUserChatManager = MultiUserChatManager.getInstanceFor(connection);

                multiUserChat = multiUserChatManager.getMultiUserChat(Utils.getRoomName(context) + "@" +
                        Utils.getRoomService(context) + "." + Utils.SERVICE_NAME);
                DiscussionHistory histroy = new DiscussionHistory();
                histroy.setMaxStanzas(historyLength);

                try {
//                    Utils.log("LZM-MN-AC", "TRYING TO JOIN ROOM START");
                    if (!multiUserChat.isJoined()) {
//                        Utils.log("LZM-MN-AC", "NOT JOINED START");
                        Utils.log("XMPP", "Trying to join muc");
                        multiUserChat.join(mUser, mPassword, histroy, SmackConfiguration.getDefaultPacketReplyTimeout());

                        multiUserChat.addMessageListener(new MessageListener() {
                            @Override
                            public void processMessage(Message message) {
//                                Utils.log("LZM-MN-AC", "CHAT LISTENER START");

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

//                                Utils.log("MSG", from + ": " + body + " (" + date + ")   -   " + df);

                                Mensaje mensaje = new Mensaje(context, body, from, df, false);
                                if (mensaje.mostrar()) {
//                                    chatFragmentList.addMensaje(mensaje);
                                    chatFragmentList.showMessage(mensaje);
                                }

//                                try {
////                                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//                                    Uri notification = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.popup_notification);
//                                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
//                                    r.play();
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }

//                                if (isDoneLoading && !from.equalsIgnoreCase(mUser)) {
//                                if (isDoneLoading) {
//                                Utils.vibrate(context);
//                                Utils.log("LZM", "from=" + from + "   mUser=" + mUser);
//                                if (!from.equalsIgnoreCase(mUser)) {
//
//                                    // Start immediately, Vibrate for 200 milliseconds, Sleep for 500 milliseconds
//                                    long[] pattern = {0, 200, 500};
//
//                                    //Define sound URI
//                                    //default sound
////                                    Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
////                                    notification.defaults |= Notification.DEFAULT_SOUND;
//                                    //default vibrate
////                                    Notification.DEFAULT_VIBRATE;
////                                    notification.defaults |= Notification.DEFAULT_VIBRATE;
//                                    Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.popup_notification);
//
//                                    mBuilder =
//                                            new NotificationCompat.Builder(context)
//                                                    .setSmallIcon(R.drawable.notification_icon)
//                                                    .setVibrate(pattern)
//                                                    .setSound(soundUri, AudioManager.STREAM_NOTIFICATION)
//                                                    .setLights(Color.BLUE, 500, 500)
//                                                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
//                                                    .setPriority(NotificationCompat.PRIORITY_MAX)
//                                                    .setContentTitle(from)
//                                                    .setStyle(new NotificationCompat.InboxStyle())
//                                                    .setContentText(body);
//
//                                    // Creates an explicit intent for an Activity in your app
//                                    Intent resultIntent = new Intent(context, MainActivity.class);
//                                    // The stack builder object will contain an artificial back stack for the
//                                    // started Activity.
//                                    // This ensures that navigating backward from the Activity leads out of
//                                    // your application to the Home screen.
//                                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
//                                    // Adds the back stack for the Intent (but not the Intent itself)
//                                    stackBuilder.addParentStack(MainActivity.class);
//                                    // Adds the Intent that starts the Activity to the top of the stack
//                                    stackBuilder.addNextIntent(resultIntent);
//                                    PendingIntent resultPendingIntent =
//                                            stackBuilder.getPendingIntent(
//                                                    0,
//                                                    PendingIntent.FLAG_UPDATE_CURRENT
//                                            );
//                                    mBuilder.setContentIntent(resultPendingIntent);
//
//                                    // Gets an instance of the NotificationManager service
//                                    NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//                                    // Builds the notification and issues it.
//                                    mNotifyMgr.notify(mNotificationId, mBuilder.build());
//                                }
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
                    Utils.toast(context, getString(R.string.error_joining_room));
                    logout();
//                    Utils.log("LZM-MN-AC", "ERROR JOINING ROOM END");
                }
                Utils.log("XMPP", "muc joined 2!!!");
//                Utils.log("LZM-MN-AC", "ON POST EXECUTE SUCCESS END");
            } else {
//                Utils.log("LZM-MN-AC", "ON POST EXECUTE FAIL START");
                logout();
//                Intent intent = new Intent(context, LoginActivity.class);
//            String message = editText.getText().toString();
//            intent.putExtra(EXTRA_MESSAGE, message);
//                startActivity(intent);
//                Utils.log("LZM-MN-AC", "ON POST EXECUTE FAIL END");
            }
//            Utils.log("LZM-MN-AC", "ON POST EXECUTE SUCCESS: " + success + " END");
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

}
