package club.modelo.usuarios;

/**
 * Base de todos los usuarios del sistema (todos tienen login y contraseña).
 * La contraseña se guarda como hash SHA-256, nunca en claro.
 */
public abstract class Usuario {

    private final String login;
    private String nombre;
    private String contrasenaHash;

    public Usuario(String login, String nombre, String contrasenaHash) {
        this.login = login;
        this.nombre = nombre;
        this.contrasenaHash = contrasenaHash;
    }

    public String getLogin() {
        return login;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public void setContrasenaHash(String contrasenaHash) {
        this.contrasenaHash = contrasenaHash;
    }

    /**
     * TODO (a mano): calcular el SHA-256 de la contraseña recibida
     * (java.security.MessageDigest) y compararlo con contrasenaHash.
     */
    public boolean verificarContrasena(String contrasena) {
        throw new UnsupportedOperationException("TODO: verificarContrasena");
    }

    /** Solo Administrador lo sobrescribe para devolver true. */
    public boolean esAdministrador() {
        return false;
    }
}
