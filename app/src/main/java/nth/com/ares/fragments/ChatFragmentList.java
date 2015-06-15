package nth.com.ares.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import nth.com.ares.MainActivity;
import nth.com.ares.R;
import nth.com.ares.adapters.ChatListArrayAdapter;
import nth.com.ares.domains.Mensaje;
import nth.com.ares.utils.Utils;
import org.jivesoftware.smack.packet.Message;

import java.util.ArrayList;

public class ChatFragmentList extends ListFragment {

    MainActivity context;
    ChatFragmentList este;

    ImageButton btnSend;
    EditText txtMensaje;
    ListView chatListView;

    int fontSize = 12;

    String roomName;
    String roomService;
    String serviceName;

    ArrayList<Mensaje> mensajes;

    ChatListArrayAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        Utils.log("LZM-CH-FR", "ON CREATE VIEW START");
        context = (MainActivity) getActivity();
        este = this;
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        mensajes = new ArrayList<>();

        roomName = Utils.getRoomName(context);
        roomService = Utils.getRoomService(context);
        serviceName = Utils.SERVICE_NAME;

        txtMensaje = (EditText) view.findViewById(R.id.txtMensaje);

        btnSend = (ImageButton) view.findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Utils.log("LZM-CH-FR", "BTN SEND: CLICK START");
                String texto = txtMensaje.getText().toString().trim();
                if (!texto.equals("")) {
                    context.isDoneLoading = true;
                    try {
//                        Utils.log("LZM-CH-FR", "TRYING TO SEND MESSAGE START");
                        Utils.log("XMPP", "Trying to send message");
                        Utils.log("XMPP", "Sending text [" + texto + "] to room [" + roomName + "]");
                        Message message = new Message();
                        message.setType(Message.Type.groupchat);
                        message.setBody(texto);
                        message.setTo(roomName);
                        context.multiUserChat.sendMessage(message);

//                        Mensaje mensaje = new Mensaje(context, texto, context.mUser, true);
//                        addMensaje(mensaje);

                        txtMensaje.setText("");

                        Utils.log("XMPP", "Message sent");
//                        Utils.log("LZM-CH-FR", "TRYING TO SEND MESSAGE END");
                    } catch (Exception e) {
                        Utils.log("XMPP", "Error sending message");
                        e.printStackTrace();
                    }
                }
//                Utils.log("LZM-CH-FR", "BTN SEND: CLICK END");
            }
        });
//        Utils.log("LZM-CH-FR", "ON CREATE VIEW END");
        return view;
    }

    public void addMensaje(final Mensaje mensaje) {
//        Utils.log("LZM-CH-FR", "ADD MENSAJE START");
        Utils.log("CHAT TEST", "sender: " + mensaje.sender + "   body: " + mensaje.body + "  enviando: " + mensaje.enviando);
        if (mensaje.sender != null && mensaje.body != null && !mensaje.body.trim().equals("")) {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    Utils.log("LZM-CH-FR", "ADD MENSAJE RUN ON UI THREAD START");
                    mensajes.add(mensaje);
                    adapter.notifyDataSetChanged();
//                    Utils.log("LZM-CH-FR", "ADD MENSAJE RUN ON UI THREAD END");
                }
            });
        }
//        Utils.log("LZM-CH-FR", "ADD MENSAJE END");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        Utils.log("LZM-CH-FR", "ON ACTIVITY CREATED START");
        adapter = new ChatListArrayAdapter(context, mensajes);
        setListAdapter(adapter);

        chatListView = getListView();
        chatListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String item = getListAdapter().getItem(position).toString();
                txtMensaje.setText(item);
//                Utils.toast(context, item + " selected");
                // Return true to consume the click event. In this case the
                // onListItemClick listener is not called anymore.
                return true;
            }
        });
//        Utils.log("LZM-CH-FR", "ON ACTIVITY CREATED END");
    }

//    @Override
//    public void onListItemClick(ListView l, View v, int position, long id) {
//        String item = getListAdapter().getItem(position).toString();
//        Utils.toast(context, item + " selected");
//    }

    @Override
    public void onResume() {
        super.onResume();
        context.setTitle(R.string.chat_title);
    }
}
