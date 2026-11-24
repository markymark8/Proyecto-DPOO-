
package club.modelo.conjunto;

import club.modelo.usuarios.Socio;
import club.servicios.ReglaDeNegocioException;

public class EstadisticaFutbol extends EstadisticaJugador {

    private final int goles;
    private final int asistencias;
    private final int tarjetasAmarillas;
    private final boolean tarjetaRoja;

    public EstadisticaFutbol(Socio jugador, int goles, int asistencias,
            int tarjetasAmarillas, boolean tarjetaRoja) {

        super(jugador);

        if (goles < 0 || asistencias < 0) {
            throw new ReglaDeNegocioException(
                "Los goles y las asistencias no pueden ser negativos"
            );
        }

        if (tarjetasAmarillas < 0 || tarjetasAmarillas > 2) {
            throw new ReglaDeNegocioException(
                "Las tarjetas amarillas deben estar entre 0 y 2"
            );
        }

        this.goles = goles;
        this.asistencias = asistencias;
        this.tarjetasAmarillas = tarjetasAmarillas;

        // Dos amarillas en un partido implican tarjeta roja.
        this.tarjetaRoja = tarjetaRoja || tarjetasAmarillas == 2;
    }

    public int getGoles() {
        return goles;
    }

    public int getAsistencias() {
        return asistencias;
    }

    public int getTarjetasAmarillas() {
        return tarjetasAmarillas;
    }

    public boolean isTarjetaRoja() {
        return tarjetaRoja;
    }
}
