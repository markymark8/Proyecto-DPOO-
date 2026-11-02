package club.modelo.usuarios;
import java.util.Date;

public class cambioTurno {
	
	private Date fecha;
	private String estado;
	private Turno turno;
	private Administrador administrador;
	private Date nuevaFecha;
	private String nuevaHoraInicio;
	private String nuevaHoraFinal;
	
	public cambioTurno(Date fecha, Turno turno, Administrador administrador, Date nuevaFecha, String nuevaHoraInicio, String nuevaHoraFinal) {

	    this.fecha = fecha;
	    this.estado = "Pendiente";
	    this.turno = turno;
	    this.administrador = administrador;
	    this.nuevaFecha = nuevaFecha;
	    this.nuevaHoraInicio = nuevaHoraInicio;
	    this.nuevaHoraFinal = nuevaHoraFinal;
	}
	
	public String getEstado() {
	    return estado;
	}

	public void setEstado(String estado) {
	    this.estado = estado;
	}
	
	public Turno getTurno() {
	    return turno;
	}
	
	public Date getNuevaFecha() {
	    return nuevaFecha;
	}

	public String getNuevaHoraInicio() {
	    return nuevaHoraInicio;
	}

	public String getNuevaHoraFinal() {
	    return nuevaHoraFinal;
	}
	

	
	

}
