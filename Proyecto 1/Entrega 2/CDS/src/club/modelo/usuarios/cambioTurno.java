package club.modelo.usuarios;
import java.util.Date;

public class cambioTurno {
	
	private Date fecha;
	private String estado;
	private Turno turno;
	private Administrador administrador;
	
	public cambioTurno(Date fecha, String estado, Turno turno, Administrador administrador) {
		super();
		this.fecha = fecha;
		this.estado = estado;
		this.turno = turno;
        this.administrador = administrador;
	}
	
	

}
