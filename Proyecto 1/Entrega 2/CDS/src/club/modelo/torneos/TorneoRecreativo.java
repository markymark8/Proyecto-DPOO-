package club.modelo.torneos;
import java.time.LocalDate;

public class TorneoRecreativo extends Torneo {

	public TorneoRecreativo(String id, String nombre, Modalidad modalidad, String disciplina, String categoria,
			int cupos, LocalDate fecha) {
		super(id, nombre, modalidad, disciplina, categoria, cupos,fecha);
	}

	@Override
	public int getTarifaInscripcion() {
		return 0;
	}

}
