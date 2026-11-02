package club.consola;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import club.modelo.conjunto.AcumuladoJugador;
import club.modelo.conjunto.Categoria;
import club.modelo.conjunto.Competencia;
import club.modelo.conjunto.Deporte;
import club.modelo.conjunto.FilaPosicion;
import club.modelo.conjunto.InscripcionCompetencia;
import club.modelo.conjunto.InstitucionRival;
import club.modelo.conjunto.PartidoCompetencia;
import club.modelo.conjunto.Equipo;
import club.modelo.conjunto.EstadisticaBasket;
import club.modelo.conjunto.EstadisticaFutbol;
import club.modelo.conjunto.EstadisticaJugador;
import club.modelo.conjunto.EstadisticaVoleibol;
import club.modelo.conjunto.IntegranteEquipo;
import club.modelo.conjunto.Partido;
import club.modelo.conjunto.PartidoInterno;
import club.modelo.conjunto.Posicion;
import club.modelo.conjunto.TipoTarjeta;
import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Usuario;
import club.servicios.ClubSeneca;
import club.servicios.ReglaDeNegocioException;
import club.servicios.ServicioCompetencias;
import club.servicios.ServicioConjunto;
import club.servicios.ServicioPartidos;

/** Menu del modulo de deportes de conjunto y competencias. */
public class ConsolaConjunto {

    private final ClubSeneca club;
    private final Scanner in;

    public ConsolaConjunto(ClubSeneca club, Scanner in) {
        this.club = club;
        this.in = in;
    }

