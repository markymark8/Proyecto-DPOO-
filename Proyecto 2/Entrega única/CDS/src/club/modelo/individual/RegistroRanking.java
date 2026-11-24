package club.modelo.individual;

import java.time.LocalDate;

import club.modelo.usuarios.Socio;


public class RegistroRanking {

    private final Socio socio;
    private final Disciplina disciplina;
    private final LocalDate fecha;
    private final int puntajeAnterior;
    private final int puntajeNuevo;
    private final PartidoPractica partido;

    public RegistroRanking(Socio socio, Disciplina disciplina, LocalDate fecha,
            int puntajeAnterior, int puntajeNuevo, PartidoPractica partido) {
        this.socio = socio;
        this.disciplina = disciplina;
        this.fecha = fecha;
        this.puntajeAnterior = puntajeAnterior;
        this.puntajeNuevo = puntajeNuevo;
        this.partido = partido;
    }

    public Socio getSocio() {
        return socio;
    }

    public Disciplina getDisciplina() {
        return disciplina;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public int getPuntajeAnterior() {
        return puntajeAnterior;
    }

    public int getPuntajeNuevo() {
        return puntajeNuevo;
    }

    public PartidoPractica getPartido() {
        return partido;
    }

}
