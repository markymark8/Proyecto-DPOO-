package club.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import club.modelo.conjunto.Categoria;
import club.modelo.conjunto.Competencia;
import club.modelo.conjunto.Deporte;
import club.modelo.conjunto.Equipo;
import club.modelo.conjunto.Posicion;
import club.modelo.conjunto.TipoFase;
import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Usuario;
import club.servicios.ClubSeneca;
import club.servicios.ReglaDeNegocioException;
import club.modelo.conjunto.PartidoInterno;
import club.modelo.conjunto.InscripcionCompetencia;

//imports persistencia

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import club.modelo.conjunto.Fase;
import club.modelo.conjunto.PartidoCompetencia;


// Acá se testean los RF que se definieron en la entrega del Proyecto 1, se encuentran en la documentación
public class ConjuntoTest {

    private ClubSeneca club;
    private Deporte futbol;
    private LocalDate fecha;

    // preparacion

    @BeforeEach
    void preparar() {
        club = new ClubSeneca();
        fecha = LocalDate.now().plusDays(10);
        futbol = new Deporte("FUTBOL", 7, 3, 1, 0, true, false, true);
        club.getServicioConjunto().registrarDeporte(futbol);
    }

    private Socio socio(String login) {
        Socio s = new Socio(login, login, Usuario.hash("1234"), LocalDate.of(2000, 1, 1));
        club.registrarUsuario(s);
        return s;
    }

    private Equipo equipoConJugadores(String nombre, String prefijo, int n) {
        Equipo e = club.getServicioConjunto().crearEquipo(nombre, futbol, Categoria.MAYORES, 20);
        for (int i = 1; i <= n; i++) {
            club.getServicioConjunto().inscribirJugador(
                    e, socio(prefijo + i), Posicion.DELANTERO, i);
        }
        return e;
    }

