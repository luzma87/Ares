package nth.com.ares.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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
import nth.com.ares.classes.Mensaje;
import nth.com.ares.utils.Utils;
import org.jivesoftware.smack.packet.Message;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatFragment extends Fragment {

    MainActivity context;

    ImageButton btnSend;
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

        btnSend = (ImageButton) view.findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String texto = txtMensaje.getText().toString().trim();
                if (!texto.equals("")) {
                    try {
                        Utils.log("XMPP", "Trying to send message");
                        Utils.log("XMPP", "Sending text [" + texto + "] to room [" + roomName + "]");
                        Message message = new Message();
                        message.setType(Message.Type.groupchat);
                        message.setBody(texto);
                        message.setTo(roomName);
                        context.multiUserChat.sendMessage(message);

                        showMessage(true, message.getFrom(), texto);
                        txtMensaje.setText("");

                        Utils.log("XMPP", "Message sent");
                    } catch (Exception e) {
                        Utils.log("XMPP", "Error sending message");
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

                    int dp1 = Utils.pixels2dp(context, 1);
                    int dp5 = Utils.pixels2dp(context, 5);
                    int dp15 = Utils.pixels2dp(context, 15);

                    LinearLayout linearLayout = new LinearLayout(context);
                    linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                    LinearLayout.LayoutParams LLParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    linearLayout.setLayoutParams(LLParams);

                    TextView txvMensaje = new TextView(context);
                    txvMensaje.setText(mensaje);

//                    LinearLayout.LayoutParams lpM = (LinearLayout.LayoutParams) txvMensaje.getLayoutParams();
                    LinearLayout.LayoutParams lpM = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    lpM.setMargins(dp5, dp5, dp5, dp5);
                    if (esMio) {
                        View emptyView = new View(context);
                        LinearLayout.LayoutParams lpE = new LinearLayout.LayoutParams(0, 0, 1f);
                        lpE.gravity = Gravity.BOTTOM;
                        lpE.setMargins(dp5, 0, 0, dp1);
                        emptyView.setLayoutParams(lpE);

                        txvMensaje.setBackgroundResource(R.drawable.bubble_mio);
                        txvMensaje.setTextColor(context.getResources().getColor(R.color.mensaje_mio_text));
                        txvMensaje.setPadding(dp5, dp5, dp15, dp5);
                        lpM.gravity = Gravity.END;

                        linearLayout.addView(emptyView);
                        linearLayout.addView(txvMensaje);
                    } else {
                        TextView txvUsuario = new TextView(context);
                        txvUsuario.setText(sentBy);

                        txvMensaje.setBackgroundResource(R.drawable.bubble_recibe);
                        txvMensaje.setTextColor(context.getResources().getColor(R.color.mensaje_recibe_text));
                        txvMensaje.setPadding(dp15, dp5, dp5, dp5);
                        lpM.gravity = Gravity.START;

                        linearLayout.addView(txvUsuario);
                        linearLayout.addView(txvMensaje);
                    }
                    txvMensaje.setLayoutParams(lpM);
                    layoutMessages.addView(linearLayout);
                    /* ****************************************************************** */

//                    TextView txvChunk = new TextView(context);
//                    txvChunk.setText(mensaje);
//                    txvChunk.setMaxWidth(screenWidth - 20);
//                    txvChunk.setTextAppearance(context, R.style.chunk);
//                    if (esMio) {
//                        txvChunk.setBackgroundResource(R.drawable.selector_sent);
//                        txvChunk.setPadding(10, 10, 20, 10);
//                        txvChunk.setGravity(Gravity.END);
//                    } else {
//                        txvChunk.setBackgroundResource(R.drawable.selector_received);
//                        txvChunk.setPadding(20, 10, 10, 10);
//                        txvChunk.setGravity(Gravity.START);
//                    }
//                    txvChunk.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
//
//                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(txvChunk.getMeasuredWidth(), ViewGroup.LayoutParams.WRAP_CONTENT);
//                    params.setMargins(5, 5, 2, 0);
//
//                    layoutMessages.addView(txvChunk);

                    sendScroll();
//                    scrollViewMessages.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            scrollViewMessages.fullScroll(View.FOCUS_DOWN);
//                        }
//                    });
                } else {
                    Utils.log("XMPP", "Typing");
                }
            }
        });
    }

    private void sendScroll() {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollViewMessages.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        }).start();
    }

    public void showMessage(Mensaje mensaje) {
        String sender = mensaje.sender;
        String mns = mensaje.body;
        Boolean mio = mensaje.esMio();
        showMessage(mio, sender, mns);
    }

}
