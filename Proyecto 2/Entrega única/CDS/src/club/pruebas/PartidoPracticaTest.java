package club.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import club.modelo.individual.BloqueHorario;
import club.modelo.individual.EstadisticaIndividual;
import club.modelo.individual.EstadisticaTenisPadel;
import club.modelo.individual.EstadoReserva;
import club.modelo.individual.Instalacion;
import club.modelo.individual.Modalidad;
import club.modelo.individual.PartidoPractica;
import club.modelo.usuarios.Socio;
import club.servicios.ReglaDeNegocioException;
import club.pruebas.Datos;

public class PartidoPracticaTest {

	
	private PartidoPractica partido;
	private Socio ana;
	private Socio luis;

	
	public void setup() {
		ana = Datos.socio("ana");
		luis = Datos.socio("luis");
		Instalacion cancha = new Instalacion(1, "Cancha 1", "TENIS", 4, false, true);
		BloqueHorario bloque = new BloqueHorario(LocalDate.of(2026, 12, 1), LocalTime.of(10, 0), LocalTime.of(11, 0));
		partido = new PartidoPractica(1, cancha, bloque, ana, Arrays.asList(ana), Arrays.asList(luis), Modalidad.SENCILLOS);
	}

	

	@Test
	public void crearPartidoTest() {
		setup();
		assertEquals(2, partido.getNumeroJugadores(), "El número de jugadores es la suma de ambos lados");
		assertFalse(partido.isJugado(), "Un partido nuevo no está jugado");
		assertTrue(partido.estaActiva(), "Un partido nuevo está activo");
	}

	@Test
	public void registrarResultadoTest() {
		setup();
		List<EstadisticaIndividual> estadisticas = new ArrayList<EstadisticaIndividual>();
		estadisticas.add(new EstadisticaTenisPadel(ana, null, 2, 12, 3, 5));
		estadisticas.add(new EstadisticaTenisPadel(luis, null, 1, 8, 1, 6));

		try {
			partido.registrarResultado(estadisticas);
		}
		catch (ReglaDeNegocioException e) {
			fail("No debió lanzar excepción");
		}
		assertTrue(partido.isJugado(), "Debió quedar jugado");
		assertEquals(EstadoReserva.FINALIZADA, partido.getEstado(), "Debió quedar FINALIZADA");
		assertEquals(2, partido.getEstadisticas().size(), "Debió guardar las 2 estadísticas");
		assertEquals(partido, estadisticas.get(0).getPartido(), "Cada estadística debe quedar ligada al partido");

		try {
			partido.registrarResultado(estadisticas);
			fail("Debió lanzar excepción: el partido ya fue jugado");
		}
		catch (ReglaDeNegocioException e) {
			assertEquals(2, partido.getEstadisticas().size(), "No debió duplicar las estadísticas");
		}
	}

}