
package club.servicios;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import club.modelo.conjunto.*;
import club.modelo.usuarios.Socio;

public class ServicioPartidosTest {

    @Test
    public void testEstadisticaFutbolSinTarjetas() {

        // 1. Crear deporte
        Deporte futbol = new Deporte(
            "FUTBOL", 7, 3, 1, 0,
            true, false, true
        );

        // 2. Crear equipos
        Equipo local = new Equipo(
            "E1", "Equipo Local", futbol,
            Categoria.MAYORES, 20
        );

        Equipo visitante = new Equipo(
            "E2", "Equipo Visitante", futbol,
            Categoria.MAYORES, 20
        );

        // 3. Crear jugador
        Socio jugador = new Socio(
            "jugador1",
            "Jugador Prueba",
            "hash",
            LocalDate.of(2000, 5, 10)
        );

        // 4. Agregarlo al equipo local
        local.agregarIntegrante(
            jugador,
            Posicion.DELANTERO,
            10
        );

        // 5. Crear partido y servicio
        PartidoInterno partido = new PartidoInterno(
            "PT-001", local, visitante,
            LocalDate.now()
        );

        ServicioPartidos servicio = new ServicioPartidos();

        // 6. Registrar estadística
        servicio.registrarEstadisticaFutbol(
            partido, jugador, 2, 1
        );

        // 7. Recuperar y comprobar resultados
        EstadisticaFutbol estadistica =
            (EstadisticaFutbol) partido.getEstadisticaJugador(jugador);

        assertNotNull(estadistica);
        assertEquals(2, estadistica.getGoles());
        assertEquals(1, estadistica.getAsistencias());
        assertEquals(0, estadistica.getTarjetasAmarillas());
        assertFalse(estadistica.isTarjetaRoja());
    }
    @Test
    public void testDosAmarillasGeneranRojaYSuspension() {

        Deporte futbol = new Deporte(
            "FUTBOL", 7, 3, 1, 0,
            true, false, true
        );

        Equipo local = new Equipo(
            "E1", "Equipo Local", futbol,
            Categoria.MAYORES, 20
        );

        Equipo visitante = new Equipo(
            "E2", "Equipo Visitante", futbol,
            Categoria.MAYORES, 20
        );

        Socio jugador = new Socio(
            "jugador2",
            "Jugador Prueba",
            "hash",
            LocalDate.of(2000, 5, 10)
        );

        local.agregarIntegrante(
            jugador,
            Posicion.DELANTERO,
            10
        );

        PartidoInterno partido = new PartidoInterno(
            "PT-002", local, visitante,
            LocalDate.now()
        );

        ServicioPartidos servicio = new ServicioPartidos();

        // Registrar las dos amarillas
        servicio.registrarTarjeta(
            partido, jugador, TipoTarjeta.AMARILLA
        );

        servicio.registrarTarjeta(
            partido, jugador, TipoTarjeta.AMARILLA
        );

        // Comprobar acumulación disciplinaria
        IntegranteEquipo integrante =
            local.getIntegrantes().get(0);

        assertEquals(2, integrante.getAmarillasAcumuladasInterno());
        assertTrue(integrante.isSuspendidoInterno());

        // Construir estadística del partido
        servicio.registrarEstadisticaFutbol(
            partido, jugador, 0, 0
        );

        EstadisticaFutbol estadistica =
            (EstadisticaFutbol) partido.getEstadisticaJugador(jugador);

        assertNotNull(estadistica);
        assertEquals(2, estadistica.getTarjetasAmarillas());
        assertTrue(estadistica.isTarjetaRoja());

        // El jugador no debe poder registrar otra estadística
        // ni recibir más tarjetas en este partido.
        assertThrows(
            ReglaDeNegocioException.class,
            () -> servicio.registrarEstadisticaFutbol(
                partido, jugador, 1, 0
            )
        );
    }
    @Test
    public void testRojaDirecta() {

        Deporte futbol = new Deporte(
            "FUTBOL", 7, 3, 1, 0,
            true, false, true
        );

        Equipo local = new Equipo(
            "E1", "Equipo Local", futbol,
            Categoria.MAYORES, 20
        );

        Equipo visitante = new Equipo(
            "E2", "Equipo Visitante", futbol,
            Categoria.MAYORES, 20
        );

        Socio jugador = new Socio(
            "jugador3",
            "Jugador Prueba",
            "hash",
            LocalDate.of(2000, 5, 10)
        );

        local.agregarIntegrante(
            jugador,
            Posicion.DELANTERO,
            10
        );

        PartidoInterno partido = new PartidoInterno(
            "PT-003", local, visitante,
            LocalDate.now()
        );

        ServicioPartidos servicio = new ServicioPartidos();

        // Registrar roja directa
        servicio.registrarTarjeta(
            partido, jugador, TipoTarjeta.ROJA
            
        );
        assertTrue(partido.recibioRojaPartido("jugador3"));

        IntegranteEquipo integrante =
            local.getIntegrantes().get(0);

        // Comprobar suspensión
        assertTrue(integrante.isSuspendidoInterno());

        // Registrar estadística del encuentro
        servicio.registrarEstadisticaFutbol(
            partido, jugador, 1, 0
        );

        EstadisticaFutbol estadistica =
            (EstadisticaFutbol) partido.getEstadisticaJugador(jugador);

        assertNotNull(estadistica);
        assertEquals(1, estadistica.getGoles());
        assertEquals(0, estadistica.getTarjetasAmarillas());
        assertTrue(estadistica.isTarjetaRoja());
    }
    @Test
    public void testSuspensionNoSeLevantaEnElMismoPartido() {

        Deporte futbol = new Deporte(
            "FUTBOL", 7, 3, 1, 0,
            true, false, true
        );

        Equipo local = new Equipo(
            "E1", "Equipo Local", futbol,
            Categoria.MAYORES, 20
        );

        Equipo visitante = new Equipo(
            "E2", "Equipo Visitante", futbol,
            Categoria.MAYORES, 20
        );

        Socio jugador = new Socio(
            "jugador4",
            "Jugador Prueba",
            "hash",
            LocalDate.of(2000, 5, 10)
        );

        local.agregarIntegrante(
            jugador,
            Posicion.DELANTERO,
            10
        );

        PartidoInterno partido = new PartidoInterno(
            "PT-004", local, visitante,
            LocalDate.now()
        );

        ServicioPartidos servicio = new ServicioPartidos();

        // Recibir roja directa
        servicio.registrarTarjeta(
            partido, jugador, TipoTarjeta.ROJA
        );

        IntegranteEquipo integrante =
            local.getIntegrantes().get(0);

        assertTrue(integrante.isSuspendidoInterno());

        // Cerrar el mismo partido
        servicio.registrarResultado(partido, 1, 0);

        // La suspensión debe permanecer activa
        assertTrue(integrante.isSuspendidoInterno());

        // El contador de amarillas debe seguir en cero
        assertEquals(
            0,
            integrante.getAmarillasAcumuladasInterno()
        );

        // El partido debe estar finalizado
        assertTrue(partido.estaJugado());
    }
    @Test
    public void testSuspensionSeLevantaEnElSiguientePartido() {

        Deporte futbol = new Deporte(
            "FUTBOL", 7, 3, 1, 0,
            true, false, true
        );

        Equipo local = new Equipo(
            "E1", "Equipo Local", futbol,
            Categoria.MAYORES, 20
        );

        Equipo visitante = new Equipo(
            "E2", "Equipo Visitante", futbol,
            Categoria.MAYORES, 20
        );

        Socio jugador = new Socio(
            "jugador5",
            "Jugador Prueba",
            "hash",
            LocalDate.of(2000, 5, 10)
        );

        local.agregarIntegrante(
            jugador,
            Posicion.DELANTERO,
            10
        );

        ServicioPartidos servicio = new ServicioPartidos();

        // Primer partido: el jugador recibe roja.
        PartidoInterno partido1 = new PartidoInterno(
            "PT-005", local, visitante,
            LocalDate.now()
        );

        servicio.registrarTarjeta(
            partido1, jugador, TipoTarjeta.ROJA
        );

        servicio.registrarResultado(partido1, 1, 0);

        assertTrue(integranteSuspendido(local, jugador));

        // Segundo partido: el jugador cumple la suspensión.
        PartidoInterno partido2 = new PartidoInterno(
            "PT-006", local, visitante,
            LocalDate.now().plusDays(1)
        );

        // No debe poder registrar estadísticas.
        assertThrows(
            ReglaDeNegocioException.class,
            () -> servicio.registrarEstadisticaFutbol(
                partido2, jugador, 1, 0
            )
        );

        // Al cerrar el segundo partido se levanta la suspensión.
        servicio.registrarResultado(partido2, 0, 0);

        assertFalse(integranteSuspendido(local, jugador));
    }
    private boolean integranteSuspendido(Equipo equipo, Socio socio) {

        for (IntegranteEquipo integrante : equipo.getIntegrantes()) {
            if (integrante.getSocio().getLogin().equals(socio.getLogin())) {
                return integrante.isSuspendidoInterno();
            }
        }

        return false;
    }
    @Test
    public void testEstadisticaBasket() {

        Deporte basket = new Deporte(
            "BASKET", 5, 2, 0, 0,
            false, false, false
        );

        Equipo local = new Equipo(
            "E1", "Equipo Local", basket,
            Categoria.MAYORES, 20
        );

        Equipo visitante = new Equipo(
            "E2", "Equipo Visitante", basket,
            Categoria.MAYORES, 20
        );

        Socio jugador = new Socio(
            "basket1",
            "Jugador Basket",
            "hash",
            LocalDate.of(2000, 5, 10)
        );

        local.agregarIntegrante(
            jugador,
            Posicion.BASE,
            10
        );

        PartidoInterno partido = new PartidoInterno(
            "PT-007", local, visitante,
            LocalDate.now()
        );

        ServicioPartidos servicio = new ServicioPartidos();

        servicio.registrarEstadisticaBasket(
            partido, jugador, 18, 7
        );

        EstadisticaBasket estadistica =
            (EstadisticaBasket) partido.getEstadisticaJugador(jugador);

        assertNotNull(estadistica);
        assertEquals(18, estadistica.getPuntos());
        assertEquals(7, estadistica.getRebotes());
    }
    @Test
    public void testEstadisticaVoleibol() {
        Deporte voleibol = new Deporte(
            "VOLEIBOL", 6, 3, 0, 0,
            false, true, false
        );

        Equipo local = new Equipo(
            "E1", "Equipo Local", voleibol, Categoria.MAYORES, 20
        );

        Equipo visitante = new Equipo(
            "E2", "Equipo Visitante", voleibol, Categoria.MAYORES, 20
        );

        Socio jugador = new Socio(
            "voleibol1", "Jugador Voleibol", "hash",
            LocalDate.of(2000, 5, 10)
        );

        // Usa aquí una posición válida para voleibol.
        local.agregarIntegrante(jugador, Posicion.CENTRAL, 10);

        PartidoInterno partido = new PartidoInterno(
            "PT-008", local, visitante, LocalDate.now()
        );

        ServicioPartidos servicio = new ServicioPartidos();

        servicio.registrarEstadisticaVoleibol(partido, jugador, 4, 3);

        EstadisticaVoleibol estadistica =
            (EstadisticaVoleibol) partido.getEstadisticaJugador(jugador);

        assertNotNull(estadistica);
        assertEquals(4, estadistica.getAces());
        assertEquals(3, estadistica.getBloqueos());
    }
    @Test
    public void testEstadisticaNoCorrespondeAlDeporte() {
        Deporte futbol = new Deporte(
            "FUTBOL", 7, 3, 1, 0,
            true, false, true
        );

        Equipo local = new Equipo(
            "E1", "Equipo Local", futbol, Categoria.MAYORES, 20
        );

        Equipo visitante = new Equipo(
            "E2", "Equipo Visitante", futbol, Categoria.MAYORES, 20
        );

        Socio jugador = new Socio(
            "jugador1", "Jugador Prueba", "hash",
            LocalDate.of(2000, 5, 10)
        );

        local.agregarIntegrante(jugador, Posicion.DELANTERO, 10);

        PartidoInterno partido = new PartidoInterno(
            "PT-009", local, visitante, LocalDate.now()
        );

        EstadisticaBasket estadistica = new EstadisticaBasket(
            jugador, 15, 8
        );

        assertThrows(
            ReglaDeNegocioException.class,
            () -> partido.agregarEstadistica(estadistica)
        );
    }
    @Test
    public void testJugadorAjenoNoPuedeRegistrarEstadistica() {
        Deporte basket = new Deporte(
            "BASKET", 5, 2, 0, 0,
            false, false, false
        );

        Equipo local = new Equipo(
            "E1", "Equipo Local", basket, Categoria.MAYORES, 20
        );

        Equipo visitante = new Equipo(
            "E2", "Equipo Visitante", basket, Categoria.MAYORES, 20
        );

        Socio jugadorAjeno = new Socio(
            "ajeno1", "Jugador Ajeno", "hash",
            LocalDate.of(2000, 5, 10)
        );

        PartidoInterno partido = new PartidoInterno(
            "PT-010", local, visitante, LocalDate.now()
        );

        EstadisticaBasket estadistica = new EstadisticaBasket(
            jugadorAjeno, 15, 8
        );

        assertThrows(
            ReglaDeNegocioException.class,
            () -> partido.agregarEstadistica(estadistica)
        );
    }
    @Test
    public void testJugadorNoPuedeRegistrarDosEstadisticas() {
        Deporte basket = new Deporte(
            "BASKET", 5, 2, 0, 0,
            false, false, false
        );

        Equipo local = new Equipo(
            "E1", "Equipo Local", basket, Categoria.MAYORES, 20
        );

        Equipo visitante = new Equipo(
            "E2", "Equipo Visitante", basket, Categoria.MAYORES, 20
        );

        Socio jugador = new Socio(
            "basket1", "Jugador Basket", "hash",
            LocalDate.of(2000, 5, 10)
        );

        local.agregarIntegrante(jugador, Posicion.BASE, 10);

        PartidoInterno partido = new PartidoInterno(
            "PT-011", local, visitante, LocalDate.now()
        );

        EstadisticaBasket primera = new EstadisticaBasket(jugador, 15, 8);
        EstadisticaBasket segunda = new EstadisticaBasket(jugador, 20, 10);

        partido.agregarEstadistica(primera);

        assertThrows(
            ReglaDeNegocioException.class,
            () -> partido.agregarEstadistica(segunda)
        );

        assertEquals(1, partido.getEstadisticas().size());
    }
    


}
