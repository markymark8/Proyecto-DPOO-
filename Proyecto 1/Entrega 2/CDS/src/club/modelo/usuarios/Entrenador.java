package club.modelo.usuarios;


public class Entrenador extends Empleado {

    private int codigo;

    public Entrenador(String login, String nombre, String contrasenaHash,
            String codigoDescuento, int codigo) {
        super(login, nombre, contrasenaHash, codigoDescuento);
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }
}
