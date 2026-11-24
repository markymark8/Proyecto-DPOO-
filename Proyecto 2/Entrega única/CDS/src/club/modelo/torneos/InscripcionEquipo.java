package club.modelo.torneos;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import club.modelo.conjunto.Equipo;
import club.modelo.usuarios.Usuario;
import club.servicios.ReglaDeNegocioException;

public class InscripcionEquipo extends InscripcionTorneo {

    private final Equipo equipo;
    private final List<Usuario> jugadores;

    public InscripcionEquipo(Equipo equipo, List<Usuario> jugadores,
                             boolean preferencial, int puntajeRanking) {

        super(preferencial, puntajeRanking);

        validar(equipo, jugadores);

        this.equipo = equipo;
        this.jugadores = new ArrayList<>(jugadores);
    }

    private static void validar(Equipo equipo, List<Usuario> jugadores) {

        if (equipo == null) {
            throw new ReglaDeNegocioException(
                    "El equipo no puede ser nulo");
        }

        if (jugadores == null || jugadores.isEmpty()) {
            throw new ReglaDeNegocioException(
                    "La nómina no puede estar vacía");
        }

        for (Usuario jugador : jugadores) {
            if (jugador == null) {
                throw new ReglaDeNegocioException(
                        "La nómina no puede contener participantes nulos");
            }
        }
    }

    public Equipo getEquipo() {
        return equipo;
    }

    public List<Usuario> getJugadores() {
        return Collections.unmodifiableList(jugadores);
    }

    @Override
    public String getNombre() {
        return equipo.getNombre();
    }

    @Override
    public String getIdentificador() {
        return equipo.getId();
    }

    @Override
    public List<Usuario> getParticipantes() {
        return Collections.unmodifiableList(jugadores);
    }
}