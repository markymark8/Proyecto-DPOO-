package club.pruebas;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import club.modelo.individual.Academia;
import club.modelo.individual.Disciplina;
import club.modelo.individual.InscripcionAcademia;
import club.modelo.individual.Nivel;
import club.modelo.torneos.EstadoTorneo;
import club.modelo.torneos.InscripcionIndividual;
import club.modelo.torneos.Modalidad;
import club.modelo.torneos.Torneo;
import club.modelo.usuarios.Empleado;
import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Turno;
import club.modelo.usuarios.Usuario;
import club.servicios.ClubSeneca;
import club.servicios.ReglaDeNegocioException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import club.modelo.conjunto.Categoria;
import club.modelo.conjunto.Deporte;
import club.modelo.conjunto.Equipo;
import club.modelo.conjunto.Posicion;
import club.modelo.torneos.InscripcionEquipo;

public class TorneosTest {

	private ClubSeneca club;
	private LocalDate fecha;

	private Socio maria; // INTERMEDIO, ranking 1200
	private Socio guillermo; // INTERMEDIO, ranking 1000
	private Socio camila; // AVANZADO
	private Socio fabio; // sin academia
	private Empleado libre; // sin turno el dia del torneo
	private Empleado ocupado; // con turno el dia del torneo
	private Deporte futbol;

	// el BeforeEach crea el club, la fecha (+10 días), diferentes socios con 
	// caracteristicas distintas y empleados con diferentes turnos

	@BeforeEach
	void preparar() {
		club = new ClubSeneca();
		fecha = LocalDate.now().plusDays(10);

		Academia tenis = new Academia(Disciplina.TENIS, 2);
		club.getServicioIndividual().getAcademias().add(tenis);

		maria = socio("maria");
		guillermo = socio("guillermo");
		camila = socio("camila");
		fabio = socio("fabio");
		inscribirEnAcademia(tenis, maria, Nivel.INTERMEDIO, 1200);
		inscribirEnAcademia(tenis, guillermo, Nivel.INTERMEDIO, 1000);
		inscribirEnAcademia(tenis, camila, Nivel.AVANZADO, 1500);

		libre = new Empleado("emp1", "Empleado libre", Usuario.hash("1234"), "COD1");
		ocupado = new Empleado("emp2", "Empleado de turno", Usuario.hash("1234"), "COD2");
		ocupado.agregarTurno(
				new Turno(Date.from(fecha.atStartOfDay(ZoneId.systemDefault()).toInstant()), "08:00", "16:00"));
		club.registrarUsuario(libre);
		club.registrarUsuario(ocupado);
		
		futbol = new Deporte("FUTBOL", 7, 3, 1, 0, true, false, true);
		club.getServicioConjunto().registrarDeporte(futbol);
	}

	private Socio socio(String login) {
		Socio s = new Socio(login, login, Usuario.hash("1234"), LocalDate.of(2000, 1, 1));
		club.registrarUsuario(s);
		return s;
	}

	private void inscribirEnAcademia(Academia academia, Socio s, Nivel nivel, int puntaje) {
		academia.agregarInscripcion(new InscripcionAcademia(s, academia, nivel, puntaje, LocalDate.of(2026, 1, 1)));
	}

	private Torneo torneoIndividual(int cupos) {
		return club.getServicioTorneos().crearTorneoRecreativo("Copa Tenis " + cupos, Modalidad.INDIVIDUAL, "TENIS",
				"INTERMEDIO", cupos, fecha);
	}

	private Equipo crearEquipoFutbol(String id, String nombre, String prefijo) {

		Deporte futbol = new Deporte("FUTBOL", 7, 3, 1, 0, true, false, true);

		Equipo equipo = new Equipo(id, nombre, futbol, Categoria.MAYORES, 20);

		for (int i = 1; i <= 7; i++) {
			Socio jugador = socio(prefijo + i);

			equipo.agregarIntegrante(jugador, Posicion.DELANTERO, i);
		}

		return equipo;

	}

