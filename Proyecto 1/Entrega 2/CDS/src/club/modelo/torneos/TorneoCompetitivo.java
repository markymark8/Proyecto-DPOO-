package club.modelo.torneos;

import java.time.LocalDate;

import club.servicios.ReglaDeNegocioException;

public class TorneoCompetitivo extends Torneo {

    private final int tarifa;

    public TorneoCompetitivo(String id, String nombre,
                             Modalidad modalidad, String disciplina,
                             String categoria, int cupos,
                             LocalDate fecha, int tarifa) {

        super(id, nombre, modalidad, disciplina, categoria, cupos, fecha);

        if (tarifa <= 0) {
            throw new ReglaDeNegocioException(
                    "La tarifa de inscripción debe ser mayor que cero");
        }

        this.tarifa = tarifa;
    }

    @Override
    public int getTarifaInscripcion() {
        return tarifa;
    }

}