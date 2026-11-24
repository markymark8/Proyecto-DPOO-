package club.consola;

import java.time.LocalDate;

import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Empleado;
import club.modelo.usuarios.Entrenador;
import club.modelo.usuarios.Fisioterapeuta;
import club.modelo.usuarios.Usuario;

public class PruebaUsuarios {

    public static void main(String[] args) {

        Socio socio = new Socio(
            "socio1", "Mariana", Usuario.hash("1234"),
            LocalDate.of(2004, 5, 10)
        );

        Empleado empleado = new Empleado(
            "empleado1", "Martin", Usuario.hash("abcd"),
            "DESC123"
        );

        Entrenador entrenador = new Entrenador(
            "entrenador1", "Maria", Usuario.hash("1234"),
            "ENT123", 101
        );

        Fisioterapeuta fisioterapeuta = new Fisioterapeuta(
            "fisio1", "Ana", Usuario.hash("1234"),
            "FIS123"
        );

        System.out.println("=== USUARIOS ===");
        System.out.println("Socio: " + socio.getNombre());
        System.out.println("Empleado: " + empleado.getNombre());
        System.out.println("Entrenador: " + entrenador.getNombre());
        System.out.println("Fisioterapeuta: " + fisioterapeuta.getNombre());

        System.out.println("\n=== CONTRASEÑA ===");
        System.out.println(socio.verificarContrasena("1234"));
        System.out.println(socio.verificarContrasena("incorrecta"));

        System.out.println("\n=== PUNTOS ===");
        socio.sumarPuntos(500);
        System.out.println("Después de sumar: " + socio.getPuntosFidelidad());

        socio.restarPuntos(200);
        System.out.println("Después de restar: " + socio.getPuntosFidelidad());

        System.out.println("\n=== VINCULACIÓN ===");
        empleado.vincularSocio(socio);
        System.out.println("Empleado vinculado: " +
            socio.getEmpleado().getNombre());
    }
}
