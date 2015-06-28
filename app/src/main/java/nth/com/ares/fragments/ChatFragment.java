package nth.com.ares.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.xgc1986.ripplebutton.widget.RippleButton;
import com.xgc1986.ripplebutton.widget.RippleImageButton;
import it.sephiroth.android.library.tooltip.TooltipManager;
import nth.com.ares.MainActivity;
import nth.com.ares.R;
import nth.com.ares.domains.Mensaje;
import nth.com.ares.utils.Utils;


/*
        https://github.com/sephiroth74/android-target-tooltip
        https://github.com/xgc1986/RippleViews
 */

public class ChatFragment extends Fragment {

    MainActivity context;

    ImageButton btnSend;
    EditText txtMensaje;
    ScrollView scrollViewMessages;
    LinearLayout layoutMessages;

    public ScrollView scrollViewBotones;

    private final String[] botonesIds = {
            "asalto",
            "accidente",
            "sospechoso",
            "intruso",
            "libadores",
            "ubicacion"
    };
    RippleImageButton[] botones = new RippleImageButton[botonesIds.length];
    String[] botonesTitle = new String[botonesIds.length];
    String[] botonesMsg = new String[botonesIds.length];
    String[] botonesPrefix = new String[botonesIds.length];
    String[] botonesPrefixLoc = new String[botonesIds.length];

    public int screenHeight;
    public int screenWidth;

    int fontSize = 12;

    String roomName;
    String roomService;
    String serviceName;

    private void sendMessage() {
        String pref = Utils.getStringResourceByName(context, "mensaje_prefix");
        sendMessage(pref);
    }

    private void sendMessage(String prefix) {
        String texto = txtMensaje.getText().toString().trim();
        if (!texto.equals("")) {
            try {
                context.sendMessage(prefix + ":" + texto);
                txtMensaje.setText("");
            } catch (Exception e) {
                Utils.log("XMPP", "Error sending message");
                e.printStackTrace();
            }
        }
    }

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
                sendMessage();
            }
        });

        for (int i = 0; i < botonesIds.length; i++) {
            final int pos = i;
            String id = botonesIds[i];
            int resID = getResources().getIdentifier("btn_" + id, "id", context.getPackageName());
            botones[i] = (RippleImageButton) view.findViewById(resID);
            botonesPrefix[i] = Utils.getStringResourceByName(context, "btn_" + id + "_prefix");
            botonesPrefixLoc[i] = Utils.getStringResourceByName(context, "btn_" + id + "_prefix_loc");
            botonesTitle[i] = Utils.getStringResourceByName(context, "btn_" + id + "_title");
            botonesMsg[i] = Utils.getStringResourceByName(context, "btn_" + id + "_msg");

            botones[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TooltipManager.getInstance(context)
                            .create(pos)
                            .anchor(botones[pos], TooltipManager.Gravity.LEFT)
                            .closePolicy(TooltipManager.ClosePolicy.TouchOutside, 3000)
                            .activateDelay(800)
                            .text(botonesTitle[pos])
                            .maxWidth(500)
                            .show();
                }
            });

            final String postfix = context.getString(R.string.mensaje_postfix);

            botones[i].setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Utils.vibrate(200, context);
                    if (pos < 5) {
                        setMessage(botonesPrefix[pos], botonesMsg[pos] + " " + postfix);
                    }
                    context.sendMyLoc(botonesPrefixLoc[pos]);
                    return true;
                }
            });
        }

        txtMensaje = (EditText) view.findViewById(R.id.txtMensaje);

        scrollViewMessages = (ScrollView) view.findViewById(R.id.scrollViewMessages);
        scrollViewBotones = (ScrollView) view.findViewById(R.id.scrollViewBotones);
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

    public void clean() {
        if (layoutMessages != null)
            layoutMessages.removeAllViews();
    }

    public void showMessage(final boolean mio, final String sender, final String mensaje, final String fecha) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (sender != null && mensaje != null) {

                    String prefix = mensaje.substring(0, 4);
                    String msg = mensaje.substring(4);

                    Utils.log("LZM", prefix + "          " + msg);

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
                    txvMensaje.setText(msg);
                    txvMensaje.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);

//                    LinearLayout.LayoutParams lpM = (LinearLayout.LayoutParams) txvMensaje.getLayoutParams();
                    LinearLayout.LayoutParams lpM = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    lpM.setMargins(dp5, dp5, dp5, dp5);

                    TextView txvFecha = new TextView(context);
                    txvFecha.setText(fecha);
//                    txvFecha.setTextAppearance(context, android.R.style.TextAppearance_Small);
                    txvFecha.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize - 2);

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
                        linearLayout.addView(txvFecha);
                    } else {
                        LinearLayout linearLayoutVert = new LinearLayout(context);
                        linearLayoutVert.setOrientation(LinearLayout.VERTICAL);
                        LinearLayout.LayoutParams LLParamsVert = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        linearLayoutVert.setLayoutParams(LLParamsVert);

                        TextView txvUsuario = new TextView(context);
                        txvUsuario.setText(sentBy);
                        txvUsuario.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);

                        txvMensaje.setBackgroundResource(R.drawable.bubble_recibe);
                        txvMensaje.setTextColor(context.getResources().getColor(R.color.mensaje_recibe_text));
                        txvMensaje.setPadding(dp15, dp5, dp5, dp5);
                        lpM.gravity = Gravity.START;

                        linearLayoutVert.addView(txvUsuario);
                        linearLayoutVert.addView(txvFecha);

                        linearLayout.addView(linearLayoutVert);
                        linearLayout.addView(txvMensaje);
                    }
                    txvMensaje.setLayoutParams(lpM);
                    layoutMessages.addView(linearLayout);
                    /* ****************************************************************** */

                    sendScroll();

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
        String fecha = mensaje.fecha;
        if (fecha.length() > 18)
            fecha = fecha.substring(10, 19);
        Boolean mio = mensaje.esMio();
        showMessage(mio, sender, mns, fecha);
    }

    public void setMessage(String prefix, String message) {
        txtMensaje.setText(message);
        txtMensaje.setSelection(txtMensaje.getText().length());
        sendMessage(prefix);
    }

}
