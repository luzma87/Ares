package nth.com.ares.classes;

import android.content.Context;
import android.os.AsyncTask;
import nth.com.ares.utils.Utils;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Responsible for establishing a connection between a client and an XMPP server.
 * Provides authentication and connection methods, and callbacks for handling
 * connection failures.
 * Library used: aSmack 4.0.0
 *
 * @author Richard Lopes
 * @version %I%, %G%
 * @since 1.0
 */

public class XMPP {
    private static final String TAG = "XMPP_CLASS";
    /**
     * Contains all information and methods about the management of the connection.
     */
    private XMPPTCPConnection connection;

    /**
     * User login name without domain name: {userName}@{domainName}
     */
    private String mUser;

    /**
     * User password
     */
    private String mPass;

    MultiUserChatManager multiUserChatManager;
    MultiUserChat multiUserChat;

    Context context;

    /**
     * Default constructor
     *
     * @param mUser
     * @param mPass
     */
    public XMPP(String mUser, String mPass, Context context) {
        this.mUser = mUser;
        this.mPass = mPass;
        this.context = context;
    }

    /**
     * Creates an AsyncTask to starts a connection usign serverAddress attribute from this class.
     * It also attach a listener  to handle with changes on connection, like fall down.
     */
    public void connect() {
        Utils.log(TAG, "connect");
        AsyncTask<Void, Void, Boolean> connectionThread = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... arg0) {
                boolean isConnected = false;

                XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                        .setUsernameAndPassword(mUser, mPass)
                        .setServiceName(Utils.SERVICE_NAME)
                        .setHost(Utils.SERVER_HOST)
                        .setPort(Utils.SERVER_PORT)
                        .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                        .build();
                connection = new XMPPTCPConnection(config);

                XMPPConnectionListener connectionListener = new XMPPConnectionListener();
                connection.addConnectionListener(connectionListener);

                try {
                    connection.connect();
                    isConnected = true;
                } catch (IOException e) {
                } catch (SmackException e) {
                } catch (XMPPException e) {
                }

                return isConnected;
            }
        };
        connectionThread.execute();
    }

    /**
     * Provides an authentication to the server using an username and a password.
     *
     * @param connection
     */
    private void login(XMPPTCPConnection connection) {
        Utils.log(TAG, "login");
        try {
            connection.login();

            multiUserChatManager = MultiUserChatManager.getInstanceFor(connection);

            int historyLength = Utils.getHistoryLength(context);

            multiUserChat = multiUserChatManager.getMultiUserChat(Utils.getRoomName(context) + "@" +
                    Utils.getRoomService(context) + "." + Utils.SERVICE_NAME);
            DiscussionHistory history = new DiscussionHistory();
            history.setMaxStanzas(0);

            multiUserChat.join(mUser, mPass, history, SmackConfiguration.getDefaultPacketReplyTimeout());

        } catch (SmackException.NotConnectedException e) {
            // If is not connected, a timer is schelude and a it will try to reconnect
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    connect();
                }
            }, 5 * 1000);
        } catch (XMPPException e) {
        } catch (SmackException e) {
        } catch (IOException e) {
        }
    }

    public XMPPTCPConnection getConnection() {
        return this.connection;
    }

    public MultiUserChat getMultiUserChat() {
        return this.multiUserChat;
    }

    /**
     * Listener for changes in connection
     *
     * @see ConnectionListener from org.jivesoftware.smack
     */
    public class XMPPConnectionListener implements ConnectionListener {
        @Override
        public void connected(XMPPConnection connection) {
            Utils.log(TAG, "connected");
            if (!connection.isAuthenticated()) {
                login((XMPPTCPConnection) connection);
                Utils.log(TAG, "connection not auth: login again");
            }
        }

        @Override
        public void authenticated(XMPPConnection connection, boolean resumed) {
            Utils.log(TAG, "authenticated   resumed? " + resumed);
        }

        @Override
        public void connectionClosed() {
            Utils.log(TAG, "connection closed");
        }

        @Override
        public void connectionClosedOnError(Exception arg0) {
            Utils.log(TAG, "connection closed on error");
        }

        @Override
        public void reconnectingIn(int arg0) {
            Utils.log(TAG, "reconnecting in");
        }

        @Override
        public void reconnectionFailed(Exception arg0) {
            Utils.log(TAG, "reconnection failed");
        }

        @Override
        public void reconnectionSuccessful() {
            Utils.log(TAG, "reconnection sucessful");
        }
    }
}