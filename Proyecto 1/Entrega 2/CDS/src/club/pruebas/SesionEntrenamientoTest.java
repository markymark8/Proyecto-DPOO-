package club.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import club.modelo.individual.BloqueHorario;
import club.modelo.individual.EstadoAsistencia;
import club.modelo.individual.Instalacion;
import club.modelo.individual.SesionEntrenamiento;
import club.modelo.usuarios.Socio;
import club.servicios.ReglaDeNegocioException;
import club.pruebas.Datos;


public class SesionEntrenamientoTest {


	private SesionEntrenamiento sesion;
	private Socio ana;
	private Socio luis;
	private Socio pedro;

	//PASO 3: Sesion de entrenamiento programada para 1 de diciembre de 10:00 a 11:00; ana y luis convocados, pero pedro no
	public void setup() {
		ana = Datos.socio("ana");
		luis = Datos.socio("luis");
		pedro = Datos.socio("pedro");
		Instalacion cancha = new Instalacion(1, "Cancha 1", "TENIS", 4, false, true);
		BloqueHorario bloque = new BloqueHorario(LocalDate.of(2026, 12, 1), LocalTime.of(10, 0), LocalTime.of(11, 0));
		sesion = new SesionEntrenamiento(1, cancha, bloque, Datos.entrenador("carlos"), null);
		sesion.convocar(ana);
		sesion.convocar(luis);
	}

	

	@Test
	public void convocarTest() {
		setup();
		assertEquals(2, sesion.getConvocados().size(), "Debe haber 2 convocados");
		sesion.convocar(ana);
		assertEquals(2, sesion.getConvocados().size(), "No debe repetir un convocado");
	}

	@Test
	public void registrarFeedbackTest() {
		setup();
		try {
			sesion.registrarFeedback(ana, EstadoAsistencia.ASISTIO, 5, "Muy bien");
			sesion.registrarFeedback(luis, EstadoAsistencia.TARDE, null, "Llegó tarde");
		}
		catch (ReglaDeNegocioException e) {
			fail("No debió lanzar excepción");
		}
		assertEquals(2, sesion.getFeedbacks().size(), "Debió guardar los 2 feedbacks");
		assertEquals(EstadoAsistencia.ASISTIO, sesion.getFeedbacks().get(0).getAsistencia(), "La asistencia no es correcta");
		assertEquals(5, sesion.getFeedbacks().get(0).getNota().intValue(), "La nota no es correcta");
	}

	@Test
	public void registrarFeedbackInvalidoTest() {
		
		try {
			sesion.registrarFeedback(pedro, EstadoAsistencia.ASISTIO, 4, "");
			fail("Debió lanzar excepción: pedro no fue convocado");
		}
		catch (ReglaDeNegocioException e) {
		}

		
		try {
			sesion.registrarFeedback(ana, EstadoAsistencia.ASISTIO, 6, "");
			fail("Debió lanzar excepción: la nota debe estar entre 1 y 5");
		}
		catch (ReglaDeNegocioException e) {
		}

		try {
			sesion.registrarFeedback(ana, EstadoAsistencia.ASISTIO, 4, "");
			sesion.registrarFeedback(ana, EstadoAsistencia.FALTO, null, "");
			fail("Debió lanzar excepción: ana ya tiene feedback");
		}
		catch (ReglaDeNegocioException e) {
			assertEquals(1, sesion.getFeedbacks().size(), "Solo debe quedar el primer feedback");
		}
	}


}