    private List<Socio> nomina(Equipo e, int cantidad) {
        List<Socio> lista = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            lista.add(e.getIntegrantesActivos().get(i).getSocio());
        }
        return lista;
    }

    //RF 1 equipos (ejemplo)

    @Test
    void rf1_crearConsultarYDarDeBajaEquipo() {
        Equipo e = club.getServicioConjunto().crearEquipo(
                "Los Halcones", futbol, Categoria.MAYORES, 15);
        
        assertEquals("Los Halcones", e.getNombre());
        assertEquals(15, e.getCupoMaximo());
        assertNotNull(e.getId());
        assertEquals(e, club.getServicioConjunto().getEquipo(e.getId()));
        assertTrue(e.isActivo());

        club.getServicioConjunto().darDeBajaEquipo(e);

        assertFalse(e.isActivo());

        assertEquals(e, club.getServicioConjunto().getEquipo(e.getId()));
    }

    @Test
    void rf1_equipoConCupoInvalidoSeRechaza() {

        assertThrows(ReglaDeNegocioException.class,
                () -> club.getServicioConjunto().crearEquipo(
                        "Equipo A", futbol, Categoria.MAYORES, 0));

        assertThrows(ReglaDeNegocioException.class,
                () -> club.getServicioConjunto().crearEquipo(
                        "Equipo B", futbol, Categoria.MAYORES, -5));

        assertEquals(0, club.getServicioConjunto().getEquipos().size());
    }
    
    @Test
    void rf1_noPermitirEquiposDuplicados() {

        club.getServicioConjunto().crearEquipo(
                "Los Halcones", futbol, Categoria.MAYORES, 15);

        assertThrows(ReglaDeNegocioException.class,
                () -> club.getServicioConjunto().crearEquipo(
                        "LOS HALCONES", futbol, Categoria.MAYORES, 20));

        assertEquals(1, club.getServicioConjunto().getEquipos().size());
    }

    // RF 2 jugadores (ejemplo)

    @Test
    void rf2_inscribirJugadorRegistraPosicionYCamiseta() {
        Equipo e = club.getServicioConjunto().crearEquipo(
                "Halcones", futbol, Categoria.MAYORES, 15);

        Socio a = socio("a");

        club.getServicioConjunto().inscribirJugador(
                e, a, Posicion.PORTERO, 1);

        assertEquals(1, e.getIntegrantesActivos().size());

        assertEquals(a, e.getIntegrantesActivos().get(0).getSocio());
        assertEquals(Posicion.PORTERO,
                e.getIntegrantesActivos().get(0).getPosicion());
        assertEquals(1,
                e.getIntegrantesActivos().get(0).getNumeroCamiseta());
    }

    @Test
    void rf2_camisetaRepetidaSeRechaza() {
        Equipo e = club.getServicioConjunto().crearEquipo("Halcones", futbol, Categoria.MAYORES, 15);
        club.getServicioConjunto().inscribirJugador(e, socio("a"), Posicion.PORTERO, 1);

        Socio b = socio("b");
        assertThrows(ReglaDeNegocioException.class,
                () -> club.getServicioConjunto().inscribirJugador(e, b, Posicion.DEFENSA, 1));
        assertEquals(1, e.getIntegrantesActivos().size());
    }

	@Test
	void rf2_retirarJugadorLiberaCupoYCamiseta() {
		Equipo e = club.getServicioConjunto().crearEquipo("Halcones", futbol, Categoria.MAYORES, 1);

		Socio a = socio("a");
		Socio b = socio("b");

		club.getServicioConjunto().inscribirJugador(e, a, Posicion.PORTERO, 1);

		club.getServicioConjunto().retirarJugador(e, a);

		assertEquals(0, e.getIntegrantesActivos().size());
		assertEquals(1, e.getIntegrantes().size());
		assertFalse(e.getIntegrantes().get(0).isActivo());

		club.getServicioConjunto().inscribirJugador(e, b, Posicion.DELANTERO, 1);

		assertEquals(1, e.getIntegrantesActivos().size());
		assertEquals(b, e.getIntegrantesActivos().get(0).getSocio());
	}

	@Test
	void rf2_unSoloEquipoPorDeporte() {
		Equipo primero = club.getServicioConjunto().crearEquipo("Halcones", futbol, Categoria.MAYORES, 15);

		Equipo segundo = club.getServicioConjunto().crearEquipo("Aguilas", futbol, Categoria.MAYORES, 15);

		Socio a = socio("a");

		club.getServicioConjunto().inscribirJugador(primero, a, Posicion.PORTERO, 1);

		assertThrows(ReglaDeNegocioException.class,
				() -> club.getServicioConjunto().inscribirJugador(segundo, a, Posicion.DEFENSA, 2));

		assertEquals(1, primero.getIntegrantesActivos().size());
		assertEquals(0, segundo.getIntegrantesActivos().size());
	}

	@Test
	void rf2_cupoMaximoSeRechaza() {
		Equipo e = club.getServicioConjunto().crearEquipo("Halcones", futbol, Categoria.MAYORES, 2);

		club.getServicioConjunto().inscribirJugador(e, socio("a"), Posicion.PORTERO, 1);

		club.getServicioConjunto().inscribirJugador(e, socio("b"), Posicion.DEFENSA, 2);

		assertThrows(ReglaDeNegocioException.class,
				() -> club.getServicioConjunto().inscribirJugador(e, socio("c"), Posicion.DELANTERO, 3));

		assertEquals(2, e.getIntegrantesActivos().size());
	}

	@Test
	void rf2_edadNoPermitidaParaLaCategoria() {
		Equipo e = club.getServicioConjunto().crearEquipo("Halcones", futbol, Categoria.MAYORES, 15);

		Socio menor = new Socio("menor", "Jugador Menor", Usuario.hash("1234"), LocalDate.now().minusYears(15));

		club.registrarUsuario(menor);

		assertThrows(ReglaDeNegocioException.class,
				() -> club.getServicioConjunto().inscribirJugador(e, menor, Posicion.DELANTERO, 1));

		assertEquals(0, e.getIntegrantesActivos().size());
	}
    
	@Test
	void rf2_posicionIncompatibleConDeporteSeRechaza() {
		Equipo e = club.getServicioConjunto().crearEquipo("Halcones", futbol, Categoria.MAYORES, 15);

		Socio a = socio("a");

		assertThrows(ReglaDeNegocioException.class,
				() -> club.getServicioConjunto().inscribirJugador(e, a, Posicion.BASE, 1));

		assertEquals(0, e.getIntegrantesActivos().size());
	}

    // RF 3 partido interno

	@Test
	void rf3_programarPartidoInterno() {
	    Equipo local = equipoConJugadores("Halcones", "local", 7);
	    Equipo visitante = equipoConJugadores("Aguilas", "visitante", 7);

	    var partido = club.getServicioPartidos()
	            .programarPartidoInterno(local, visitante, fecha);

	    assertNotNull(partido);
	    assertEquals(local, partido.getLocal());
	    assertEquals(visitante, partido.getVisitante());
	    assertEquals(fecha, partido.getFecha());

	    assertEquals(1, club.getServicioPartidos().getPartidos().size());
	}

	@Test
	void rf3_equiposDeDistintaCategoriaSeRechaza() {
	    Equipo local = equipoConJugadores("Halcones", "local", 7);

	    Equipo visitante = club.getServicioConjunto().crearEquipo(
	            "Aguilas", futbol, Categoria.SUB_17, 20);

	    for (int i = 1; i <= 7; i++) {
	        Socio jugador = new Socio(
	                "visitante" + i,
	                "Jugador " + i,
	                Usuario.hash("1234"),
	                LocalDate.now().minusYears(16));

	        club.registrarUsuario(jugador);

	        club.getServicioConjunto().inscribirJugador(
	                visitante, jugador, Posicion.DELANTERO, i);
	    }

	    assertThrows(ReglaDeNegocioException.class,
	            () -> club.getServicioPartidos()
	                    .programarPartidoInterno(local, visitante, fecha));

	    assertEquals(0, club.getServicioPartidos().getPartidos().size());
	}
	
	@Test
	void rf3_equiposDeDistintoDeporteSeRechaza() {
	    Equipo local = equipoConJugadores("Halcones", "local", 7);

	    Deporte basket = new Deporte(
	            "BASKET", 5, 2, 0, 0, false, false, false);

	    club.getServicioConjunto().registrarDeporte(basket);

	    Equipo visitante = club.getServicioConjunto().crearEquipo(
	            "Titanes", basket, Categoria.MAYORES, 15);

	    for (int i = 1; i <= 5; i++) {
	        club.getServicioConjunto().inscribirJugador(
	                visitante, socio("basket" + i), Posicion.BASE, i);
	    }

	    assertThrows(ReglaDeNegocioException.class,
	            () -> club.getServicioPartidos()
	                    .programarPartidoInterno(local, visitante, fecha));

	    assertEquals(0, club.getServicioPartidos().getPartidos().size());
	}
	
	@Test
	void rf3_equipoSinMinimoDeJugadoresSeRechaza() {
	    Equipo local = equipoConJugadores("Halcones", "local", 3);
	    Equipo visitante = equipoConJugadores("Aguilas", "visitante", 7);

	    assertThrows(ReglaDeNegocioException.class,
	            () -> club.getServicioPartidos()
	                    .programarPartidoInterno(local, visitante, fecha));

	    assertEquals(0, club.getServicioPartidos().getPartidos().size());
	}

    //  RF 4 y RF 5 competencias

	@Test
	void rf4_registrarYConsultarCompetencia() {
		Competencia competencia = club.getServicioCompetencias().registrarCompetencia("Liga Nacional",
				"Federacion Colombiana", "2026", futbol, Categoria.MAYORES);

		assertNotNull(competencia);
		assertNotNull(competencia.getId());

		assertEquals("Liga Nacional", competencia.getNombre());
		assertEquals("Federacion Colombiana", competencia.getOrganizador());
		assertEquals("2026", competencia.getTemporada());
		assertEquals(futbol, competencia.getDeporte());
		assertEquals(Categoria.MAYORES, competencia.getCategoria());

		assertEquals(competencia, club.getServicioCompetencias().getCompetencia(competencia.getId()));

		assertEquals(1, club.getServicioCompetencias().getCompetencias().size());
	}
	
	@Test
	void rf4_competenciaDuplicadaSeRechaza() {
		club.getServicioCompetencias().registrarCompetencia("Liga Nacional", "Federacion", "2026", futbol,
				Categoria.MAYORES);

		assertThrows(ReglaDeNegocioException.class, () -> club.getServicioCompetencias()
				.registrarCompetencia("LIGA NACIONAL", "Otro organizador", "2026", futbol, Categoria.MAYORES));

		assertEquals(1, club.getServicioCompetencias().getCompetencias().size());
	}

	@Test
	void rf5_inscribirEquipoEnCompetencia() {
		Competencia competencia = club.getServicioCompetencias().registrarCompetencia("Liga", "Federacion", "2026",
				futbol, Categoria.MAYORES);

		Equipo equipo = equipoConJugadores("Halcones", "jugador", 7);

		var inscripcion = club.getServicioCompetencias().inscribirEquipo(competencia, equipo, nomina(equipo, 7));

		assertNotNull(inscripcion);
		assertEquals(equipo, inscripcion.getEquipo());
		assertEquals(7, inscripcion.getNomina().size());

		assertEquals(inscripcion, competencia.getInscripcion(equipo));
		assertEquals(1, competencia.getInscripciones().size());
	}

	@Test
	void rf5_equipoDeOtraCategoriaSeRechaza() {
		Competencia competencia = club.getServicioCompetencias().registrarCompetencia("Liga", "Federacion", "2026",
				futbol, Categoria.MAYORES);

		Equipo equipo = club.getServicioConjunto().crearEquipo("Juveniles", futbol, Categoria.SUB_17, 20);

		for (int i = 1; i <= 7; i++) {
			Socio jugador = new Socio("juvenil" + i, "Jugador " + i, Usuario.hash("1234"),
					LocalDate.now().minusYears(16));

			club.registrarUsuario(jugador);

			club.getServicioConjunto().inscribirJugador(equipo, jugador, Posicion.DELANTERO, i);
		}

		assertThrows(ReglaDeNegocioException.class,
				() -> club.getServicioCompetencias().inscribirEquipo(competencia, equipo, nomina(equipo, 7)));

		assertEquals(0, competencia.getInscripciones().size());
	}

	@Test
	void rf5_inscripcionDuplicadaSeRechaza() {
		Competencia competencia = club.getServicioCompetencias().registrarCompetencia("Liga", "Federacion", "2026",
				futbol, Categoria.MAYORES);

		Equipo equipo = equipoConJugadores("Halcones", "jugador", 7);

		club.getServicioCompetencias().inscribirEquipo(competencia, equipo, nomina(equipo, 7));

		assertThrows(ReglaDeNegocioException.class,
				() -> club.getServicioCompetencias().inscribirEquipo(competencia, equipo, nomina(equipo, 7)));

		assertEquals(1, competencia.getInscripciones().size());
	}
	
	@Test
	void rf5_nominaInferiorAlMinimoSeRechaza() {
		Competencia competencia = club.getServicioCompetencias().registrarCompetencia("Liga", "Federacion", "2026",
				futbol, Categoria.MAYORES);

		Equipo equipo = equipoConJugadores("Halcones", "jugador", 7);

		assertThrows(ReglaDeNegocioException.class,
				() -> club.getServicioCompetencias().inscribirEquipo(competencia, equipo, nomina(equipo, 3)));

		assertEquals(0, competencia.getInscripciones().size());
	}

    // RF 6 partidos, RF 10 fases

	@Test
	void rf10_definirFasesYOrden() {
		Competencia competencia = club.getServicioCompetencias().registrarCompetencia("Liga", "Federacion", "2026",
				futbol, Categoria.MAYORES);

		var eliminacion = club.getServicioCompetencias().definirFase(competencia, 2, "Semifinal", TipoFase.ELIMINACION,
				0);

		var liga = club.getServicioCompetencias().definirFase(competencia, 1, "Fase regular", TipoFase.LIGA, 4);

		assertEquals(2, competencia.getFases().size());
		assertEquals(1, competencia.getFases().get(0).getOrden());
		assertEquals(2, competencia.getFases().get(1).getOrden());
		assertEquals(liga, competencia.getFase(1));
		assertEquals(eliminacion, competencia.getFase(2));
		assertEquals(4, liga.getClasificados());
		assertTrue(liga.esLiga());
		assertTrue(eliminacion.esEliminacion());
	}
	
	@Test
	void rf10_ordenDeFaseDuplicadoSeRechaza() {
		Competencia competencia = club.getServicioCompetencias().registrarCompetencia("Liga", "Federacion", "2026",
				futbol, Categoria.MAYORES);

		club.getServicioCompetencias().definirFase(competencia, 1, "Fase regular", TipoFase.LIGA, 4);

		assertThrows(ReglaDeNegocioException.class,
				() -> club.getServicioCompetencias().definirFase(competencia, 1, "Semifinal", TipoFase.ELIMINACION, 0));

		assertEquals(1, competencia.getFases().size());
	}
	
	@Test
	void rf10_eliminacionNoPuedeTenerClasificados() {
		Competencia competencia = club.getServicioCompetencias().registrarCompetencia("Liga", "Federacion", "2026",
				futbol, Categoria.MAYORES);

		assertThrows(ReglaDeNegocioException.class, () -> club.getServicioCompetencias().definirFase(competencia, 1,
				"Eliminacion", TipoFase.ELIMINACION, 4));

		assertEquals(0, competencia.getFases().size());
	}
	@Test
	void rf6_registrarPartidoDeCompetenciaEnUnaFase() {
	    Competencia competencia = club.getServicioCompetencias()
	            .registrarCompetencia(
	                    "Liga", "Federacion", "2026",
	                    futbol, Categoria.MAYORES);

	    Equipo equipo = equipoConJugadores("Halcones", "jugador", 7);

	    club.getServicioCompetencias().inscribirEquipo(
	            competencia, equipo, nomina(equipo, 7));

	    club.getServicioCompetencias().registrarRival(
	            competencia, "Tigres FC");

	    club.getServicioCompetencias().definirFase(
	            competencia, 1, "Fase regular", TipoFase.LIGA, 4);

	    var partido = club.getServicioCompetencias().registrarPartido(
	            competencia,
	            equipo,
	            "Tigres FC",
	            1,
	            1,
	            true,
	            "Estadio Principal",
	            fecha
	    );

	    assertNotNull(partido);
	    assertEquals(competencia.getId(), partido.getIdCompetencia());
	    assertEquals(equipo, partido.getEquipoClub());
	    assertEquals("Tigres FC", partido.getRival().getNombre());
	    assertEquals(1, partido.getJornada());
	    assertEquals(1, partido.getOrdenFase());
	    assertTrue(partido.clubEsLocal());
	    assertEquals("Estadio Principal", partido.getEscenario());

	    assertEquals(1,
	            club.getServicioCompetencias().getPartidosDe(competencia).size());
	}
	
	@Test
	void rf6_partidoEnFaseInexistenteSeRechaza() {
	    Competencia competencia = club.getServicioCompetencias()
	            .registrarCompetencia(
	                    "Liga", "Federacion", "2026",
	                    futbol, Categoria.MAYORES);

	    Equipo equipo = equipoConJugadores("Halcones", "jugador", 7);

	    club.getServicioCompetencias().inscribirEquipo(
	            competencia, equipo, nomina(equipo, 7));

	    club.getServicioCompetencias().registrarRival(
	            competencia, "Tigres FC");

	    assertThrows(ReglaDeNegocioException.class,
	            () -> club.getServicioCompetencias().registrarPartido(
	                    competencia,
	                    equipo,
	                    "Tigres FC",
	                    1,
	                    5,
	                    true,
	                    "Estadio Principal",
	                    fecha
	            ));

	    assertEquals(0,
	            club.getServicioCompetencias().getPartidosDe(competencia).size());
	}
	
	@Test
	void rf6_partidoExternoEntreRivales() {
	    Competencia competencia = club.getServicioCompetencias()
	            .registrarCompetencia(
	                    "Liga", "Federacion", "2026",
	                    futbol, Categoria.MAYORES);

	    club.getServicioCompetencias().registrarRival(
	            competencia, "Tigres FC");

	    club.getServicioCompetencias().registrarRival(
	            competencia, "Leones FC");

	    club.getServicioCompetencias().definirFase(
	            competencia, 1, "Fase regular", TipoFase.LIGA, 4);

	    var partido = club.getServicioCompetencias()
	            .registrarPartidoExterno(
	                    competencia,
	                    "Tigres FC",
	                    "Leones FC",
	                    1,
	                    1,
	                    "Estadio Municipal",
	                    fecha
	            );

	    assertNotNull(partido);
	    assertEquals("Tigres FC", partido.getRivalLocal().getNombre());
	    assertEquals("Leones FC", partido.getRivalVisitante().getNombre());
	    assertEquals("Estadio Municipal", partido.getEscenario());

	    assertEquals(1,
	            club.getServicioCompetencias()
	                    .getPartidosExternosDe(competencia).size());

	    club.getServicioCompetencias()
	            .registrarResultadoExterno(partido, 2, 1);

	    assertTrue(partido.estaJugado());
	    assertEquals(2, partido.getMarcadorLocal());
	    assertEquals(1, partido.getMarcadorVisitante());
	}

    // RF 9 historial y tablas
	
	@Test
	void rf9_tablaDePosicionesReflejaResultados() {
	    Competencia competencia = club.getServicioCompetencias()
	            .registrarCompetencia(
	                    "Liga", "Federacion", "2026",
	                    futbol, Categoria.MAYORES);

	    Equipo equipo = equipoConJugadores("Halcones", "jugador", 7);

	    club.getServicioCompetencias().inscribirEquipo(
	            competencia, equipo, nomina(equipo, 7));

	    club.getServicioCompetencias().registrarRival(
	            competencia, "Tigres FC");

	    club.getServicioCompetencias().definirFase(
	            competencia, 1, "Fase regular", TipoFase.LIGA, 4);

	    var partido = club.getServicioCompetencias().registrarPartido(
	            competencia, equipo, "Tigres FC",
	            1, 1, true, "Estadio Principal", fecha);

	    club.getServicioCompetencias().registrarResultado(
	            partido, 2, 1);

	    var tabla = club.getServicioCompetencias()
	            .tablaPosiciones(competencia, 1);

	    assertEquals(2, tabla.size());

	    var primero = tabla.get(0);
	    var segundo = tabla.get(1);

	    assertEquals("Halcones", primero.getNombre());
	    assertTrue(primero.esClub());
	    assertEquals(3, primero.getPuntos());
	    assertEquals(1, primero.getJugados());
	    assertEquals(1, primero.getGanados());
	    assertEquals(2, primero.getGolesFavor());
	    assertEquals(1, primero.getGolesContra());

	    assertEquals("Tigres FC", segundo.getNombre());
	    assertFalse(segundo.esClub());
	    assertEquals(0, segundo.getPuntos());
	    assertEquals(1, segundo.getPerdidos());
	}

	@Test
	void rf9_tablaEnFaseDeEliminacionSeRechaza() {
	    Competencia competencia = club.getServicioCompetencias()
	            .registrarCompetencia(
	                    "Liga", "Federacion", "2026",
	                    futbol, Categoria.MAYORES);

	    club.getServicioCompetencias().definirFase(
	            competencia, 1, "Semifinal",
	            TipoFase.ELIMINACION, 0);

	    assertThrows(ReglaDeNegocioException.class,
	            () -> club.getServicioCompetencias()
	                    .tablaPosiciones(competencia, 1));
	}

    // RF 11 nomina, RF 12 resultado

	@Test
	void rf11_nominaConJugadoresDelEquipo() {
		Equipo equipo = equipoConJugadores("Halcones", "H", 7);

		Competencia competencia = club.getServicioCompetencias().registrarCompetencia("Liga Nacional", "Federación",
				"2026", futbol, Categoria.MAYORES);

		List<Socio> nominaValida = nomina(equipo, 7);

		InscripcionCompetencia inscripcion = club.getServicioCompetencias().inscribirEquipo(competencia, equipo,
				nominaValida);

		assertNotNull(inscripcion);
		assertEquals(7, inscripcion.getNomina().size());

		for (Socio jugador : nominaValida) {
			assertTrue(inscripcion.getNomina().contains(jugador));
		}
	} 
	@Test
	void rf11_nominaConMenosDelMinimoSeRechaza() {
		Equipo equipo = equipoConJugadores("Halcones", "H", 7);

		Competencia competencia = club.getServicioCompetencias().registrarCompetencia("Liga Nacional", "Federación",
				"2026", futbol, Categoria.MAYORES);

		List<Socio> nominaInsuficiente = nomina(equipo, 3);

		assertThrows(ReglaDeNegocioException.class,
				() -> club.getServicioCompetencias().inscribirEquipo(competencia, equipo, nominaInsuficiente));

		assertTrue(competencia.getInscripciones().isEmpty());
	}

	@Test
	void rf11_nominaConJugadorAjenoAlEquipoSeRechaza() {
		Equipo equipo = equipoConJugadores("Halcones", "H", 7);
		Socio jugadorAjeno = socio("EXTERNO");

		Competencia competencia = club.getServicioCompetencias().registrarCompetencia("Liga Nacional", "Federación",
				"2026", futbol, Categoria.MAYORES);

		List<Socio> nominaInvalida = nomina(equipo, 6);
		nominaInvalida.add(jugadorAjeno);

		assertThrows(ReglaDeNegocioException.class,
				() -> club.getServicioCompetencias().inscribirEquipo(competencia, equipo, nominaInvalida));

		assertTrue(competencia.getInscripciones().isEmpty());
	}
	@Test
	void rf12_registrarResultadoDePartidoInterno() {
	    Equipo local = equipoConJugadores("Halcones", "H", 7);
	    Equipo visitante = equipoConJugadores("Tigres", "T", 7);

	    PartidoInterno partido = club.getServicioPartidos()
	        .programarPartidoInterno(local, visitante, fecha);

	    assertFalse(partido.estaJugado());

	    club.getServicioPartidos().registrarResultado(partido, 2, 1);

	    assertTrue(partido.estaJugado());
	    assertEquals(2, partido.getMarcadorLocal());
	    assertEquals(1, partido.getMarcadorVisitante());
	}

    // RF 14, 15, 16 jugador

	@Test
	void rf14_equiposDelJugador() {
		Equipo equipo = equipoConJugadores("Halcones", "H", 7);

		Socio jugador = equipo.getIntegrantesActivos().get(0).getSocio();

		List<Equipo> equiposDelJugador = club.getServicioConjunto().getEquiposDe(jugador);

		assertEquals(1, equiposDelJugador.size());
		assertTrue(equiposDelJugador.contains(equipo));
		
		Socio socioSinEquipo = socio("SIN_EQUIPO");

		assertTrue(club.getServicioConjunto().getEquiposDe(socioSinEquipo).isEmpty());
	}
	@Test
	void rf15_participacionEnCompetencia() {
		Equipo equipo = equipoConJugadores("Halcones", "H", 7);

		Socio jugador = equipo.getIntegrantesActivos().get(0).getSocio();

		Socio socioAjeno = socio("AJENO");

		Competencia competencia = club.getServicioCompetencias().registrarCompetencia("Liga Nacional", "Federación",
				"2026", futbol, Categoria.MAYORES);

		List<Socio> nominaValida = nomina(equipo, 7);

		club.getServicioCompetencias().inscribirEquipo(competencia, equipo, nominaValida);

		assertTrue(club.getServicioCompetencias().participa(competencia, jugador));

		assertFalse(club.getServicioCompetencias().participa(competencia, socioAjeno));
	}  

    // PERSISTENCIA, código depurado con IA.

	@Test
	void persistencia_conjuntoYCompetencias() throws IOException {

	    Equipo equipo = equipoConJugadores("Halcones", "H", 7);

	    Competencia competencia = club.getServicioCompetencias()
	        .registrarCompetencia(
	            "Liga Nacional", "Federación", "2026",
	            futbol, Categoria.MAYORES
	        );

	    List<Socio> nominaValida = nomina(equipo, 7);

	    club.getServicioCompetencias()
	        .inscribirEquipo(competencia, equipo, nominaValida);

	    Fase fase = new Fase(1, "Fase de liga", TipoFase.LIGA, 4);
	    competencia.agregarFase(fase);

	    club.getServicioCompetencias()
	        .registrarRival(competencia, "Tigres FC");

	    PartidoCompetencia partidoCompetencia =
	        club.getServicioCompetencias().registrarPartido(
	            competencia, equipo, "Tigres FC",
	            1, 1, true, "Estadio Central", fecha
	        );

	    club.getServicioCompetencias()
	        .registrarResultado(partidoCompetencia, 2, 1);

	    Equipo visitante = equipoConJugadores("Leones", "L", 7);

	    PartidoInterno partidoInterno = club.getServicioPartidos()
	        .programarPartidoInterno(equipo, visitante, fecha);

	    club.getServicioPartidos()
	        .registrarResultado(partidoInterno, 3, 2);

	    Path carpeta = Files.createTempDirectory("conjunto_competencias");

	    club.guardar(carpeta);
	    ClubSeneca clubCargado = new ClubSeneca();

	    for (int i = 1; i <= 7; i++) {
	        clubCargado.registrarUsuario(
	            new Socio(
	                "H" + i, "H" + i,
	                Usuario.hash("1234"),
	                LocalDate.of(2000, 1, 1)
	            )
	        );

	        clubCargado.registrarUsuario(
	            new Socio(
	                "L" + i, "L" + i,
	                Usuario.hash("1234"),
	                LocalDate.of(2000, 1, 1)
	            )
	        );
	    }
	    
	    clubCargado.cargar(carpeta);
	    
	    Equipo equipoRecuperado = clubCargado.getServicioConjunto()
	        .getEquipo(equipo.getId());

	    Equipo visitanteRecuperado = clubCargado.getServicioConjunto()
	        .getEquipo(visitante.getId());

	    assertNotNull(equipoRecuperado);
	    assertNotNull(visitanteRecuperado);

	    assertEquals(equipo.getNombre(), equipoRecuperado.getNombre());
	    assertEquals(7, equipoRecuperado.getIntegrantesActivos().size());

	    Competencia competenciaRecuperada =
	        clubCargado.getServicioCompetencias()
	            .getCompetencia(competencia.getId());

	    assertNotNull(competenciaRecuperada);
	    assertEquals(competencia.getNombre(), competenciaRecuperada.getNombre());
	    assertEquals(1, competenciaRecuperada.getInscripciones().size());

	    assertEquals(
	        7,
	        competenciaRecuperada.getInscripciones().get(0).getNomina().size()
	    );

	    Fase faseRecuperada = competenciaRecuperada.getFase(1);

	    assertNotNull(faseRecuperada);
	    assertEquals("Fase de liga", faseRecuperada.getNombre());
	    assertEquals(TipoFase.LIGA, faseRecuperada.getTipo());

	    List<PartidoCompetencia> partidosRecuperados =
	        clubCargado.getServicioCompetencias()
	            .getPartidosDe(competenciaRecuperada);

	    assertEquals(1, partidosRecuperados.size());

	    PartidoCompetencia partidoRecuperado = partidosRecuperados.get(0);

	    assertTrue(partidoRecuperado.estaJugado());
	    assertEquals(2, partidoRecuperado.getMarcadorClub());
	    assertEquals(1, partidoRecuperado.getMarcadorRival());
	    assertEquals("Tigres FC", partidoRecuperado.getRival().getNombre());

	    PartidoInterno partidoInternoRecuperado = null;

	    for (var partido : clubCargado.getServicioPartidos().getPartidos()) {
	        if (partido instanceof PartidoInterno) {
	            partidoInternoRecuperado = (PartidoInterno) partido;
	            break;
	        }
	    }

	    assertNotNull(partidoInternoRecuperado);
	    assertTrue(partidoInternoRecuperado.estaJugado());
	    assertEquals(3, partidoInternoRecuperado.getMarcadorLocal());
	    assertEquals(2, partidoInternoRecuperado.getMarcadorVisitante());

	    assertEquals(
	        equipo.getId(),
	        partidoInternoRecuperado.getLocal().getId()
	    );

	    assertEquals(
	        visitante.getId(),
	        partidoInternoRecuperado.getVisitante().getId()
	    );
	}
}
