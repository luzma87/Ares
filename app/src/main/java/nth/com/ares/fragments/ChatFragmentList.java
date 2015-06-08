package nth.com.ares.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import nth.com.ares.MainActivity;
import nth.com.ares.R;
import nth.com.ares.adapters.ChatListArrayAdapter;
import nth.com.ares.classes.Mensaje;
import nth.com.ares.utils.Utils;
import org.jivesoftware.smack.packet.Message;

import java.util.ArrayList;

public class ChatFragmentList extends ListFragment {

    MainActivity context;

    ImageButton btnSend;
    EditText txtMensaje;
    ListView chatListView;

    int fontSize = 12;

    String roomName;
    String roomService;
    String serviceName;

    ArrayList<Mensaje> mensajes;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        context = (MainActivity) getActivity();
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

                        txtMensaje.setText("");

                        Utils.log("XMPP", "Message sent");
                    } catch (Exception e) {
                        Utils.log("XMPP", "Error sending message");
                        e.printStackTrace();
                    }
                }
            }
        });

        return view;
    }


    public void addMensaje(Mensaje mensaje) {
        mensajes.add(mensaje);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ChatListArrayAdapter adapter = new ChatListArrayAdapter(context, mensajes);
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
