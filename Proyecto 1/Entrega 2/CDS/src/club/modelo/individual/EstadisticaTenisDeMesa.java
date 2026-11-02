package club.modelo.individual;

import club.servicios.ReglaDeNegocioException;

import club.modelo.usuarios.Socio;

public class EstadisticaTenisDeMesa extends EstadisticaIndividual {

    private final int puntosTotales;

    public EstadisticaTenisDeMesa(Socio socio, PartidoPractica partido, int setsGanados, int puntosTotales) {
        super(socio, partido, setsGanados);
        this.puntosTotales = puntosTotales;
    }

    public int getPuntosTotales() {
        return puntosTotales;
    }

    @Override
    public void validarValores() {
        validarSets();
        if (puntosTotales < 0) {
            throw new ReglaDeNegocioException("Los puntos totales no pueden ser negativos");
        }
    }
}
