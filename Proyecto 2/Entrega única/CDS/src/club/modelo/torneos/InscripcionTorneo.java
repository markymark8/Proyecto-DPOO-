package club.modelo.torneos;
import java.util.List;
import club.modelo.usuarios.Usuario;

public abstract class InscripcionTorneo {

    private boolean preferencial;
    private final int puntajeRanking;

    public InscripcionTorneo(boolean preferencial, int puntajeRanking) {
        this.preferencial = preferencial;
        this.puntajeRanking = puntajeRanking;
    }

    public boolean isPreferencial() {
        return preferencial;
    }

    public void setPreferencial(boolean preferencial) {
        this.preferencial = preferencial;
    }

    public int getPuntajeRanking() {
        return puntajeRanking;
    }

    public abstract String getNombre();
    public abstract String getIdentificador();
    public abstract List<Usuario> getParticipantes();
}

