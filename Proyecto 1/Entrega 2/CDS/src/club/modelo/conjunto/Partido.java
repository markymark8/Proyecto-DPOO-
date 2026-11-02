
package club.modelo.conjunto;

import java.time.LocalDate;
import club.servicios.ReglaDeNegocioException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import club.modelo.usuarios.Socio;

public abstract class Partido {

    private final String id;
    private final Deporte deporte;
    private final LocalDate fecha;
    private EstadoPartido estado;
    private int marcadorLocal;
    private int marcadorVisitante;
    private final List<EstadisticaJugador> estadisticas;

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
        this.estadisticas = new ArrayList<>();
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
        this.estadisticas = new ArrayList<>();
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
    public void agregarEstadistica(EstadisticaJugador estadistica) {

        if (estadistica == null) {
            throw new ReglaDeNegocioException(
                "La estadística no puede ser nula"
            );
        }

        if (estaJugado()) {
            throw new ReglaDeNegocioException(
                "No se pueden agregar estadísticas a un partido finalizado"
            );
        }

        if (!tipoEstadisticaValido(estadistica)) {
            throw new ReglaDeNegocioException(
                "El tipo de estadística no corresponde al deporte del partido"
            );
        }

        Socio jugador = estadistica.getJugador();

        if (!participaJugador(jugador)) {
            throw new ReglaDeNegocioException(
                "El jugador no pertenece a los equipos participantes"
            );
        }
        if (this instanceof PartidoInterno) {
            PartidoInterno partidoInterno = (PartidoInterno) this;

            if (partidoInterno.estaSuspendidoInternamente(jugador)
                    && !partidoInterno.fueSancionado(jugador.getLogin())) {

                throw new ReglaDeNegocioException(
                    "El jugador está cumpliendo una suspensión interna"
                );
            }
        }

        for (EstadisticaJugador existente : estadisticas) {
            if (existente.getJugador().getLogin()
                    .equals(jugador.getLogin())) {

                throw new ReglaDeNegocioException(
                    "El jugador ya tiene estadísticas registradas en este partido"
                );
            }
        }

        estadisticas.add(estadistica);
    }

    public List<EstadisticaJugador> getEstadisticas() {
        return Collections.unmodifiableList(estadisticas);
    }

    public EstadisticaJugador getEstadisticaJugador(Socio jugador) {

        if (jugador == null) {
            throw new ReglaDeNegocioException(
                "El jugador no puede ser nulo"
            );
        }

        for (EstadisticaJugador estadistica : estadisticas) {
            if (estadistica.getJugador().getLogin()
                    .equals(jugador.getLogin())) {

                return estadistica;
            }
        }

        return null;
    }

    private boolean tipoEstadisticaValido(EstadisticaJugador estadistica) {

        String nombreDeporte = deporte.getNombre();

        if (nombreDeporte.equalsIgnoreCase("FUTBOL")) {
            return estadistica instanceof EstadisticaFutbol;
        }

        if (nombreDeporte.equalsIgnoreCase("BASKET")) {
            return estadistica instanceof EstadisticaBasket;
        }

        if (nombreDeporte.equalsIgnoreCase("VOLEIBOL")) {
            return estadistica instanceof EstadisticaVoleibol;
        }

        return false;
    }
    protected abstract boolean participaJugador(Socio socio);  
}
