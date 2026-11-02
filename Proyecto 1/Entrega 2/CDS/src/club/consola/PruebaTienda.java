
package club.consola;

import java.util.Date;
import java.time.LocalDate;
import java.util.ArrayList;

import club.servicios.ClubSeneca;
import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Empleado;
import modelo.tienda.Articulo;
import modelo.ventas.VentaTienda;

public class PruebaTienda {

    public static void main(String[] args) {

        ClubSeneca club = new ClubSeneca();

        Socio socio = new Socio(
            "socio1", "Mariana", "1234",
            LocalDate.of(2004, 5, 10)
        );

        club.registrarUsuario(socio);

        Articulo raqueta = new Articulo(
            "Raqueta", "Deportes", 100000, 10
        );

        club.getServicioComercio().registrarArticulo(raqueta);

        VentaTienda venta = new VentaTienda(
            new Date(), 0, 0, 0, 0.19, socio, null
        );

        venta.agregarArticulo(raqueta, 2);

        club.getServicioComercio().realizarCompraTienda(
            venta,
            socio,
            null,
            new ArrayList<Empleado>(),
            0
        );

        System.out.println("=== PRUEBA TIENDA ===");
        System.out.println("Total: " + venta.getTotal());
        System.out.println("Inventario final: "
            + raqueta.getCantidadDisponible());
        System.out.println("Puntos acumulados: "
            + socio.getPuntosFidelidad());
        System.out.println("Ventas registradas: "
            + club.getServicioComercio().getVentas().size());
    }
}
