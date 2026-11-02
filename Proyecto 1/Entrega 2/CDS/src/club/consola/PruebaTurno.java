
package club.consola;

import java.util.ArrayList;
import java.util.Date;

import club.modelo.usuarios.Administrador;
import club.modelo.usuarios.Empleado;
import club.modelo.usuarios.Entrenador;
import club.modelo.usuarios.Fisioterapeuta;
import club.modelo.usuarios.Turno;
import club.modelo.usuarios.cambioTurno;
import club.servicios.ReglaDeNegocioException;

public class PruebaTurno {

    public static void main(String[] args) {

        Date fecha = new Date();

        Administrador admin = new Administrador(
            "admin1", "Carlos", "1234"
        );

        Entrenador pedro = new Entrenador(
            "e1", "Pedro", "1234", "DESC1", 101
        );

        Entrenador juan = new Entrenador(
            "e2", "Juan", "1234", "DESC2", 102
        );

        Entrenador luis = new Entrenador(
            "e3", "Luis", "1234", "DESC3", 103
        );

        Fisioterapeuta ana = new Fisioterapeuta(
            "f1", "Ana", "1234", "DESC4"
        );

        Turno turnoPedro = new Turno(fecha, "08:00", "12:00");
        Turno turnoJuan = new Turno(fecha, "08:00", "12:00");
        Turno turnoLuis = new Turno(fecha, "08:00", "12:00");
        Turno turnoAna = new Turno(fecha, "08:00", "12:00");

        pedro.agregarTurno(turnoPedro);
        juan.agregarTurno(turnoJuan);
        luis.agregarTurno(turnoLuis);
        ana.agregarTurno(turnoAna);

        ArrayList<Empleado> empleados = new ArrayList<>();
        empleados.add(pedro);
        empleados.add(juan);
        empleados.add(luis);
        empleados.add(ana);

        cambioTurno cambio = new cambioTurno(
            fecha, turnoPedro, admin,
            fecha, "13:00", "17:00"
        );

        admin.agregarCambioTurno(cambio);

        System.out.println("=== PRUEBA APROBACION ===");
        System.out.println("Estado inicial: " + cambio.getEstado());

        try {
            admin.aprobarCambioTurno(cambio, empleados);

            System.out.println("Estado final: " + cambio.getEstado());
            System.out.println("Nuevo inicio: " + turnoPedro.getHoraInicio());
            System.out.println("Nuevo final: " + turnoPedro.getHoraFinal());

        } catch (ReglaDeNegocioException e) {
            System.out.println("Cambio rechazado: " + e.getMessage());
        }
    }
}

