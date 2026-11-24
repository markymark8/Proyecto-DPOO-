package club.modelo.usuarios;

/** Empleado que cuenta para la regla de cobertura de turnos (minimo 1 en todo momento). */
public class Fisioterapeuta extends Empleado {

    public Fisioterapeuta(String login, String nombre, String contrasenaHash, String codigoDescuento) {
        super(login, nombre, contrasenaHash, codigoDescuento);
    }
    
    @Override
    public String getTipoEmpleado() {
        return "Fisioterapeuta";
    }
}
