
package club.modelo.conjunto;

import club.modelo.usuarios.Socio;
import club.servicios.ReglaDeNegocioException;

public class EstadisticaBasket extends EstadisticaJugador {

    private final int puntos;
    private final int rebotes;

    public EstadisticaBasket(Socio jugador, int puntos, int rebotes) {
        super(jugador);

        if (puntos < 0 || rebotes < 0) {
            throw new ReglaDeNegocioException(
                "Los puntos y rebotes no pueden ser negativos"
            );
        }

        this.puntos = puntos;
        this.rebotes = rebotes;
    }

    public int getPuntos() {
        return puntos;
    }

    public int getRebotes() {
        return rebotes;
    }
}
