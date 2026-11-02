package club.modelo.conjunto;

import java.time.LocalDate;

import club.servicios.ReglaDeNegocioException;
import club.modelo.usuarios.Socio;

/**
 * Partido que pertenece a una competencia (liga). Extiende Partido igual que PartidoInterno.
 *
 * EXTENSION PENDIENTE: cuando existan Competencia, Fase, Grupo e InscripcionCompetencia,
 * local y visitante pasarian a ser inscripciones, y se agregaria la nomina de jugadores.
 * Por ahora usa Equipo y un identificador de competencia para no depender de clases futuras.
 */
public class PartidoCompetencia extends Partido {

    private final String idCompetencia;
    private final int jornada;
    private final Equipo local;
    private final Equipo visitante;

    public PartidoCompetencia(String id, String idCompetencia, int jornada,
                              Equipo local, Equipo visitante, LocalDate fecha) {
        super(id, deporteDe(local), fecha);
        validar(idCompetencia, jornada, local, visitante);
        this.idCompetencia = idCompetencia;
        this.jornada = jornada;
        this.local = local;
        this.visitante = visitante;
    }

    public PartidoCompetencia(String id, String idCompetencia, int jornada,
                              Equipo local, Equipo visitante, LocalDate fecha,
                              EstadoPartido estado, int marcadorLocal, int marcadorVisitante) {
        super(id, deporteDe(local), fecha, estado, marcadorLocal, marcadorVisitante);
        validar(idCompetencia, jornada, local, visitante);
        this.idCompetencia = idCompetencia;
        this.jornada = jornada;
        this.local = local;
        this.visitante = visitante;
    }

    public String getIdCompetencia() { return idCompetencia; }
    public int getJornada() { return jornada; }
    public Equipo getLocal() { return local; }
    public Equipo getVisitante() { return visitante; }

    private static Deporte deporteDe(Equipo local) {
        if (local == null) {
            throw new ReglaDeNegocioException("El equipo local no puede ser nulo");
        }
        return local.getDeporte();
    }

    private static void validar(String idCompetencia, int jornada, Equipo local, Equipo visitante) {
        if (idCompetencia == null || idCompetencia.isBlank()) {
            throw new ReglaDeNegocioException("El partido debe pertenecer a una competencia");
        }
        if (jornada <= 0) {
            throw new ReglaDeNegocioException("La jornada debe ser positiva");
        }
        if (visitante == null) {
            throw new ReglaDeNegocioException("El equipo visitante no puede ser nulo");
        }
        if (local == visitante) {
            throw new ReglaDeNegocioException("Un equipo no puede jugar contra sí mismo");
        }
        if (!local.getDeporte().getNombre().equals(visitante.getDeporte().getNombre())) {
            throw new ReglaDeNegocioException("Los equipos deben ser del mismo deporte");
        }
        if (local.getCategoria() != visitante.getCategoria()) {
            throw new ReglaDeNegocioException("Los equipos deben ser de la misma categoría");
        }
    }
    @Override
    protected boolean participaJugador(Socio socio) {

        if (socio == null) {
            return false;
        }
        for (IntegranteEquipo integrante : local.getIntegrantesActivos()) {
            if (integrante.getSocio().getLogin().equals(socio.getLogin())) {
                return true;
            }
        }
        for (IntegranteEquipo integrante : visitante.getIntegrantesActivos()) {
            if (integrante.getSocio().getLogin().equals(socio.getLogin())) {
                return true;
            }
        }

        return false;
    }
    
}