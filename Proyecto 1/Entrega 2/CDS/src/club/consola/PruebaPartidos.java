package club.consola;

import java.time.LocalDate;

import club.modelo.conjunto.Categoria;
import club.modelo.conjunto.Deporte;
import club.modelo.conjunto.Equipo;
import club.modelo.conjunto.EstadoPartido;
import club.modelo.conjunto.IntegranteEquipo;
import club.modelo.conjunto.PartidoInterno;
import club.modelo.conjunto.Posicion;
import club.modelo.conjunto.TipoTarjeta;
import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Usuario;
import club.servicios.ClubSeneca;
import club.servicios.ReglaDeNegocioException;
import club.servicios.ServicioConjunto;
import club.servicios.ServicioPartidos;

public class PruebaPartidos {

    private static int fallas = 0;

    private static final Posicion[] POS_FUTBOL = {
        Posicion.PORTERO, Posicion.DEFENSA, Posicion.DEFENSA, Posicion.DEFENSA,
        Posicion.MEDIOCAMPISTA, Posicion.MEDIOCAMPISTA, Posicion.DELANTERO, Posicion.DELANTERO
    };
    private static final Posicion[] POS_BASKET = {
        Posicion.BASE, Posicion.ESCOLTA, Posicion.ALERO, Posicion.ALA_PIVOT, Posicion.PIVOT
    };

    private static void verificar(String descripcion, boolean condicion) {
        System.out.println((condicion ? "OK     " : "FALLA  ") + descripcion);
        if (!condicion) {
            fallas++;
        }
    }

    private static void debeRechazar(String descripcion, Runnable accion) {
        try {
            accion.run();
            verificar(descripcion + " (debia rechazar y no lo hizo)", false);
        } catch (ReglaDeNegocioException e) {
            verificar(descripcion + " -> " + e.getMessage(), true);
        }
    }

    /** Crea un equipo con "cantidad" socios nuevos, de login prefijo1, prefijo2, ... */
    private static Equipo armarEquipo(ClubSeneca club, String nombre, Deporte deporte,
                                      Categoria categoria, String prefijo, int cantidad,
                                      Posicion[] posiciones) {
        ServicioConjunto sc = club.getServicioConjunto();
        Equipo equipo = sc.crearEquipo(nombre, deporte, categoria, 14);
        for (int i = 1; i <= cantidad; i++) {
            String login = prefijo + i;
            Socio socio = new Socio(login, "Jugador " + login, Usuario.hash("1234"), LocalDate.of(2000, 1, 1));
            club.registrarUsuario(socio);
            sc.inscribirJugador(equipo, socio, posiciones[(i - 1) % posiciones.length], i);
        }
        return equipo;
    }

