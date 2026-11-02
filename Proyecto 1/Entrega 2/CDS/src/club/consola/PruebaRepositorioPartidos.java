package club.consola;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
import club.persistencia.RepositorioConjunto;
import club.persistencia.RepositorioPartidos;
import club.servicios.ClubSeneca;
import club.servicios.ReglaDeNegocioException;
import club.servicios.ServicioConjunto;
import club.servicios.ServicioPartidos;

public class PruebaRepositorioPartidos {

    private static int fallas = 0;

    private static final Posicion[] POS_FUTBOL = {
        Posicion.PORTERO, Posicion.DEFENSA, Posicion.DEFENSA, Posicion.DEFENSA,
        Posicion.MEDIOCAMPISTA, Posicion.MEDIOCAMPISTA, Posicion.DELANTERO, Posicion.DELANTERO
    };

    private static final String ENCABEZADO =
        "id;tipo;idEquipoLocal;idEquipoVisitante;fecha;estado;marcadorLocal;marcadorVisitante;sancionados\n";

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

    private static void registrarSocios(ClubSeneca club, String prefijo, int cantidad) {
        for (int i = 1; i <= cantidad; i++) {
            String login = prefijo + i;
            club.registrarUsuario(new Socio(login, "Jugador " + login, Usuario.hash("1234"),
                    LocalDate.of(2000, 1, 1)));
        }
    }

    private static Equipo armarEquipo(ClubSeneca club, String nombre, Deporte deporte, String prefijo, int cantidad) {
        registrarSocios(club, prefijo, cantidad);
        ServicioConjunto sc = club.getServicioConjunto();
        Equipo equipo = sc.crearEquipo(nombre, deporte, Categoria.MAYORES, 14);
        for (int i = 1; i <= cantidad; i++) {
            sc.inscribirJugador(equipo, club.getSocio(prefijo + i), POS_FUTBOL[(i - 1) % POS_FUTBOL.length], i);
        }
        return equipo;
    }

    /** Club nuevo con los socios registrados y los equipos cargados desde una carpeta. */
    private static ClubSeneca clubCargado(Path carpeta) {
        ClubSeneca club = new ClubSeneca();
        registrarSocios(club, "a", 8);
        registrarSocios(club, "b", 8);
        RepositorioConjunto.cargar(carpeta, club);
        return club;
    }

    private static IntegranteEquipo buscar(Equipo equipo, String login) {
        for (IntegranteEquipo i : equipo.getIntegrantes()) {
            if (i.getSocio().getLogin().equals(login)) {
                return i;
            }
        }
        return null;
    }

