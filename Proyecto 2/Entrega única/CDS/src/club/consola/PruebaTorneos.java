package club.consola;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import club.modelo.individual.Academia;
import club.modelo.individual.Disciplina;
import club.modelo.individual.InscripcionAcademia;
import club.modelo.individual.Nivel;
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
import club.servicios.ServicioTorneos;

public class PruebaTorneos {

    private static Socio socio(ClubSeneca club, String login) {
        Socio s = new Socio(login, login, Usuario.hash("1234"), LocalDate.of(2000, 1, 1));
        club.registrarUsuario(s);
        return s;
    }

    private static void inscribirEnAcademia(Academia academia, Socio s, Nivel nivel, int puntaje) {
        academia.agregarInscripcion(
                new InscripcionAcademia(s, academia, nivel, puntaje, LocalDate.of(2026, 1, 1)));
    }

    /** Ejecuta la acción e imprime si fue aceptada o rechazada, y por qué. */
    private static void intentar(String descripcion, Runnable accion) {
        try {
            accion.run();
            System.out.println("ACEPTADO   " + descripcion);
        } catch (ReglaDeNegocioException e) {
            System.out.println("RECHAZADO  " + descripcion + " -> " + e.getMessage());
        }
    }
    
    private static void comparar(Path a, Path b, String archivo) throws IOException {
        List<String> original = Files.readAllLines(a.resolve(archivo));
        List<String> guardado = Files.readAllLines(b.resolve(archivo));
        boolean igual = original.equals(guardado);
        System.out.println(archivo + ": " + (igual ? "IGUAL" : "DIFERENTE"));
        if (!igual) {
            System.out.println("  original: " + original);
            System.out.println("  guardado: " + guardado);
        }
    }

    private static void pruebaIdaYVuelta() throws IOException {
        Path datos = Path.of("datos");
        Path temporal = Files.createTempDirectory("torneos");

        ClubSeneca club = new ClubSeneca();
        // Los usuarios deben existir antes de cargar (jug01..jug22 de tus CSV)
        for (int i = 1; i <= 22; i++) {
            socio(club, String.format("jug%02d", i));
        }

        club.cargar(datos);
        club.guardar(temporal);

        comparar(datos, temporal, "torneos.csv");
        comparar(datos, temporal, "inscripcionesTorneo.csv");

        ServicioTorneos servicio = club.getServicioTorneos();
        System.out.println("Torneos cargados: " + servicio.getTorneos().size() + " (esperado 3)");

        Torneo nuevo = servicio.crearTorneoRecreativo(
                "Nuevo", Modalidad.CONJUNTO, "FUTBOL", "MAYORES", 4, LocalDate.now().plusDays(10));
        System.out.println("Id del torneo nuevo: " + nuevo.getId() + " (esperado TN-004)");
    }

    public static void main(String[] args) throws IOException {
        ClubSeneca club = new ClubSeneca();
        LocalDate fecha = LocalDate.now().plusDays(10);

        // Datos de ejemplo: academia de tenis con socios de distinto nivel y puntaje
        Academia tenis = new Academia(Disciplina.TENIS, 2);
        club.getServicioIndividual().getAcademias().add(tenis);

        Socio ana = socio(club, "ana");
        Socio beto = socio(club, "beto");
        Socio carla = socio(club, "carla");
        Socio dani = socio(club, "dani");   // sin inscripción a la academia
        inscribirEnAcademia(tenis, ana, Nivel.INTERMEDIO, 1200);
        inscribirEnAcademia(tenis, beto, Nivel.INTERMEDIO, 1000);
        inscribirEnAcademia(tenis, carla, Nivel.AVANZADO, 1500);

        Empleado libre = new Empleado("emp1", "Empleado libre", Usuario.hash("1234"), "COD1");
        Empleado ocupado = new Empleado("emp2", "Empleado de turno", Usuario.hash("1234"), "COD2");
        ocupado.agregarTurno(new Turno(
                Date.from(fecha.atStartOfDay(ZoneId.systemDefault()).toInstant()), "08:00", "16:00"));

        // Torneo individual de tenis, nivel INTERMEDIO, 5 cupos (1 preferencial)
        Torneo torneo = club.getServicioTorneos().crearTorneoRecreativo(
                "Copa Tenis", Modalidad.INDIVIDUAL, "TENIS", "INTERMEDIO", 5, fecha);
        String id = torneo.getId();
        System.out.println("Torneo " + id + ": cupos preferenciales = " + torneo.getCuposPreferenciales());

        InscripcionIndividual[] resultado = new InscripcionIndividual[1];

        intentar("ana (1200) - debe ser PREFERENCIAL",
                () -> resultado[0] = club.getServicioInscripcion().inscribirJugador(id, ana));
        System.out.println("   preferencial = " + resultado[0].isPreferencial());

        intentar("beto (1000) - debe ser REGULAR",
                () -> resultado[0] = club.getServicioInscripcion().inscribirJugador(id, beto));
        System.out.println("   preferencial = " + resultado[0].isPreferencial());

        intentar("carla (nivel AVANZADO) - debe rechazarse por nivel",
                () -> club.getServicioInscripcion().inscribirJugador(id, carla));
        intentar("dani (sin academia) - debe rechazarse",
                () -> club.getServicioInscripcion().inscribirJugador(id, dani));
        intentar("ana otra vez - debe rechazarse por duplicada",
                () -> club.getServicioInscripcion().inscribirJugador(id, ana));
        intentar("empleado libre - debe aceptarse como regular, puntaje 0",
                () -> club.getServicioInscripcion().inscribirJugador(id, libre));
        intentar("empleado de turno ese día - debe rechazarse",
                () -> club.getServicioInscripcion().inscribirJugador(id, ocupado));

        System.out.println("Inscritos: " + torneo.getInscripciones().size() + " (esperado 3)");

        intentar("retirar a beto - debe aceptarse",
                () -> club.getServicioInscripcion().retirarInscripcion(id, "beto"));
        System.out.println("Inscritos: " + torneo.getInscripciones().size() + " (esperado 2)");

        intentar("iniciar el torneo - debe aceptarse (hay 2)",
                () -> club.getServicioTorneos().iniciarTorneo(id));
        intentar("inscribir a beto con el torneo en curso - debe rechazarse",
                () -> club.getServicioInscripcion().inscribirJugador(id, beto));

        // Dobles: un jugador no puede estar en dos parejas del mismo torneo
        Torneo dobles = club.getServicioTorneos().crearTorneoRecreativo(
                "Dobles", Modalidad.DOBLES, "TENIS", "INTERMEDIO", 4, fecha);
        String idD = dobles.getId();
        intentar("pareja ana-beto - debe aceptarse",
                () -> club.getServicioInscripcion().inscribirPareja(idD, ana, beto));
        intentar("pareja ana-carla - debe rechazarse (ana ya está inscrita)",
                () -> club.getServicioInscripcion().inscribirPareja(idD, ana, carla));
        intentar("empleado en dobles - debe rechazarse",
                () -> club.getServicioInscripcion().inscribirPareja(idD, libre, beto));
        
        System.out.println("\n--- Ida y vuelta de persistencia ---");
        pruebaIdaYVuelta();
    }
}