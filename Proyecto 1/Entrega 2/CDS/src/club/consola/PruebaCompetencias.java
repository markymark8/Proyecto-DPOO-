package club.consola;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import club.modelo.conjunto.AcumuladoJugador;
import club.modelo.conjunto.Categoria;
import club.modelo.conjunto.Competencia;
import club.modelo.conjunto.Deporte;
import club.modelo.conjunto.Equipo;
import club.modelo.conjunto.FilaPosicion;
import club.modelo.conjunto.Partido;
import club.modelo.conjunto.PartidoCompetencia;
import club.modelo.conjunto.Posicion;
import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Usuario;
import club.servicios.ClubSeneca;
import club.servicios.ReglaDeNegocioException;
import club.servicios.ServicioCompetencias;
import club.servicios.ServicioPartidos;

/**
 * Prueba por consola del modulo de competencias: reglas del servicio (HDU 7, 8, 9, 10, 11, 20) y
 * ida y vuelta por CSV. Usa una carpeta temporal: no toca datos/.
 */
public class PruebaCompetencias {

    private static int fallas = 0;

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

    private static ClubSeneca nuevoClub() {
        ClubSeneca club = new ClubSeneca();
        for (int i = 1; i <= 12; i++) {
            String login = String.format("jug%02d", i);
            club.registrarUsuario(new Socio(login, "Jugador " + login, Usuario.hash("1234"),
                    LocalDate.of(2000, 1, 1)));
        }
        return club;
    }

    private static List<Socio> socios(ClubSeneca club, int desde, int hasta) {
        List<Socio> lista = new ArrayList<>();
        for (int i = desde; i <= hasta; i++) {
            lista.add(club.getSocio(String.format("jug%02d", i)));
        }
        return lista;
    }

