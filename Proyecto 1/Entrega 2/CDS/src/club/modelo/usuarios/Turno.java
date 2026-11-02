package club.modelo.usuarios;
import java.util.Date;
import java.util.ArrayList;
import club.servicios.ReglaDeNegocioException;

public class Turno {
	private Date fecha;
	private String horaInicio;
	private String horaFinal;
	private ArrayList<cambioTurno> cambiosTurno;
	
	public Turno(Date fecha, String horaInicio, String horaFinal) {
		super();
		this.fecha = fecha;
		this.horaInicio = horaInicio;
		this.horaFinal = horaFinal;
		this.cambiosTurno = new ArrayList<>();
	}
	
	public Date getFecha() {
	    return fecha;
	}

	public String getHoraInicio() {
	    return horaInicio;
	}

	public String getHoraFinal() {
	    return horaFinal;
	}
	
	public void actualizarHorario(Date nuevaFecha, String nuevaHoraInicio, String nuevaHoraFinal) {

	    if (nuevaFecha == null) {
	        throw new ReglaDeNegocioException("La fecha no puede ser nula");
	    }

	    if (nuevaHoraInicio == null || nuevaHoraFinal == null) {
	        throw new ReglaDeNegocioException("Las horas no pueden ser nulas");
	    }

	    this.fecha = nuevaFecha;
	    this.horaInicio = nuevaHoraInicio;
	    this.horaFinal = nuevaHoraFinal;
	}
	
	
	
	

}
