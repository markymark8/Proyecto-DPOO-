package club.servicios;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Usuario;
import club.servicios.ReglaDeNegocioException;
import club.persistencia.RepositorioConjunto;
import club.persistencia.RepositorioPartidos;



public class ClubSeneca {

    private final Map<String, Usuario> usuarios;
    private final ServicioConjunto servicioConjunto;
    private final ServicioPartidos servicioPartidos;

    public ClubSeneca() {
        this.usuarios = new HashMap<>();
        this.servicioConjunto = new ServicioConjunto();
        this.servicioPartidos = new ServicioPartidos();
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
    

    /** TODO: orden de carga: usuarios, modulo de Marco, modulo de Daniel, comercio. */
    public void cargar(Path carpetaDatos) {
    	 RepositorioConjunto.cargar(carpetaDatos, this);
         RepositorioPartidos.cargar(carpetaDatos, this);   
    }

    /** TODO: cada modulo guarda sus propios archivos. */
    public void guardar(Path carpetaDatos) {
    	RepositorioConjunto.guardar(carpetaDatos, this);
        RepositorioPartidos.guardar(carpetaDatos, this);
    }
}