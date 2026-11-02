
package club.modelo.conjunto;

import java.time.LocalDate;

import club.servicios.ReglaDeNegocioException;

public abstract class Partido {

    private final String id;
    private final Deporte deporte;
    private final LocalDate fecha;
    private EstadoPartido estado;
    private int marcadorLocal;
    private int marcadorVisitante;

    protected Partido(String id, Deporte deporte, LocalDate fecha) {

        if (id == null || deporte == null || fecha == null) {
            throw new ReglaDeNegocioException(
                "Datos incompletos para crear el partido"
            );
        }

        this.id = id;
        this.deporte = deporte;
        this.fecha = fecha;
        this.estado = EstadoPartido.PROGRAMADO;
        this.marcadorLocal = 0;
        this.marcadorVisitante = 0;
    }

    protected Partido(String id, Deporte deporte, LocalDate fecha,
                      EstadoPartido estado, int marcadorLocal,
                      int marcadorVisitante) {

        if (id == null || deporte == null || fecha == null || estado == null) {
            throw new ReglaDeNegocioException(
                "Datos incompletos para restaurar el partido"
            );
        }

        if (marcadorLocal < 0 || marcadorVisitante < 0) {
            throw new ReglaDeNegocioException(
                "Los marcadores no pueden ser negativos"
            );
        }

        this.id = id;
        this.deporte = deporte;
        this.fecha = fecha;
        this.estado = estado;
        this.marcadorLocal = marcadorLocal;
        this.marcadorVisitante = marcadorVisitante;
    }

    public String getId() {
        return id;
    }

    public Deporte getDeporte() {
        return deporte;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public EstadoPartido getEstado() {
        return estado;
    }

    public int getMarcadorLocal() {
        return marcadorLocal;
    }

    public int getMarcadorVisitante() {
        return marcadorVisitante;
    }

    public boolean estaJugado() {
        return estado == EstadoPartido.JUGADO;
    }

    public void cerrar(int marcadorLocal, int marcadorVisitante) {

        if (estado == EstadoPartido.JUGADO) {
            throw new ReglaDeNegocioException(
                "El partido ya fue jugado"
            );
        }

        if (!deporte.validaMarcador(marcadorLocal, marcadorVisitante)) {
            throw new ReglaDeNegocioException(
                "El marcador no es válido para este deporte"
            );
        }

        this.marcadorLocal = marcadorLocal;
        this.marcadorVisitante = marcadorVisitante;
        this.estado = EstadoPartido.JUGADO;
    }
}
