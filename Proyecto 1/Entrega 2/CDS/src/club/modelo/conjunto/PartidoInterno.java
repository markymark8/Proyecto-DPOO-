package club.modelo.conjunto;

import java.time.LocalDate;

import club.servicios.ReglaDeNegocioException;

import java.util.HashSet;
import java.util.Set;

public class PartidoInterno extends Partido {

    private final Equipo local;
    private final Equipo visitante;
    private final Set<String> sancionados = new HashSet<>();

    public PartidoInterno(String id, Equipo local, Equipo visitante, LocalDate fecha) {
        super(id, deporteDe(local), fecha);
        validarEquipos(local, visitante);
        this.local = local;
        this.visitante = visitante;
    }

    public PartidoInterno(String id, Equipo local, Equipo visitante, LocalDate fecha,
                          EstadoPartido estado, int marcadorLocal, int marcadorVisitante) {
        super(id, deporteDe(local), fecha, estado, marcadorLocal, marcadorVisitante);
        validarEquipos(local, visitante);
        this.local = local;
        this.visitante = visitante;
    }

    public Equipo getLocal() { return local; }
    public Equipo getVisitante() { return visitante; }

    private static Deporte deporteDe(Equipo local) {
        if (local == null) {
            throw new ReglaDeNegocioException("El equipo local no puede ser nulo");
        }
        return local.getDeporte();
    }

    private static void validarEquipos(Equipo local, Equipo visitante) {
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
    public void marcarSancionado(String login) {
        sancionados.add(login);
    }

    public boolean fueSancionado(String login) {
        return sancionados.contains(login);
    }

    public Set<String> getSancionados() {
        return new HashSet<>(sancionados);
    }
}