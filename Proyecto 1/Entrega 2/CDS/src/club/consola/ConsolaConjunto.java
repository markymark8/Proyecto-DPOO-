package club.consola;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import club.modelo.conjunto.Categoria;
import club.modelo.conjunto.Deporte;
import club.modelo.conjunto.Equipo;
import club.modelo.conjunto.IntegranteEquipo;
import club.modelo.conjunto.Partido;
import club.modelo.conjunto.PartidoInterno;
import club.modelo.conjunto.Posicion;
import club.modelo.conjunto.TipoTarjeta;
import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Usuario;
import club.servicios.ClubSeneca;
import club.servicios.ReglaDeNegocioException;
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
        }
        if (usuario.esAdministrador()) {
            System.out.println("5. Crear equipo");
            System.out.println("6. Dar de baja un equipo");
            System.out.println("7. Inscribir jugador");
            System.out.println("8. Retirar jugador");
            System.out.println("9. Programar partido interno");
            System.out.println("10. Registrar tarjeta");
            System.out.println("11. Registrar resultado");
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
                exigirAdmin(usuario);
                crearEquipo();
                break;
            case "6":
                exigirAdmin(usuario);
                darDeBajaEquipo();
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
        String enfrentamiento = "(competencia)";
        if (p instanceof PartidoInterno) {
            PartidoInterno i = (PartidoInterno) p;
            enfrentamiento = i.getLocal().getNombre() + " vs " + i.getVisitante().getNombre();
        }
        return p.getId() + " | " + enfrentamiento + " | " + p.getFecha() + " | " + p.getEstado()
                + (p.estaJugado() ? " | " + p.getMarcadorLocal() + "-" + p.getMarcadorVisitante() : "");
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
