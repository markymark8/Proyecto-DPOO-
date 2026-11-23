package club.consola;

import club.modelo.usuarios.Empleado;

public class MainEmpleado {

    public static void main(String[] args) {
        ArranqueClub.ejecutar("empleados", u -> u instanceof Empleado);
    }
}
