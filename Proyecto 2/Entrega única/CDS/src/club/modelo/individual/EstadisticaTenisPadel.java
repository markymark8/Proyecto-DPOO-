package club.modelo.individual;

import club.servicios.ReglaDeNegocioException;

import club.modelo.usuarios.Socio;

public class EstadisticaTenisPadel extends EstadisticaIndividual{

    private final int gamesGanados;
    private final int aces;
    private final int erroresNoForzados;

    public EstadisticaTenisPadel(Socio socio, PartidoPractica partido, int setsGanados,
            int gamesGanados, int aces, int erroresNoForzados) {
        super(socio, partido, setsGanados);
        this.gamesGanados = gamesGanados;
        this.aces = aces;
        this.erroresNoForzados = erroresNoForzados;
    }

    public int getGamesGanados() {
        return gamesGanados;
    }

    public int getAces() {
        return aces;
    }

    public int getErroresNoForzados() {
        return erroresNoForzados;
    }

    @Override
    public void validarValores() {
        validarSets();
        if (gamesGanados < 0 || aces < 0 || erroresNoForzados < 0) {
            throw new ReglaDeNegocioException("Las estadisticas de tenis/padel no pueden ser negativas");
        }
    }
}

