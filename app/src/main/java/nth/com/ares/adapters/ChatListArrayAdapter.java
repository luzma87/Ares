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
import nth.com.ares.classes.Mensaje;
import nth.com.ares.utils.Utils;

import java.util.ArrayList;

/**
 * Created by LUZ on 07-Jun-15.
 */
public class ChatListArrayAdapter extends ArrayAdapter<Mensaje> {
    private final Context context;
    private final ArrayList<Mensaje> mensajes;

    static class ViewHolder {
        public TextView lblUser;
        public TextView lblMessage;
        public View lblEmpty;
    }

    public ChatListArrayAdapter(Context context, ArrayList<Mensaje> mensajes) {
        super(context, R.layout.fragment_chat_list_row, mensajes);
        this.context = context;
        this.mensajes = mensajes;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Mensaje current = mensajes.get(position);
        Utils.log("LZM-LS_AD", "****************************************************************************************");
        Utils.log("LZM-LS_AD", "GET VIEW START: " + current.sender + ": " + current.body);
        View rowView = convertView;
        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.fragment_chat_list_row, null);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.lblUser = (TextView) rowView.findViewById(R.id.lbl_username);
            viewHolder.lblMessage = (TextView) rowView.findViewById(R.id.lbl_mns);
            viewHolder.lblEmpty = rowView.findViewById(R.id.lbl_empty);
            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        Utils.log("LZM-LS_AD", "GET VIEW CURRENT: " + current.sender + ": " + current.body + " MOSTRAR? " + current.mostrar());
        if (current.mostrar()) {
            Utils.log("LZM-LS_AD", "MOSTRANDO MENSAJE START: " + current.sender + ": " + current.body);
            String mns = current.body;
            holder.lblMessage.setText(mns);
            String from = current.sender;
            holder.lblUser.setText(from);

            LinearLayout.LayoutParams lpM = (LinearLayout.LayoutParams) holder.lblMessage.getLayoutParams();
            //Check whether message is mine to show blue background and align to right
            if (current.esMio()) {
                Utils.log("LZM-LS_AD", "MENSAJE ES MIO: " + current.sender + ": " + current.body);
                holder.lblMessage.setBackgroundResource(R.drawable.bubble_mio);
                holder.lblMessage.setTextColor(context.getResources().getColor(R.color.mensaje_mio_text));
                holder.lblMessage.setPadding(Utils.pixels2dp(context, 5), Utils.pixels2dp(context, 5), Utils.pixels2dp(context, 15), Utils.pixels2dp(context, 5));
                lpM.gravity = Gravity.END;
                holder.lblUser.setVisibility(View.GONE);
                holder.lblEmpty.setVisibility(View.VISIBLE);
            }
            //If not mine then it is from sender to show orange background and align to left
            else {
                Utils.log("LZM-LS_AD", "MENSAJE NO ES MIO: " + current.sender + ": " + current.body);
                holder.lblMessage.setBackgroundResource(R.drawable.bubble_recibe);
                holder.lblMessage.setTextColor(context.getResources().getColor(R.color.mensaje_recibe_text));
                holder.lblMessage.setPadding(Utils.pixels2dp(context, 15), Utils.pixels2dp(context, 5), Utils.pixels2dp(context, 5), Utils.pixels2dp(context, 5));
                lpM.gravity = Gravity.START;
                holder.lblUser.setVisibility(View.VISIBLE);
                holder.lblEmpty.setVisibility(View.GONE);
            }
            holder.lblMessage.setLayoutParams(lpM);
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
