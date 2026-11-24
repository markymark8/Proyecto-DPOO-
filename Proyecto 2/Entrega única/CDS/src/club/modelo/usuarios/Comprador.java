package club.modelo.usuarios;

/**
 * Lo que una venta necesita de quien compra (Socio o Empleado).
 * 1 punto de fidelidad = 1 peso.
 */
public interface Comprador {

    String getNombre();

    int getPuntosFidelidad();

    void sumarPuntos(int puntos);

    void restarPuntos(int puntos);
}
