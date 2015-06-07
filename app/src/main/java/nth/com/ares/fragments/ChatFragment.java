package nth.com.ares.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import nth.com.ares.MainActivity;
import nth.com.ares.R;
import nth.com.ares.utils.Utils;
import org.jivesoftware.smack.packet.Message;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatFragment extends Fragment {

    MainActivity context;

    Button btnSend;
    EditText txtMensaje;
    ScrollView scrollViewMessages;
    LinearLayout layoutMessages;

    public int screenHeight;
    public int screenWidth;

    int fontSize = 12;

    String roomName;
    String roomService;
    String serviceName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        context = (MainActivity) getActivity();
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        roomName = Utils.getRoomName(context);
        roomService = Utils.getRoomService(context);
        serviceName = Utils.SERVICE_NAME;

        btnSend = (Button) view.findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String texto = txtMensaje.getText().toString().trim();
                if (!texto.equals("")) {
                    try {
                        Log.i("XMPP", "Trying to send message");
                        Log.i("XMPP", "Sending text [" + texto + "] to room [" + roomName + "]");
                        Message message = new Message();
                        message.setType(Message.Type.groupchat);
                        message.setBody(texto);
                        message.setTo(roomName);
                        context.multiUserChat.sendMessage(message);

                        showMessage(true, message.getFrom(), texto);
                        txtMensaje.setText("");

                        Log.i("XMPP", "Message sent");
                    } catch (Exception e) {
                        Log.i("XMPP", "Error sending message");
                        e.printStackTrace();
                    }
                }
            }
        });

        txtMensaje = (EditText) view.findViewById(R.id.txtMensaje);

        scrollViewMessages = (ScrollView) view.findViewById(R.id.scrollViewMessages);
        layoutMessages = (LinearLayout) view.findViewById(R.id.layoutMessages);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenHeight = displaymetrics.heightPixels;
        screenWidth = displaymetrics.widthPixels;

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        context.setTitle(R.string.chat_title);
    }

    public void showMessage(final boolean mio, final String sender, final String mensaje) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (sender != null && mensaje != null) {
                    Boolean esMio = mio;
                    String find = roomName + "@" + roomService + "\\." + serviceName + "/";
                    String sentBy = "";
                    sentBy += sender;
                    sentBy = sentBy.replaceAll("(?i)" + find, "");

                    if (sentBy.equalsIgnoreCase(context.mUser)) {
                        esMio = true;
                    }
                    Locale current = getResources().getConfiguration().locale;
                    DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", current);
                    Date date = new Date();
//                    DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();

                    TextView txvChunk = new TextView(context);
                    txvChunk.setText(sentBy + " at " + dateFormat.format(date) + ": " + mensaje);
                    txvChunk.setMaxWidth(screenWidth - 20);
                    txvChunk.setTextAppearance(context, R.style.chunk);
                    if (esMio) {
                        txvChunk.setBackgroundResource(R.drawable.selector_sent);
                        txvChunk.setPadding(10, 10, 20, 10);
                        txvChunk.setGravity(Gravity.START | Gravity.LEFT);
                    } else {
                        txvChunk.setBackgroundResource(R.drawable.selector_received);
                        txvChunk.setPadding(20, 10, 10, 10);
                        txvChunk.setGravity(Gravity.END | Gravity.RIGHT);
                    }
                    txvChunk.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(txvChunk.getMeasuredWidth(), ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(5, 5, 2, 0);

                    layoutMessages.addView(txvChunk);
                } else {
                    Log.i("XMPP", "Typing");
                }
            }
        });
    }

}