    private static String leer(Path carpeta, String archivo) throws IOException {
        return Files.readString(carpeta.resolve(archivo), StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws IOException {
        ClubSeneca club = nuevoClub();
        LocalDate fecha = LocalDate.of(2026, 10, 10);

        Deporte futbol = new Deporte("FUTBOL", 7, 3, 1, 0, true, false, true);
        Deporte basket = new Deporte("BASKET", 5, 2, 0, 0, false, false, false);
        club.getServicioConjunto().registrarDeporte(futbol);
        club.getServicioConjunto().registrarDeporte(basket);

        Equipo seneca = club.getServicioConjunto().crearEquipo("Seneca FC", futbol, Categoria.MAYORES, 14);
        Posicion[] posiciones = { Posicion.PORTERO, Posicion.DEFENSA, Posicion.DEFENSA,
            Posicion.MEDIOCAMPISTA, Posicion.MEDIOCAMPISTA, Posicion.MEDIOCAMPISTA,
            Posicion.DELANTERO, Posicion.DELANTERO, Posicion.DELANTERO };
        for (int i = 1; i <= 9; i++) {
            club.getServicioConjunto().inscribirJugador(seneca, club.getSocio(String.format("jug%02d", i)),
                    posiciones[i - 1], i);
        }
        Equipo equipoBasket = club.getServicioConjunto().crearEquipo("Seneca Basket", basket, Categoria.MAYORES, 10);
        Equipo juvenil = club.getServicioConjunto().crearEquipo("Seneca Juvenil", futbol, Categoria.SUB_17, 14);

        ServicioCompetencias sc = club.getServicioCompetencias();
        ServicioPartidos sp = club.getServicioPartidos();

        System.out.println("=== HDU 7: registrar competencia ===");
        Competencia liga = sc.registrarCompetencia("Liga Metropolitana", "Liga Bogotana", "2026", futbol, Categoria.MAYORES);
        verificar("Primera competencia tiene id CP-001", liga.getId().equals("CP-001"));
        debeRechazar("Misma competencia (nombre en otras mayusculas)",
                () -> sc.registrarCompetencia("LIGA METROPOLITANA", "Otro", "2026", futbol, Categoria.MAYORES));
        Competencia ligaOtraTemporada = sc.registrarCompetencia("Liga Metropolitana", "Liga Bogotana", "2027", futbol, Categoria.MAYORES);
        verificar("Otra temporada es otra competencia y el rechazo no consumio id (CP-002)",
                ligaOtraTemporada.getId().equals("CP-002"));
        debeRechazar("Nombre vacio", () -> sc.registrarCompetencia(" ", "Liga", "2026", futbol, Categoria.MAYORES));
        debeRechazar("Nombre con ';'", () -> sc.registrarCompetencia("Copa;Sur", "Liga", "2026", futbol, Categoria.MAYORES));
        debeRechazar("Deporte nulo", () -> sc.registrarCompetencia("Copa", "Liga", "2026", null, Categoria.MAYORES));
        verificar("Hay 2 competencias", sc.getCompetencias().size() == 2);

        System.out.println("=== Rivales ===");
        sc.registrarRival(liga, "Colegio San Jorge");
        sc.registrarRival(liga, "Club Norte");
        debeRechazar("Rival repetido (otras mayusculas)", () -> sc.registrarRival(liga, "colegio san jorge"));
        debeRechazar("Rival con coma", () -> sc.registrarRival(liga, "Club, Sur"));
        debeRechazar("Rival vacio", () -> sc.registrarRival(liga, ""));
        verificar("La competencia tiene 2 rivales", liga.getRivales().size() == 2);

        System.out.println("=== HDU 10: inscribir equipo con nomina ===");
        debeRechazar("Nomina por debajo del minimo (3 < 7)", () -> sc.inscribirEquipo(liga, seneca, socios(club, 1, 3)));
        debeRechazar("Nomina con un jugador que no es del equipo (jug12)",
                () -> sc.inscribirEquipo(liga, seneca, socios(club, 6, 12)));
        List<Socio> repetida = socios(club, 1, 7);
        repetida.add(club.getSocio("jug01"));
        debeRechazar("Nomina con un jugador repetido", () -> sc.inscribirEquipo(liga, seneca, repetida));
        debeRechazar("Equipo de otro deporte", () -> sc.inscribirEquipo(liga, equipoBasket, socios(club, 1, 7)));
        debeRechazar("Equipo de otra categoria", () -> sc.inscribirEquipo(liga, juvenil, socios(club, 1, 7)));
        debeRechazar("Equipo nulo", () -> sc.inscribirEquipo(liga, null, socios(club, 1, 7)));
        verificar("Los rechazos no inscribieron nada", liga.getInscripciones().isEmpty());
        sc.inscribirEquipo(liga, seneca, socios(club, 1, 8));
        verificar("Inscripcion valida con 8 jugadores", liga.getInscripciones().size() == 1
                && liga.getInscripcion(seneca).getNomina().size() == 8);
        debeRechazar("Inscribir dos veces el mismo equipo", () -> sc.inscribirEquipo(liga, seneca, socios(club, 1, 8)));

        System.out.println("=== HDU 9: registrar partido de competencia ===");
        debeRechazar("Equipo no inscrito en la competencia",
                () -> sc.registrarPartido(ligaOtraTemporada, seneca, "Club Norte", 1, true, "Cancha", fecha));
        debeRechazar("Rival no registrado",
                () -> sc.registrarPartido(liga, seneca, "Rival Fantasma", 1, true, "Cancha", fecha));
        debeRechazar("Jornada 0", () -> sc.registrarPartido(liga, seneca, "Club Norte", 0, true, "Cancha", fecha));
        debeRechazar("Escenario vacio", () -> sc.registrarPartido(liga, seneca, "Club Norte", 1, true, " ", fecha));
        debeRechazar("Fecha nula", () -> sc.registrarPartido(liga, seneca, "Club Norte", 1, true, "Cancha", null));
        PartidoCompetencia p1 = sc.registrarPartido(liga, seneca, "Colegio San Jorge", 1, true, "Cancha Los Pinos", fecha);
        PartidoCompetencia p2 = sc.registrarPartido(liga, seneca, "Club Norte", 2, false, "Estadio Norte", fecha.plusDays(14));
        PartidoCompetencia p3 = sc.registrarPartido(liga, seneca, "Colegio San Jorge", 3, true, "Cancha Los Pinos", fecha.plusDays(28));
        verificar("Ids PC-001, PC-002, PC-003 sin huecos por los rechazos",
                p1.getId().equals("PC-001") && p2.getId().equals("PC-002") && p3.getId().equals("PC-003"));
        verificar("Los partidos de competencia quedan en ServicioPartidos", sp.getPartido("PC-002") == p2);
        verificar("getPartidosDe devuelve los 3 de la liga", sc.getPartidosDe(liga).size() == 3);
        verificar("El siguiente id de competencia es PC-004", sp.generarIdPartidoCompetencia().equals("PC-004"));

        System.out.println("=== HDU 9 y 11: estadisticas solo de la nomina ===");
        debeRechazar("Jugador del equipo pero fuera de la nomina (jug09)",
                () -> sp.registrarEstadisticaFutbol(p1, club.getSocio("jug09"), 1, 0));
        debeRechazar("Socio que no es del equipo (jug12)",
                () -> sp.registrarEstadisticaFutbol(p1, club.getSocio("jug12"), 1, 0));
        sp.registrarEstadisticaFutbol(p1, club.getSocio("jug01"), 1, 0);
        sp.registrarEstadisticaFutbol(p1, club.getSocio("jug02"), 2, 1);
        sp.registrarEstadisticaFutbol(p2, club.getSocio("jug01"), 1, 0);
        debeRechazar("Estadistica repetida del mismo jugador en el partido",
                () -> sp.registrarEstadisticaFutbol(p1, club.getSocio("jug01"), 3, 3));
        debeRechazar("Estadistica de basket en un partido de futbol",
                () -> sp.registrarEstadisticaBasket(p1, club.getSocio("jug03"), 4, 4));

        System.out.println("=== HDU 8: tabla de posiciones ===");
        debeRechazar("Tabla sin partidos jugados", () -> sc.tablaPosiciones(liga));
        debeRechazar("Tabla de una competencia sin partidos", () -> sc.tablaPosiciones(ligaOtraTemporada));
        sc.registrarResultado(p1, 3, 1);
        sc.registrarResultado(p2, 1, 2);
        verificar("Club local: marcador local 3, visitante 1", p1.getMarcadorLocal() == 3 && p1.getMarcadorVisitante() == 1);
        verificar("Club visitante: marcador local 2 (rival), visitante 1 (club)",
                p2.getMarcadorLocal() == 2 && p2.getMarcadorVisitante() == 1);
        verificar("getMarcadorClub y getMarcadorRival respetan el lado", p2.getMarcadorClub() == 1 && p2.getMarcadorRival() == 2);
        debeRechazar("Resultado de un partido ya jugado", () -> sc.registrarResultado(p1, 0, 0));
        debeRechazar("Resultado negativo", () -> sc.registrarResultado(p3, -1, 0));
        debeRechazar("Resultado nulo", () -> sc.registrarResultado(null, 1, 0));
        debeRechazar("Estadisticas en un partido ya jugado",
                () -> sp.registrarEstadisticaFutbol(p1, club.getSocio("jug03"), 0, 0));

        List<FilaPosicion> tabla = sc.tablaPosiciones(liga);
        verificar("Tabla con 3 filas (1 equipo del club y 2 rivales)", tabla.size() == 3);
        verificar("1.o Seneca FC: 3 pts, 2 jugados, GF 4, GC 3",
                tabla.get(0).getNombre().equals("Seneca FC") && tabla.get(0).esClub()
                && tabla.get(0).getPuntos() == 3 && tabla.get(0).getJugados() == 2
                && tabla.get(0).getGolesFavor() == 4 && tabla.get(0).getGolesContra() == 3);
        verificar("2.o Club Norte: 3 pts (empata en puntos y diferencia, menos goles a favor)",
                tabla.get(1).getNombre().equals("Club Norte") && tabla.get(1).getPuntos() == 3
                && !tabla.get(1).esClub());
        verificar("3.o Colegio San Jorge: 0 pts, 1 perdido",
                tabla.get(2).getNombre().equals("Colegio San Jorge") && tabla.get(2).getPuntos() == 0
                && tabla.get(2).getPerdidos() == 1);

        System.out.println("=== HDU 11 y 20: estadisticas acumuladas ===");
        List<AcumuladoJugador> acumulados = sc.estadisticasAcumuladas(liga);
        verificar("Dos jugadores con estadisticas", acumulados.size() == 2);
        verificar("jug02 lidera (2 goles, 1 asistencia en 1 partido)",
                acumulados.get(0).getLogin().equals("jug02") && acumulados.get(0).getA() == 2
                && acumulados.get(0).getB() == 1 && acumulados.get(0).getPartidos() == 1);
        verificar("jug01 acumula 2 partidos y 2 goles",
                acumulados.get(1).getLogin().equals("jug01") && acumulados.get(1).getPartidos() == 2
                && acumulados.get(1).getA() == 2 && acumulados.get(1).getB() == 0);
        verificar("Posiciones de jug01: 1.o en goles (empata), 2.o en asistencias",
                Arrays.equals(sc.posicionesDe(liga, club.getSocio("jug01")), new int[] { 1, 2 }));
        verificar("jug09 (no esta en la nomina) no participa", !sc.participa(liga, club.getSocio("jug09")));
        verificar("jug01 participa", sc.participa(liga, club.getSocio("jug01")));
        verificar("jug03 participa pero no tiene estadisticas", sc.participa(liga, club.getSocio("jug03"))
                && sc.acumuladoDe(liga, club.getSocio("jug03")) == null
                && sc.posicionesDe(liga, club.getSocio("jug03")) == null);
        verificar("Un partido sin jugar (p3) no suma a las estadisticas", p3.getEstadisticas().isEmpty());

        System.out.println("=== Persistencia CSV ===");
        Path carpeta = Files.createTempDirectory("cds-competencias-1");
        club.guardar(carpeta);
        verificar("Se crearon los 3 archivos de competencias",
                Files.exists(carpeta.resolve("competencias.csv"))
                && Files.exists(carpeta.resolve("inscripciones.csv"))
                && Files.exists(carpeta.resolve("partidosCompetencia.csv")));
        verificar("partidos.csv solo tiene internos (no mezcla competencia)",
                !leer(carpeta, "partidos.csv").contains("PC-"));
        verificar("estadisticas.csv incluye las estadisticas de partidos de competencia",
                leer(carpeta, "estadisticas.csv").contains("PC-001;jug02;FUTBOL;2;1"));

        ClubSeneca recargado = nuevoClub();
        recargado.cargar(carpeta);
        ServicioCompetencias sc2 = recargado.getServicioCompetencias();
        Competencia liga2 = sc2.getCompetencia("CP-001");
        verificar("Se recargaron las 2 competencias", sc2.getCompetencias().size() == 2 && liga2 != null);
        verificar("Datos de la competencia", liga2.getNombre().equals("Liga Metropolitana")
                && liga2.getTemporada().equals("2026") && liga2.getDeporte().getNombre().equals("FUTBOL")
                && liga2.getCategoria() == Categoria.MAYORES);
        verificar("Se recargaron los 2 rivales", liga2.getRivales().size() == 2 && liga2.getRival("club norte") != null);
        verificar("Se recargo la inscripcion con su nomina de 8",
                liga2.getInscripciones().size() == 1 && liga2.getInscripciones().get(0).getNomina().size() == 8);
        verificar("Se recargaron los 3 partidos de la liga", sc2.getPartidosDe(liga2).size() == 3);
        Partido q2 = recargado.getServicioPartidos().getPartido("PC-002");
        verificar("PC-002 conserva lado, estado y marcador",
                q2 instanceof PartidoCompetencia && !((PartidoCompetencia) q2).clubEsLocal()
                && q2.estaJugado() && q2.getMarcadorLocal() == 2 && q2.getMarcadorVisitante() == 1
                && ((PartidoCompetencia) q2).getEscenario().equals("Estadio Norte"));
        List<FilaPosicion> tabla2 = sc2.tablaPosiciones(liga2);
        verificar("La tabla recargada es la misma", tabla2.size() == 3
                && tabla2.get(0).getNombre().equals("Seneca FC") && tabla2.get(0).getPuntos() == 3
                && tabla2.get(2).getNombre().equals("Colegio San Jorge"));
        verificar("Las estadisticas recargadas son las mismas",
                sc2.estadisticasAcumuladas(liga2).get(0).getLogin().equals("jug02")
                && sc2.estadisticasAcumuladas(liga2).get(1).getPartidos() == 2);
        Equipo senecaRecargado = recargado.getServicioConjunto().getEquipo(seneca.getId());
        verificar("Los ids siguen sin repetirse: CP-003 y PC-003 -> PC-004",
                sc2.registrarCompetencia("Copa Sur", "Liga", "2026", futbol, Categoria.MAYORES).getId().equals("CP-003")
                && sc2.registrarPartido(liga2, senecaRecargado, "Club Norte", 4, true, "Cancha", fecha).getId().equals("PC-004"));

        ClubSeneca idem = nuevoClub();
        idem.cargar(carpeta);
        Path carpeta2 = Files.createTempDirectory("cds-competencias-2");
        idem.guardar(carpeta2);
        boolean identicos = true;
        for (String archivo : new String[] { "competencias.csv", "inscripciones.csv",
                "partidosCompetencia.csv", "estadisticas.csv", "partidos.csv", "equipos.csv" }) {
            if (!leer(carpeta, archivo).equals(leer(carpeta2, archivo))) {
                System.out.println("   (difiere " + archivo + ")");
                identicos = false;
            }
        }
        verificar("Cargar y volver a guardar deja los archivos identicos", identicos);

        System.out.println("=== Archivos danados ===");
        Path danada = Files.createTempDirectory("cds-competencias-3");
        Files.writeString(danada.resolve("competencias.csv"), "id;nombre\nCP-001;Liga\n", StandardCharsets.UTF_8);
        debeRechazar("competencias.csv con columnas de menos", () -> nuevoClub().cargar(danada));

        Path rivalDesconocido = Files.createTempDirectory("cds-competencias-4");
        for (String archivo : Files.list(carpeta).map(p -> p.getFileName().toString()).toList()) {
            Files.copy(carpeta.resolve(archivo), rivalDesconocido.resolve(archivo));
        }
        Files.writeString(rivalDesconocido.resolve("partidosCompetencia.csv"),
                leer(carpeta, "partidosCompetencia.csv").replace("Club Norte", "Rival Fantasma"), StandardCharsets.UTF_8);
        debeRechazar("partidosCompetencia.csv con un rival no registrado", () -> nuevoClub().cargar(rivalDesconocido));

        Path sinArchivos = Files.createTempDirectory("cds-competencias-5");
        for (String archivo : new String[] { "deportes.csv", "equipos.csv", "integrantes.csv", "partidos.csv" }) {
            Files.copy(carpeta.resolve(archivo), sinArchivos.resolve(archivo));
        }
        ClubSeneca vacio = nuevoClub();
        vacio.cargar(sinArchivos);
        verificar("Una carpeta sin archivos de competencias carga sin error y sin competencias",
                vacio.getServicioCompetencias().getCompetencias().isEmpty());

        System.out.println();
        System.out.println(fallas == 0 ? "TODO OK" : "HAY " + fallas + " FALLA(S): revisa las lineas marcadas");
    }
}
