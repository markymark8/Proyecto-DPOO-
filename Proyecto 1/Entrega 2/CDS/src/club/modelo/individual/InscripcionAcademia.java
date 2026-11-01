package club.modelo.individual;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import club.modelo.usuarios.Socio;


public class InscripcionAcademia {

    private Socio socio;
    private Academia academia;
    private Nivel nivel;
    private int puntajeRanking;
    private LocalDate fechaInscripcion;
    private List<RegistroRanking> historial;

    /** El nivel inicial y el puntaje inicial los decide el servicio (decision de diseno suya). */
    public InscripcionAcademia(Socio socio, Academia academia, Nivel nivel,
            int puntajeRanking, LocalDate fechaInscripcion) {
        this.socio = socio;
        this.academia = academia;
        this.nivel = nivel;
        this.puntajeRanking = puntajeRanking;
        this.fechaInscripcion = fechaInscripcion;
        this.historial = new ArrayList<>();
    }

    public Socio getSocio() {
        return socio;
    }

    public Academia getAcademia() {
        return academia;
    }

    public Nivel getNivel() {
        return nivel;
    }

    public int getPuntajeRanking() {
        return puntajeRanking;
    }

    public LocalDate getFechaInscripcion() {
        return fechaInscripcion;
    }

    public List<RegistroRanking> getHistorial() {
        return historial;
    }
    
    /* MÉTODOS IMPLEMENTADOS */

    public void agregarRegistro(RegistroRanking registro) {
        historial.add(registro);
    }

    public void asignarNivel(Nivel nuevoNivel) {
        this.nivel = nuevoNivel;
    }

    
    public void actualizarPuntaje(int nuevoPuntaje) {
        this.puntajeRanking = nuevoPuntaje;
    }
}
