package club.modelo.usuarios;
import java.util.Date;
import java.util.ArrayList;

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
	
	
	
	

}
