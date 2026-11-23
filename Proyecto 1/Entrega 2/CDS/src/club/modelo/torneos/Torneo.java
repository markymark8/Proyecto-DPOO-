package club.modelo.torneos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import club.servicios.ReglaDeNegocioException;
import java.time.LocalDate;

public abstract class Torneo {

	private final String id;
	private final String nombre;
	private final Modalidad modalidad;
	private final String disciplina;
	private final String categoria;
	private final int cupos;
	private final LocalDate fecha;

	private EstadoTorneo estado;

	private final List<InscripcionTorneo> inscripciones;
	private boolean preferencialesLiberados = false;

	protected Torneo(String id, String nombre, Modalidad modalidad, String disciplina, String categoria, int cupos, LocalDate fecha) {

		validar(id, nombre, modalidad, disciplina, categoria, cupos, fecha);

		this.id = id.trim();
		this.nombre = nombre.trim();
		this.modalidad = modalidad;
		this.disciplina = disciplina.trim();
		this.categoria = categoria.trim();
		this.cupos = cupos;
		this.fecha = fecha;
		this.estado = EstadoTorneo.EN_INSCRIPCION;
		this.inscripciones = new ArrayList<>();
	}

	private static void validar(String id, String nombre, Modalidad modalidad, String disciplina, String categoria,
			int cupos, LocalDate fecha) {

		if (id == null || id.trim().isEmpty() || id.contains(";")) {
			throw new ReglaDeNegocioException("El id no puede estar vacío ni contener ';'");
		}

		if (nombre == null || nombre.trim().isEmpty() || nombre.contains(";")) {
			throw new ReglaDeNegocioException("El nombre no puede estar vacío ni contener ';'");
		}

		if (modalidad == null) {
			throw new ReglaDeNegocioException("La modalidad no puede ser nula");
		}

		if (disciplina == null || disciplina.trim().isEmpty() || disciplina.contains(";")) {
			throw new ReglaDeNegocioException("La disciplina no puede estar vacía ni contener ';'");
		}

		if (categoria == null || categoria.trim().isEmpty() || categoria.contains(";")) {
			throw new ReglaDeNegocioException("La categoría no puede estar vacía ni contener ';'");
		}

		if (cupos < 2) {
			throw new ReglaDeNegocioException("Un torneo debe tener al menos 2 cupos");
		}
		
		if (fecha == null) {
		    throw new ReglaDeNegocioException(
		            "La fecha del torneo no puede ser nula");
		}
	}

	// CONSULTAS DE INSCRIPCIONES

	public List<InscripcionTorneo> getInscripciones() {
		return Collections.unmodifiableList(inscripciones);
	}

	public int getCuposPreferenciales() {
		return (cupos + 4) / 5;
	}

	public int getCuposRegulares() {
		return cupos - getCuposPreferenciales();
	}

	private int contarInscripcionesPreferenciales() {
		int contador = 0;

		for (InscripcionTorneo inscripcion : inscripciones) {
			if (inscripcion.isPreferencial()) {
				contador++;
			}
		}

		return contador;
	}

	private int contarInscripcionesRegulares() {
		int contador = 0;

		for (InscripcionTorneo inscripcion : inscripciones) {
			if (!inscripcion.isPreferencial()) {
				contador++;
			}
		}

		return contador;
	}

	public String getId() {
		return id;
	}

	public Modalidad getModalidad() {
		return modalidad;
	}

	public String getNombre() {
		return nombre;
	}

	public String getCategoria() {
		return categoria;
	}

	public String getDisciplina() {
		return disciplina;
	}

	public int getCupos() {
		return cupos;
	}

	public EstadoTorneo getEstado() {
		return estado;
	}
	
	public LocalDate getFecha() {
	    return fecha;
	}

	public abstract int getTarifaInscripcion();

