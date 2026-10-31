package club.modelo.usuarios;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

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

    public static String hash(String contrasena) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(contrasena.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
    
    public boolean verificarContrasena(String contrasena) {
        return hash(contrasena).equals(contrasenaHash);
    }

    /** Solo Administrador lo sobrescribe para devolver true. */
    public boolean esAdministrador() {
        return false;
    }
}
