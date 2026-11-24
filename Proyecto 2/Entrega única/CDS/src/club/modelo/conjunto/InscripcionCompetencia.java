package club.modelo.conjunto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import club.servicios.ReglaDeNegocioException;
import club.modelo.usuarios.Socio;

/**
 * Inscripcion de un equipo del club en una competencia, con la nomina de jugadores que puede alinear.
 * Esta clase solo guarda el estado: las reglas para aceptar la inscripcion (deporte, categoria,
 * nomina minima, jugadores habilitados) las valida ServicioCompetencias.
 */
public class InscripcionCompetencia {

    private final Equipo equipo;
    private final List<Socio> nomina;

    public InscripcionCompetencia(Equipo equipo, List<Socio> nomina) {
    	if (equipo == null) {
            throw new ReglaDeNegocioException(
                "El equipo de la inscripción es obligatorio"
            );
        }

        if (nomina == null) {
            throw new ReglaDeNegocioException(
                "La nómina no puede ser nula"
            );
        }

        this.equipo = equipo;
        this.nomina = new ArrayList<>(nomina);
    }

    public Equipo getEquipo() {
        return equipo;
    }

    public List<Socio> getNomina() {
        return Collections.unmodifiableList(nomina);
    }

    public boolean tieneJugador(String login) {
    	if (login == null || login.isBlank()) {
            return false;
        }

        for (Socio socio : nomina) {
            if (socio.getLogin().equals(login)) {
                return true;
            }
        }
        return false;
    }
}