    public void ejecutar(Usuario usuario) {
        boolean salir = false;
        while (!salir) {
            mostrarMenu(usuario);
            String opcion = in.nextLine().trim();
            try {
                salir = procesar(opcion, usuario);
            } catch (ReglaDeNegocioException e) {
                System.out.println(">> RECHAZADO: " + e.getMessage());
            } catch (IllegalArgumentException | DateTimeParseException e) {
                System.out.println(">> DATO INVALIDO: " + e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------ menu

    private void mostrarMenu(Usuario usuario) {
        System.out.println();
        System.out.println("=== DEPORTES DE CONJUNTO (" + usuario.getNombre() + ") ===");
        System.out.println("1. Listar equipos");
        System.out.println("2. Listar partidos");
        if (usuario instanceof Socio) {
            System.out.println("3. Ver mis equipos");
            System.out.println("4. Ver mis partidos");
            System.out.println("5. Ver mis estadisticas");
            System.out.println("6. Ver mi desempeno en una competencia");
        }
        if (usuario.esAdministrador()) {
            System.out.println("5. Crear equipo");
            System.out.println("6. Dar de baja un equipo");
            System.out.println("7. Inscribir jugador");
            System.out.println("8. Retirar jugador");
            System.out.println("9. Programar partido interno");
            System.out.println("10. Registrar tarjeta");
            System.out.println("11. Registrar resultado");
            System.out.println("12. Registrar estadisticas de un jugador");
            System.out.println("13. Ver estadisticas de un partido");
            System.out.println("14. Registrar competencia");
            System.out.println("15. Registrar rival en una competencia");
            System.out.println("16. Inscribir equipo en una competencia");
            System.out.println("17. Registrar partido de competencia");
            System.out.println("18. Registrar resultado de partido de competencia");
            System.out.println("19. Ver tabla de posiciones");
            System.out.println("20. Ver estadisticas acumuladas de una competencia");
            System.out.println("21. Listar competencias");
        }
        System.out.println("0. Salir");
        System.out.print("Opcion: ");
    }

    private boolean procesar(String opcion, Usuario usuario) {
        switch (opcion) {
            case "0":
                return true;
            case "1":
                listarEquipos();
                break;
            case "2":
                listarPartidos();
                break;
            case "3":
                exigirSocio(usuario);
                verMisEquipos((Socio) usuario);
                break;
            case "4":
                exigirSocio(usuario);
                verMisPartidos((Socio) usuario);
                break;
            case "5":
                if (usuario instanceof Socio) {
                    verMisEstadisticas((Socio) usuario);
                } else {
                    exigirAdmin(usuario);
                    crearEquipo();
                }
                break;
            case "6":
                if (usuario instanceof Socio) {
                    verMiDesempenoCompetencia((Socio) usuario);
                } else {
                    exigirAdmin(usuario);
                    darDeBajaEquipo();
                }
                break;
            case "7":
                exigirAdmin(usuario);
                inscribirJugador();
                break;
            case "8":
                exigirAdmin(usuario);
                retirarJugador();
                break;
            case "9":
                exigirAdmin(usuario);
                programarPartido();
                break;
            case "10":
                exigirAdmin(usuario);
                registrarTarjeta();
                break;
            case "11":
                exigirAdmin(usuario);
                registrarResultado();
                break;
            case "12":
                exigirAdmin(usuario);
                registrarEstadistica();
                break;
            case "13":
                exigirAdmin(usuario);
                verEstadisticasPartido();
                break;
            case "14":
                exigirAdmin(usuario);
                registrarCompetencia();
                break;
            case "15":
                exigirAdmin(usuario);
                registrarRival();
                break;
            case "16":
                exigirAdmin(usuario);
                inscribirEquipoEnCompetencia();
                break;
            case "17":
                exigirAdmin(usuario);
                registrarPartidoCompetencia();
                break;
            case "18":
                exigirAdmin(usuario);
                registrarResultadoCompetencia();
                break;
            case "19":
                exigirAdmin(usuario);
                verTablaPosiciones();
                break;
            case "20":
                exigirAdmin(usuario);
                verEstadisticasAcumuladas();
                break;
            case "21":
                exigirAdmin(usuario);
                listarCompetencias();
                break;
            default:
                System.out.println("Opcion no valida");
        }
        return false;
    }

    private void exigirAdmin(Usuario usuario) {
        if (!usuario.esAdministrador()) {
            throw new ReglaDeNegocioException("Solo el administrador puede realizar esta operacion");
        }
    }

    private void exigirSocio(Usuario usuario) {
        if (!(usuario instanceof Socio)) {
            throw new ReglaDeNegocioException("Esta opcion es solo para socios");
        }
    }

    // ----------------------------------------------------------- operaciones

    private void crearEquipo() {
        ServicioConjunto sc = club.getServicioConjunto();
        String nombre = leer("Nombre del equipo: ");
        System.out.println("Deportes disponibles: " + nombresDeportes());
        Deporte deporte = sc.getDeporte(leer("Deporte: ").toUpperCase());
        if (deporte == null) {
            throw new ReglaDeNegocioException("No existe ese deporte");
        }
        Categoria categoria = Categoria.valueOf(
                leer("Categoria (SUB_13, SUB_15, SUB_17, MAYORES): ").toUpperCase());
        int cupo = leerEntero("Cupo maximo: ");
        Equipo equipo = sc.crearEquipo(nombre, deporte, categoria, cupo);
        System.out.println(">> Equipo creado con id " + equipo.getId());
    }

    private void darDeBajaEquipo() {
        Equipo equipo = pedirEquipo("Id del equipo a dar de baja: ");
        club.getServicioConjunto().darDeBajaEquipo(equipo);
        System.out.println(">> Equipo " + equipo.getId() + " dado de baja (se conserva el historial)");
    }

    private void inscribirJugador() {
        Equipo equipo = pedirEquipo("Id del equipo: ");
        Socio socio = club.getSocio(leer("Login del socio: "));
        Posicion posicion = Posicion.valueOf(leer("Posicion: ").toUpperCase());
        int camiseta = leerEntero("Numero de camiseta: ");
        club.getServicioConjunto().inscribirJugador(equipo, socio, posicion, camiseta);
        System.out.println(">> " + socio.getLogin() + " inscrito en " + equipo.getNombre());
    }

    private void retirarJugador() {
        Equipo equipo = pedirEquipo("Id del equipo: ");
        Socio socio = club.getSocio(leer("Login del socio: "));
        club.getServicioConjunto().retirarJugador(equipo, socio);
        System.out.println(">> " + socio.getLogin() + " retirado de " + equipo.getNombre());
    }

    private void programarPartido() {
        Equipo local = pedirEquipo("Id del equipo local: ");
        Equipo visitante = pedirEquipo("Id del equipo visitante: ");
        LocalDate fecha = LocalDate.parse(leer("Fecha (yyyy-MM-dd): "));
        PartidoInterno partido = club.getServicioPartidos().programarPartidoInterno(local, visitante, fecha);
        System.out.println(">> Partido programado: " + partido.getId());
    }

    private void registrarTarjeta() {
        PartidoInterno partido = pedirPartido("Id del partido: ");
        Socio socio = club.getSocio(leer("Login del jugador: "));
        TipoTarjeta tipo = TipoTarjeta.valueOf(leer("Tarjeta (AMARILLA/ROJA): ").toUpperCase());
        boolean suspendido = club.getServicioPartidos().registrarTarjeta(partido, socio, tipo);
        System.out.println(">> Tarjeta registrada" + (suspendido
                ? ". El jugador queda SUSPENDIDO y cumplira su sancion en el siguiente partido."
                : "."));
    }

    private void registrarResultado() {
        PartidoInterno partido = pedirPartido("Id del partido: ");
        int local = leerEntero("Marcador del local: ");
        int visitante = leerEntero("Marcador del visitante: ");
        club.getServicioPartidos().registrarResultado(partido, local, visitante);
        System.out.println(">> Resultado registrado: " + local + " - " + visitante);
    }

    private void registrarEstadistica() {
        Partido partido = pedirPartidoCualquiera("Id del partido (PT- interno, PC- competencia): ");
        Socio socio = club.getSocio(leer("Login del jugador: "));
        ServicioPartidos sp = club.getServicioPartidos();
        String deporte = partido.getDeporte().getNombre().toUpperCase();
        switch (deporte) {
            case "FUTBOL":
                sp.registrarEstadisticaFutbol(partido, socio,
                        leerEntero("Goles: "), leerEntero("Asistencias: "));
                break;
            case "BASKET":
                sp.registrarEstadisticaBasket(partido, socio,
                        leerEntero("Puntos: "), leerEntero("Rebotes: "));
                break;
            case "VOLEIBOL":
                sp.registrarEstadisticaVoleibol(partido, socio,
                        leerEntero("Aces: "), leerEntero("Bloqueos: "));
                break;
            default:
                throw new ReglaDeNegocioException("Deporte sin estadisticas: " + deporte);
        }
        System.out.println(">> Estadisticas registradas");
    }

    private void verEstadisticasPartido() {
        Partido partido = pedirPartidoCualquiera("Id del partido (PT- interno, PC- competencia): ");
        if (partido.getEstadisticas().isEmpty()) {
            System.out.println("Sin estadisticas registradas");
            return;
        }
        for (EstadisticaJugador e : partido.getEstadisticas()) {
            System.out.println(" - " + e.getJugador().getLogin() + ": " + describirEstadistica(e));
        }
    }

    /** HDU 18: estadisticas del jugador, por partido y acumuladas. */
    private void verMisEstadisticas(Socio socio) {
        int partidos = 0;
        int a = 0;
        int b = 0;
        for (Partido p : club.getServicioPartidos().getPartidos()) {
            EstadisticaJugador e = p.getEstadisticaJugador(socio);
            if (e == null) {
                continue;
            }
            partidos++;
            System.out.println(" - " + p.getId() + " (" + p.getDeporte().getNombre() + ", "
                    + p.getFecha() + "): " + describirEstadistica(e));
            if (e instanceof EstadisticaFutbol) {
                a += ((EstadisticaFutbol) e).getGoles();
                b += ((EstadisticaFutbol) e).getAsistencias();
            } else if (e instanceof EstadisticaBasket) {
                a += ((EstadisticaBasket) e).getPuntos();
                b += ((EstadisticaBasket) e).getRebotes();
            } else if (e instanceof EstadisticaVoleibol) {
                a += ((EstadisticaVoleibol) e).getAces();
                b += ((EstadisticaVoleibol) e).getBloqueos();
            }
        }
        if (partidos == 0) {
            System.out.println("Aun no tienes estadisticas registradas");
        } else {
            System.out.println("Partidos con estadisticas: " + partidos
                    + " | acumulado (1.o + 2.o campo del deporte): " + a + " / " + b);
        }
    }

    private String describirEstadistica(EstadisticaJugador e) {
        if (e instanceof EstadisticaFutbol) {
            EstadisticaFutbol f = (EstadisticaFutbol) e;
            return f.getGoles() + " goles, " + f.getAsistencias() + " asistencias, "
                    + f.getTarjetasAmarillas() + " amarillas" + (f.isTarjetaRoja() ? ", ROJA" : "");
        }
        if (e instanceof EstadisticaBasket) {
            EstadisticaBasket k = (EstadisticaBasket) e;
            return k.getPuntos() + " puntos, " + k.getRebotes() + " rebotes";
        }
        if (e instanceof EstadisticaVoleibol) {
            EstadisticaVoleibol v = (EstadisticaVoleibol) e;
            return v.getAces() + " aces, " + v.getBloqueos() + " bloqueos";
        }
        return "?";
    }

    // ----------------------------------------------------------- consultas

    private void listarEquipos() {
        List<Equipo> equipos = club.getServicioConjunto().getEquipos();
        if (equipos.isEmpty()) {
            System.out.println("No hay equipos.");
            return;
        }
        for (Equipo e : equipos) {
            imprimirEquipo(e);
        }
    }

    private void imprimirEquipo(Equipo e) {
        System.out.println(e.getId() + " | " + e.getNombre() + " | " + e.getDeporte().getNombre()
                + " | " + e.getCategoria() + " | " + (e.isActivo() ? "ACTIVO" : "DE BAJA")
                + " | jugadores " + e.getIntegrantesActivos().size() + "/" + e.getCupoMaximo()
                + " | minimo habilitados: " + (e.tieneMinimoHabilitados() ? "si" : "no"));
        for (IntegranteEquipo i : e.getIntegrantesActivos()) {
            System.out.println("     #" + i.getNumeroCamiseta() + " " + i.getSocio().getLogin()
                    + " " + i.getPosicion()
                    + (i.isHabilitado() ? "" : " [SUSPENDIDO]")
                    + (i.getAmarillasAcumuladasInterno() > 0
                            ? " (amarillas: " + i.getAmarillasAcumuladasInterno() + ")" : ""));
        }
    }

    private void listarPartidos() {
        List<Partido> partidos = club.getServicioPartidos().getPartidos();
        if (partidos.isEmpty()) {
            System.out.println("No hay partidos.");
            return;
        }
        for (Partido p : partidos) {
            System.out.println(describir(p));
        }
    }

    private void verMisEquipos(Socio socio) {
        List<Equipo> equipos = club.getServicioConjunto().getEquiposDe(socio);
        if (equipos.isEmpty()) {
            System.out.println("No perteneces a ningun equipo activo.");
            return;
        }
        for (Equipo e : equipos) {
            imprimirEquipo(e);
        }
    }

    private void verMisPartidos(Socio socio) {
        List<Equipo> mios = club.getServicioConjunto().getEquiposDe(socio);
        List<Partido> propios = new ArrayList<>();
        for (Partido p : club.getServicioPartidos().getPartidos()) {
            if (p instanceof PartidoInterno) {
                PartidoInterno interno = (PartidoInterno) p;
                if (mios.contains(interno.getLocal()) || mios.contains(interno.getVisitante())) {
                    propios.add(p);
                }
            } else if (p instanceof PartidoCompetencia
                    && ((PartidoCompetencia) p).getInscripcion().tieneJugador(socio.getLogin())) {
                propios.add(p);
            }
        }
        if (propios.isEmpty()) {
            System.out.println("No tienes partidos de tus equipos.");
            return;
        }
        for (Partido p : propios) {
            System.out.println(describir(p));
        }
    }

    private String describir(Partido p) {
        String enfrentamiento = "?";
        if (p instanceof PartidoInterno) {
            PartidoInterno i = (PartidoInterno) p;
            enfrentamiento = i.getLocal().getNombre() + " vs " + i.getVisitante().getNombre();
        } else if (p instanceof PartidoCompetencia) {
            PartidoCompetencia c = (PartidoCompetencia) p;
            enfrentamiento = c.getNombreLocal() + " vs " + c.getNombreVisitante()
                    + " [" + c.getIdCompetencia() + ", jornada " + c.getJornada()
                    + ", " + c.getEscenario() + "]";
        }
        return p.getId() + " | " + enfrentamiento + " | " + p.getFecha() + " | " + p.getEstado()
                + (p.estaJugado() ? " | " + p.getMarcadorLocal() + "-" + p.getMarcadorVisitante() : "");
    }

    // --------------------------------------------------------- competencias

    private void registrarCompetencia() {
        String nombre = leer("Nombre de la competencia: ");
        String organizador = leer("Organizador: ");
        String temporada = leer("Temporada (ej. 2026): ");
        System.out.println("Deportes disponibles: " + nombresDeportes());
        Deporte deporte = club.getServicioConjunto().getDeporte(leer("Deporte: ").toUpperCase());
        if (deporte == null) {
            throw new ReglaDeNegocioException("No existe ese deporte");
        }
        Categoria categoria = Categoria.valueOf(
                leer("Categoria (SUB_13, SUB_15, SUB_17, MAYORES): ").toUpperCase());
        Competencia competencia = club.getServicioCompetencias()
                .registrarCompetencia(nombre, organizador, temporada, deporte, categoria);
        System.out.println(">> Competencia registrada con id " + competencia.getId());
    }

    private void registrarRival() {
        Competencia competencia = pedirCompetencia("Id de la competencia: ");
        InstitucionRival rival = club.getServicioCompetencias()
                .registrarRival(competencia, leer("Nombre de la institucion rival: "));
        System.out.println(">> Rival " + rival.getNombre() + " registrado en " + competencia.getId());
    }

    private void inscribirEquipoEnCompetencia() {
        Competencia competencia = pedirCompetencia("Id de la competencia: ");
        Equipo equipo = pedirEquipo("Id del equipo: ");
        List<Socio> nomina = new ArrayList<>();
        for (String login : leer("Nomina (logins separados por coma): ").split(",")) {
            if (!login.trim().isEmpty()) {
                nomina.add(club.getSocio(login.trim()));
            }
        }
        club.getServicioCompetencias().inscribirEquipo(competencia, equipo, nomina);
        System.out.println(">> " + equipo.getNombre() + " inscrito en " + competencia.getId()
                + " con " + nomina.size() + " jugadores");
    }

    private void registrarPartidoCompetencia() {
        Competencia competencia = pedirCompetencia("Id de la competencia: ");
        Equipo equipo = pedirEquipo("Id del equipo del club: ");
        String rival = leer("Institucion rival: ");
        int jornada = leerEntero("Jornada: ");
        boolean local = leer("El club juega de local? (s/n): ").equalsIgnoreCase("s");
        String escenario = leer("Escenario: ");
        LocalDate fecha = LocalDate.parse(leer("Fecha (yyyy-MM-dd): "));
        PartidoCompetencia partido = club.getServicioCompetencias()
                .registrarPartido(competencia, equipo, rival, jornada, local, escenario, fecha);
        System.out.println(">> Partido de competencia registrado: " + partido.getId()
                + ". Registra las estadisticas (opcion 12) antes del resultado (opcion 18).");
    }

    private void registrarResultadoCompetencia() {
        Partido partido = pedirPartidoCualquiera("Id del partido de competencia: ");
        if (!(partido instanceof PartidoCompetencia)) {
            throw new ReglaDeNegocioException("No existe el partido de competencia " + partido.getId());
        }
        int marcadorClub = leerEntero("Marcador del club: ");
        int rival = leerEntero("Marcador del rival: ");
        club.getServicioCompetencias().registrarResultado((PartidoCompetencia) partido, marcadorClub, rival);
        System.out.println(">> Resultado registrado: club " + marcadorClub + " - rival " + rival);
    }

    /** HDU 8: tabla de posiciones de una competencia. */
    private void verTablaPosiciones() {
        Competencia competencia = pedirCompetencia("Id de la competencia: ");
        List<FilaPosicion> filas = club.getServicioCompetencias().tablaPosiciones(competencia);
        System.out.println("Tabla de " + competencia.getNombre() + " (" + competencia.getTemporada()
                + ")  [* = equipo del club]");
        System.out.printf("%-3s %-28s %3s %3s %3s %3s %4s %4s %4s %4s%n",
                "#", "Equipo", "PJ", "PG", "PE", "PP", "GF", "GC", "DG", "Pts");
        int puesto = 1;
        for (FilaPosicion f : filas) {
            System.out.printf("%-3d %-28s %3d %3d %3d %3d %4d %4d %4d %4d%n", puesto++,
                    (f.esClub() ? "* " : "") + f.getNombre(), f.getJugados(), f.getGanados(),
                    f.getEmpatados(), f.getPerdidos(), f.getGolesFavor(), f.getGolesContra(),
                    f.getDiferencia(), f.getPuntos());
        }
    }

    /** HDU 11: estadisticas individuales acumuladas de una competencia. */
    private void verEstadisticasAcumuladas() {
        Competencia competencia = pedirCompetencia("Id de la competencia: ");
        List<AcumuladoJugador> lista = club.getServicioCompetencias().estadisticasAcumuladas(competencia);
        if (lista.isEmpty()) {
            System.out.println("Aun no hay estadisticas de partidos jugados en esta competencia.");
            return;
        }
        String[] etiquetas = etiquetas(competencia.getDeporte());
        for (AcumuladoJugador a : lista) {
            System.out.println(" - " + a.getLogin() + ": " + a.getPartidos() + " partidos, "
                    + a.getA() + " " + etiquetas[0] + ", " + a.getB() + " " + etiquetas[1]);
        }
    }

    /** HDU 20: desempeno del jugador en una competencia y su posicion en las clasificaciones. */
    private void verMiDesempenoCompetencia(Socio socio) {
        ServicioCompetencias sc = club.getServicioCompetencias();
        for (Competencia c : sc.getCompetencias()) {
            System.out.println(" " + c.getId() + " | " + c.getNombre() + " | " + c.getTemporada());
        }
        Competencia competencia = pedirCompetencia("Id de la competencia: ");
        if (!sc.participa(competencia, socio)) {
            throw new ReglaDeNegocioException("No participas en esa competencia");
        }
        AcumuladoJugador acumulado = sc.acumuladoDe(competencia, socio);
        if (acumulado == null) {
            System.out.println("Participas en " + competencia.getNombre()
                    + ", pero aun no tienes estadisticas registradas.");
            return;
        }
        String[] etiquetas = etiquetas(competencia.getDeporte());
        int[] posiciones = sc.posicionesDe(competencia, socio);
        System.out.println(competencia.getNombre() + ": " + acumulado.getPartidos() + " partidos");
        System.out.println(" - " + acumulado.getA() + " " + etiquetas[0]
                + " (posicion " + posiciones[0] + " en la clasificacion de " + etiquetas[0] + ")");
        System.out.println(" - " + acumulado.getB() + " " + etiquetas[1]
                + " (posicion " + posiciones[1] + " en la clasificacion de " + etiquetas[1] + ")");
    }

    private void listarCompetencias() {
        List<Competencia> competencias = club.getServicioCompetencias().getCompetencias();
        if (competencias.isEmpty()) {
            System.out.println("No hay competencias.");
            return;
        }
        for (Competencia c : competencias) {
            StringBuilder rivales = new StringBuilder();
            for (InstitucionRival r : c.getRivales()) {
                rivales.append(rivales.length() > 0 ? ", " : "").append(r.getNombre());
            }
            System.out.println(c.getId() + " | " + c.getNombre() + " | " + c.getOrganizador()
                    + " | " + c.getTemporada() + " | " + c.getDeporte().getNombre()
                    + " | " + c.getCategoria() + " | equipos inscritos: " + c.getInscripciones().size()
                    + " | rivales: " + (rivales.length() == 0 ? "ninguno" : rivales));
            for (InscripcionCompetencia i : c.getInscripciones()) {
                StringBuilder nomina = new StringBuilder();
                for (Socio s : i.getNomina()) {
                    nomina.append(nomina.length() > 0 ? ", " : "").append(s.getLogin());
                }
                System.out.println("     " + i.getEquipo().getNombre() + " | nomina (" + i.getNomina().size()
                        + "): " + nomina);
            }
        }
    }

    private String[] etiquetas(Deporte deporte) {
        switch (deporte.getNombre().toUpperCase()) {
            case "FUTBOL":
                return new String[] { "goles", "asistencias" };
            case "BASKET":
                return new String[] { "puntos", "rebotes" };
            case "VOLEIBOL":
                return new String[] { "aces", "bloqueos" };
            default:
                return new String[] { "campo 1", "campo 2" };
        }
    }

    private Competencia pedirCompetencia(String mensaje) {
        String id = leer(mensaje).toUpperCase();
        Competencia competencia = club.getServicioCompetencias().getCompetencia(id);
        if (competencia == null) {
            throw new ReglaDeNegocioException("No existe la competencia " + id);
        }
        return competencia;
    }

    private Partido pedirPartidoCualquiera(String mensaje) {
        String id = leer(mensaje).toUpperCase();
        Partido partido = club.getServicioPartidos().getPartido(id);
        if (partido == null) {
            throw new ReglaDeNegocioException("No existe el partido " + id);
        }
        return partido;
    }

    // --------------------------------------------------------------- ayudas

    private String nombresDeportes() {
        StringBuilder sb = new StringBuilder();
        for (Deporte d : club.getServicioConjunto().getDeportes()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(d.getNombre());
        }
        return sb.toString();
    }

    private Equipo pedirEquipo(String mensaje) {
        String id = leer(mensaje).toUpperCase();
        Equipo equipo = club.getServicioConjunto().getEquipo(id);
        if (equipo == null) {
            throw new ReglaDeNegocioException("No existe el equipo " + id);
        }
        return equipo;
    }

    private PartidoInterno pedirPartido(String mensaje) {
        String id = leer(mensaje).toUpperCase();
        Partido partido = club.getServicioPartidos().getPartido(id);
        if (!(partido instanceof PartidoInterno)) {
            throw new ReglaDeNegocioException("No existe el partido interno " + id);
        }
        return (PartidoInterno) partido;
    }

    private String leer(String mensaje) {
        System.out.print(mensaje);
        return in.nextLine().trim();
    }

    private int leerEntero(String mensaje) {
        return Integer.parseInt(leer(mensaje));
    }
}
