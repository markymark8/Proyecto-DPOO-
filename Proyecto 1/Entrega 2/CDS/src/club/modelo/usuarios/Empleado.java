package club.modelo.usuarios;

/**
 * Empleado del club (entrenadores y fisioterapeutas). Puede comprar y acumular puntos.
 * Es dueno del codigo de descuento: 15% para el, 8% para los socios que lo reciben.
 * Puede tener un Socio asociado (opcional).
 */
public class Empleado extends Usuario implements Comprador {

    private String codigoDescuento;
    private int puntosFidelidad;
    private Socio socio; // puede ser null

    public Empleado(String login, String nombre, String contrasenaHash, String codigoDescuento) {
        super(login, nombre, contrasenaHash);
        this.codigoDescuento = codigoDescuento;
        this.puntosFidelidad = 0;
        this.socio = null;
    }

    public String getCodigoDescuento() {
        return codigoDescuento;
    }

    public void setCodigoDescuento(String codigoDescuento) {
        this.codigoDescuento = codigoDescuento;
    }

    @Override
    public int getPuntosFidelidad() {
        return puntosFidelidad;
    }

    public void setPuntosFidelidad(int puntosFidelidad) {
        this.puntosFidelidad = puntosFidelidad;
    }

    public Socio getSocio() {
        return socio;
    }

    /** TODO (a mano): asignar this.socio y socio.setEmpleado(this) (vinculo en ambos lados). */
    public void vincularSocio(Socio socio) {
        throw new UnsupportedOperationException("TODO: vincularSocio");
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
