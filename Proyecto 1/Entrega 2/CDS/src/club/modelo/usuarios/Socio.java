package club.modelo.usuarios;

import java.time.LocalDate;

/**
 * Socio del club. Es tambien el "jugador": no existe una clase Jugador.
 * Puede estar asociado (opcionalmente) a un Empleado.
 */
public class Socio extends Usuario implements Comprador {

    private LocalDate fechaNacimiento;
    private int puntosFidelidad;
    private boolean bebidaCaliente;
    private boolean activo;
    private Empleado empleado; // puede ser null

    public Socio(String login, String nombre, String contrasenaHash, LocalDate fechaNacimiento) {
        super(login, nombre, contrasenaHash);
        this.fechaNacimiento = fechaNacimiento;
        this.puntosFidelidad = 0;
        this.bebidaCaliente = false;
        this.activo = true;
        this.empleado = null;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    @Override
    public int getPuntosFidelidad() {
        return puntosFidelidad;
    }

    public void setPuntosFidelidad(int puntosFidelidad) {
        this.puntosFidelidad = puntosFidelidad;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public boolean tieneBebidaCaliente() {
        return bebidaCaliente;
    }

    public void marcarBebidaCaliente(boolean bebidaCaliente) {
        this.bebidaCaliente = bebidaCaliente;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    /** Solo asigna la referencia; para vincular en ambos lados use Empleado.vincularSocio. */
    void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    /** TODO (a mano): edad en anios cumplidos a la fecha recibida (java.time.Period). */
    public int getEdad(LocalDate hoy) {
        throw new UnsupportedOperationException("TODO: getEdad");
    }

    /** TODO (a mano): true si empleado != null y ese empleado es un Entrenador (instanceof). */
    public boolean esEntrenador() {
        throw new UnsupportedOperationException("TODO: esEntrenador");
    }

    /** TODO (a mano): sumar puntos (rechazar valores negativos con ReglaDeNegocioException). */
    @Override
    public void sumarPuntos(int puntos) {
        throw new UnsupportedOperationException("TODO: sumarPuntos");
    }

    /** TODO (a mano): restar puntos (rechazar si supera el saldo con ReglaDeNegocioException). */
    @Override
    public void restarPuntos(int puntos) {
        throw new UnsupportedOperationException("TODO: restarPuntos");
    }
}
