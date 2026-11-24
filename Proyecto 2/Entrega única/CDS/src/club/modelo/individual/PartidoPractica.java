package club.modelo.individual;

import club.servicios.ReglaDeNegocioException;

import java.util.ArrayList;
import java.util.List;

import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Usuario;


public class PartidoPractica extends Reserva {

    private  List<Socio> ladoA;
    private  List<Socio> ladoB;
    private  boolean jugado;
    private  List<EstadisticaIndividual> estadisticas;
    private  List<RegistroRanking> registros;

    public PartidoPractica(int id, Instalacion instalacion, BloqueHorario bloque, Usuario responsable,
            List<Socio> ladoA, List<Socio> ladoB, Modalidad modalidad) {
        super(id, instalacion, bloque, responsable, ladoA.size() + ladoB.size(), modalidad);
        this.ladoA = new ArrayList<>(ladoA);
        this.ladoB = new ArrayList<>(ladoB);
        this.jugado = false;
        this.estadisticas = new ArrayList<>();
        this.registros = new ArrayList<>();
    }

    public List<Socio> getLadoA() {
        return ladoA;
    }

    public List<Socio> getLadoB() {
        return ladoB;
    }

    public boolean isJugado() {
        return jugado;
    }

    public void setJugado(boolean jugado) {
        this.jugado = jugado;
    }

    public List<EstadisticaIndividual> getEstadisticas() {
        return estadisticas;
    }

    public List<RegistroRanking> getRegistros() {
        return registros;
    }

    /* MÉTODOS IMPLEMENTADOS*/

    public void agregarRegistro(RegistroRanking registro) {
        registros.add(registro);
    }


    public void registrarResultado(List<EstadisticaIndividual> nuevas) {
        if (jugado) {
            throw new ReglaDeNegocioException("El partido " + getId() + " ya fue jugado");
        }
        for (EstadisticaIndividual e : nuevas) {
            e.setPartido(this);
            estadisticas.add(e);
        }
        jugado = true;
        setEstado(EstadoReserva.FINALIZADA);
    }
}

