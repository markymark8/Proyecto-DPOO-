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
	

	public void setEstado(String estado) {

    if (estado == null) {
        throw new IllegalArgumentException(
            "El estado no puede ser nulo");
    }

    if (!this.estado.equals("Pendiente")) {
        throw new IllegalStateException(
            "La solicitud ya fue procesada");
    }

    if (!estado.equals("Aprobado") &&
        !estado.equals("Rechazado")) {
        throw new IllegalArgumentException(
            "El estado debe ser Aprobado o Rechazado");
    }

    this.estado = estado;
}
	
	public String getEstado() {
	    return estado;
	}

	public Administrador getAdministrador() {
	    return administrador;
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