    private static IntegranteEquipo buscar(Equipo equipo, String login) {
        for (IntegranteEquipo i : equipo.getIntegrantes()) {
            if (i.getSocio().getLogin().equals(login)) {
                return i;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        ClubSeneca club = new ClubSeneca();
        ServicioConjunto sc = club.getServicioConjunto();
        ServicioPartidos sp = club.getServicioPartidos();

        Deporte futbol = new Deporte("FUTBOL", 7, 3, 1, 0, true, false, true);
        Deporte basket = new Deporte("BASKET", 5, 2, 0, 0, false, false, false);
        sc.registrarDeporte(futbol);
        sc.registrarDeporte(basket);

        Equipo a = armarEquipo(club, "Aguilas", futbol, Categoria.MAYORES, "a", 8, POS_FUTBOL);
        Equipo b = armarEquipo(club, "Bravos", futbol, Categoria.MAYORES, "b", 8, POS_FUTBOL);
        Equipo chico = armarEquipo(club, "Chicos", futbol, Categoria.MAYORES, "c", 3, POS_FUTBOL);
        Equipo dormido = armarEquipo(club, "Dormidos", futbol, Categoria.MAYORES, "d", 8, POS_FUTBOL);
        sc.darDeBajaEquipo(dormido);
        Equipo bk1 = armarEquipo(club, "Basket Uno", basket, Categoria.MAYORES, "k", 5, POS_BASKET);
        Equipo bk2 = armarEquipo(club, "Basket Dos", basket, Categoria.MAYORES, "m", 5, POS_BASKET);

        LocalDate fecha = LocalDate.of(2026, 11, 8);

        System.out.println("=== Programar partidos ===");
        PartidoInterno p1 = sp.programarPartidoInterno(a, b, fecha);
        verificar("Primer id es PT-001", p1.getId().equals("PT-001"));
        verificar("Estado inicial PROGRAMADO", p1.getEstado() == EstadoPartido.PROGRAMADO);
        verificar("getPartido lo encuentra", sp.getPartido("PT-001") == p1);
        verificar("getPartido de un id inexistente da null", sp.getPartido("PT-999") == null);
        verificar("getPartido(null) da null", sp.getPartido(null) == null);
        debeRechazar("Fecha nula", () -> sp.programarPartidoInterno(a, b, null));
        debeRechazar("Equipo nulo", () -> sp.programarPartidoInterno(a, null, fecha));
        debeRechazar("Un equipo contra si mismo", () -> sp.programarPartidoInterno(a, a, fecha));
        debeRechazar("Equipo sin minimo de habilitados", () -> sp.programarPartidoInterno(a, chico, fecha));
        debeRechazar("Equipo dado de baja", () -> sp.programarPartidoInterno(a, dormido, fecha));
        debeRechazar("Deportes distintos", () -> sp.programarPartidoInterno(a, bk1, fecha));

        System.out.println("=== Tarjetas ===");
        Socio a1 = club.getSocio("a1");
        Socio b2 = club.getSocio("b2");
        verificar("Primera amarilla no suspende", !sp.registrarTarjeta(p1, a1, TipoTarjeta.AMARILLA));
        verificar("Segunda amarilla suspende", sp.registrarTarjeta(p1, a1, TipoTarjeta.AMARILLA));
        verificar("a1 queda no habilitado", !buscar(a, "a1").isHabilitado());
        debeRechazar("Tarjeta a un suspendido", () -> sp.registrarTarjeta(p1, a1, TipoTarjeta.AMARILLA));
        verificar("Roja suspende de inmediato", sp.registrarTarjeta(p1, b2, TipoTarjeta.ROJA));
        debeRechazar("Tarjeta a un socio de otro equipo", () -> sp.registrarTarjeta(p1, club.getSocio("c1"), TipoTarjeta.AMARILLA));
        debeRechazar("Tarjeta sin tipo", () -> sp.registrarTarjeta(p1, club.getSocio("a2"), null));
        debeRechazar("Tarjeta sin partido", () -> sp.registrarTarjeta(null, club.getSocio("a2"), TipoTarjeta.AMARILLA));

        PartidoInterno pk = sp.programarPartidoInterno(bk1, bk2, fecha);
        debeRechazar("Tarjetas en un deporte que no las usa", () -> sp.registrarTarjeta(pk, club.getSocio("k1"), TipoTarjeta.AMARILLA));

        System.out.println("=== Resultado ===");
        debeRechazar("Marcador negativo", () -> sp.registrarResultado(p1, -1, 2));
        verificar("Tras el rechazo sigue PROGRAMADO", p1.getEstado() == EstadoPartido.PROGRAMADO);
        sp.registrarResultado(p1, 2, 1);
        verificar("Partido JUGADO", p1.getEstado() == EstadoPartido.JUGADO);
        verificar("Marcador 2-1", p1.getMarcadorLocal() == 2 && p1.getMarcadorVisitante() == 1);
        verificar("Suspendido DURANTE el partido sigue suspendido (a1)", !buscar(a, "a1").isHabilitado());
        verificar("Expulsado DURANTE el partido sigue suspendido (b2)", !buscar(b, "b2").isHabilitado());
        debeRechazar("Cerrar un partido ya jugado", () -> sp.registrarResultado(p1, 3, 3));
        debeRechazar("Tarjeta en un partido jugado", () -> sp.registrarTarjeta(p1, club.getSocio("a2"), TipoTarjeta.AMARILLA));
        debeRechazar("Basket no admite empate", () -> sp.registrarResultado(pk, 70, 70));
        sp.registrarResultado(pk, 71, 70);
        verificar("Basket cerrado con marcador valido", pk.estaJugado());

        System.out.println("=== Cumplir la suspension en el siguiente partido ===");
        PartidoInterno p2 = sp.programarPartidoInterno(a, b, fecha.plusDays(7));
        verificar("Segundo id es PT-002 (los rechazos no consumieron ids)", p2.getId().equals("PT-002"));
        verificar("a1 sigue suspendido antes de jugar el siguiente", !buscar(a, "a1").isHabilitado());
        sp.registrarResultado(p2, 1, 1);
        verificar("a1 cumplio y vuelve a estar habilitado", buscar(a, "a1").isHabilitado());
        verificar("b2 cumplio y vuelve a estar habilitado", buscar(b, "b2").isHabilitado());
        verificar("Las amarillas se reinician al cumplir", buscar(a, "a1").getAmarillasAcumuladasInterno() == 0);

        System.out.println("=== Partidos cargados ===");
        PartidoInterno cargado = new PartidoInterno("PT-010", a, b, fecha, EstadoPartido.JUGADO, 3, 0);
        sp.agregarPartidoCargado(cargado);
        verificar("El partido cargado se encuentra", sp.getPartido("PT-010") == cargado);
        PartidoInterno p3 = sp.programarPartidoInterno(a, b, fecha.plusDays(14));
        verificar("El contador sigue despues de lo cargado (PT-011)", p3.getId().equals("PT-011"));
        debeRechazar("Cargar un id repetido",
                () -> sp.agregarPartidoCargado(new PartidoInterno("PT-010", a, b, fecha, EstadoPartido.JUGADO, 1, 0)));
        debeRechazar("Cargar un partido nulo", () -> sp.agregarPartidoCargado(null));

        System.out.println();
        System.out.println(fallas == 0 ? "TODO OK" : "HAY " + fallas + " FALLA(S): revisa las lineas marcadas");
    }
}