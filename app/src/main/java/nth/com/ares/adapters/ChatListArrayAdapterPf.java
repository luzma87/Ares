package nth.com.ares.adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import nth.com.ares.R;
import nth.com.ares.domains.Mensaje;
import nth.com.ares.utils.Utils;

import java.util.ArrayList;

/**
 * Created by LUZ on 07-Jun-15.
 */
public class ChatListArrayAdapterPf extends ArrayAdapter<Mensaje> {
    private final Context context;
    private final ArrayList<Mensaje> mensajes;

    public ChatListArrayAdapterPf(Context context, ArrayList<Mensaje> mensajes) {
        super(context, R.layout.fragment_chat_list_row, mensajes);
        this.context = context;
        this.mensajes = mensajes;
    }

    @Override
    public View getView(int position, View rowView, ViewGroup parent) {
        Mensaje current = mensajes.get(position);
        Utils.log("LZM-LS_AD", "****************************************************************************************");
        Utils.log("LZM-LS_AD", "GET VIEW START: " + current.sender + ": " + current.body);
//        View rowView = convertView;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rowView = inflater.inflate(R.layout.fragment_chat_list_row, null);

        TextView lblUser = (TextView) rowView.findViewById(R.id.lbl_username);
        TextView lblMessage = (TextView) rowView.findViewById(R.id.lbl_mns);
        View lblEmpty = rowView.findViewById(R.id.lbl_empty);

        Utils.log("LZM-LS_AD", "GET VIEW CURRENT: " + current.sender + ": " + current.body + " MOSTRAR? " + current.mostrar());
        if (current.mostrar()) {
            Utils.log("LZM-LS_AD", "MOSTRANDO MENSAJE START: " + current.sender + ": " + current.body);
            String mns = current.body;
            lblMessage.setText(mns);
            String from = current.sender;
            lblUser.setText(from);

            LinearLayout.LayoutParams lpM = (LinearLayout.LayoutParams) lblMessage.getLayoutParams();
            //Check whether message is mine to show blue background and align to right
            if (current.esMio()) {
                Utils.log("LZM-LS_AD", "MENSAJE ES MIO: " + current.sender + ": " + current.body);
                lblMessage.setBackgroundResource(R.drawable.bubble_mio);
                lblMessage.setTextColor(context.getResources().getColor(R.color.mensaje_mio_text));
                lblMessage.setPadding(Utils.pixels2dp(context, 5), Utils.pixels2dp(context, 5), Utils.pixels2dp(context, 15), Utils.pixels2dp(context, 5));
                lpM.gravity = Gravity.END;
                lblUser.setVisibility(View.GONE);
                lblEmpty.setVisibility(View.VISIBLE);
            }
            //If not mine then it is from sender to show orange background and align to left
            else {
                Utils.log("LZM-LS_AD", "MENSAJE NO ES MIO: " + current.sender + ": " + current.body);
                lblMessage.setBackgroundResource(R.drawable.bubble_recibe);
                lblMessage.setTextColor(context.getResources().getColor(R.color.mensaje_recibe_text));
                lblMessage.setPadding(Utils.pixels2dp(context, 15), Utils.pixels2dp(context, 5), Utils.pixels2dp(context, 5), Utils.pixels2dp(context, 5));
                lpM.gravity = Gravity.START;
                lblUser.setVisibility(View.VISIBLE);
                lblEmpty.setVisibility(View.GONE);
            }
            lblMessage.setLayoutParams(lpM);
            Utils.log("LZM-LS_AD", "MOSTRANDO MENSAJE END: " + current.sender + ": " + current.body);
        }
        Utils.log("LZM-LS_AD", "GET VIEW END: " + current.sender + ": " + current.body);
        Utils.log("LZM-LS_AD", "****************************************************************************************");
        return rowView;
    }

    public Mensaje getItem(int pos) {
        return mensajes.get(pos);
    }
}
