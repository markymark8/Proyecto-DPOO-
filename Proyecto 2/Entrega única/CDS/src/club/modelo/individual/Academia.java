package club.modelo.individual;

import java.util.ArrayList;
import java.util.List;

import club.modelo.usuarios.Socio;


public class Academia {

    private  Disciplina disciplina;
    private int maxInstalacionesSimultaneas;
    private  List<InscripcionAcademia> inscripciones;

    public Academia(Disciplina disciplina, int maxInstalacionesSimultaneas) {
        this.disciplina = disciplina;
        this.maxInstalacionesSimultaneas = maxInstalacionesSimultaneas;
        this.inscripciones = new ArrayList<>();
    }

    public Disciplina getDisciplina() {
        return disciplina;
    }

    public int getMaxInstalacionesSimultaneas() {
        return maxInstalacionesSimultaneas;
    }

    public void setMaxInstalacionesSimultaneas(int max) {
        this.maxInstalacionesSimultaneas = max;
    }

    public List<InscripcionAcademia> getInscripciones() {
        return inscripciones;
    }


    /*MÉTODOS IMPLEMENTADOS */
    
    public void agregarInscripcion(InscripcionAcademia inscripcion) {
        inscripciones.add(inscripcion);
    }


    public boolean estaInscrito(Socio socio) {
        return getInscripcionDe(socio) != null;
    }

    public InscripcionAcademia getInscripcionDe(Socio socio) {
        for (InscripcionAcademia i : inscripciones) {
            if (i.getSocio().getLogin().equals(socio.getLogin())) {
                return i;
            }
        }
        return null;
    }
}
