
package club.consola;

import java.util.Date;

import club.modelo.usuarios.Administrador;
import club.modelo.usuarios.Turno;
import club.modelo.usuarios.cambioTurno;
import club.servicios.ReglaDeNegocioException;

public class PruebaAdministrador {

    public static void main(String[] args) {

        Administrador admin = new Administrador(
            "admin1", "John", "1234"
        );

        Turno turno = new Turno(
            new Date(), "08:00", "12:00"
        );

        cambioTurno cambio = new cambioTurno(
            new Date(),
            turno,
            admin,
            new Date(),
            "13:00",
            "17:00"
        );

        admin.agregarCambioTurno(cambio);

        System.out.println("=== ADMINISTRACION ===");
        System.out.println("Estado inicial: " + cambio.getEstado());

        admin.rechazarCambioTurno(cambio);

        System.out.println("Estado final: " + cambio.getEstado());

        System.out.println("Solicitudes registradas: "
            + admin.getCambiosTurno().size());

        try {
            admin.rechazarCambioTurno(cambio);
            System.out.println("ERROR: Permitio rechazar dos veces");
        } catch (ReglaDeNegocioException e) {
            System.out.println("Correcto: No permite rechazar dos veces");
        }
    }
}

