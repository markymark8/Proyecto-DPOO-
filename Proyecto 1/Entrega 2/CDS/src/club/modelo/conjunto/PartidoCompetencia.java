package club.modelo.conjunto;

import java.time.LocalDate;

import club.modelo.usuarios.Socio;
import club.servicios.ReglaDeNegocioException;

public class PartidoCompetencia extends Partido {

    private final String idCompetencia;
    private final int jornada;
    private final InscripcionCompetencia inscripcion;
    private final InstitucionRival rival;
    private final boolean clubEsLocal;
    private final String escenario;

    public PartidoCompetencia(String id, String idCompetencia, int jornada,
                              InscripcionCompetencia inscripcion, InstitucionRival rival,
                              boolean clubEsLocal, String escenario, LocalDate fecha) {
        super(id, deporteDe(inscripcion), fecha);
        validar(idCompetencia, jornada, inscripcion, rival, escenario);
        this.idCompetencia = idCompetencia;
        this.jornada = jornada;
        this.inscripcion = inscripcion;
        this.rival = rival;
        this.clubEsLocal = clubEsLocal;
        this.escenario = escenario;
    }

    /** Constructor de restauracion (lo usa el repositorio al cargar). */
    public PartidoCompetencia(String id, String idCompetencia, int jornada,
                              InscripcionCompetencia inscripcion, InstitucionRival rival,
                              boolean clubEsLocal, String escenario, LocalDate fecha,
                              EstadoPartido estado, int marcadorLocal, int marcadorVisitante) {
        super(id, deporteDe(inscripcion), fecha, estado, marcadorLocal, marcadorVisitante);
        validar(idCompetencia, jornada, inscripcion, rival, escenario);
        this.idCompetencia = idCompetencia;
        this.jornada = jornada;
        this.inscripcion = inscripcion;
        this.rival = rival;
        this.clubEsLocal = clubEsLocal;
        this.escenario = escenario;
    }

    public String getIdCompetencia() { return idCompetencia; }
    public int getJornada() { return jornada; }
    public InscripcionCompetencia getInscripcion() { return inscripcion; }
    public Equipo getEquipoClub() { return inscripcion.getEquipo(); }
    public InstitucionRival getRival() { return rival; }
    public boolean clubEsLocal() { return clubEsLocal; }
    public String getEscenario() { return escenario; }

    /** Marcador del equipo del club, sin importar si juega de local o de visitante. */
    public int getMarcadorClub() {
        return clubEsLocal ? getMarcadorLocal() : getMarcadorVisitante();
    }

    /** Marcador de la institucion rival. */
    public int getMarcadorRival() {
        return clubEsLocal ? getMarcadorVisitante() : getMarcadorLocal();
    }

    public String getNombreLocal() {
        return clubEsLocal ? getEquipoClub().getNombre() : rival.getNombre();
    }

    public String getNombreVisitante() {
        return clubEsLocal ? rival.getNombre() : getEquipoClub().getNombre();
    }

    private static Deporte deporteDe(InscripcionCompetencia inscripcion) {
        if (inscripcion == null) {
            throw new ReglaDeNegocioException("La inscripción del equipo no puede ser nula");
        }
        return inscripcion.getEquipo().getDeporte();
    }

    private static void validar(String idCompetencia, int jornada, InscripcionCompetencia inscripcion,
                                InstitucionRival rival, String escenario) {
    	if (idCompetencia == null || idCompetencia.isBlank()) {
            throw new ReglaDeNegocioException(
                "El partido debe pertenecer a una competencia"
            );
        }

        if (jornada <= 0) {
            throw new ReglaDeNegocioException(
                "La jornada debe ser un número positivo"
            );
        }

        if (inscripcion == null) {
            throw new ReglaDeNegocioException(
                "La inscripción del equipo no puede ser nula"
            );
        }

        if (rival == null) {
            throw new ReglaDeNegocioException(
                "La institución rival es obligatoria"
            );
        }

        if (escenario == null || escenario.isBlank()) {
            throw new ReglaDeNegocioException(
                "El escenario del partido es obligatorio"
            );
        }

        if (escenario.contains(";")) {
            throw new ReglaDeNegocioException(
                "El escenario no puede contener ';'"
            );
        }
    }

    @Override
    protected boolean participaJugador(Socio socio) {
        return socio != null && inscripcion.tieneJugador(socio.getLogin());
    }
} 