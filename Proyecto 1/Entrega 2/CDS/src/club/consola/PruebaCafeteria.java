
package club.consola;

import java.util.Date;
import java.time.LocalDate;

import club.servicios.ClubSeneca;
import club.modelo.usuarios.Socio;
import modelo.cafeteria.Bebida;
import modelo.ventas.VentaCafeteria;

public class PruebaCafeteria {

    public static void main(String[] args) {

        ClubSeneca club = new ClubSeneca();

        Socio socio = new Socio(
            "socio1", "Mariana", "1234",
            LocalDate.of(2004, 5, 10)
        );

        club.registrarUsuario(socio);

        Bebida cafe = new Bebida(
            "Cafe", 10000, 10, "Caliente"
        );

        club.getServicioComercio().registrarProductoCafeteria(cafe);

        VentaCafeteria venta = new VentaCafeteria(
            new Date(), 0, 0, 0, 0.08, 0.10, socio, null
        );

        venta.agregarProducto(cafe, 2);

        club.getServicioComercio().realizarCompraCafeteria(
            venta, socio, 0
        );

        System.out.println("=== PRUEBA CAFETERIA ===");

        System.out.println("Total: " + venta.getTotal());

        System.out.println("Inventario final: "
            + cafe.getCantidadDisponible());

        System.out.println("Puntos acumulados: "
            + socio.getPuntosFidelidad());

        System.out.println("Ventas registradas: "
            + club.getServicioComercio().getVentas().size());
    }
}
