package club.consola;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Scanner;
import java.util.function.Predicate;

import club.modelo.usuarios.Administrador;
import club.modelo.usuarios.Comprador;
import club.modelo.usuarios.Empleado;
import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Usuario;
import club.servicios.ClubSeneca;
import club.servicios.ReglaDeNegocioException;


//Arranque comun de los tres mains (administrador, jugador, empleado): carga los datos, exige un rol, 
//muestra el menu de modulos,
//y guarda automaticamente al salir.
 
public class ArranqueClub {

    private static final Path DATOS = Path.of("datos");

    private ArranqueClub() {
    }

    public static void ejecutar(String nombreRol, Predicate<Usuario> aceptaRol) {
        ClubSeneca club = new ClubSeneca();

        registrarUsuarios(club);

        try {
            club.cargar(DATOS);
        } catch (ReglaDeNegocioException e) {
            System.out.println("No se pudieron cargar los datos: " + e.getMessage());
            return;
        }

        Scanner in = new Scanner(System.in);
        Usuario usuario = autenticar(club, in, nombreRol, aceptaRol);
        if (usuario == null) {
            System.out.println("Demasiados intentos. Adios.");
            return;
        }

        try {
            menu(club, in, usuario);
        } finally {
            club.guardar(DATOS);
            System.out.println("Datos guardados en " + DATOS + "/.");
        }
    }

    /**
     * TEMPORAL: reemplazar por la carga de usuarios.csv cuando exista
     * (Mariana). Debe ejecutarse ANTES de club.cargar().
     */
    private static void registrarUsuarios(ClubSeneca club) {
        club.registrarUsuario(new Administrador("admin", "Administrador", Usuario.hash("admin123")));
        for (int i = 1; i <= 22; i++) {
            String login = String.format("jug%02d", i);
            club.registrarUsuario(new Socio(login, "Jugador " + login,
                    Usuario.hash("1234"), LocalDate.of(2000, 1, 1)));
        }
        club.registrarUsuario(new Empleado("emp1", "Empleado Uno", Usuario.hash("1234"), "COD1"));
        club.registrarUsuario(new Empleado("emp2", "Empleado Dos", Usuario.hash("1234"), "COD2"));
    }

    private static Usuario autenticar(ClubSeneca club, Scanner in,
                                      String nombreRol, Predicate<Usuario> aceptaRol) {
        for (int intento = 1; intento <= 3; intento++) {
            System.out.print("Login: ");
            String login = in.nextLine().trim();
            System.out.print("Contrasena: ");
            String clave = in.nextLine();

            Usuario candidato = club.buscarUsuario(login);
            if (candidato == null || !candidato.verificarContrasena(clave)) {
                System.out.println("Login o contrasena incorrectos.");
            } else if (!aceptaRol.test(candidato)) {
                System.out.println("Este acceso es solo para " + nombreRol + ".");
            } else {
                return candidato;
            }
        }
        return null;
    }

    private static void menu(ClubSeneca club, Scanner in, Usuario usuario) {
        boolean ofreceConjunto = usuario.esAdministrador() || usuario instanceof Socio;
        boolean ofreceComercio = usuario instanceof Comprador;

        boolean salir = false;
        while (!salir) {
            System.out.println();
            System.out.println("=== CLUB SENECA (" + usuario.getNombre() + ") ===");
            if (ofreceConjunto) {
                System.out.println("1. Deportes de conjunto y competencias");
            }
            System.out.println("2. Torneos");
            System.out.println("3. Deportes individuales");
            if (ofreceComercio) {
                System.out.println("4. Tienda y cafeteria");
            }
            System.out.println("0. Salir");
            System.out.print("Opcion: ");

            String opcion = in.nextLine().trim();
            if (opcion.equals("1") && ofreceConjunto) {
                new ConsolaConjunto(club, in).ejecutar(usuario);
            } else if (opcion.equals("2")) {
                new ConsolaTorneos(club, in).ejecutar(usuario);
            } else if (opcion.equals("3")) {
                new ConsolaIndividual(club, in).ejecutar(usuario);
            } else if (opcion.equals("4") && ofreceComercio) {
                new ConsolaComercio(club, (Comprador) usuario, in).mostrarMenu();
            } else if (opcion.equals("0")) {
                salir = true;
            } else {
                System.out.println("Opcion invalida.");
            }
        }
    }
}