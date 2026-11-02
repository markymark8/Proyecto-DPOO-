package club.modelo.individual;

import club.servicios.ReglaDeNegocioException;

import java.util.ArrayList;
import java.util.List;

import club.modelo.usuarios.Entrenador;
import club.modelo.usuarios.Socio;



public class SesionEntrenamiento extends Reserva {

    private  TipoSesion tipo;
    private  Entrenador entrenador;
    private  List<Socio> convocados;
    private  List<FeedbackEntrenamiento> feedbacks;

    public SesionEntrenamiento(int id, Instalacion instalacion, BloqueHorario bloque,
            Entrenador entrenador, TipoSesion tipo) {
        super(id, instalacion, bloque, entrenador, 0, null);
        this.tipo = tipo;
        this.entrenador = entrenador;
        this.convocados = new ArrayList<>();
        this.feedbacks = new ArrayList<>();
    }

    public TipoSesion getTipo() {
        return tipo;
    }

    public Entrenador getEntrenador() {
        return entrenador;
    }

    public List<Socio> getConvocados() {
        return convocados;
    }


    public List<FeedbackEntrenamiento> getFeedbacks() {
        return feedbacks;
    }

   
    public void convocar(Socio socio) {
        for (Socio s : convocados) {
            if (s.getLogin().equals(socio.getLogin())) {
                return;
            }
        }
        convocados.add(socio);
    }

    
    public void registrarFeedback(Socio socio, EstadoAsistencia asistencia, Integer nota, String comentario) {
        boolean convocado = false;
        for (Socio s : convocados) {
            if (s.getLogin().equals(socio.getLogin())) {
                convocado = true;
            }
        }
        if (!convocado) {
            throw new ReglaDeNegocioException("El socio no fue convocado a la sesion " );
        }
        if (asistencia == null) {
            throw new ReglaDeNegocioException("Debe indicar la asistencia");
        }
        if (nota != null && (nota < 1 || nota > 5)) {
            throw new ReglaDeNegocioException("La nota debe estar entre 1 y 5 (recibida: " + nota + ")");
        }
        for (FeedbackEntrenamiento f : feedbacks) {
            if (f.getSocio().getLogin().equals(socio.getLogin())) {
                throw new ReglaDeNegocioException("El socio ya tiene feedback en esta sesion");
            }
        }
        feedbacks.add(new FeedbackEntrenamiento(socio, asistencia, nota, comentario));
    }

}