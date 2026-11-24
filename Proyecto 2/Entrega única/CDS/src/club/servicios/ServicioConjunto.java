package club.servicios;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import club.modelo.conjunto.Categoria;
import club.modelo.conjunto.Deporte;
import club.modelo.conjunto.Equipo;
import club.modelo.conjunto.Posicion;
import club.modelo.usuarios.Socio;
import club.modelo.conjunto.IntegranteEquipo;

/**
 * Servicio del módulo de deportes de conjunto. Es el único que conoce todos los equipos, por eso
 * valida las reglas que un Equipo no puede comprobar por sí solo y expone los contratos que usan
 * los otros módulos.
 *
 * Deportes: registrarDeporte guarda un deporte por su nombre; getDeporte lo busca (null si no
 * existe); getDeportes devuelve una copia de todos.
 *
 * Equipos: crearEquipo valida que los datos no sean nulos y que el cupo sea positivo, rechaza si
 * ya existe un equipo con el mismo nombre, deporte y categoría, y genera el id (EQ-001, EQ-002...).
 * agregarEquipoCargado registra un equipo leído del CSV conservando su id y ajusta el contador para
 * que los nuevos no repitan ids. darDeBajaEquipo marca el equipo como inactivo y retira a sus
 * integrantes, conservando el historial. getEquipo busca por id y devuelve null si no existe;
 * getEquipos devuelve una copia de la lista.
 *
 * Plantillas: inscribirJugador agrega un socio a un equipo y rechaza si ya pertenece a otro
 * equipo activo del mismo deporte (puede estar en equipos de deportes distintos); el resto de las
 * reglas (cupo, edad, posición, camiseta) las valida Equipo. retirarJugador retira al socio de un
 * equipo concreto.
 *
 * Consultas: getEquiposDe devuelve los equipos activos en los que el socio es integrante activo.
 *
 * Contratos con otros módulos: socioTieneEquipoActivo indica si el socio está en algún equipo
 * activo y retirarSocioDeTodosLosEquipos lo retira de todos; ambos los usa la baja de socio.
 * Los errores de negocio se lanzan como ReglaDeNegocioException.
 */
public class ServicioConjunto {

    private final Map<String, Deporte> deportes;  
    private final List<Equipo> equipos;
    private int contadorEquipos;        

    public ServicioConjunto() {
        this.deportes = new HashMap<>();
        this.equipos = new ArrayList<>();
        this.contadorEquipos = 0;
    }

    // ---------- Estructura ----------

    public void registrarDeporte(Deporte deporte) {
        if (deporte == null) {
            throw new ReglaDeNegocioException("El deporte no puede ser nulo");
        }
        deportes.put(deporte.getNombre(), deporte);
    }

    public Deporte getDeporte(String nombre) {
        return deportes.get(nombre);
    }

    public List<Deporte> getDeportes() {
        return new ArrayList<>(deportes.values());
    }

    public List<Equipo> getEquipos() {
        return new ArrayList<>(equipos);
    }

    // ---------- Equipos ----------

 
    public Equipo crearEquipo(String nombre, Deporte deporte, Categoria categoria, int cupoMaximo) {

        if (nombre == null || deporte == null || categoria == null) {
            throw new ReglaDeNegocioException("Los datos del equipo no pueden ser nulos");
        }

        if (cupoMaximo <= 0) {
            throw new ReglaDeNegocioException("El cupo máximo debe ser positivo");
        }

        for (Equipo equipo : equipos) {
            if (equipo.getNombre().equalsIgnoreCase(nombre)
                    && equipo.getDeporte().equals(deporte)
                    && equipo.getCategoria() == categoria) {

                throw new ReglaDeNegocioException("Ya existe un equipo con ese nombre, deporte y categoría");
            }
        }

        contadorEquipos++;

        String id = String.format("EQ-%03d", contadorEquipos);

        Equipo nuevo = new Equipo(id, nombre, deporte, categoria, cupoMaximo);

        equipos.add(nuevo);

        return nuevo;
    }

    public void agregarEquipoCargado(Equipo equipo) {
    	if (getEquipo(equipo.getId()) != null) {
    	    throw new ReglaDeNegocioException("Ya existe un equipo con el id " + equipo.getId());
    	}
        equipos.add(equipo);

        String id = equipo.getId();
        int numero = Integer.parseInt(id.substring(3));

        if (numero > contadorEquipos) {
            contadorEquipos = numero;
        }
    }

    public void darDeBajaEquipo(Equipo equipo) {
        if (equipo == null || !equipos.contains(equipo)) {
            throw new ReglaDeNegocioException("El equipo no existe en el servicio");
        }

        equipo.darDeBaja();
    }

    public Equipo getEquipo(String id) {
        for (Equipo equipo : equipos) {
            if (equipo.getId().equals(id)) {
                return equipo;
            }
        }

        return null;
    }

    // ---------- Plantillas ----------

    public void inscribirJugador(Equipo equipo, Socio socio, Posicion posicion, int numeroCamiseta) {

        if (equipo == null) {
            throw new ReglaDeNegocioException("El equipo no puede ser nulo");
        }
        if (socio == null) {
            throw new ReglaDeNegocioException("El socio no puede ser nulo");
        }
        if (posicion == null) {
            throw new ReglaDeNegocioException("La posición no puede ser nula");
        }
        if (!equipos.contains(equipo)) {
            throw new ReglaDeNegocioException("El equipo no pertenece al servicio");
        }
        for (Equipo equipoExistente : getEquiposDe(socio)) {

            if (equipoExistente == equipo) {
                throw new ReglaDeNegocioException("El socio ya pertenece a este equipo");
            }

            if (equipoExistente.getDeporte().getNombre()
                    .equalsIgnoreCase(equipo.getDeporte().getNombre())) {
                throw new ReglaDeNegocioException(
                        "El socio ya pertenece a otro equipo activo de este deporte");
            }
        }
        equipo.agregarIntegrante(socio, posicion, numeroCamiseta);
    }

    public void retirarJugador(Equipo equipo, Socio socio) {

        if (equipo == null || socio == null) {
            throw new ReglaDeNegocioException("El equipo y el socio no pueden ser nulos");
        }

        if (!equipos.contains(equipo)) {
            throw new ReglaDeNegocioException("El equipo no pertenece a este servicio");
        }

        equipo.retirarIntegrante(socio);
    }
    // ---------- Consultas ----------
    public List<Equipo> getEquiposDe(Socio socio) {
    	if (socio == null) {
    	    throw new ReglaDeNegocioException("El socio no puede ser nulo");
    	}
        List<Equipo> resultado = new ArrayList<>();

        for (Equipo equipo : equipos) {
            if (!equipo.isActivo()) {
                continue;
            }

            for (IntegranteEquipo integrante : equipo.getIntegrantesActivos()) {
                if (integrante.getSocio().getLogin().equals(socio.getLogin())) {
                    resultado.add(equipo);
                    break;
                }
            }
        }

        return resultado;
    }

    // ---------- Contratos con otros modulos ----------

    public boolean socioTieneEquipoActivo(Socio socio) {
        return !getEquiposDe(socio).isEmpty();
    }

    public void retirarSocioDeTodosLosEquipos(Socio socio) {
        List<Equipo> equiposSocio = getEquiposDe(socio);

        for (Equipo equipo : equiposSocio) {
            equipo.retirarIntegrante(socio);
        }
    }
}