package club.modelo.usuarios;
import java.util.ArrayList;


public class Administrador extends Usuario {
	
	private ArrayList<cambioTurno> cambiosTurno;
	private ArrayList<SugerenciaProducto> sugerenciasProducto;

    public Administrador(String login, String nombre, String contrasenaHash) {
        super(login, nombre, contrasenaHash);
        this.cambiosTurno = new ArrayList<>();
        this.sugerenciasProducto = new ArrayList<>();
    }

    @Override
    public boolean esAdministrador() {
        return true;
    }
}
