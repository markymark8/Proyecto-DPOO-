
package club.consola;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Scanner;

import club.servicios.ClubSeneca;
import club.servicios.ServicioIndividual;
import club.servicios.ReglaDeNegocioException;
import club.modelo.individual.*;
import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Administrador;
import club.modelo.usuarios.Entrenador;


public class ConsolaIndividual {

    private static ClubSeneca club = new ClubSeneca();
    private static ServicioIndividual servicio =
            club.getServicioIndividual();

    private static Scanner scanner = new Scanner(System.in);

    private static Socio socio;
    private static Instalacion instalacion;
    private static Reserva reserva;
    private static Socio socio2;
    private static PartidoPractica partido;
    private static SesionEntrenamiento sesion;
    private static Entrenador entrenador;


    public static void main(String[] args) {

        prepararDatos();

        int opcion = -1;

        while (opcion != 0) {

            System.out.println("\n=== CONSOLA DE RESERVAS ===");
            System.out.println("1. Crear reserva");
            System.out.println("2. Consultar reservas");
            System.out.println("3. Registrar ingreso");
            System.out.println("4. Cancelar reserva");
            System.out.println("5. Programar partido de practica");
            System.out.println("6. Consultar partidos");
            System.out.println("7. Registrar resultado");
            System.out.println("8. Consultar ranking");
            System.out.println("9. Programar sesion de entrenamiento");
            System.out.println("10. Consultar sesiones");
            System.out.println("11. Registrar asistencia y feedback");
            System.out.println("12. Consultar reporte de asistencia");
            System.out.println("0. Salir");
            System.out.print("Seleccione una opcion: ");

            try {
                opcion = Integer.parseInt(scanner.nextLine());

                switch (opcion) {
                    case 1:
                        crearReserva();
                        break;
                    case 2:
                        consultarReservas();
                        break;
                    case 3:
                        registrarIngreso();
                        break;
                    case 4:
                        cancelarReserva();
                        break;
                    case 5:
                        programarPartido();
                        break;

                    case 6:
                        consultarPartidos();
                        break;

                    case 7:
                        registrarResultado();
                        break;

                    case 8:
                        consultarRanking();
                        break;
                    case 9:
                        programarSesion();
                        break;

                    case 10:
                        consultarSesiones();
                        break;

                    case 11:
                        registrarFeedback();
                        break;

                    case 12:
                        consultarAsistencia();
                        break;
                    case 0:
                        System.out.println("Fin de la prueba.");
                        break;
                    default:
                        System.out.println("Opcion invalida.");
                }

            } catch (ReglaDeNegocioException e) {
                System.out.println("Operacion rechazada: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        scanner.close();
    }

    private static void prepararDatos() {

        socio = new Socio(
            "socio1", "Mariana", "1234",
            LocalDate.of(2004, 5, 10)
        );

        club.registrarUsuario(socio);

        Academia academia = new Academia(Disciplina.TENIS, 2);
        servicio.getAcademias().add(academia);
       

        instalacion = new Instalacion(
            1, "Cancha de tenis 1", "TENIS",
            2, false, true
        );

        servicio.getInstalaciones().add(instalacion);

        servicio.inscribirEnAcademia(socio, Disciplina.TENIS);
        
        socio2 = new Socio(
        	    "socio2", "Camilo", "1234",
        	    LocalDate.of(2003, 8, 15)
        	);

        	club.registrarUsuario(socio2);

        	servicio.inscribirEnAcademia(socio2, Disciplina.TENIS);
        	
        entrenador = new Entrenador(
        		    "entrenador1",
        		    "Andres",
        		    "1234",
        		    "DESC10",
        		    101
        		);

        		club.registrarUsuario(entrenador);

        System.out.println("Datos de prueba preparados.");
    }

    private static void crearReserva() {

        System.out.println("\n--- CREAR RESERVA ---");

        LocalDate fecha = LocalDate.now().plusDays(1);

        BloqueHorario bloque = new BloqueHorario(
            fecha,
            LocalTime.of(10, 0),
            LocalTime.of(11, 0)
        );

        reserva = servicio.reservar(
            socio,
            instalacion,
            bloque,
            2,
            Modalidad.SENCILLOS
        );

        System.out.println(
            "Reserva creada con ID: " + reserva.getId()
        );
    }

    private static void consultarReservas() {

        System.out.println("\n--- HISTORIAL DE RESERVAS ---");

        for (Reserva r : servicio.getHistorialReservas()) {

            System.out.println(
                "ID: " + r.getId()
                + " | Instalacion: " + r.getInstalacion().getNombre()
                + " | Fecha: " + r.getBloque().getFecha()
                + " | Estado: " + r.getEstado()
            );
        }
    }

    private static void registrarIngreso() {

        if (reserva == null) {
            System.out.println("Primero debes crear una reserva.");
            return;
        }

        servicio.registrarIngreso(
            socio,
            reserva,
            LocalDateTime.of(
                reserva.getBloque().getFecha(),
                reserva.getBloque().getHoraInicio()
            )
        );

        System.out.println("Ingreso registrado correctamente.");
    }

    private static void cancelarReserva() {

        if (reserva == null) {
            System.out.println("Primero debes crear una reserva.");
            return;
        }

        servicio.cancelarReserva(socio, reserva);

        System.out.println("Reserva cancelada correctamente.");
    }
    

private static void programarPartido() {

    System.out.println("\n--- PROGRAMAR PARTIDO ---");

    BloqueHorario bloque = new BloqueHorario(
        LocalDate.now().plusDays(2),
        LocalTime.of(14, 0),
        LocalTime.of(15, 0)
    );

    partido = servicio.programarPartido(
        socio,
        instalacion,
        bloque,
        java.util.Arrays.asList(socio),
        java.util.Arrays.asList(socio2)
    );

    System.out.println("Partido programado correctamente.");
    System.out.println("ID: " + partido.getId());
    System.out.println("Jugador A: " + socio.getNombre());
    System.out.println("Jugador B: " + socio2.getNombre());
}

private static void consultarPartidos() {

    System.out.println("\n--- PARTIDOS DE PRACTICA ---");

    for (PartidoPractica p : servicio.getHistorialPartidosPractica()) {

        System.out.println(
            "ID: " + p.getId()
            + " | Fecha: " + p.getBloque().getFecha()
            + " | Jugado: " + p.isJugado()
        );
    }
}

private static void registrarResultado() {

    if (partido == null) {
        System.out.println("Primero debes programar un partido.");
        return;
    }

    System.out.println("\n--- REGISTRAR RESULTADO ---");

    EstadisticaTenisPadel estadisticaA =
        new EstadisticaTenisPadel(
            socio, partido, 2, 12, 4, 3
        );

    EstadisticaTenisPadel estadisticaB =
        new EstadisticaTenisPadel(
            socio2, partido, 0, 5, 2, 6
        );

    servicio.registrarResultado(
        partido,
        java.util.Arrays.asList(estadisticaA, estadisticaB)
    );

    System.out.println("Resultado registrado correctamente.");
    System.out.println("Ganador: " + socio.getNombre());
}

private static void consultarRanking() {

    System.out.println("\n--- RANKING DE TENIS ---");

    int ranking1 = servicio.getRanking(socio, Disciplina.TENIS);
    int ranking2 = servicio.getRanking(socio2, Disciplina.TENIS);

    System.out.println(socio.getNombre() + ": " + ranking1 + " puntos");
    System.out.println(socio2.getNombre() + ": " + ranking2 + " puntos");
}


private static void programarSesion() {

    System.out.println("\n--- PROGRAMAR SESION ---");

    BloqueHorario bloque = new BloqueHorario(
        LocalDate.now().plusDays(3),
        LocalTime.of(9, 0),
        LocalTime.of(10, 0)
    );

    sesion = new SesionEntrenamiento(
        servicio.nuevoId(),
        instalacion,
        bloque,
        entrenador,
        TipoSesion.INDIVIDUAL
    );

    sesion.convocar(socio);
    sesion.convocar(socio2);

    servicio.getReservas().add(sesion);

    System.out.println("Sesion creada correctamente.");
    System.out.println("ID: " + sesion.getId());
    System.out.println("Convocados: " + sesion.getConvocados().size());
}

private static void consultarSesiones() {

    System.out.println("\n--- SESIONES DE ENTRENAMIENTO ---");

    for (SesionEntrenamiento s : servicio.getHistorialSesiones()) {

        System.out.println(
            "ID: " + s.getId()
            + " | Fecha: " + s.getBloque().getFecha()
            + " | Entrenador: " + s.getEntrenador().getNombre()
            + " | Convocados: " + s.getConvocados().size()
        );
    }
}

private static void registrarFeedback() {

    if (sesion == null) {
        System.out.println("Primero debes programar una sesion.");
        return;
    }

    System.out.println("\n--- REGISTRAR ASISTENCIA ---");

    sesion.registrarFeedback(
        socio,
        EstadoAsistencia.ASISTIO,
        5,
        "Excelente entrenamiento"
    );

    sesion.registrarFeedback(
        socio2,
        EstadoAsistencia.TARDE,
        4,
        "Llego unos minutos tarde"
    );

    System.out.println("Asistencias registradas correctamente.");
}

private static void consultarAsistencia() {

    System.out.println("\n--- REPORTE DE ASISTENCIA ---");

    String reporte = servicio.reporteAsistencia(
        Disciplina.TENIS,
        LocalDate.now(),
        LocalDate.now().plusDays(7)
    );

    System.out.println(reporte);
}


}