	// test 1: crear

	@Test
	void t1_crearTorneoValidoQuedaEnInscripcion() {
		Torneo t = torneoIndividual(5);

		assertEquals(EstadoTorneo.EN_INSCRIPCION, t.getEstado());
		assertEquals(5, t.getCupos());
		assertEquals(Modalidad.INDIVIDUAL, t.getModalidad());
		assertEquals("TENIS", t.getDisciplina());
		assertEquals("INTERMEDIO", t.getCategoria());

		assertEquals(t, club.getServicioTorneos().getTorneo(t.getId()));
	}

	@Test
	void t1_crearTorneoConCuposCeroSeRechaza() {
		assertThrows(ReglaDeNegocioException.class, () -> torneoIndividual(0));

		assertEquals(0, club.getServicioTorneos().getTorneos().size());
	}

	// test 2: individual

	@Test
	void t2_inscripcionIndividualAceptada() {
		Torneo t = torneoIndividual(5);

		InscripcionIndividual i = club.getServicioInscripcion().inscribirJugador(t.getId(), maria);

		assertTrue(i.isPreferencial());
		assertEquals(1200, i.getPuntajeRanking());
		assertEquals(1, t.getInscripciones().size());
		assertEquals("maria", i.getIdentificador());
	}

	@Test
	void t2_inscripcionDuplicadaSeRechaza() {
		Torneo t = torneoIndividual(5);

		club.getServicioInscripcion().inscribirJugador(t.getId(), maria);

		assertThrows(ReglaDeNegocioException.class,
				() -> club.getServicioInscripcion().inscribirJugador(t.getId(), maria));

		assertEquals(1, t.getInscripciones().size());
	}

	// test 3 : pareja y equipo
	
	@Test
	void t3_parejaAceptadaEnDobles() {
	    Torneo t = club.getServicioTorneos().crearTorneoRecreativo(
	            "Copa Dobles",
	            Modalidad.DOBLES,
	            "TENIS",
	            "INTERMEDIO",
	            5,
	            fecha);

	    InscripcionIndividual i = club.getServicioInscripcion()
	            .inscribirPareja(t.getId(), maria, guillermo);

	    assertNotNull(i);
	    assertFalse(i.isPreferencial());
	    assertEquals(2, i.getParticipantes().size());
	    assertEquals(1, t.getInscripciones().size());
	}
	
	@Test
	void t3_parejaRechazadaSiUnoYaEstaInscrito() {
	    Torneo t = club.getServicioTorneos().crearTorneoRecreativo(
	            "Copa Dobles",
	            Modalidad.DOBLES,
	            "TENIS",
	            "INTERMEDIO",
	            5,
	            fecha);

	    club.getServicioInscripcion()
	            .inscribirPareja(t.getId(), maria, guillermo);

	    assertThrows(
	            ReglaDeNegocioException.class,
	            () -> club.getServicioInscripcion()
	                    .inscribirPareja(t.getId(), maria, camila));

	    assertEquals(1, t.getInscripciones().size());
	}
	
	@Test
	void t3_parejaConLaMismaPersonaSeRechaza() {
	    Torneo t = club.getServicioTorneos().crearTorneoRecreativo(
	            "Copa Dobles Misma Persona",
	            Modalidad.DOBLES,
	            "TENIS",
	            "INTERMEDIO",
	            5,
	            fecha);

	    assertThrows(
	            ReglaDeNegocioException.class,
	            () -> club.getServicioInscripcion()
	                    .inscribirPareja(t.getId(), maria, maria));

	    assertEquals(0, t.getInscripciones().size());
	}
	

