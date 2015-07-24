package nth.com.ares.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.xgc1986.ripplebutton.widget.RippleButton;
import com.xgc1986.ripplebutton.widget.RippleImageButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import it.sephiroth.android.library.tooltip.TooltipManager;
import nth.com.ares.MainActivity;
import nth.com.ares.R;
import nth.com.ares.classes.MiniMapFragment;
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
    public LinearLayout layoutMessages;
    public ArrayList<MiniMapFragment> mapas;

    public ScrollView scrollViewBotones;

    private final String[] botonesIds = {
            "asalto",
            "accidente",
            "sospechoso",
            "intruso",
            "libadores",
            "ubicacion"
    };
    private final String[] mostrarMapa = {
            "asL:",
            "acL:",
            "ssL:",
            "inL:",
            "lbL:",
            "loc:"
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
        mapas = new ArrayList<>();
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
        layoutMessages.removeAllViews();
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
                    boolean esMapa=false;
                    LatLng ubicacion;
                    Utils.log("LZM", prefix + "     mensaje:     " + msg+"  "+msg.indexOf("Mi"));
                    if(Arrays.asList(mostrarMapa).contains(prefix)) {
                        if(msg.indexOf("Mi")>-1){
                            Utils.log("MAPA","Es mapa con ubicacion");
                            esMapa = true;
                            String[] parts =msg.split(":");
                            parts = parts[1].split(",");
                            ubicacion=new LatLng(Double.parseDouble(parts[0]),Double.parseDouble(parts[1]));
                        }else{
                            Utils.log("MAPA","Es mapa");
                            esMapa = true;
                            String[] parts = msg.split(",");
                            ubicacion=new LatLng(Double.parseDouble(parts[0]),Double.parseDouble(parts[1]));
                        }
                    }else{
                        ubicacion=null;
                    }
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


//                    LinearLayout.LayoutParams lpM = (LinearLayout.LayoutParams) txvMensaje.getLayoutParams();
                    LinearLayout.LayoutParams lpM = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    lpM.setMargins(dp5, dp5, dp5, dp5);

                    TextView txvFecha = new TextView(context);
                    txvFecha.setText(fecha);
//                    txvFecha.setTextAppearance(context, android.R.style.TextAppearance_Small);
                    txvFecha.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize - 2);

                    if (esMio) {
                        txvMensaje.setText(msg);
                        txvMensaje.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
                        View emptyView = new View(context);
                        LinearLayout.LayoutParams lpE = new LinearLayout.LayoutParams(0, 0, 1f);
                        lpE.gravity = Gravity.BOTTOM;
                        lpE.setMargins(dp5, 0, 0, dp1);
                        emptyView.setLayoutParams(lpE);

                        txvMensaje.setBackgroundResource(R.drawable.bubble_mio);
                        txvMensaje.setTextColor(context.getResources().getColor(R.color.mensaje_mio_text));
                        txvMensaje.setPadding(dp5, dp5, dp15, dp5);
                        lpM.gravity = Gravity.END;
                        if(esMapa){
                            LinearLayout lv = new LinearLayout(context);
                            Random r = new Random();
                            int id = r.nextInt(19990000);
                            lv.setId(id);
                            lv.setOrientation(LinearLayout.VERTICAL);
                            LinearLayout.LayoutParams LLParamsVert = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                            LLParamsVert.setMargins(5, 5, 5, 10);
                            lv.setLayoutParams(LLParamsVert);
                            lv.setBackgroundResource(R.drawable.bubble_mio);
                            lv.setPadding(dp5, dp5, dp15, dp5);
                            MiniMapFragment mMapFragment = MiniMapFragment.newInstance(ubicacion,botonesIds[Arrays.asList(mostrarMapa).indexOf(prefix)]);
                            getFragmentManager().beginTransaction().add( lv.getId(),mMapFragment, "addMap").commit();
                            linearLayout.addView(emptyView);
                            linearLayout.addView(lv);
                            linearLayout.addView(txvFecha);
                            mapas.add(mMapFragment);
                        }else{
                            txvMensaje.setText(msg);
                            linearLayout.addView(emptyView);
                            linearLayout.addView(txvMensaje);
                            linearLayout.addView(txvFecha);
                        }

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

                        if(esMapa){
                            LinearLayout lv = new LinearLayout(context);
                            Random r = new Random();
                            int id = r.nextInt(19990000);
                            lv.setId(id);
                            lv.setOrientation(LinearLayout.VERTICAL);
                            LLParamsVert.setMargins(5,5,5,10);
                            lv.setLayoutParams(LLParamsVert);
                            lv.setBackgroundResource(R.drawable.bubble_recibe);
                            lv.setPadding(dp15, dp5, dp5, dp5);
                            MiniMapFragment mMapFragment = MiniMapFragment.newInstance(ubicacion,botonesIds[Arrays.asList(mostrarMapa).indexOf(prefix)]);
                            getFragmentManager().beginTransaction().add( lv.getId(),mMapFragment, "addMap").commit();
                            linearLayout.addView(linearLayoutVert);
                            linearLayout.addView(lv);
                            mapas.add(mMapFragment);
                        }else{
                            txvMensaje.setText(msg);
                            txvMensaje.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
                            linearLayout.addView(linearLayoutVert);
                            linearLayout.addView(txvMensaje);
                        }


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
