package club.modelo.conjunto;

import java.time.LocalDate;

import club.servicios.ReglaDeNegocioException;

import java.util.HashSet;
import java.util.Set;
import club.modelo.usuarios.Socio;

public class PartidoInterno extends Partido {

    private final Equipo local;
    private final Equipo visitante;
    private final Set<String> sancionados = new HashSet<>();
    private final java.util.Map<String, Integer> amarillasPorJugador = new java.util.HashMap<>();
    private final Set<String> rojasPorJugador = new HashSet<>();

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
    public boolean estaSuspendidoInternamente(Socio socio) {

        if (socio == null) {
            return false;
        }

        for (IntegranteEquipo integrante : local.getIntegrantes()) {
            if (integrante.getSocio().getLogin().equals(socio.getLogin())) {
                return integrante.isSuspendidoInterno();
            }
        }

        for (IntegranteEquipo integrante : visitante.getIntegrantes()) {
            if (integrante.getSocio().getLogin().equals(socio.getLogin())) {
                return integrante.isSuspendidoInterno();
            }
        }

        return false;
    }
    public void registrarAmarillaPartido(String login) {
        if (login == null) {
            throw new ReglaDeNegocioException(
                "El login del jugador no puede ser nulo"
            );
        }

        amarillasPorJugador.put(
            login,
            amarillasPorJugador.getOrDefault(login, 0) + 1
        );
    }

    public int getAmarillasPartido(String login) {
        if (login == null) {
            return 0;
        }

        return amarillasPorJugador.getOrDefault(login, 0);
    }
    public void registrarRojaPartido(String login) {
        if (login == null) {
            throw new ReglaDeNegocioException(
                "El login del jugador no puede ser nulo"
            );
        }

        rojasPorJugador.add(login);
    }

    public boolean recibioRojaPartido(String login) {
        if (login == null) {
            return false;
        }

        return rojasPorJugador.contains(login);
    }  

    /** Solo para el repositorio: restaura las tarjetas que un jugador recibio en este partido. */
    public void restaurarTarjetasPartido(String login, int amarillas, boolean roja) {
        if (login == null || amarillas < 0) {
            throw new ReglaDeNegocioException("Datos inválidos para restaurar las tarjetas del partido");
        }
        if (amarillas > 0) {
            amarillasPorJugador.put(login, amarillas);
        }
        if (roja) {
            rojasPorJugador.add(login);
        }
    }

    /** Logins de los jugadores que recibieron alguna tarjeta en este partido, ordenados. */
    public java.util.Set<String> getLoginsConTarjetaPartido() {
        java.util.Set<String> logins = new java.util.TreeSet<>(amarillasPorJugador.keySet());
        logins.addAll(rojasPorJugador);
        return logins;
    }
}