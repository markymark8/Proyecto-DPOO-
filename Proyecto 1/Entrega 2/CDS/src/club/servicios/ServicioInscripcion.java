package club.servicios;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import club.modelo.conjunto.Equipo;
import club.modelo.conjunto.IntegranteEquipo;
import club.modelo.torneos.InscripcionEquipo;
import club.modelo.torneos.InscripcionIndividual;
import club.modelo.torneos.Modalidad;
import club.modelo.torneos.Torneo;
import club.modelo.usuarios.Empleado;
import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Turno;
import club.modelo.usuarios.Usuario;
import club.modelo.individual.Academia;
import club.modelo.individual.InscripcionAcademia;

public class ServicioInscripcion {

    private final ClubSeneca club;

    public ServicioInscripcion(ClubSeneca club) {

        if (club == null) {
            throw new ReglaDeNegocioException(
                "El club no puede ser nulo."
            );
        }

        this.club = club;
    }

    // RETIRAR INSCRIPCIONES

    public void retirarInscripcion(
        String idTorneo,
        String identificador
    ) {

        Torneo torneo =
            club.getServicioTorneos().exigirTorneo(idTorneo);

        torneo.retirar(identificador);
    }

    // LIBERAR CUPOS PREFERENCIALES

    public void liberarCuposPreferenciales(String idTorneo) {

        Torneo torneo =
            club.getServicioTorneos().exigirTorneo(idTorneo);

        torneo.liberarCuposPreferenciales();
    }

    // VALIDACIÓN DE TURNOS

    private boolean estaDeTurno(
        Empleado empleado,
        LocalDate fechaTorneo
    ) {

        for (Turno turno : empleado.getTurnos()) {

            Date fecha = turno.getFecha();

            if (fecha != null) {

                LocalDate fechaTurno = fecha.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

                if (fechaTurno.equals(fechaTorneo)) {
                    return true;
                }
            }
        }

        return false;
    }

    // VALIDACIÓN DE PARTICIPANTES

    private void validarParticipante(
        Torneo torneo,
        Usuario participante
    ) {

        if (participante instanceof Empleado) {

            Empleado empleado = (Empleado) participante;

            if (torneo.getModalidad() != Modalidad.INDIVIDUAL) {
                throw new ReglaDeNegocioException(
                    "Los empleados solo pueden participar en torneos individuales."
                );
            }

            if (estaDeTurno(empleado, torneo.getFecha())) {
                throw new ReglaDeNegocioException(
                    "El empleado tiene un turno asignado el día del torneo."
                );
            }

        } else if (!(participante instanceof Socio)) {

            throw new ReglaDeNegocioException(
                "El participante debe ser un socio o un empleado."
            );
        }
    }

    // INSCRIPCIÓN INDIVIDUAL

    public InscripcionIndividual inscribirJugador(
        String idTorneo,
        Usuario participante
    ) {

        Torneo torneo =
            club.getServicioTorneos().exigirTorneo(idTorneo);

        if (torneo.getModalidad() != Modalidad.INDIVIDUAL) {
            throw new ReglaDeNegocioException(
                "El torneo no admite inscripciones individuales."
            );
        }

        if (participante == null) {
            throw new ReglaDeNegocioException(
                "El participante no puede ser nulo."
            );
        }

        validarParticipante(torneo, participante);

        List<Usuario> participantes = new ArrayList<>();
        participantes.add(participante);

        InscripcionIndividual inscripcion =
        	    crearInscripcionIndividual(torneo, participantes);

        torneo.inscribir(inscripcion);

        return inscripcion;
    }

    // INSCRIPCIÓN DE PAREJAS

    public InscripcionIndividual inscribirPareja(
        String idTorneo,
        Usuario participante,
        Usuario pareja
    ) {

        Torneo torneo =
            club.getServicioTorneos().exigirTorneo(idTorneo);

        if (torneo.getModalidad() != Modalidad.DOBLES) {
            throw new ReglaDeNegocioException(
                "El torneo no admite inscripciones de dobles."
            );
        }

        if (participante == null || pareja == null) {
            throw new ReglaDeNegocioException(
                "Los dos participantes deben ser válidos."
            );
        }

        validarParticipante(torneo, participante);
        validarParticipante(torneo, pareja);

        List<Usuario> participantes = new ArrayList<>();
        participantes.add(participante);
        participantes.add(pareja);

        InscripcionIndividual inscripcion =
            new InscripcionIndividual(
                participantes,
                false,
                0
            );

        torneo.inscribir(inscripcion);

        return inscripcion;
    }

