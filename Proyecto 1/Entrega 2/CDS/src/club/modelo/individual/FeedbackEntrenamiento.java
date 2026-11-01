package club.modelo.individual;

import club.modelo.usuarios.Socio;


public class FeedbackEntrenamiento {

    private final Socio socio;
    private final EstadoAsistencia asistencia;
    private final Integer nota; 
    private final String comentario;

    public FeedbackEntrenamiento(Socio socio, EstadoAsistencia asistencia, Integer nota, String comentario) {
        this.socio = socio;
        this.asistencia = asistencia;
        this.nota = nota;
        this.comentario = comentario;
    }

    public Socio getSocio() {
        return socio;
    }

    public EstadoAsistencia getAsistencia() {
        return asistencia;
    }

    public Integer getNota() {
        return nota;
    }

    public String getComentario() {
        return comentario;
    }
}