	@Test
	void t3_equipoAceptadoEnTorneoDeConjunto() {

		Torneo t = club.getServicioTorneos().crearTorneoRecreativo("Copa Futbol", Modalidad.CONJUNTO, "FUTBOL",
				"MAYORES", 5, fecha);

		Equipo equipo = crearEquipoFutbol("EQ-001", "Los Campeones", "jugadorA");

		InscripcionEquipo inscripcion = club.getServicioInscripcion().inscribirEquipo(t.getId(), equipo);

		assertNotNull(inscripcion);
		assertEquals(equipo, inscripcion.getEquipo());
		assertEquals("EQ-001", inscripcion.getIdentificador());
		assertEquals(7, inscripcion.getJugadores().size());
		assertEquals(1, t.getInscripciones().size());
	}

	@Test
	void t3_equipoRechazadoSinCupos() {

		Torneo t = club.getServicioTorneos().crearTorneoRecreativo("Copa Futbol Cupos", Modalidad.CONJUNTO, "FUTBOL",
				"MAYORES", 2, fecha);

		Equipo equipo1 = crearEquipoFutbol("EQ-001", "Equipo Uno", "uno");

		Equipo equipo2 = crearEquipoFutbol("EQ-002", "Equipo Dos", "dos");

		Equipo equipo3 = crearEquipoFutbol("EQ-003", "Equipo Tres", "tres");
		club.getServicioInscripcion().inscribirEquipo(t.getId(), equipo1);
		club.getServicioInscripcion().liberarCuposPreferenciales(t.getId());
		club.getServicioInscripcion().inscribirEquipo(t.getId(), equipo2);

		assertEquals(2, t.getInscripciones().size());
		assertThrows(ReglaDeNegocioException.class,
				() -> club.getServicioInscripcion().inscribirEquipo(t.getId(), equipo3));

		assertEquals(2, t.getInscripciones().size());
	}

	// test 4: retiro

	@Test
	void t4_retiroLiberaCupo() {
		Torneo t = torneoIndividual(5);

		club.getServicioInscripcion().inscribirJugador(t.getId(), maria);

		assertEquals(1, t.getInscripciones().size());

		club.getServicioInscripcion().retirarInscripcion(t.getId(), maria.getLogin());

		assertEquals(0, t.getInscripciones().size());
	}

	@Test
	void t4_retiroDeNoInscritoSeRechaza() {
		Torneo t = torneoIndividual(5);

		assertThrows(ReglaDeNegocioException.class,
				() -> club.getServicioInscripcion().retirarInscripcion(t.getId(), maria.getLogin()));

		assertEquals(0, t.getInscripciones().size());
	}

	@Test
	void t4_retiroConTorneoEnCursoSeRechaza() {
		Torneo t = torneoIndividual(5);

		club.getServicioInscripcion().inscribirJugador(t.getId(), maria);

		club.getServicioInscripcion().inscribirJugador(t.getId(), guillermo);

		club.getServicioTorneos().iniciarTorneo(t.getId());

		assertEquals(EstadoTorneo.EN_CURSO, t.getEstado());

		assertThrows(ReglaDeNegocioException.class,
				() -> club.getServicioInscripcion().retirarInscripcion(t.getId(), maria.getLogin()));

		assertEquals(2, t.getInscripciones().size());
	}

	// test 5: empleados y turnos

	@Test
	void t5_empleadoLibreSeInscribe() {
		Torneo t = torneoIndividual(5);

		InscripcionIndividual i = club.getServicioInscripcion().inscribirJugador(t.getId(), libre);

		assertNotNull(i);
		assertFalse(i.isPreferencial());
		assertEquals(1, t.getInscripciones().size());
	}

	@Test
	void t5_empleadoConTurnoElDiaDelTorneoSeRechaza() {
		Torneo t = torneoIndividual(5);

		assertThrows(ReglaDeNegocioException.class,
				() -> club.getServicioInscripcion().inscribirJugador(t.getId(), ocupado));

		assertEquals(0, t.getInscripciones().size());
	}

	@Test
	void t5_empleadoEnDoblesSeRechaza() {
		Torneo t = club.getServicioTorneos().crearTorneoRecreativo("Copa Dobles", Modalidad.DOBLES, "TENIS",
				"INTERMEDIO", 5, fecha);

		assertThrows(ReglaDeNegocioException.class,
				() -> club.getServicioInscripcion().inscribirPareja(t.getId(), libre, maria));

		assertEquals(0, t.getInscripciones().size());
	}

