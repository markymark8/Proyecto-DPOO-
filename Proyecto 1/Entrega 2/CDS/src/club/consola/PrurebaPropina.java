package club.consola;

import java.util.Date;
import java.time.LocalDate;

import club.modelo.usuarios.Socio;
import club.servicios.ClubSeneca;

import modelo.cafeteria.Bebida;
import modelo.ventas.VentaCafeteria;

public class PrurebaPropina {

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

        club.getServicioComercio()
            .registrarProductoCafeteria(cafe);

        VentaCafeteria venta = new VentaCafeteria(
            new Date(), 0, 0, 0, 0.08, 0.10, socio, null
        );

        venta.agregarProducto(cafe, 2);

        System.out.println("=== PROPINA SUGERIDA 10% ===");
        System.out.println("Subtotal: " + venta.calcularSubtotal());
        System.out.println("Impuesto: " + venta.calcularImpuestoConsumo());
        System.out.println("Propina: " + venta.calcularPropina());
        System.out.println("Total: " + venta.calcularTotal());

        venta.setPropina(0.05);

        System.out.println("=== PROPINA MODIFICADA 5% ===");
        System.out.println("Propina: " + venta.calcularPropina());
        System.out.println("Total: " + venta.calcularTotal());

        venta.setPropina(0);

        System.out.println("=== SIN PROPINA ===");
        System.out.println("Propina: " + venta.calcularPropina());
        System.out.println("Total: " + venta.calcularTotal());
    }
}
