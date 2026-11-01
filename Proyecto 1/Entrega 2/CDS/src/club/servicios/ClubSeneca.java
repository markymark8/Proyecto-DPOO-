package club.servicios;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Usuario;
import club.servicios.ReglaDeNegocioException;



public class ClubSeneca {

    private final Map<String, Usuario> usuarios;
    private final ServicioConjunto servicioConjunto;

    public ClubSeneca() {
        this.usuarios = new HashMap<>();
        this.servicioConjunto = new ServicioConjunto();
    }

    public void registrarUsuario(Usuario usuario) {
        usuarios.put(usuario.getLogin(), usuario);
    }

    /** Devuelve el usuario con ese login, o null si no existe. */
    public Usuario buscarUsuario(String login) {
        return usuarios.get(login);
    }

    /** Servicio del modulo de deportes de conjunto. */
    public ServicioConjunto getServicioConjunto() {       
        return servicioConjunto;                            
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
        // TODO
    }

    /** TODO: cada modulo guarda sus propios archivos. */
    public void guardar(Path carpetaDatos) {
        // TODO
    }
}