	public void retirar(String identificador) {

		if (estado != EstadoTorneo.EN_INSCRIPCION) {
			throw new ReglaDeNegocioException("Solo se pueden retirar inscripciones durante el periodo de inscripción");
		}

		InscripcionTorneo encontrada = null;

		for (InscripcionTorneo inscripcion : inscripciones) {
			if (inscripcion.getIdentificador().equals(identificador)) {
				encontrada = inscripcion;
				break;
			}
		}

		if (encontrada == null) {
			throw new ReglaDeNegocioException("No existe una inscripción con el identificador " + identificador);
		}

		inscripciones.remove(encontrada);
	}

	public void inscribir(InscripcionTorneo inscripcion) {
		if (inscripcion == null) {
			throw new ReglaDeNegocioException("La inscripción no puede ser nula");
		}
		if (estado != EstadoTorneo.EN_INSCRIPCION) {
			throw new ReglaDeNegocioException("El torneo no está en periodo de inscripción");
		}
		if (modalidad == Modalidad.CONJUNTO) {

			if (!(inscripcion instanceof InscripcionEquipo)) {
				throw new ReglaDeNegocioException("La modalidad CONJUNTO requiere una inscripción de equipo");
			}

		} else if (modalidad == Modalidad.INDIVIDUAL) {

			if (!(inscripcion instanceof InscripcionIndividual) || inscripcion.getParticipantes().size() != 1) {
				throw new ReglaDeNegocioException("La modalidad INDIVIDUAL requiere exactamente un participante");
			}

		} else if (modalidad == Modalidad.DOBLES) {

			if (!(inscripcion instanceof InscripcionIndividual) || inscripcion.getParticipantes().size() != 2) {
				throw new ReglaDeNegocioException("La modalidad DOBLES requiere exactamente dos participantes");
			}
		}
		for (InscripcionTorneo existente : inscripciones) {
			if (existente.getIdentificador().equals(inscripcion.getIdentificador())) {
				throw new ReglaDeNegocioException("El participante ya está inscrito en este torneo");
			}
		}
		int np = contarInscripcionesPreferenciales();
		int nr = contarInscripcionesRegulares();

		int P = getCuposPreferenciales();
		int R = getCuposRegulares();

		boolean comoRegular = !inscripcion.isPreferencial();

		if (inscripcion.isPreferencial() && np < P && inscripciones.size() < cupos) {

			inscripciones.add(inscripcion);
			return;
		}
		
		// Decidimos que lo preferencial se vuelve regular cuando los preferenciales están llenos.

		if (inscripcion.isPreferencial()) {
			comoRegular = true;
		}

		if (preferencialesLiberados) {

			if (np + nr >= cupos) {
				throw new ReglaDeNegocioException("No hay cupos disponibles para esta inscripción");
			}

		} else {

			if (nr >= R) {
				throw new ReglaDeNegocioException("No hay cupos regulares disponibles para esta inscripción");
			}
		}

		if (comoRegular) {
			inscripcion.setPreferencial(false);
		}

		inscripciones.add(inscripcion);
	}

	public void liberarCuposPreferenciales() {

		if (estado != EstadoTorneo.EN_INSCRIPCION) {
			throw new ReglaDeNegocioException("Solo se pueden liberar cupos durante el periodo de inscripción");
		}

		preferencialesLiberados = true;
	}

	// INICIAR Y FINALIZAR
	public void iniciar() {

		if (estado != EstadoTorneo.EN_INSCRIPCION) {
			throw new ReglaDeNegocioException("El torneo debe estar en inscripción para poder iniciar");
		}

		if (inscripciones.size() < 2) {
			throw new ReglaDeNegocioException("Se necesitan al menos dos inscripciones para iniciar el torneo");
		}

		liberarCuposPreferenciales();

		estado = EstadoTorneo.EN_CURSO;
	}

	public void finalizar() {

		if (estado != EstadoTorneo.EN_CURSO) {
			throw new ReglaDeNegocioException("Solo se puede finalizar un torneo que esté en curso");
		}

		estado = EstadoTorneo.FINALIZADO;
	}

}

