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
    }

    public ChatListArrayAdapter(Context context, ArrayList<Mensaje> mensajes) {
        super(context, R.layout.chat_list_row, mensajes);
        this.context = context;
        this.mensajes = mensajes;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.chat_list_row, null);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.lblUser = (TextView) rowView.findViewById(R.id.lbl_username);
            viewHolder.lblMessage = (TextView) rowView.findViewById(R.id.lbl_mns);
            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        Mensaje current = mensajes.get(position);
        String mns = current.body;
        holder.lblMessage.setText(mns);
        String from = current.sender;
        holder.lblUser.setText(from);

        LinearLayout.LayoutParams lpM = (LinearLayout.LayoutParams) holder.lblMessage.getLayoutParams();
        LinearLayout.LayoutParams lpS = (LinearLayout.LayoutParams) holder.lblUser.getLayoutParams();
        //Check whether message is mine to show green background and align to right
        if (current.esMio) {
            holder.lblMessage.setBackgroundResource(R.drawable.bubble_mio);
            holder.lblMessage.setTextColor(context.getResources().getColor(R.color.mensaje_mio_text));
            holder.lblMessage.setPadding(Utils.pixels2dp(context, 5), Utils.pixels2dp(context, 5), Utils.pixels2dp(context, 15), Utils.pixels2dp(context, 5));
            lpM.gravity = Gravity.RIGHT;
            lpS.gravity = Gravity.RIGHT;
        }
        //If not mine then it is from sender to show orange background and align to left
        else {
            holder.lblMessage.setBackgroundResource(R.drawable.bubble_recibe);
            holder.lblMessage.setTextColor(context.getResources().getColor(R.color.mensaje_recibe_text));
            holder.lblMessage.setPadding(Utils.pixels2dp(context, 15), Utils.pixels2dp(context, 5), Utils.pixels2dp(context, 5), Utils.pixels2dp(context, 5));
            lpM.gravity = Gravity.LEFT;
            lpS.gravity = Gravity.LEFT;
        }
        holder.lblMessage.setLayoutParams(lpM);
        return rowView;
    }

    public Mensaje getItem(int pos) {
        return mensajes.get(pos);
    }
}
