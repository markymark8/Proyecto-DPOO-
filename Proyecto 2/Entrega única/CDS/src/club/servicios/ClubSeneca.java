package club.servicios;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Usuario;
import club.servicios.ReglaDeNegocioException;
import club.persistencia.RepositorioCompetencias;
import club.persistencia.RepositorioConjunto;
import club.persistencia.RepositorioPartidos;
import club.persistencia.RepositorioEstadisticas;
import club.persistencia.RepositorioComercio;
import club.persistencia.RepositorioTorneos;
import java.util.ArrayList;
import club.modelo.usuarios.Empleado;
import club.persistencia.RepositorioIndividual;

public class ClubSeneca {

	private final Map<String, Usuario> usuarios;

	private final ServicioConjunto servicioConjunto;
	private final ServicioPartidos servicioPartidos;
	private final ServicioCompetencias servicioCompetencias;
	private final ServicioComercio servicioComercio;
	private final ServicioIndividual servicioIndividual;
	private final ServicioTorneos servicioTorneos;
	private final ServicioInscripcion servicioInscripcion;

	public ClubSeneca() {
		this.usuarios = new HashMap<>();

		this.servicioConjunto = new ServicioConjunto();
		this.servicioPartidos = new ServicioPartidos();
		this.servicioCompetencias = new ServicioCompetencias(servicioPartidos);
		this.servicioComercio = new ServicioComercio();
		this.servicioIndividual = new ServicioIndividual(this);
		this.servicioTorneos = new ServicioTorneos(this);
		this.servicioInscripcion = new ServicioInscripcion(this);
	}

	public ServicioCompetencias getServicioCompetencias() {
		return servicioCompetencias;
	}

	public void registrarUsuario(Usuario usuario) {
		usuarios.put(usuario.getLogin(), usuario);
	}

	public Usuario buscarUsuario(String login) {
		return usuarios.get(login);
	}

	public ServicioConjunto getServicioConjunto() {
		return servicioConjunto;
	}

	public ServicioPartidos getServicioPartidos() {
		return servicioPartidos;
	}

	public ServicioComercio getServicioComercio() {
		return servicioComercio;
	}

	public ServicioIndividual getServicioIndividual() {
		return servicioIndividual;
	}

	public ServicioTorneos getServicioTorneos() {
		return servicioTorneos;
	}
	public ServicioInscripcion getServicioInscripcion() {
	    return servicioInscripcion;
	}

	public Socio getSocio(String login) {

		Usuario usuario = buscarUsuario(login);

		if (usuario == null) {
			throw new ReglaDeNegocioException("No existe el usuario " + login);
		}

		if (!(usuario instanceof Socio)) {
			throw new ReglaDeNegocioException("El usuario " + login + " no es un socio");
		}

		return (Socio) usuario;
	}

	// se debe cargar en este orden: usuarios, Deportes en conjunto (equipos,
	// competencias, etc.), lo individual, comercio. */
	public void cargar(Path carpetaDatos) {
		RepositorioConjunto.cargar(carpetaDatos, this);
		RepositorioPartidos.cargar(carpetaDatos, this);
		RepositorioCompetencias.cargar(carpetaDatos, this);
		RepositorioEstadisticas.cargar(carpetaDatos, this);
		RepositorioComercio.cargar(carpetaDatos, this);
		RepositorioTorneos.cargar(carpetaDatos, this);
		RepositorioIndividual.cargar(carpetaDatos, this);
		
	}

	// cada uno debería guardar sus propios archivod
	public void guardar(Path carpetaDatos) {
		RepositorioConjunto.guardar(carpetaDatos, this);
		RepositorioPartidos.guardar(carpetaDatos, this);
		RepositorioCompetencias.guardar(carpetaDatos, this);
		RepositorioEstadisticas.guardar(carpetaDatos, this);
		RepositorioComercio.guardar(carpetaDatos, this);
		 RepositorioTorneos.guardar(carpetaDatos, this);
		 RepositorioIndividual.guardar(carpetaDatos, this);
	}

	public ArrayList<Empleado> getEmpleados() {

		ArrayList<Empleado> empleados = new ArrayList<>();

		for (Usuario usuario : usuarios.values()) {

			if (usuario instanceof Empleado) {
				empleados.add((Empleado) usuario);
			}
		}

		return empleados;
	}

}