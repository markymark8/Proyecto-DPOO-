
package club.modelo.conjunto;

import club.modelo.usuarios.Socio;
import club.servicios.ReglaDeNegocioException;

public class EstadisticaVoleibol extends EstadisticaJugador {

    private final int aces;
    private final int bloqueos;

    public EstadisticaVoleibol(Socio jugador, int aces, int bloqueos) {
        super(jugador);

        if (aces < 0 || bloqueos < 0) {
            throw new ReglaDeNegocioException(
                "Los aces y bloqueos no pueden ser negativos"
            );
        }

        this.aces = aces;
        this.bloqueos = bloqueos;
    }

    public int getAces() {
        return aces;
    }

    public int getBloqueos() {
        return bloqueos;
    }
}
