package club.modelo.conjunto;

import club.servicios.ReglaDeNegocioException;

public class Fase {

    private final int orden;
    private final String nombre;
    private final TipoFase tipo;
    private final int clasificados;

    public Fase(int orden, String nombre, TipoFase tipo, int clasificados) {
        validar(orden, nombre, tipo, clasificados);
        this.orden = orden;
        this.nombre = nombre.trim();
        this.tipo = tipo;
        this.clasificados = clasificados;
    }
    // Se valida que el orden deba ser positivo, el nombre obligatorio y compatible con los datos, tipo de fase obligatorio, clasificados no negativos y no aplicar clasificados en eliminación
    static void validar(int orden, String nombre, TipoFase tipo, int clasificados) {
        if (orden <= 0) {
            throw new ReglaDeNegocioException(
                "El orden de la fase debe ser mayor que cero"
            );
        }
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDeNegocioException(
                "El nombre de la fase es obligatorio"
            );
        }

        if (nombre.contains(";")) {
            throw new ReglaDeNegocioException(
                "El nombre de la fase no puede contener ';'"
            );
        }
        if (tipo == null) {
            throw new ReglaDeNegocioException(
                "El tipo de fase es obligatorio"
            );
        }
        if (clasificados < 0) {
            throw new ReglaDeNegocioException(
                "La cantidad de clasificados no puede ser negativa"
            );
        }
        if (tipo == TipoFase.ELIMINACION && clasificados != 0) {
            throw new ReglaDeNegocioException(
                "Una fase de eliminación debe tener cero clasificados"
            );
        }
    }
    public int getOrden() { return orden; }
    public String getNombre() { return nombre; }
    public TipoFase getTipo() { return tipo; }
    public int getClasificados() { return clasificados; }
    public boolean esLiga() { return tipo == TipoFase.LIGA; }
    public boolean esEliminacion() { return tipo == TipoFase.ELIMINACION; }
}