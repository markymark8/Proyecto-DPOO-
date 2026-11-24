
package club.consola;

import java.util.Date;
import java.util.ArrayList;

import club.servicios.ClubSeneca;
import club.modelo.usuarios.Empleado;
import modelo.tienda.Articulo;
import modelo.ventas.VentaTienda;
import club.modelo.usuarios.Socio;
import java.time.LocalDate;

public class PruebaDescuentoSocio {

    public static void main(String[] args) {

        ClubSeneca club = new ClubSeneca();

        Empleado carlos = new Empleado(
            "empleado1", "Carlos", "1234", "DESC1"
        );

        club.registrarUsuario(carlos);
        
        Socio mariana = new Socio(
        	    "socio1", "Mariana", "1234",
        	    LocalDate.of(2007, 6, 8)
        	);

        	club.registrarUsuario(mariana);

        Articulo raqueta = new Articulo(
            "Raqueta", "Deportes", 100000, 10
        );

        club.getServicioComercio().registrarArticulo(raqueta);

        VentaTienda venta = new VentaTienda(
        	    new Date(), 0, 0, 0, 0.19, mariana, null
        	);

        venta.agregarArticulo(raqueta, 1);

        ArrayList<Empleado> empleados = new ArrayList<>();
        empleados.add(carlos);

        club.getServicioComercio().realizarCompraTienda(
        	    venta,
        	    mariana,
        	    "DESC1",
        	    empleados,
        	    0
        	);

        System.out.println("=== PRUEBA DESCUENTO SOCIO 8% ===");

        System.out.println("Total final: " + venta.getTotal());

        System.out.println("Inventario final: "
            + raqueta.getCantidadDisponible());

        System.out.println("Puntos acumulados: "
        	+ mariana.getPuntosFidelidad());

        System.out.println("Ventas registradas: "
            + club.getServicioComercio().getVentas().size());
    }
}
