package club.consola;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import club.modelo.individual.BloqueHorario;
import club.modelo.individual.Disciplina;
import club.modelo.individual.EstadisticaIndividual;
import club.modelo.individual.EstadisticaTenisDeMesa;
import club.modelo.individual.EstadisticaTenisPadel;
import club.modelo.individual.Instalacion;
import club.modelo.individual.Modalidad;
import club.modelo.individual.PartidoPractica;
import club.modelo.individual.RegistroRanking;
import club.modelo.individual.Reserva;
import club.modelo.usuarios.Entrenador;
import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Usuario;
import club.servicios.ClubSeneca;
import club.servicios.ReglaDeNegocioException;
import club.servicios.ServicioIndividual;

public class ConsolaIndividual {

    private final ClubSeneca club;
    private final ServicioIndividual servicio;
    private final Scanner in;

    public ConsolaIndividual(ClubSeneca club, Scanner in) {
        this.club = club;
        this.servicio = club.getServicioIndividual();
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

    // menu

    private void mostrarMenu(Usuario usuario) {
        System.out.println();
        System.out.println("=== DEPORTES INDIVIDUALES (" + usuario.getNombre() + ") ===");
        System.out.println("1. Listar instalaciones");
        if (!usuario.esAdministrador()) {
            System.out.println("2. Crear reserva");
            System.out.println("3. Ver mis reservas");
            System.out.println("4. Cancelar una reserva");
        }
        if (usuario instanceof Socio) {
            System.out.println("5. Registrar ingreso a una reserva");
            System.out.println("6. Consultar mi ranking");
            System.out.println("7. Consultar partidos de practica");
            System.out.println("8. Programar partido de practica");
            System.out.println("9. Ver mis estadisticas");
        }
        if (usuario.esAdministrador()) {
            System.out.println("12. Reporte de asistencia");
            System.out.println("14. Registrar instalacion");
            System.out.println("13. Registrar resultado de un partido de practica");
        }
        System.out.println("0. Volver");
        System.out.print("Opcion: ");
    }

    private boolean procesar(String opcion, Usuario usuario) {
        switch (opcion) {
            case "0": return true;
            case "1": listarInstalaciones(); break;
            case "2": crearReserva(usuario); break;
            case "3": verMisReservas(usuario); break;
            case "4": cancelarReserva(usuario); break;
            case "5": registrarIngreso(usuario); break;
            case "6": consultarRanking(usuario); break;
            case "7": consultarPartidos(); break;
            case "8": programarPartido(usuario); break;
            case "9": verMisEstadisticas(usuario); break;
            case "12": reporteAsistencia(usuario); break;
            case "13": registrarResultadoPartido(usuario); break;
            case "14": registrarInstalacion(usuario); break;
            default: System.out.println("Opcion invalida.");
        }
        return false;
    }

    // lectura de datos

    private String leer(String mensaje) {
        System.out.print(mensaje + ": ");
        return in.nextLine().trim();
    }

    private int leerEntero(String mensaje) {
        return Integer.parseInt(leer(mensaje));
    }

    private Instalacion exigirInstalacion(int codigo) {
        for (Instalacion i : servicio.getInstalaciones()) {
            if (i.getCodigo() == codigo) {
                return i;
            }
        }
        throw new ReglaDeNegocioException("No existe la instalacion " + codigo);
    }

    private Reserva exigirReserva(int id) {
        for (Reserva r : servicio.getReservas()) {
            if (r.getId() == id) {
                return r;
            }
        }
        throw new ReglaDeNegocioException("No existe la reserva " + id);
    }

    private static Socio comoSocio(Usuario usuario) {
        if (!(usuario instanceof Socio)) {
            throw new ReglaDeNegocioException("Esta opcion es solo para socios");
        }
        return (Socio) usuario;
    }

    // operaciones

    private void listarInstalaciones() {
        if (servicio.getInstalaciones().isEmpty()) {
            System.out.println("No hay instalaciones registradas.");
            return;
        }
        for (Instalacion i : servicio.getInstalaciones()) {
            System.out.println(i.getCodigo() + ". " + i.getNombre()
                    + " | " + i.getDisciplina()
                    + " | capacidad " + i.getCapacidad()
                    + " | techada: " + (i.isTechada() ? "si" : "no")
                    + " | habilitada: " + (i.isHabilitada() ? "si" : "no"));
        }
    }

    private void crearReserva(Usuario usuario) {
        listarInstalaciones();
        Instalacion instalacion = exigirInstalacion(leerEntero("Codigo de la instalacion"));
        LocalDate fecha = LocalDate.parse(leer("Fecha (yyyy-MM-dd)"));
        LocalTime inicio = LocalTime.parse(leer("Hora de inicio (HH:mm)"));
        LocalTime fin = LocalTime.parse(leer("Hora de fin (HH:mm)"));
        int jugadores = leerEntero("Numero de jugadores");
        Modalidad modalidad = Modalidad.valueOf(
                leer("Modalidad (SENCILLOS / DOBLES)").toUpperCase());

        Reserva reserva = servicio.reservar(usuario, instalacion,
                new BloqueHorario(fecha, inicio, fin), jugadores, modalidad);

        System.out.println(">> Reserva creada con ID: " + reserva.getId());
    }

    private void verMisReservas(Usuario usuario) {
        boolean hay = false;
        for (Reserva r : servicio.getHistorialReservas()) {
            if (r.getResponsable().getLogin().equals(usuario.getLogin())) {
                hay = true;
                System.out.println("ID: " + r.getId()
                        + " | " + r.getInstalacion().getNombre()
                        + " | " + r.getBloque().getFecha()
                        + " " + r.getBloque().getHoraInicio()
                        + " | Estado: " + r.getEstado());
            }
        }
        if (!hay) {
            System.out.println("No tienes reservas.");
        }
    }

    private void cancelarReserva(Usuario usuario) {
        Reserva reserva = exigirReserva(leerEntero("ID de la reserva"));
        servicio.cancelarReserva(usuario, reserva);
        System.out.println(">> Reserva cancelada correctamente.");
    }

    private void registrarIngreso(Usuario usuario) {
        Socio socio = comoSocio(usuario);
        Reserva reserva = exigirReserva(leerEntero("ID de la reserva"));
        servicio.registrarIngreso(socio, reserva, LocalDateTime.now());
        System.out.println(">> Ingreso registrado correctamente.");
    }

    private void consultarRanking(Usuario usuario) {
        Socio socio = comoSocio(usuario);
        Disciplina disciplina = Disciplina.valueOf(
                leer("Disciplina (TENIS, PADEL, TENIS_DE_MESA)").toUpperCase());
        System.out.println("Tu ranking en " + disciplina + ": "
                + servicio.getRanking(socio, disciplina) + " puntos");
    }

    private void consultarPartidos() {
        if (servicio.getHistorialPartidosPractica().isEmpty()) {
            System.out.println("No hay partidos de practica.");
            return;
        }
        for (PartidoPractica p : servicio.getHistorialPartidosPractica()) {
            System.out.println("ID: " + p.getId()
                    + " | Fecha: " + p.getBloque().getFecha()
                    + " | Jugado: " + p.isJugado());
        }
    }

    private void reporteAsistencia(Usuario usuario) {
        if (!usuario.esAdministrador()) {
            throw new ReglaDeNegocioException("Esta opcion es solo para administradores");
        }
        Disciplina disciplina = Disciplina.valueOf(
                leer("Disciplina (TENIS, PADEL, TENIS_DE_MESA)").toUpperCase());
        LocalDate desde = LocalDate.parse(leer("Desde (yyyy-MM-dd)"));
        LocalDate hasta = LocalDate.parse(leer("Hasta (yyyy-MM-dd)"));
        System.out.println(servicio.reporteAsistencia(disciplina, desde, hasta));
    }

    private void registrarInstalacion(Usuario usuario) {
        if (!usuario.esAdministrador()) {
            throw new ReglaDeNegocioException("Esta opcion es solo para administradores");
        }
        int codigo = leerEntero("Codigo");
        String nombre = leer("Nombre");
        String disciplina = leer("Disciplina (TENIS, PADEL, TENIS_DE_MESA)").toUpperCase();
        int capacidad = leerEntero("Capacidad (2 o 4)");
        boolean techada = leer("Techada (s/n)").equalsIgnoreCase("s");

        Instalacion nueva = servicio.registrarInstalacion(codigo, nombre, disciplina, capacidad, techada);
        System.out.println(">> Instalacion registrada: " + nueva.getNombre());
    }

    // partidos de practica

    private List<Socio> leerSocios(String mensaje) {
        List<Socio> socios = new ArrayList<>();
        for (String login : leer(mensaje).split(",")) {
            Usuario u = club.buscarUsuario(login.trim());
            if (!(u instanceof Socio)) {
                throw new ReglaDeNegocioException("No existe el socio " + login.trim());
            }
            socios.add((Socio) u);
        }
        return socios;
    }

    private PartidoPractica exigirPartido(int id) {
        for (PartidoPractica p : servicio.getHistorialPartidosPractica()) {
            if (p.getId() == id) {
                return p;
            }
        }
        throw new ReglaDeNegocioException("No existe el partido de practica " + id);
    }

    private void programarPartido(Usuario usuario) {
        comoSocio(usuario);
        listarInstalaciones();
        Instalacion instalacion = exigirInstalacion(leerEntero("Codigo de la instalacion"));
        LocalDate fecha = LocalDate.parse(leer("Fecha (yyyy-MM-dd)"));
        LocalTime inicio = LocalTime.parse(leer("Hora de inicio (HH:mm)"));
        LocalTime fin = LocalTime.parse(leer("Hora de fin (HH:mm)"));
        List<Socio> ladoA = leerSocios("Logins del lado A, separados por coma (incluya el suyo)");
        List<Socio> ladoB = leerSocios("Logins del lado B, separados por coma");

        PartidoPractica partido = servicio.programarPartido(usuario, instalacion,
                new BloqueHorario(fecha, inicio, fin), ladoA, ladoB);
        System.out.println(">> Partido de practica programado con ID: " + partido.getId()
                + " (" + partido.getModalidad() + ")");
    }

    private void verMisEstadisticas(Usuario usuario) {
        Socio socio = comoSocio(usuario);
        Disciplina disciplina = Disciplina.valueOf(
                leer("Disciplina (TENIS, PADEL, TENIS_DE_MESA)").toUpperCase());
        List<EstadisticaIndividual> lista = servicio.getEstadisticas(socio, disciplina);
        if (lista.isEmpty()) {
            System.out.println("Todavia no tienes estadisticas en " + disciplina + ".");
            return;
        }
        for (EstadisticaIndividual e : lista) {
            String detalle = "";
            if (e instanceof EstadisticaTenisPadel) {
                EstadisticaTenisPadel t = (EstadisticaTenisPadel) e;
                detalle = " | games " + t.getGamesGanados() + " | aces " + t.getAces()
                        + " | errores no forzados " + t.getErroresNoForzados();
            } else if (e instanceof EstadisticaTenisDeMesa) {
                detalle = " | puntos totales " + ((EstadisticaTenisDeMesa) e).getPuntosTotales();
            }
            System.out.println("Partido " + e.getPartido().getId()
                    + " | sets ganados " + e.getSetsGanados() + detalle);
        }
    }

    private void registrarResultadoPartido(Usuario usuario) {
        if (!usuario.esAdministrador()) {
            throw new ReglaDeNegocioException("Esta opcion es solo para administradores");
        }
        consultarPartidos();
        PartidoPractica partido = exigirPartido(leerEntero("ID del partido"));
        Disciplina disciplina = Disciplina.valueOf(partido.getInstalacion().getDisciplina());

        List<Socio> participantes = new ArrayList<>(partido.getLadoA());
        participantes.addAll(partido.getLadoB());

        List<EstadisticaIndividual> estadisticas = new ArrayList<>();
        for (Socio s : participantes) {
            System.out.println("Estadisticas de " + s.getLogin()
                    + (partido.getLadoA().contains(s) ? " (lado A)" : " (lado B)"));
            int sets = leerEntero("  Sets ganados");
            if (disciplina == Disciplina.TENIS_DE_MESA) {
                int puntos = leerEntero("  Puntos totales");
                estadisticas.add(new EstadisticaTenisDeMesa(s, partido, sets, puntos));
            } else {
                int games = leerEntero("  Games ganados");
                int aces = leerEntero("  Aces");
                int errores = leerEntero("  Errores no forzados");
                estadisticas.add(new EstadisticaTenisPadel(s, partido, sets, games, aces, errores));
            }
        }

        servicio.registrarResultado(partido, estadisticas);
        System.out.println(">> Resultado registrado. Cambios en el ranking:");
        for (RegistroRanking r : partido.getRegistros()) {
            System.out.println("   " + r.getSocio().getLogin() + ": "
                    + r.getPuntajeAnterior() + " -> " + r.getPuntajeNuevo());
        }
    }
}
