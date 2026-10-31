package club.modelo.usuarios;

/** Administrador del club. No compra ni reserva para uso personal. */
public class Administrador extends Usuario {

    public Administrador(String login, String nombre, String contrasenaHash) {
        super(login, nombre, contrasenaHash);
    }

    @Override
    public boolean esAdministrador() {
        return true;
    }
}
