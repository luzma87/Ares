package nth.com.ares.classes;

/**
 * Created by LUZ on 07-Jun-15.
 */
public class Mensaje {

    public String sender;
    public String body;
    public boolean esMio;

    public Mensaje(String body, String sender, boolean esMio) {
        this.body = body;
        this.sender = sender;
        this.esMio = esMio;
    }

    public String toString() {
        return this.body;
    }
}
