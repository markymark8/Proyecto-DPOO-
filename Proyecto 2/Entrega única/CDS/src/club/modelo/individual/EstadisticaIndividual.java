package club.modelo.individual;

import club.servicios.ReglaDeNegocioException;

import club.modelo.usuarios.Socio;


public abstract class EstadisticaIndividual {

    private final Socio socio;
    private PartidoPractica partido;
    private final int setsGanados;

    public EstadisticaIndividual(Socio socio, PartidoPractica partido, int setsGanados) {
        this.socio = socio;
        this.partido = partido;
        this.setsGanados = setsGanados;
    }

    public Socio getSocio() {
        return socio;
    }

    public PartidoPractica getPartido() {
        return partido;
    }

    public void setPartido(PartidoPractica partido) {
        this.partido = partido;
    }

    public int getSetsGanados() {
        return setsGanados;
    }

    /*MÉTODOS IMPLEMENTADOS*/
    
    public abstract void validarValores();

    
    protected void validarSets() {
        if (setsGanados < 0) {
            throw new ReglaDeNegocioException("Los sets ganados no pueden ser negativos");
        }
    }
}
