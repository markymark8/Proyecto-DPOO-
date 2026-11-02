package club.servicios;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Usuario;

/**
 * Raiz del sistema: une los tres modulos, carga y guarda todo desde la carpeta de datos.
 *
 * Por ahora solo conoce a los usuarios. Cada integrante agrega aqui su servicio
 * (ServicioConjunto, ServicioRaqueta, ...) cuando exista y compile.
 */
public class ClubSeneca {

private final Map<String, Usuario> usuarios;
private final ServicioComercio servicioComercio;

public ClubSeneca() {
    this.usuarios = new HashMap<>();
    this.servicioComercio = new ServicioComercio();
}

    public void registrarUsuario(Usuario usuario) {
        usuarios.put(usuario.getLogin(), usuario);
    }

    /** Devuelve el usuario con ese login, o null si no existe. */
    public Usuario buscarUsuario(String login) {
        return usuarios.get(login);
    }
    public ServicioComercio getServicioComercio() {
        return servicioComercio;
    }
    /** TODO (a mano): devolver el Socio con ese login; si no existe o no es Socio, lanzar ReglaDeNegocioException. */
    public Socio getSocio(String login) {
        throw new UnsupportedOperationException("TODO: getSocio");
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
