 package club.consola;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Scanner;

import club.modelo.usuarios.Administrador;
import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Usuario;
import club.servicios.ClubSeneca;
import club.servicios.ReglaDeNegocioException;

public class MainConjunto {

    private static final LocalDate[] NACIMIENTOS = {
        LocalDate.of(2000, 3, 15), LocalDate.of(1999, 7, 22), LocalDate.of(2001, 11, 2),
        LocalDate.of(1998, 1, 30), LocalDate.of(2002, 5, 9), LocalDate.of(2003, 9, 18),
        LocalDate.of(2010, 4, 12), LocalDate.of(2010, 8, 25), LocalDate.of(1995, 12, 5),
        LocalDate.of(1997, 6, 14)
    };

    public static void main(String[] args) {
        Path datos = Path.of("datos");
        ClubSeneca club = new ClubSeneca();

        // TEMPORAL: reemplazar por la carga de usuarios.csv cuando exista (Mariana)
        club.registrarUsuario(new Administrador("admin", "Administrador", Usuario.hash("admin123")));
        for (int i = 0; i < 22; i++) {
            String login = String.format("jug%02d", i + 1);
            club.registrarUsuario(new Socio(login, "Jugador " + login, Usuario.hash("1234"),
                    NACIMIENTOS[i % NACIMIENTOS.length]));
        }

        try {
            club.cargar(datos);
        } catch (ReglaDeNegocioException e) {
            System.out.println("No se pudieron cargar los datos: " + e.getMessage());
            return;
        }

        Scanner in = new Scanner(System.in);
        Usuario usuario = null;
        for (int intento = 1; intento <= 3 && usuario == null; intento++) {
            System.out.print("Login: ");
            String login = in.nextLine().trim();
            System.out.print("Contrasena: ");
            String clave = in.nextLine();
            Usuario candidato = club.buscarUsuario(login);
            if (candidato != null && candidato.verificarContrasena(clave)) {
                usuario = candidato;
            } else {
                System.out.println("Login o contrasena incorrectos.");
            }
        }
        if (usuario == null) {
            System.out.println("Demasiados intentos. Adios.");
            return;
        }

        new ConsolaConjunto(club, in).ejecutar(usuario);

        System.out.print("Guardar los cambios en datos/? (s/n): ");
        if (in.nextLine().trim().equalsIgnoreCase("s")) {
            club.guardar(datos);
            System.out.println("Datos guardados.");
        }
    }
}
