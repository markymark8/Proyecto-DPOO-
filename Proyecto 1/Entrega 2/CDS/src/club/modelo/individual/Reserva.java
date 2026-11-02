package club.modelo.individual;

import club.servicios.ReglaDeNegocioException;

import java.time.LocalDateTime;

import club.modelo.usuarios.Usuario;

/** Ocupa una instalacion en un bloque. Es la superclase de PartidoPractica y SesionEntrenamiento. */
public class Reserva {

    private int id;
    private Instalacion instalacion;
    private BloqueHorario bloque;
    private Usuario responsable;
    private int numeroJugadores;
    private Modalidad modalidad; // puede ser null en sesiones
    private EstadoReserva estado;
    private boolean ingresoRegistrado;

    public Reserva(int id, Instalacion instalacion, BloqueHorario bloque, Usuario responsable,
            int numeroJugadores, Modalidad modalidad) {
        this.id = id;
        this.instalacion = instalacion;
        this.bloque = bloque;
        this.responsable = responsable;
        this.numeroJugadores = numeroJugadores;
        this.modalidad = modalidad;
        this.estado = EstadoReserva.ACTIVA;
        this.ingresoRegistrado = false;
    }

    public int getId() {
        return id;
    }

    public Instalacion getInstalacion() {
        return instalacion;
    }

    public BloqueHorario getBloque() {
        return bloque;
    }

    public Usuario getResponsable() {
        return responsable;
    }

    public int getNumeroJugadores() {
        return numeroJugadores;
    }

    public Modalidad getModalidad() {
        return modalidad;
    }

    public EstadoReserva getEstado() {
        return estado;
    }


    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public boolean isIngresoRegistrado() {
        return ingresoRegistrado;
    }

    public void setIngresoRegistrado(boolean ingresoRegistrado) {
        this.ingresoRegistrado = ingresoRegistrado;
    }

    public boolean estaActiva() {
        return estado == EstadoReserva.ACTIVA;
    }

    /* MÉTODOS IMPLMENTADOS: para el metodo cancelar incluimos como atributo a un usuarioresponsable dado que solo 
    el que hizo la reserva y el admin deben poder cancelarla */
    public void cancelar(Usuario quien) {
        if (quien == null) {
            throw new ReglaDeNegocioException("Debe indicar quien cancela la reserva");
        }
        boolean esResponsable = quien.getLogin().equals(responsable.getLogin());
        if (!esResponsable && !quien.esAdministrador()) {
            throw new ReglaDeNegocioException(
                    "Solo el responsable de la reserva o un administrador pueden cancelarla");
        }
        if (estado != EstadoReserva.ACTIVA) {
            throw new ReglaDeNegocioException("La reserva " + id + " no esta activa (estado: " + estado + ")");
        }
        estado = EstadoReserva.CANCELADA;
    }

    
    public void registrarIngreso() {
        if (estado != EstadoReserva.ACTIVA) {
            throw new ReglaDeNegocioException("La reserva " + id + " no esta activa (estado: " + estado + ")");
        }
        if (ingresoRegistrado) {
            throw new ReglaDeNegocioException("El ingreso a la reserva " + id + " ya fue registrado");
        }
        ingresoRegistrado = true;
    }

    /** estado NO_ASISTIO (cuando paso el bloque sin ingreso). */
    public void marcarNoAsistio() {
        if (estado != EstadoReserva.ACTIVA || ingresoRegistrado) {
            throw new ReglaDeNegocioException(
                    "Solo una reserva activa y sin ingreso puede marcarse como NO_ASISTIO");
        }
        estado = EstadoReserva.NO_ASISTIO;
    }

    
    public boolean esFutura(LocalDateTime ahora) {
        return estaActiva() && bloque.getInicio().isAfter(ahora);
    }
}
