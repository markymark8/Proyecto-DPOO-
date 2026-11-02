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



public class ClubSeneca {

    private final Map<String, Usuario> usuarios;
    private final ServicioConjunto servicioConjunto;
    private final ServicioPartidos servicioPartidos;
    private final ServicioCompetencias servicioCompetencias;

    public ClubSeneca() {
        this.usuarios = new HashMap<>();
        this.servicioConjunto = new ServicioConjunto();
        this.servicioPartidos = new ServicioPartidos();
        this.servicioCompetencias = new ServicioCompetencias(servicioPartidos);
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
    private final ServicioIndividual servicioIndividual = new ServicioIndividual(this);

    public ServicioIndividual getServicioIndividual() {
        return servicioIndividual;
    }

    public Socio getSocio(String login) {

        Usuario usuario = buscarUsuario(login);

        if (usuario == null) {
            throw new ReglaDeNegocioException(
                "No existe el usuario " + login
            );
        }

        if (!(usuario instanceof Socio)) {
            throw new ReglaDeNegocioException(
                "El usuario " + login + " no es un socio"
            );
        }

        return (Socio) usuario;
    }
    

    // se debe cargar en este orden:  usuarios, Deportes en conjunto (equipos, competencias, etc.), lo individual, comercio. */
    public void cargar(Path carpetaDatos) {
    	 RepositorioConjunto.cargar(carpetaDatos, this);
         RepositorioPartidos.cargar(carpetaDatos, this);
         RepositorioCompetencias.cargar(carpetaDatos, this);
         RepositorioEstadisticas.cargar(carpetaDatos, this);
    }

     // cada uno debería guardar sus propios archivod
    public void guardar(Path carpetaDatos) {
    	RepositorioConjunto.guardar(carpetaDatos, this);
        RepositorioPartidos.guardar(carpetaDatos, this);
        RepositorioCompetencias.guardar(carpetaDatos, this);
        RepositorioEstadisticas.guardar(carpetaDatos, this);
    }
}