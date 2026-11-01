package club.modelo.conjunto;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * TODO (a mano). Rechazar con ReglaDeNegocioException si: el equipo esta inactivo,
     * esta lleno, la edad del socio no cabe en la categoria (categoria.admite),
     * la posicion no es del deporte (deporte.validaPosicion), la camiseta ya esta usada
     * o el socio ya es integrante activo de este equipo.
     * Si todo pasa, crear el IntegranteEquipo y agregarlo a la lista.
     */
    public void agregarIntegrante(Socio socio, Posicion posicion, int camiseta) {
        throw new UnsupportedOperationException("TODO: agregarIntegrante");
    }

    /** TODO (a mano). Buscar el integrante activo de ese socio y llamar a retirar(); rechazar si no esta. */
    public void retirarIntegrante(Socio socio) {
        throw new UnsupportedOperationException("TODO: retirarIntegrante");
    }

    /** TODO (a mano). true si los integrantes activos ya alcanzan cupoMaximo. */
    public boolean estaLleno() {
        throw new UnsupportedOperationException("TODO: estaLleno");
    }

    /** TODO (a mano). true si ningun integrante activo usa ese numero de camiseta. */
    public boolean camisetaDisponible(int camiseta) {
        throw new UnsupportedOperationException("TODO: camisetaDisponible");
    }

    /** TODO (a mano). Integrantes activos que ademas no estan suspendidos (apoyate en isHabilitado). */
    public List<IntegranteEquipo> getHabilitadosInterno() {
        throw new UnsupportedOperationException("TODO: getHabilitadosInterno");
    }

    /** TODO (a mano). Marcar el equipo como inactivo y retirar a todos sus integrantes activos. */
    public void darDeBaja() {
        throw new UnsupportedOperationException("TODO: darDeBaja");
    }
}