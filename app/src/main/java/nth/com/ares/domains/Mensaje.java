package nth.com.ares.domains;

import android.content.Context;

import nth.com.ares.MainActivity;
import nth.com.ares.utils.Utils;

/**
 * Created by LUZ on 07-Jun-15.
 */
public class Mensaje {

    public String sender;
    public String body;
    public String fecha;
    public String user;
    public String visto;
    public long id=0;
    public MensajeDbHelper mensajeDbHelper;
    public boolean enviando;

    Context context;

    public Mensaje(MainActivity context, String body, String sender, String fecha, boolean enviando) {
        this.context = context;
        this.body = body;
        this.sender = sender;
        this.fecha = fecha;
        this.enviando = enviando;
    }
    public Mensaje(Context context){
        this.context=context;
    }

    public String toString() {
        return this.body;
    }

    public boolean esMio() {
        return sender.equalsIgnoreCase(user);
    }


    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    public String getVisto() {
        return visto;
    }

    public void setVisto(String visto) {
        this.visto = visto;
    }
    public void setMensajeDbHelper(){
        this.mensajeDbHelper=new MensajeDbHelper(context);
    }
    public void save() {
        if (this.id == 0) {
            this.id = this.mensajeDbHelper.createMensaje(this);
        } else {
            this.mensajeDbHelper.updateMensaje(this);
        }
    }
}
