
package club.consola;

import java.util.Date;
import java.time.LocalDate;

import club.servicios.ClubSeneca;
import club.modelo.usuarios.Socio;
import modelo.cafeteria.Snack;
import modelo.ventas.VentaCafeteria;
import club.servicios.ReglaDeNegocioException;

public class PruebaAlergenos {

    public static void main(String[] args) {

        ClubSeneca club = new ClubSeneca();

        Socio socio = new Socio(
            "socio1", "Mariana", "1234",
            LocalDate.of(2004, 5, 10)
        );

        club.registrarUsuario(socio);

        Snack galleta = new Snack(
        	    "Galleta", 10000, 10, "Gluten"
        	);

        club.getServicioComercio().registrarProductoCafeteria(galleta);

        VentaCafeteria venta = new VentaCafeteria(
            new Date(), 0, 0, 0, 0.08, 0.10, socio, null
        );

        venta.agregarProducto(galleta, 2);

        try {
        	
        	System.out.println("Alergenos: " + venta.consultarAlergenos());
        	venta.confirmarInformacionAlergenos();
        	
            club.getServicioComercio().realizarCompraCafeteria(
                venta, socio, 0
            );

            System.out.println("Compra realizada correctamente");

        } catch (ReglaDeNegocioException e) {

            System.out.println("=== PRUEBA ALERGENOS ===");
            System.out.println("Compra rechazada: " + e.getMessage());
        }

        System.out.println("=== PRUEBA CAFETERIA ===");

        System.out.println("Total: " + venta.getTotal());

        System.out.println("Inventario final: "
        	    + galleta.getCantidadDisponible());

        	System.out.println("Ventas registradas: "
        	    + club.getServicioComercio().getVentas().size());
        	
    }
}
