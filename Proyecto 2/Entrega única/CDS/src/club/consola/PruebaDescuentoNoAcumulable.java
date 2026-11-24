
package club.consola;

import java.util.Date;
import java.util.ArrayList;

import club.servicios.ClubSeneca;
import club.modelo.usuarios.Empleado;
import modelo.tienda.Articulo;
import modelo.ventas.VentaTienda;
import club.servicios.ReglaDeNegocioException;

public class PruebaDescuentoNoAcumulable {

    public static void main(String[] args) {

        ClubSeneca club = new ClubSeneca();

        Empleado carlos = new Empleado(
            "empleado1", "Carlos", "1234", "DESC1"
        );

        club.registrarUsuario(carlos);

        Articulo raqueta = new Articulo(
            "Raqueta", "Deportes", 100000, 10
        );

        club.getServicioComercio().registrarArticulo(raqueta);

        VentaTienda venta = new VentaTienda(
            new Date(), 0, 0, 0, 0.19, null, carlos
        );

        venta.agregarArticulo(raqueta, 1);

        ArrayList<Empleado> empleados = new ArrayList<>();
        empleados.add(carlos);

        try {
            club.getServicioComercio().realizarCompraTienda(
                venta,
                carlos,
                "DESC1",
                empleados,
                0
            );

            System.out.println("Compra aceptada");
            System.out.println("Total: " + venta.getTotal());

        } catch (ReglaDeNegocioException e) {
            System.out.println("Compra rechazada: " + e.getMessage());
        }

        System.out.println("=== PRUEBA DESCUENTO EMPLEADO ===");

        System.out.println("Total final: " + venta.getTotal());

        System.out.println("Inventario final: "
            + raqueta.getCantidadDisponible());

        System.out.println("Puntos acumulados: "
            + carlos.getPuntosFidelidad());

        System.out.println("Ventas registradas: "
            + club.getServicioComercio().getVentas().size());
    }
}
