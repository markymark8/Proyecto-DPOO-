
package club.modelo.conjunto;

import club.modelo.usuarios.Socio;
import club.servicios.ReglaDeNegocioException;

public abstract class EstadisticaJugador {

    private final Socio jugador;

    public EstadisticaJugador(Socio jugador) {
        if (jugador == null) {
            throw new ReglaDeNegocioException(
                "El jugador no puede ser nulo"
            );
        }

        this.jugador = jugador;
    }

    public Socio getJugador() {
        return jugador;
    }
}