	// test 6: externo

	@Test
	void t6_externoSeInscribeComoSocio() {

		Torneo t = club.getServicioTorneos().crearTorneoRecreativo("Copa Dobles Externos", Modalidad.DOBLES, "TENIS",
				"INTERMEDIO", 5, fecha);

		InscripcionIndividual i = club.getServicioInscripcion().inscribirPareja(t.getId(), fabio, maria);

		assertNotNull(i);
		assertFalse(i.isPreferencial());
		assertEquals(2, i.getParticipantes().size());
		assertTrue(i.getParticipantes().contains(fabio));
		assertEquals(1, t.getInscripciones().size());
	}

	@Test
	void t6_externoSinCuposSeRechaza() {
		Torneo t = club.getServicioTorneos().crearTorneoRecreativo("Copa Dobles Cupos", Modalidad.DOBLES, "TENIS",
				"INTERMEDIO", 2, fecha);
		club.getServicioInscripcion().inscribirPareja(t.getId(), maria, guillermo);
		club.getServicioInscripcion().liberarCuposPreferenciales(t.getId());

		Socio elena = socio("elena");
		club.getServicioInscripcion().inscribirPareja(t.getId(), camila, elena);
		assertEquals(2, t.getInscripciones().size());
		Socio diego = socio("diego");
		assertThrows(ReglaDeNegocioException.class,
				() -> club.getServicioInscripcion().inscribirPareja(t.getId(), fabio, diego));

		assertEquals(2, t.getInscripciones().size());
	}

	// test 7: consulta

	@Test
	void t7_listaTorneosYEstados() {
		Torneo enCurso = torneoIndividual(5);

		Torneo enInscripcion = club.getServicioTorneos().crearTorneoRecreativo("Copa Tenis Dobles",
				Modalidad.INDIVIDUAL, "TENIS", "INTERMEDIO", 6, fecha);

		club.getServicioInscripcion().inscribirJugador(enCurso.getId(), maria);
		club.getServicioInscripcion().inscribirJugador(enCurso.getId(), guillermo);

		club.getServicioTorneos().iniciarTorneo(enCurso.getId());

		assertEquals(2, club.getServicioTorneos().getTorneos().size());

		assertEquals(1, club.getServicioTorneos().getTorneosPorEstado(EstadoTorneo.EN_CURSO).size());

		assertEquals(1, club.getServicioTorneos().getTorneosPorEstado(EstadoTorneo.EN_INSCRIPCION).size());

		assertEquals(EstadoTorneo.EN_CURSO, enCurso.getEstado());
		assertEquals(EstadoTorneo.EN_INSCRIPCION, enInscripcion.getEstado());
	}

	@Test
	void t7_idInvalidoSeRechaza() {
		assertThrows(ReglaDeNegocioException.class, () -> club.getServicioTorneos().exigirTorneo("TN-999"));
	}

	// test 9 cupos preferenciales (20%)

	@Test
	void t9_cuposPreferencialesSonElVeintePorCiento() {
		int[][] casos = { { 2, 1 }, { 5, 1 }, { 6, 2 }, { 10, 2 }, { 21, 5 } };
		for (int[] caso : casos) {
			Torneo t = torneoIndividual(caso[0]);
			assertEquals(caso[1], t.getCuposPreferenciales(), "cupos=" + caso[0]);
		}
	}

