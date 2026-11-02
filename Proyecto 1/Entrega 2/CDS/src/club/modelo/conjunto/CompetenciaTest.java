package club.modelo.conjunto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import club.servicios.ReglaDeNegocioException;
import java.util.ArrayList;
import java.util.List;
import club.modelo.usuarios.Socio;
import java.time.LocalDate;

public class CompetenciaTest {

    @Test
    public void testCrearCompetencia() {

        Deporte futbol = new Deporte(
            "FUTBOL", 7, 3, 1, 0,
            true, false, true
        );

        Competencia competencia = new Competencia(
            "C001",
            " Liga Universitaria ",
            "Universidad Nacional",
            "2026",
            futbol,
            Categoria.MAYORES
        );

        assertEquals("C001", competencia.getId());
        assertEquals("Liga Universitaria", competencia.getNombre());
        assertEquals("Universidad Nacional", competencia.getOrganizador());
        assertEquals("2026", competencia.getTemporada());
        assertNotNull(competencia.getDeporte());
        assertNotNull(competencia.getCategoria());
    }

    @Test
    public void testCrearCompetenciaInvalida() {

        Deporte futbol = new Deporte(
            "FUTBOL", 7, 3, 1, 0,
            true, false, true
        );

        assertThrows(
            ReglaDeNegocioException.class,
            () -> new Competencia(
                null, "Liga", "Universidad", "2026",
                futbol, Categoria.MAYORES
            )
        );

        assertThrows(
            ReglaDeNegocioException.class,
            () -> new Competencia(
                "C001", "Liga; Universitaria", "Universidad", "2026",
                futbol, Categoria.MAYORES
            )
        );
    }@Test
    public void testAgregarRival() {

        Deporte futbol = new Deporte(
            "FUTBOL", 7, 3, 1, 0,
            true, false, true
        );

        Competencia competencia = new Competencia(
            "C001", "Liga", "Universidad", "2026",
            futbol, Categoria.MAYORES
        );

        InstitucionRival rival = new InstitucionRival("Universidad Nacional");

        competencia.agregarRival(rival);

        assertEquals(1, competencia.getRivales().size());
        assertNotNull(competencia.getRival("universidad nacional"));
    }

    @Test
    public void testNoAgregarRivalDuplicado() {

        Deporte futbol = new Deporte(
            "FUTBOL", 7, 3, 1, 0,
            true, false, true
        );

        Competencia competencia = new Competencia(
            "C001", "Liga", "Universidad", "2026",
            futbol, Categoria.MAYORES
        );

        competencia.agregarRival(new InstitucionRival("Universidad Nacional"));

        assertThrows(
            ReglaDeNegocioException.class,
            () -> competencia.agregarRival(
                new InstitucionRival("universidad nacional")
            )
        );
    }
    @Test
    public void testCrearInscripcion() {

        Deporte futbol = new Deporte(
            "FUTBOL", 7, 3, 1, 0,
            true, false, true
        );

        Equipo equipo = new Equipo(
            "E1", "Equipo Local", futbol, Categoria.MAYORES, 20
        );

        Socio jugador = new Socio(
            "jugador1", "Jugador Prueba", "hash",
            java.time.LocalDate.of(2000, 5, 10)
        );

        List<Socio> nomina = new ArrayList<>();
        nomina.add(jugador);

        InscripcionCompetencia inscripcion =
            new InscripcionCompetencia(equipo, nomina);

        assertEquals(equipo, inscripcion.getEquipo());
        assertEquals(1, inscripcion.getNomina().size());
        assertTrue(inscripcion.tieneJugador("jugador1"));
        assertFalse(inscripcion.tieneJugador("jugador2"));
    }

    @Test
    public void testInscripcionInvalida() {

        assertThrows(
            ReglaDeNegocioException.class,
            () -> new InscripcionCompetencia(null, new ArrayList<>())
        );

        Deporte futbol = new Deporte(
            "FUTBOL", 7, 3, 1, 0,
            true, false, true
        );

        Equipo equipo = new Equipo(
            "E1", "Equipo Local", futbol, Categoria.MAYORES, 20
        );

        assertThrows(
            ReglaDeNegocioException.class,
            () -> new InscripcionCompetencia(equipo, null)
        );
    }
    @Test
    public void testAgregarInscripcion() {

        Deporte futbol = new Deporte(
            "FUTBOL", 7, 3, 1, 0,
            true, false, true
        );

        Competencia competencia = new Competencia(
            "C001", "Liga Universitaria", "Universidad", "2026",
            futbol, Categoria.MAYORES
        );

        Equipo equipo = new Equipo(
            "E1", "Equipo Local", futbol, Categoria.MAYORES, 20
        );

        Socio jugador = new Socio(
            "jugador1", "Jugador Prueba", "hash",
            LocalDate.of(2000, 5, 10)
        );

        List<Socio> nomina = new ArrayList<>();
        nomina.add(jugador);

        InscripcionCompetencia inscripcion =
            new InscripcionCompetencia(equipo, nomina);

        competencia.agregarInscripcion(inscripcion);

        assertEquals(1, competencia.getInscripciones().size());
        assertNotNull(competencia.getInscripcion(equipo));
        assertTrue(competencia.getInscripcion(equipo).tieneJugador("jugador1"));
    }
    @Test
    public void testNoInscribirEquipoDuplicado() {

        Deporte futbol = new Deporte(
            "FUTBOL", 7, 3, 1, 0,
            true, false, true
        );

        Competencia competencia = new Competencia(
            "C001", "Liga Universitaria", "Universidad", "2026",
            futbol, Categoria.MAYORES
        );

        Equipo equipo = new Equipo(
            "E1", "Equipo Local", futbol, Categoria.MAYORES, 20
        );

        InscripcionCompetencia primera =
            new InscripcionCompetencia(equipo, new ArrayList<>());

        InscripcionCompetencia segunda =
            new InscripcionCompetencia(equipo, new ArrayList<>());

        competencia.agregarInscripcion(primera);

        assertThrows(
            ReglaDeNegocioException.class,
            () -> competencia.agregarInscripcion(segunda)
        );

        assertEquals(1, competencia.getInscripciones().size());
    }
    @Test
    public void testNoAgregarInscripcionNula() {

        Deporte futbol = new Deporte(
            "FUTBOL", 7, 3, 1, 0,
            true, false, true
        );

        Competencia competencia = new Competencia(
            "C001", "Liga Universitaria", "Universidad", "2026",
            futbol, Categoria.MAYORES
        );

        assertThrows(
            ReglaDeNegocioException.class,
            () -> competencia.agregarInscripcion(null)
        );

        assertEquals(0, competencia.getInscripciones().size());
    }
}