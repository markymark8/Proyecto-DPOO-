package club.modelo.conjunto;

import java.util.ArrayList;
import club.servicios.ReglaDeNegocioException;
import java.util.List;
import java.time.LocalDate;

import club.modelo.usuarios.Socio;

public class Equipo {

    private final String id;
    private String nombre;
    private Deporte deporte;
    private Categoria categoria;
    private int cupoMaximo;
    private boolean activo;
    private final List<IntegranteEquipo> integrantes;

    public Equipo(String id, String nombre, Deporte deporte, Categoria categoria, int cupoMaximo) {
        this.id = id;
        this.nombre = nombre;
        this.deporte = deporte;
        this.categoria = categoria;
        this.cupoMaximo = cupoMaximo;
        this.activo = true;
        this.integrantes = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Deporte getDeporte() {
        return deporte;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public int getCupoMaximo() {
        return cupoMaximo;
    }

    public boolean isActivo() {
        return activo;
    }

    public List<IntegranteEquipo> getIntegrantes() {
        return integrantes;
    }

    public List<IntegranteEquipo> getIntegrantesActivos() {
        List<IntegranteEquipo> activos = new ArrayList<>();

        for (IntegranteEquipo integrante : integrantes) {
            if (integrante.isActivo()) {
                activos.add(integrante);
            }
        }

        return activos;
    }

    public boolean tieneMinimoHabilitados() {
        int cantidad = 0;

        for (IntegranteEquipo integrante : getIntegrantesActivos()) {
            if (integrante.isHabilitado()) {
                cantidad++;
            }
        }
        return cantidad >= deporte.getMinJugadores();
    }

    public void agregarIntegrante(Socio socio, Posicion posicion, int camiseta) {
    	
    	if (socio == null) {
    	    throw new ReglaDeNegocioException("El socio no puede ser nulo");
    	}

        if (!activo) {
            throw new ReglaDeNegocioException("El equipo está inactivo");
        }

        if (estaLleno()) {
            throw new ReglaDeNegocioException("El equipo está lleno");
        }

        int edad = socio.getEdad(LocalDate.now());

        if (!categoria.admite(edad)) {
            throw new ReglaDeNegocioException("El socio no cumple con la edad de la categoría");
        }

        if (!deporte.validaPosicion(posicion)) {
            throw new ReglaDeNegocioException("La posición no corresponde al deporte");
        }

        if (!camisetaDisponible(camiseta)) {
            throw new ReglaDeNegocioException("El número de camiseta ya está ocupado");
        }

        for (IntegranteEquipo integrante : getIntegrantesActivos()) {
            if (integrante.getSocio().getLogin().equals(socio.getLogin())) {
                throw new ReglaDeNegocioException("El socio ya pertenece a este equipo");
            }
        }

        IntegranteEquipo nuevo = new IntegranteEquipo(
            socio,
            posicion,
            camiseta,
            LocalDate.now()
        );

        integrantes.add(nuevo);
    }

    public void retirarIntegrante(Socio socio) {
        for (IntegranteEquipo integrante : integrantes) {
            if (integrante.isActivo()
                    && integrante.getSocio().getLogin().equals(socio.getLogin())) {

                integrante.retirar();
                return;
            }
        }
        throw new ReglaDeNegocioException("El socio no es integrante activo de este equipo");
    }

    public boolean estaLleno() {
    	return getIntegrantesActivos().size() >= cupoMaximo;
    }

    public boolean camisetaDisponible(int camiseta) {
    	if (camiseta <= 0) {
    	    throw new ReglaDeNegocioException("El número de camiseta debe ser positivo");
    	}
        for (IntegranteEquipo integrante : getIntegrantesActivos()) {
            if (integrante.getNumeroCamiseta() == camiseta) {
                return false;
            }
        }
        return true;
    }

    public List<IntegranteEquipo> getHabilitadosInterno() {
        List<IntegranteEquipo> habilitados = new ArrayList<>();

        for (IntegranteEquipo integrante : integrantes) {
            if (integrante.isHabilitado()) {
                habilitados.add(integrante);
            }
        }
        return habilitados;
    }

    public void darDeBaja() {
    	if (!activo) {
            throw new ReglaDeNegocioException("El equipo ya está inactivo");
        }
        activo = false;

        for (IntegranteEquipo integrante : integrantes) {
            if (integrante.isActivo()) {
                integrante.retirar();
            }
        }
    }
}