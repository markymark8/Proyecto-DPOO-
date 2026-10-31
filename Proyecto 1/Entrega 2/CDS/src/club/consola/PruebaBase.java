package club.consola;

import java.time.LocalDate;

import club.modelo.usuarios.Administrador;
import club.modelo.usuarios.Entrenador;
import club.modelo.usuarios.Fisioterapeuta;
import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Usuario;
import club.servicios.ClubSeneca;
import club.servicios.ReglaDeNegocioException;

public class PruebaBase {

    private static int fallas = 0;

    private static void verificar(String descripcion, boolean condicion) {
        System.out.println((condicion ? "OK     " : "FALLA  ") + descripcion);
        if (!condicion) {
            fallas++;
        }
    }

    /** Ejecuta algo que debe lanzar ReglaDeNegocioException. */
    private static void debeRechazar(String descripcion, Runnable accion) {
        try {
            accion.run();
            verificar(descripcion + " (debia rechazar y no lo hizo)", false);
        } catch (ReglaDeNegocioException e) {
            verificar(descripcion + " -> " + e.getMessage(), true);
        }
    }

    public static void main(String[] args) {
        ClubSeneca club = new ClubSeneca();
        LocalDate hoy = LocalDate.of(2026, 10, 31);

        Socio ana = new Socio("ana", "Ana Perez", Usuario.hash("1234"), LocalDate.of(2000, 5, 1));
        Socio luis = new Socio("luis", "Luis Gomez", Usuario.hash("abcd"), LocalDate.of(2010, 12, 25));
        Entrenador carlos = new Entrenador("carlos", "Carlos Ruiz", Usuario.hash("clave"), "COD-CARLOS", 101);
        Fisioterapeuta sofia = new Fisioterapeuta("sofia", "Sofia Mora", Usuario.hash("fisio"), "COD-SOFIA");
        Administrador admin = new Administrador("admin", "Admin Club", Usuario.hash("admin"));

        System.out.println("=== Usuario: hash y contrasena ===");
        verificar("Mismo texto da el mismo hash", Usuario.hash("1234").equals(Usuario.hash("1234")));
        verificar("Textos distintos dan hash distinto", !Usuario.hash("1234").equals(Usuario.hash("4321")));
        verificar("El hash no es la contrasena en claro", !ana.getContrasenaHash().equals("1234"));
        verificar("Contrasena correcta", ana.verificarContrasena("1234"));
        verificar("Contrasena incorrecta", !ana.verificarContrasena("abcd"));
        verificar("Contrasena vacia", !ana.verificarContrasena(""));
        verificar("Mayusculas distintas", !carlos.verificarContrasena("CLAVE"));
        verificar("Login de otro usuario no sirve", !luis.verificarContrasena("1234"));

        System.out.println("=== Roles ===");
        verificar("Administrador.esAdministrador", admin.esAdministrador());
        verificar("Socio no es administrador", !ana.esAdministrador());
        verificar("Entrenador no es administrador", !carlos.esAdministrador());

        System.out.println("=== Socio: edad ===");
        verificar("Edad el 2026-10-31 (nacio 2000-05-01) = 26", ana.getEdad(hoy) == 26);
        verificar("Edad un dia antes del cumpleanos = 25", ana.getEdad(LocalDate.of(2026, 4, 30)) == 25);
        verificar("Edad el dia del cumpleanos = 26", ana.getEdad(LocalDate.of(2026, 5, 1)) == 26);
        verificar("Edad de Luis (nacio 2010-12-25) = 15", luis.getEdad(hoy) == 15);
        debeRechazar("Edad con fecha anterior al nacimiento", () -> ana.getEdad(LocalDate.of(1990, 1, 1)));

        System.out.println("=== Socio: puntos ===");
        verificar("Saldo inicial 0", ana.getPuntosFidelidad() == 0);
        ana.sumarPuntos(100);
        verificar("Sumar 100 -> 100", ana.getPuntosFidelidad() == 100);
        ana.restarPuntos(30);
        verificar("Restar 30 -> 70", ana.getPuntosFidelidad() == 70);
        debeRechazar("Restar 500 (mayor al saldo)", () -> ana.restarPuntos(500));
        verificar("Saldo intacto tras rechazo = 70", ana.getPuntosFidelidad() == 70);
        debeRechazar("Sumar -5", () -> ana.sumarPuntos(-5));
        debeRechazar("Restar -5", () -> ana.restarPuntos(-5));
        verificar("Saldo intacto tras rechazos = 70", ana.getPuntosFidelidad() == 70);
        ana.restarPuntos(70);
        verificar("Restar todo el saldo -> 0", ana.getPuntosFidelidad() == 0);

        System.out.println("=== Socio: bebida caliente y estado ===");
        verificar("Sin bebida caliente al inicio", !ana.tieneBebidaCaliente());
        ana.marcarBebidaCaliente(true);
        verificar("Marcar bebida caliente", ana.tieneBebidaCaliente());
        ana.marcarBebidaCaliente(false);
        verificar("Desmarcar bebida caliente", !ana.tieneBebidaCaliente());
        verificar("Socio nuevo esta activo", ana.isActivo());

        System.out.println("=== Empleado: puntos ===");
        carlos.sumarPuntos(200);
        verificar("Empleado suma 200", carlos.getPuntosFidelidad() == 200);
        carlos.restarPuntos(50);
        verificar("Empleado resta 50 -> 150", carlos.getPuntosFidelidad() == 150);
        debeRechazar("Empleado resta 999", () -> carlos.restarPuntos(999));
        debeRechazar("Empleado suma -1", () -> carlos.sumarPuntos(-1));
        verificar("Saldo del empleado intacto = 150", carlos.getPuntosFidelidad() == 150);

        System.out.println("=== Vinculo Empleado - Socio ===");
        verificar("Socio sin empleado: esEntrenador false", !ana.esEntrenador());
        verificar("Socio sin empleado: getEmpleado null", ana.getEmpleado() == null);
        carlos.vincularSocio(ana);
        verificar("Vinculo en ambos lados (empleado->socio)", carlos.getSocio() == ana);
        verificar("Vinculo en ambos lados (socio->empleado)", ana.getEmpleado() == carlos);
        verificar("Socio vinculado a Entrenador: esEntrenador true", ana.esEntrenador());
        sofia.vincularSocio(luis);
        verificar("Socio vinculado a Fisioterapeuta: esEntrenador false", !luis.esEntrenador());
        debeRechazar("Vincular null", () -> carlos.vincularSocio(null));
        debeRechazar("Vincular un socio que ya tiene otro empleado", () -> sofia.vincularSocio(ana));

        System.out.println("=== ClubSeneca: registro y busqueda ===");
        club.registrarUsuario(ana);
        club.registrarUsuario(carlos);
        club.registrarUsuario(admin);
        verificar("Buscar ana", club.buscarUsuario("ana") == ana);
        verificar("Buscar carlos", club.buscarUsuario("carlos") == carlos);
        verificar("Buscar admin", club.buscarUsuario("admin") == admin);
        verificar("Usuario inexistente devuelve null", club.buscarUsuario("nadie") == null);
        verificar("Login recuperado coincide", club.buscarUsuario("ana").getLogin().equals("ana"));

        System.out.println();
        System.out.println(fallas == 0 ? "TODO OK" : "HAY " + fallas + " FALLA(S): revisa las lineas marcadas");
        System.out.println();
        System.out.println("Hash de 1234 (para usuarios.csv): " + Usuario.hash("1234"));
    }
}