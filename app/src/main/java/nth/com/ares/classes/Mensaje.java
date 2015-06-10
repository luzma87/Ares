package nth.com.ares.classes;

import nth.com.ares.MainActivity;
import nth.com.ares.utils.Utils;

/**
 * Created by LUZ on 07-Jun-15.
 */
public class Mensaje {

    public String sender;
    public String body;

    public boolean enviando;

    MainActivity context;

    public Mensaje(MainActivity context, String body, String sender, boolean enviando) {
        this.context = context;
        this.body = body;
        this.sender = sender;
        this.enviando = enviando;
    }

    public String toString() {
        return this.body;
    }

    public boolean esMio() {
        return sender.equalsIgnoreCase(context.mUser);
    }

    public boolean mostrar() {
//        Utils.log("LZM-MENSAJE", "MOSTRAR?");
        if (esMio()) {
            if (!enviando && context.isDoneLoading) {
                Utils.log("LZM-MENSAJE", "FALSE");
                return false;
            }
        }
//        Utils.log("LZM-MENSAJE", "TRUE");
        return true;
    }
}
