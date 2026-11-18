package club.modelo.conjunto;

import java.time.LocalDate;

import club.modelo.usuarios.Socio;
import club.servicios.ReglaDeNegocioException;
public class PartidoExterno extends Partido {

    private final String idCompetencia;
    private final int jornada;
    private final InstitucionRival rivalLocal;
    private final InstitucionRival rivalVisitante;
    private final String escenario;

    public PartidoExterno(String id, String idCompetencia, int jornada, Deporte deporte,
                          InstitucionRival rivalLocal, InstitucionRival rivalVisitante,
                          String escenario, LocalDate fecha) {
        super(id, deporte, fecha);
        validar(idCompetencia, jornada, rivalLocal, rivalVisitante, escenario);
        this.idCompetencia = idCompetencia;
        this.jornada = jornada;
        this.rivalLocal = rivalLocal;
        this.rivalVisitante = rivalVisitante;
        this.escenario = escenario;
    }

    public PartidoExterno(String id, String idCompetencia, int jornada, Deporte deporte,
                          InstitucionRival rivalLocal, InstitucionRival rivalVisitante,
                          String escenario, LocalDate fecha,
                          EstadoPartido estado, int marcadorLocal, int marcadorVisitante) {
        super(id, deporte, fecha, estado, marcadorLocal, marcadorVisitante);
        validar(idCompetencia, jornada, rivalLocal, rivalVisitante, escenario);
        this.idCompetencia = idCompetencia;
        this.jornada = jornada;
        this.rivalLocal = rivalLocal;
        this.rivalVisitante = rivalVisitante;
        this.escenario = escenario;
    }

    public String getIdCompetencia() { return idCompetencia; }
    public int getJornada() { return jornada; }
    public InstitucionRival getRivalLocal() { return rivalLocal; }
    public InstitucionRival getRivalVisitante() { return rivalVisitante; }
    public String getEscenario() { return escenario; }

private static void validar(String idCompetencia, int jornada,
                            InstitucionRival rivalLocal,
                            InstitucionRival rivalVisitante,
                            String escenario) {

    // 1. La competencia es obligatoria, la jornada debe ser positiva, ambos rivales son obligatorios, no se permite enfrentar una institución contra sí misma,El escenario es obligatorio y compatible con CSV.
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
    if (rivalLocal == null || rivalVisitante == null) {
        throw new ReglaDeNegocioException(
            "Los dos equipos rivales son obligatorios"
        );
    }
    if (rivalLocal.tieneNombre(rivalVisitante.getNombre())) {
        throw new ReglaDeNegocioException(
            "Una institución no puede enfrentarse contra sí misma"
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
        return false;
    }
}