	@Test
	void t9_regularNoUsaCupoPreferencialHastaLiberarlos() {
		Torneo t = torneoIndividual(2);

		club.getServicioInscripcion().inscribirJugador(t.getId(), guillermo);
		Socio jorge = socio("jorge");
		Academia tenis = club.getServicioIndividual().getAcademias().get(0);
		inscribirEnAcademia(tenis, jorge, Nivel.INTERMEDIO, 800);
		assertThrows(ReglaDeNegocioException.class,
				() -> club.getServicioInscripcion().inscribirJugador(t.getId(), jorge));

		assertEquals(1, t.getInscripciones().size());
		assertFalse(t.isPreferencialesLiberados());
		club.getServicioInscripcion().liberarCuposPreferenciales(t.getId());
		InscripcionIndividual segunda = club.getServicioInscripcion().inscribirJugador(t.getId(), jorge);
		assertNotNull(segunda);
		assertFalse(segunda.isPreferencial());
		assertEquals(2, t.getInscripciones().size());
		assertTrue(t.isPreferencialesLiberados());
	}

	@Test
	void t9_liberarPreferencialesPermiteInscripcionRegular() {
		Torneo t = torneoIndividual(2);

		InscripcionIndividual primera = club.getServicioInscripcion().inscribirJugador(t.getId(), guillermo);

		assertFalse(primera.isPreferencial());
		assertEquals(1, t.getInscripciones().size());
		club.getServicioInscripcion().liberarCuposPreferenciales(t.getId());

		assertTrue(t.isPreferencialesLiberados());
		Socio jorge = socio("jorge");
		Academia tenis = club.getServicioIndividual().getAcademias().get(0);
		inscribirEnAcademia(tenis, jorge, Nivel.INTERMEDIO, 800);
		InscripcionIndividual segunda = club.getServicioInscripcion().inscribirJugador(t.getId(), jorge);
		assertNotNull(segunda);
		assertFalse(segunda.isPreferencial());
		assertEquals(2, t.getInscripciones().size());
	}

	// PERSISTENCIA test

	@Test
	void persistencia_guardarYCargarConservaTorneosEInscripciones() throws IOException {
		Torneo original = torneoIndividual(5);

		InscripcionIndividual inscripcionOriginal = club.getServicioInscripcion().inscribirJugador(original.getId(),
				maria);

		Path carpeta = Files.createTempDirectory("torneos");

		club.guardar(carpeta);

		ClubSeneca clubCargado = new ClubSeneca();

		Socio mariaCargada = new Socio("maria", "maria", Usuario.hash("1234"), LocalDate.of(2000, 1, 1));

		clubCargado.registrarUsuario(mariaCargada);
		
		clubCargado.registrarUsuario(
		        new Socio("guillermo", "guillermo", Usuario.hash("1234"), LocalDate.of(2000, 1, 1)));
		clubCargado.registrarUsuario(
		        new Socio("camila", "camila", Usuario.hash("1234"), LocalDate.of(2000, 1, 1)));

		clubCargado.cargar(carpeta);

		assertEquals(1, clubCargado.getServicioTorneos().getTorneos().size());

		Torneo recuperado = clubCargado.getServicioTorneos().getTorneo(original.getId());

		assertNotNull(recuperado);
		assertEquals(original.getId(), recuperado.getId());
		assertEquals(original.getNombre(), recuperado.getNombre());
		assertEquals(original.getModalidad(), recuperado.getModalidad());
		assertEquals(original.getDisciplina(), recuperado.getDisciplina());
		assertEquals(original.getCategoria(), recuperado.getCategoria());
		assertEquals(original.getCupos(), recuperado.getCupos());
		assertEquals(original.getFecha(), recuperado.getFecha());
		assertEquals(original.getEstado(), recuperado.getEstado());
		assertEquals(1, recuperado.getInscripciones().size());

		InscripcionIndividual inscripcionRecuperada = (InscripcionIndividual) recuperado.getInscripciones().get(0);

		assertEquals(inscripcionOriginal.getIdentificador(), inscripcionRecuperada.getIdentificador());

		assertEquals(inscripcionOriginal.isPreferencial(), inscripcionRecuperada.isPreferencial());

		assertEquals(inscripcionOriginal.getPuntajeRanking(), inscripcionRecuperada.getPuntajeRanking());

		assertEquals("maria", inscripcionRecuperada.getParticipantes().get(0).getLogin());
	}
}