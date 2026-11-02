package club.modelo.conjunto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import club.servicios.ReglaDeNegocioException;

//En la competencia siempre juega el equipo del club contra una institución rival
public class Competencia {

    private final String id;
    private final String nombre;
    private final String organizador;
    private final String temporada;
    private final Deporte deporte;
    private final Categoria categoria;
    private final List<InscripcionCompetencia> inscripciones;
    private final List<InstitucionRival> rivales;
    
    public Competencia(String id, String nombre, String organizador, String temporada,
                       Deporte deporte, Categoria categoria) {
    	if (id == null || id.isBlank()
                || nombre == null || nombre.isBlank()
                || organizador == null || organizador.isBlank()
                || temporada == null || temporada.isBlank()
                || deporte == null || categoria == null) {

            throw new ReglaDeNegocioException(
                "Todos los datos de la competencia son obligatorios"
            );
        }

        if (nombre.contains(";") || organizador.contains(";")
                || temporada.contains(";")) {

            throw new ReglaDeNegocioException(
                "El nombre, organizador y temporada no pueden contener ';'"
            );
        }

        this.id = id.trim();
        this.nombre = nombre.trim();
        this.organizador = organizador.trim();
        this.temporada = temporada.trim();
        this.deporte = deporte;
        this.categoria = categoria;

        this.inscripciones = new ArrayList<>();
        this.rivales = new ArrayList<>();
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getOrganizador() { return organizador; }
    public String getTemporada() { return temporada; }
    public Deporte getDeporte() { return deporte; }
    public Categoria getCategoria() { return categoria; }

    public List<InscripcionCompetencia> getInscripciones() {
        return Collections.unmodifiableList(inscripciones);
    }

    public List<InstitucionRival> getRivales() {
        return Collections.unmodifiableList(rivales);
    }


    public InscripcionCompetencia getInscripcion(Equipo equipo) {
        for (InscripcionCompetencia inscripcion : inscripciones) {
            if (inscripcion.getEquipo() == equipo) {
                return inscripcion;
            }
        }
        return null;
    }

    public InstitucionRival getRival(String nombreRival) {
        for (InstitucionRival rival : rivales) {
            if (rival.tieneNombre(nombreRival)) {
                return rival;
            }
        }
        return null;
    }

    public void agregarInscripcion(InscripcionCompetencia inscripcion) {
    	if (inscripcion == null) {
            throw new ReglaDeNegocioException(
                "La inscripción no puede ser nula"
            );
        }

        if (getInscripcion(inscripcion.getEquipo()) != null) {
            throw new ReglaDeNegocioException(
                "El equipo ya está inscrito en esta competencia"
            );
        }

        inscripciones.add(inscripcion);
    }

    public void agregarRival(InstitucionRival rival) {
    	if (rival == null) {
            throw new ReglaDeNegocioException(
                "El rival no puede ser nulo"
            );
        }

        if (getRival(rival.getNombre()) != null) {
            throw new ReglaDeNegocioException(
                "La institución rival ya está registrada"
            );
        }
        rivales.add(rival);
    }
    public void agregarInscripcionCargada(InscripcionCompetencia inscripcion) {
        if (inscripcion == null) {
            throw new ReglaDeNegocioException("La inscripción no puede ser nula");
        }
        inscripciones.add(inscripcion);
    }
    public void agregarRivalCargado(InstitucionRival rival) {
        if (rival == null) {
            throw new ReglaDeNegocioException("El rival no puede ser nulo");
        }
        rivales.add(rival);
    }
}