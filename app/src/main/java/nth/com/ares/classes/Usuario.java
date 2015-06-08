package nth.com.ares.classes;

/**
 * Created by LUZ on 07-Jun-15.
 */
public class Usuario {
    public String nombre;
    public String apellido;
    public String login;
    public String email;
    public String password;
    public Double latitud;
    public Double longitud;
    public String telefono;
    public String celular;
    public String cedula;

    public Usuario() {
    }

    public Usuario(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public Usuario(String nombre, String apellido, String login, String email, String password) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.login = login;
        this.email = email;
        this.password = password;
    }
}