    // INSCRIPCIÓN DE EQUIPOS

    public InscripcionEquipo inscribirEquipo(
        String idTorneo,
        Equipo equipo
    ) {

        Torneo torneo =
            club.getServicioTorneos().exigirTorneo(idTorneo);

        if (torneo.getModalidad() != Modalidad.CONJUNTO) {
            throw new ReglaDeNegocioException(
                "El torneo no admite inscripciones de equipos."
            );
        }

        if (equipo == null) {
            throw new ReglaDeNegocioException(
                "El equipo no puede ser nulo."
            );
        }

        if (!equipo.isActivo()) {
            throw new ReglaDeNegocioException(
                "El equipo no está activo."
            );
        }

        if (!equipo.getDeporte().getNombre()
                .equalsIgnoreCase(torneo.getDisciplina())) {

            throw new ReglaDeNegocioException(
                "El deporte del equipo no coincide con la disciplina del torneo."
            );
        }

        if (!equipo.getCategoria().name()
                .equalsIgnoreCase(torneo.getCategoria())) {

            throw new ReglaDeNegocioException(
                "La categoría del equipo no coincide con la categoría del torneo."
            );
        }

        if (!equipo.tieneMinimoHabilitados()) {
            throw new ReglaDeNegocioException(
                "El equipo no cuenta con el mínimo de jugadores habilitados."
            );
        }

        // Se inscriben todos los integrantes activos del equipo.
        List<Usuario> jugadores = new ArrayList<>();

        for (IntegranteEquipo integrante : equipo.getIntegrantesActivos()) {
            jugadores.add(integrante.getSocio());
        }

        InscripcionEquipo inscripcion = new InscripcionEquipo(
            equipo,
            jugadores,
            false,
            0
        );

        torneo.inscribir(inscripcion);

        return inscripcion;
    }
    
	private InscripcionAcademia exigirInscripcionAcademia(Torneo torneo, Socio socio) {

		for (Academia academia : club.getServicioIndividual().getAcademias()) {

			if (academia.getDisciplina().name().equalsIgnoreCase(torneo.getDisciplina())) {

				InscripcionAcademia inscripcion = academia.getInscripcionDe(socio);

				if (inscripcion == null) {
					throw new ReglaDeNegocioException("El socio " + socio.getLogin()
							+ " no está inscrito en la academia de " + torneo.getDisciplina() + ".");
				}

				if (!inscripcion.getNivel().name().equalsIgnoreCase(torneo.getCategoria())) {

					throw new ReglaDeNegocioException("El nivel del socio no corresponde al nivel del torneo.");
				}

				return inscripcion;
			}
		}

		throw new ReglaDeNegocioException("No existe una academia para la disciplina " + torneo.getDisciplina() + ".");
	}
	
	private boolean estaEnLosMejores(Torneo torneo, InscripcionAcademia propia) {

		int superiores = 0;

		for (InscripcionAcademia otra : propia.getAcademia().getInscripciones()) {

			if (otra.getNivel() == propia.getNivel() && otra.getPuntajeRanking() > propia.getPuntajeRanking()) {

				superiores++;
			}
		}

		return superiores < torneo.getCuposPreferenciales();
	}
	
	private InscripcionIndividual crearInscripcionIndividual(Torneo torneo, List<Usuario> participantes) {

		int sumaPuntajes = 0;
		int cantidadSocios = 0;

		boolean todosPreferenciales = true;
		boolean hayEmpleado = false;

		for (Usuario participante : participantes) {

			if (participante instanceof Socio) {

				Socio socio = (Socio) participante;

				InscripcionAcademia ia = exigirInscripcionAcademia(torneo, socio);

				sumaPuntajes += ia.getPuntajeRanking();
				cantidadSocios++;

				if (!estaEnLosMejores(torneo, ia)) {
					todosPreferenciales = false;
				}

			} else if (participante instanceof Empleado) {

				hayEmpleado = true;
				todosPreferenciales = false;
			}
		}

		int puntajeRanking = 0;

		if (cantidadSocios > 0) {
			puntajeRanking = sumaPuntajes / cantidadSocios;
		}

		boolean preferencial = cantidadSocios > 0 && todosPreferenciales && !hayEmpleado;

		return new InscripcionIndividual(participantes, preferencial, puntajeRanking);
	}
}
