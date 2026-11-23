package club.consola;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Scanner;

import club.modelo.usuarios.Administrador;
import club.modelo.usuarios.Empleado;
import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Usuario;
import club.servicios.ClubSeneca;

public class MainPruebaTorneos {

    public static void main(String[] args) {
        ClubSeneca club = new ClubSeneca();

        club.registrarUsuario(new Administrador("admin", "Administrador", Usuario.hash("1234")));
        for (int i = 1; i <= 22; i++) {
            String login = String.format("jug%02d", i);
            club.registrarUsuario(new Socio(login, "Jugador " + login,
                    Usuario.hash("1234"), LocalDate.of(2000, 1, 1)));
        }
        club.registrarUsuario(new Empleado("emp1", "Empleado libre", Usuario.hash("1234"), "COD1"));

        club.cargar(Path.of("datos"));

        Scanner in = new Scanner(System.in);
        ConsolaTorneos consola = new ConsolaTorneos(club, in);

        while (true) {
            System.out.print("\nLogin (admin, jug01..jug22, emp1) o 'fin': ");
            String login = in.nextLine().trim();
            if (login.equals("fin")) {
                break;
            }
            Usuario u = club.buscarUsuario(login);
            if (u == null) {
                System.out.println("No existe ese usuario.");
                continue;
            }
            consola.ejecutar(u);
        }
        // Sin club.guardar(...) a propósito: es solo una prueba.
    }
}
