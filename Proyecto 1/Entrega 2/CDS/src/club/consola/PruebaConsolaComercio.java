
package club.consola;

import java.time.LocalDate;
import java.util.Scanner;

import club.servicios.ClubSeneca;
import club.modelo.usuarios.Socio;
import modelo.tienda.Articulo;
import modelo.cafeteria.Bebida;
import modelo.cafeteria.Snack;

public class PruebaConsolaComercio {

    public static void main(String[] args) {

        ClubSeneca club = new ClubSeneca();

        Socio socio = new Socio(
            "socio1",
            "Mariana",
            "1234",
            LocalDate.of(2004, 5, 10)
        );

        club.registrarUsuario(socio);

        Articulo raqueta = new Articulo(
            "Raqueta", "Deportes", 100000, 10
        );

        Articulo camiseta = new Articulo(
            "Camiseta", "Ropa", 50000, 20
        );

        club.getServicioComercio().registrarArticulo(raqueta);
        club.getServicioComercio().registrarArticulo(camiseta);
        

        Bebida cafe = new Bebida(
        		"Cafe", 5000, 20, "Caliente"
        		);

        Snack galletas = new Snack(
        		"Galletas", 8000, 15, "Gluten y leche"
        		);

        club.getServicioComercio().registrarProductoCafeteria(cafe);
        club.getServicioComercio().registrarProductoCafeteria(galletas);


        Scanner scanner = new Scanner(System.in);

        ConsolaComercio consola = new ConsolaComercio(
            club, socio, scanner
        );

        consola.mostrarMenu();

        scanner.close();
    }
}