    public static void main(String[] args) throws IOException {

        // ---------- 1. Armar un club con partidos en distintos estados ----------
        ClubSeneca club1 = new ClubSeneca();
        ServicioPartidos sp1 = club1.getServicioPartidos();
        Deporte futbol = new Deporte("FUTBOL", 7, 3, 1, 0, true, false, true);
        club1.getServicioConjunto().registrarDeporte(futbol);

        Equipo a = armarEquipo(club1, "Aguilas", futbol, "a", 8);
        Equipo b = armarEquipo(club1, "Bravos", futbol, "b", 8);
        LocalDate fecha = LocalDate.of(2026, 11, 8);

        PartidoInterno p1 = sp1.programarPartidoInterno(a, b, fecha);
        sp1.registrarTarjeta(p1, club1.getSocio("a1"), TipoTarjeta.AMARILLA);
        sp1.registrarTarjeta(p1, club1.getSocio("a1"), TipoTarjeta.AMARILLA);   // a1 suspendido
        sp1.registrarTarjeta(p1, club1.getSocio("b2"), TipoTarjeta.ROJA);       // b2 suspendido
        sp1.registrarResultado(p1, 2, 1);                                        // PT-001 JUGADO
        sp1.programarPartidoInterno(a, b, fecha.plusDays(7));                    // PT-002 PROGRAMADO

        // ---------- 2. Guardar ----------
        System.out.println("=== Guardar ===");
        Path carpeta1 = Files.createTempDirectory("partidos1");
        RepositorioConjunto.guardar(carpeta1, club1);
        RepositorioPartidos.guardar(carpeta1, club1);
        verificar("Se creo partidos.csv", Files.exists(carpeta1.resolve("partidos.csv")));

        // ---------- 3. Cargar en un club nuevo y comparar ----------
        System.out.println("=== Cargar y comparar ===");
        ClubSeneca club2 = clubCargado(carpeta1);
        RepositorioPartidos.cargar(carpeta1, club2);
        ServicioPartidos sp2 = club2.getServicioPartidos();
        ServicioConjunto sc2 = club2.getServicioConjunto();

        verificar("2 partidos cargados", sp2.getPartidos().size() == 2);
        PartidoInterno q1 = (PartidoInterno) sp2.getPartido("PT-001");
        PartidoInterno q2 = (PartidoInterno) sp2.getPartido("PT-002");
        verificar("PT-001 existe", q1 != null);
        verificar("PT-002 existe", q2 != null);
        verificar("PT-001 esta JUGADO", q1.getEstado() == EstadoPartido.JUGADO);
        verificar("PT-001 marcador 2-1", q1.getMarcadorLocal() == 2 && q1.getMarcadorVisitante() == 1);
        verificar("PT-001 fecha igual", q1.getFecha().equals(fecha));
        verificar("PT-001 usa los equipos cargados (no copias)",
                q1.getLocal() == sc2.getEquipo("EQ-001") && q1.getVisitante() == sc2.getEquipo("EQ-002"));
        verificar("PT-001 conserva sus sancionados", q1.getSancionados().equals(p1.getSancionados()));
        verificar("a1 y b2 estan entre los sancionados", q1.fueSancionado("a1") && q1.fueSancionado("b2"));
        verificar("PT-002 esta PROGRAMADO", q2.getEstado() == EstadoPartido.PROGRAMADO);
        verificar("PT-002 sin sancionados", q2.getSancionados().isEmpty());
        verificar("a1 sigue suspendido tras cargar", !buscar(sc2.getEquipo("EQ-001"), "a1").isHabilitado());

        // ---------- 4. Estabilidad ----------
        System.out.println("=== Estabilidad (guardar -> cargar -> guardar) ===");
        Path carpeta2 = Files.createTempDirectory("partidos2");
        RepositorioConjunto.guardar(carpeta2, club2);
        RepositorioPartidos.guardar(carpeta2, club2);
        verificar("partidos.csv identico en la segunda ronda",
                Files.readString(carpeta1.resolve("partidos.csv")).equals(Files.readString(carpeta2.resolve("partidos.csv"))));

        // ---------- 5. El sistema sigue funcionando con lo cargado ----------
        System.out.println("=== Reglas sobre datos cargados ===");
        sp2.registrarResultado(q2, 1, 1);
        verificar("a1 cumple su sancion en PT-002", buscar(sc2.getEquipo("EQ-001"), "a1").isHabilitado());
        verificar("b2 cumple su sancion en PT-002", buscar(sc2.getEquipo("EQ-002"), "b2").isHabilitado());
        PartidoInterno p3 = sp2.programarPartidoInterno(sc2.getEquipo("EQ-001"), sc2.getEquipo("EQ-002"), fecha.plusDays(14));
        verificar("El contador sigue despues de lo cargado (PT-003)", p3.getId().equals("PT-003"));
        debeRechazar("Cerrar de nuevo un partido jugado cargado", () -> sp2.registrarResultado(q1, 0, 0));

        // ---------- 6. Archivos ausentes o danados ----------
        System.out.println("=== Archivos ausentes o danados ===");
        ClubSeneca sinArchivo = clubCargado(carpeta1);
        RepositorioPartidos.cargar(carpeta1.resolve("noexiste"), sinArchivo);
        verificar("Carpeta sin partidos.csv carga vacia", sinArchivo.getServicioPartidos().getPartidos().isEmpty());

        Path malo = Files.createTempDirectory("partidosMalo");
        Path archivoMalo = malo.resolve("partidos.csv");

        Files.writeString(archivoMalo, ENCABEZADO + "PT-001;INTERNO\n");
        debeRechazar("Fila con columnas faltantes", () -> RepositorioPartidos.cargar(malo, clubCargado(carpeta1)));

        Files.writeString(archivoMalo, ENCABEZADO + "PT-001;COMPETENCIA;EQ-001;EQ-002;2026-11-08;PROGRAMADO;0;0;\n");
        debeRechazar("Tipo de partido no soportado", () -> RepositorioPartidos.cargar(malo, clubCargado(carpeta1)));

        Files.writeString(archivoMalo, ENCABEZADO + "PT-001;INTERNO;EQ-099;EQ-002;2026-11-08;PROGRAMADO;0;0;\n");
        debeRechazar("Equipo inexistente", () -> RepositorioPartidos.cargar(malo, clubCargado(carpeta1)));

        Files.writeString(archivoMalo, ENCABEZADO + "PT-001;INTERNO;EQ-001;EQ-002;08/11/2026;PROGRAMADO;0;0;\n");
        debeRechazar("Fecha mal escrita", () -> RepositorioPartidos.cargar(malo, clubCargado(carpeta1)));

        Files.writeString(archivoMalo, ENCABEZADO + "PT-001;INTERNO;EQ-001;EQ-002;2026-11-08;TERMINADO;0;0;\n");
        debeRechazar("Estado inexistente", () -> RepositorioPartidos.cargar(malo, clubCargado(carpeta1)));

        Files.writeString(archivoMalo, ENCABEZADO + "PT-001;INTERNO;EQ-001;EQ-002;2026-11-08;JUGADO;x;0;\n");
        debeRechazar("Marcador no numerico", () -> RepositorioPartidos.cargar(malo, clubCargado(carpeta1)));

        Files.writeString(archivoMalo, ENCABEZADO + "PT-001;INTERNO;EQ-001;EQ-001;2026-11-08;PROGRAMADO;0;0;\n");
        debeRechazar("Un equipo contra si mismo", () -> RepositorioPartidos.cargar(malo, clubCargado(carpeta1)));

        Files.writeString(archivoMalo, ENCABEZADO
                + "PT-001;INTERNO;EQ-001;EQ-002;2026-11-08;PROGRAMADO;0;0;\n"
                + "PT-001;INTERNO;EQ-001;EQ-002;2026-11-15;PROGRAMADO;0;0;\n");
        debeRechazar("Id de partido repetido", () -> RepositorioPartidos.cargar(malo, clubCargado(carpeta1)));

        System.out.println();
        System.out.println(fallas == 0 ? "TODO OK" : "HAY " + fallas + " FALLA(S): revisa las lineas marcadas");
    }
}