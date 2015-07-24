package nth.com.ares;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import nth.com.ares.classes.MyLocation;
import nth.com.ares.domains.DbHelper;
import nth.com.ares.domains.Mensaje;
import nth.com.ares.domains.MensajeDbHelper;
import nth.com.ares.drawer.NavigationDrawerCallbacks;
import nth.com.ares.drawer.NavigationDrawerFragment;
import nth.com.ares.fragments.ChatFragment;
import nth.com.ares.services.ChatService2;
import nth.com.ares.utils.Utils;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity implements NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private Toolbar mToolbar;

    public boolean isDoneLoading = false;
    public String mUser;
    String mPass;
    String logged = "N";

    Boolean botonesShown = true;

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


    List<Mensaje> leidos;
    List<Mensaje> noLeidos;

    public final int LOGIN_RESULT = 101;
    public final int SHOW_MESSAGE = 102;
    Messenger mService = null;
    boolean mIsBound;
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            Utils.log("ChatCom", "activity recibio " + msg.what + " ");
            switch (msg.what) {
                case LOGIN_RESULT:

                    break;
                case SHOW_MESSAGE:
                    Utils.log("ChatCom", "recibe m");
                    Mensaje msn = new Mensaje(context);
                    msn.fecha = msg.getData().getString("str2");
                    msn.sender = msg.getData().getString("str1");
                    msn.body = msg.getData().getString("str3");
                    msn.user = mUser;
                    chatFragmentList.showMessage(msn);
                    break;
                case ChatService2.MSG_SET_STRING_VALUE:
                    Utils.log("ChatCom", "activity recibio " + msg.what + " " + msg.getData().getString("str1"));
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            Utils.log("ChatCom", "atached");
            try {
                android.os.Message msg = android.os.Message.obtain(null, ChatService2.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mService = null;
            Utils.log("ChatCom", "disconected");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isMyServiceRunning(ChatService2.class);
        DbHelper helper = new DbHelper(this);
        helper.getWritableDatabase();
        context = this;
        vaAlLogin = false;
        setContentView(R.layout.activity_main);

        mProgressView = findViewById(R.id.login_progress);
        mLayoutMain = findViewById(R.id.container);

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String defaultUser = "";
        String defaultPass = "";
        mUser = sharedPref.getString(getString(R.string.saved_user), defaultUser);
        mPass = sharedPref.getString(getString(R.string.saved_pass), defaultPass);
        logged = sharedPref.getString(getString(R.string.logged), "N");
        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
        historyLength = Utils.getHistoryLength(context);
        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.fragment_drawer);
        // Set up the drawer.
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        mNavigationDrawerFragment.setup(R.id.fragment_drawer, drawerLayout, mToolbar,this);
        dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());

//        Utils.log("LZM-MN-AC", "ON CREATE END");
        Utils.log("LZM_ACTIVITY", "ON CREATE");


    }

    @Override
    protected void onStart() {
        super.onStart();
        Utils.log("LZM_ACTIVITY", "ON START " + logged);
        try {
            doBindService();

        } catch (Throwable t) {
            Log.e("ChatCom", "Failed to unbind from the service", t);
        }
        if (logged.equals("N")) {
            Utils.log("LZM_ACTIVITY", "va al login");
            Intent intent = new Intent(context, LoginActivity.class);
            startActivity(intent);
            vaAlLogin = true;
        } else {
            loadMensajes();
            sendMessageToService(ChatService2.MSG_TEST);
        }

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Utils.log("SERVICE", "RUNNING!!!!");
                doBindService();
                return true;
            }
        }
        Utils.log("SERVICE", "STOPPED");
        Intent intent2 = new Intent(this, ChatService2.class);
        this.startService(intent2);

        return false;
    }

    private void CheckIfServiceIsRunning() {
        //If the service is running when the activity starts, we want to automatically bind to it.
        Utils.log("ChatCom", "check if running");
        if (ChatService2.isRunning()) {
            doBindService();
        }
    }


    public void sendMessageToService(int intvaluetosend) {
        if (mIsBound) {
            if (mService != null) {
                try {
                    android.os.Message msg = android.os.Message.obtain(null, ChatService2.MSG_SET_INT_VALUE, intvaluetosend, 0);
                    msg.replyTo = mMessenger;
                    mService.send(msg);

                } catch (RemoteException e) {
                }
            }
        }
    }

    public void sendMessage(String body) {

        try {

            //Send data as a String
            Bundle b = new Bundle();
            b.putString("str1", body);
            android.os.Message msg = android.os.Message.obtain(null, ChatService2.SEND_M);
            msg.setData(b);
            mService.send(msg);
        } catch (RemoteException e) {
            // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.

        }

    }

    void doBindService() {
        bindService(new Intent(this, ChatService2.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        Utils.log("ChatCom", "binding");
    }

    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mService != null) {
                try {
                    android.os.Message msg = android.os.Message.obtain(null, ChatService2.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
            Utils.log("ChatCom", "unbinding");
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
        chatFragmentList.layoutMessages.removeAllViews();
        chatFragmentList.mapas.clear();
        Utils.log("LZM_ACTIVITY", "ON PAUSE");

    }

    @Override
    protected void onStop() {
        super.onStop();
        Utils.log("LZM_ACTIVITY", "ON STOP");


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            doUnbindService();
        } catch (Throwable t) {
            Log.e("ChatCom", "Failed to unbind from the service", t);
        }


    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
//        Utils.log("LZM-MN-AC", "ON NAVGATION DRAWER ITEM SELECTED START");
        switch (position) {
            case Utils.CHAT_POS:
                Utils.log("POS","POS!!!! "+chatFragmentList);
                if(chatFragmentList!=null) {
                    chatFragmentList.layoutMessages.removeAllViews();
                    chatFragmentList.mapas.clear();
                }
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
        editor.putString(getString(R.string.logged), "N");
        editor.apply();

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
        int id = item.getItemId();

        if (id == R.id.action_toggle) {
            if (botonesShown) {
                chatFragmentList.scrollViewBotones.setVisibility(View.GONE);
                item.setIcon(R.drawable.ic_arrow_left);
                item.setTitle(R.string.mostrar_botones);
                botonesShown = false;
            } else {
                chatFragmentList.scrollViewBotones.setVisibility(View.VISIBLE);
                item.setIcon(R.drawable.ic_arrow_right);
                item.setTitle(R.string.ocultar_botones);
                botonesShown = true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void sendMyLoc(final String pref) {
        progress = ProgressDialog.show(this, getString(R.string.espere), getString(R.string.calculando_ubicacion), true);
        MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
            @Override
            public void gotLocation(Location location) {
                //Got the location!
                String str = getString(R.string.btn_ubicacion_msg);
                chatFragmentList.setMessage(pref, str + " " + location.getLatitude() + "," + location.getLongitude());
                progress.dismiss();
            }
        };
        MyLocation myLocation = new MyLocation();
        myLocation.getLocation(this, locationResult);
    }

    public void loadMensajes() {
        Utils.log("main act", "load mensajes");
        chatFragmentList.clean();
        MensajeDbHelper db = new MensajeDbHelper(this);
        leidos = db.getAllMensajeesByVisto("S");
        Utils.log("main act ", "leios " + leidos);
        noLeidos = db.getAllMensajeesByVisto("N");
        Utils.log("main act ", "no leios " + noLeidos);
        for (Mensaje msn : leidos) {
            chatFragmentList.showMessage(msn);
        }
        for (Mensaje msn : noLeidos) {
            msn.setVisto("S");
            msn.save();
            chatFragmentList.showMessage(msn);
        }
        if (!mIsBound)
            doBindService();
    }


}
