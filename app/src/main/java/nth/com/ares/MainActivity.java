package nth.com.ares;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import nth.com.ares.classes.Mensaje;
import nth.com.ares.drawer.NavigationDrawerCallbacks;
import nth.com.ares.drawer.NavigationDrawerFragment;
import nth.com.ares.fragments.ChatFragmentList;
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


public class MainActivity extends AppCompatActivity implements NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private Toolbar mToolbar;

    private Boolean isLoggedIn = false;

    private UserLoginTask mAuthTask = null;

    public AbstractXMPPConnection connection;
    public MultiUserChatManager multiUserChatManager;
    public MultiUserChat multiUserChat;

    public String mUser;
    String mPass;

    private View mProgressView;

    private MainActivity context;

    ChatFragmentList chatFragmentList;

    View mLayoutMain;

    int historyLength;

    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;

        setContentView(R.layout.activity_main);

        mProgressView = findViewById(R.id.login_progress);
        mLayoutMain = findViewById(R.id.container);

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String defaultUser = "";
        String defaultPass = "";
        mUser = sharedPref.getString(getString(R.string.saved_user), defaultUser);
        mPass = sharedPref.getString(getString(R.string.saved_pass), defaultPass);
        mAuthTask = new UserLoginTask(mUser, mPass);
        mAuthTask.execute((Void) null);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);

        historyLength = Utils.getHistoryLength(context);

        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.fragment_drawer);

        // Set up the drawer.
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        mNavigationDrawerFragment.setup(R.id.fragment_drawer, drawerLayout, mToolbar);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        switch (position) {
            case Utils.CHAT_POS:
                chatFragmentList = new ChatFragmentList();
                Utils.openFragment(context, chatFragmentList, getString(R.string.chat_title));
                break;
            case Utils.ROOMS_POS:
                break;
            case Utils.SETTINGS_POS:
                break;
            case Utils.LOGOUT_POS:
                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getString(R.string.saved_user), "");
                editor.putString(getString(R.string.saved_pass), "");
                editor.apply();
                isLoggedIn = false;
                Intent intent = new Intent(context, LoginActivity.class);
                startActivity(intent);
                break;
        }
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
//        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_login) {
//            mNavigationDrawerFragment.login();
//            return true;
//        } else if (id == R.id.action_logout) {
//            mNavigationDrawerFragment.logout();
//            return true;
//        }

        return super.onOptionsItemSelected(item);
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
            // attempt authentication against a network service.
            Utils.log("XMPP", "Trying to establish connection: user=" + mUser + "  pass=" + mPassword);
            if (connection == null) {
                Utils.log("XMPP", "Connection not null");
                try {
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
                    Utils.log("XMPP", "LOGIN!! " + connection);
                    return true;
                } catch (Exception e) {
                    Utils.log("XMPP", "Catch");
                    e.printStackTrace();
                    return false;
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            Utils.log("XMPP", "On post execute " + success);
            mAuthTask = null;
            showProgress(false);
            if (success) {
                isLoggedIn = true;
                // populate the navigation drawer
                Utils.log("XMPP", "USER: " + mUser);
                mNavigationDrawerFragment.setUserData(mUser, "", BitmapFactory.decodeResource(getResources(), R.drawable.avatar));

                multiUserChatManager = MultiUserChatManager.getInstanceFor(connection);

                multiUserChat = multiUserChatManager.getMultiUserChat(Utils.getRoomName(context) + "@" +
                        Utils.getRoomService(context) + "." + Utils.SERVICE_NAME);
                DiscussionHistory histroy = new DiscussionHistory();
                histroy.setMaxStanzas(historyLength);

                try {
                    if (!multiUserChat.isJoined()) {
                        Utils.log("XMPP", "Trying to join muc");
                        multiUserChat.join(mUser, mPassword, histroy, SmackConfiguration.getDefaultPacketReplyTimeout());

                        multiUserChat.addMessageListener(new MessageListener() {
                            @Override
                            public void processMessage(Message message) {
                                String from = message.getFrom();
                                String[] parts = from.split("/");
                                if (parts.length > 1) {
                                    from = parts[1];
                                } else {
                                    from = "";
                                }
                                Mensaje mensaje = new Mensaje(message.getBody(), from, from.equalsIgnoreCase(mUser));
                                chatFragmentList.addMensaje(mensaje);
//                                chatFragmentList.showMessage(false, message.getFrom(), message.getBody());
                            }
                        });
                    }
                } catch (Exception e) {
                    Utils.log("XMPP", "Error joining muc");
                    e.printStackTrace();
                }
            } else {
                isLoggedIn = false;
                Intent intent = new Intent(context, LoginActivity.class);
//            String message = editText.getText().toString();
//            intent.putExtra(EXTRA_MESSAGE, message);
                startActivity(intent);
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

}
