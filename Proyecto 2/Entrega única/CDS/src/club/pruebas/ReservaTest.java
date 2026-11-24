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
import club.modelo.individual.EstadoReserva;
import club.modelo.individual.Instalacion;
import club.modelo.individual.Modalidad;
import club.modelo.individual.Reserva;
import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Usuario;
import club.servicios.ReglaDeNegocioException;



public class ReservaTest {

	
	private Reserva reserva;
	private Socio ana;
	private Socio luis;
	private Usuario admin;

	//la reserva de ana, programada el 1 de diciembre de 10:00 a 11:00)
	public void setup() {
		ana = Datos.socio("ana");
		luis = Datos.socio("luis");
		admin = Datos.administrador("admin");
		Instalacion cancha = new Instalacion(1, "Cancha 1", "TENIS", 4, false, true);
		BloqueHorario bloque = new BloqueHorario(LocalDate.of(2026, 12, 1), LocalTime.of(10, 0), LocalTime.of(11, 0));
		reserva = new Reserva(1, cancha, bloque, ana, 2, Modalidad.SENCILLOS);
	}

	

	@Test
	public void cancelarTest() {
		setup();
		
		try {
			reserva.cancelar(luis);
			fail("Debió lanzar excepción: luis no es el responsable de la reserva");
		}
		catch (ReglaDeNegocioException e) {
			assertEquals(EstadoReserva.ACTIVA, reserva.getEstado(), "La reserva debe seguir activa");
		}

		
		reserva.cancelar(ana);
		assertEquals(EstadoReserva.CANCELADA, reserva.getEstado(), "Debió quedar cancelada");

	
		try {
			reserva.cancelar(ana);
			fail("Debió lanzar excepción: ya estaba cancelada");
		}
		catch (ReglaDeNegocioException e) {
		}
	}

	@Test
	public void cancelarComoAdministradorTest() {
		setup();
		reserva.cancelar(admin);
		assertEquals(EstadoReserva.CANCELADA, reserva.getEstado(), "El administrador puede cancelar cualquier reserva");
	}

	@Test
	public void registrarIngresoTest() {
		setup();
		reserva.registrarIngreso();
		assertTrue(reserva.isIngresoRegistrado(), "Debió registrar el ingreso");

		try {
			reserva.registrarIngreso();
			fail("Debió lanzar excepción: el ingreso ya estaba registrado");
		}
		catch (ReglaDeNegocioException e) {
		}
	}

	@Test
	public void marcarNoAsistioTest() {
		setup();
		reserva.registrarIngreso();
		try {
			reserva.marcarNoAsistio();
			fail("Debió lanzar excepción: tiene ingreso registrado");
		}
		catch (ReglaDeNegocioException e) {
			assertEquals(EstadoReserva.ACTIVA, reserva.getEstado(), "La reserva debe seguir activa");
		}
	}

	@Test
	public void marcarNoAsistioSinIngresoTest() {
		setup();
		reserva.marcarNoAsistio();
		assertEquals(EstadoReserva.NO_ASISTIO, reserva.getEstado(), "Debió quedar como NO_ASISTIO");
	}

	@Test
	public void esFuturaTest() {
		setup();
		assertTrue(reserva.esFutura(LocalDateTime.of(2026, 11, 30, 9, 0)), "Activa y empieza después de ahora");
		assertFalse(reserva.esFutura(LocalDateTime.of(2026, 12, 2, 9, 0)), "Ya pasó");
		reserva.cancelar(ana);
		assertFalse(reserva.esFutura(LocalDateTime.of(2026, 11, 30, 9, 0)), "Una reserva cancelada no es futura");
	}

}