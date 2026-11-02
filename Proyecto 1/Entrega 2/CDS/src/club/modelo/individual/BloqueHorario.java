package club.modelo.individual;

import java.time.Duration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


public class BloqueHorario {

    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;

    public BloqueHorario(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    /**
     MÈTODOS IMPLEMENTADOS: se solpa con() verifica que dos bloques horarios no se crucen entre si, 
     contiene() verifica si una hora especifica esta dentro de un bloque horario
     */
    public boolean seSolapaCon(BloqueHorario otro) {
        return fecha.equals(otro.fecha)
                && horaInicio.isBefore(otro.horaFin)
                && otro.horaInicio.isBefore(horaFin);
    }

    
    public boolean contiene(LocalDateTime instante) {
        return !instante.isBefore(getInicio()) && instante.isBefore(getFin());
    }

    
    public long getDuracionMinutos() {
        return Duration.between(horaInicio, horaFin).toMinutes();
    }

    public LocalDateTime getInicio() {
        return LocalDateTime.of(fecha, horaInicio);
    }

    public LocalDateTime getFin() {
        return LocalDateTime.of(fecha, horaFin);
    }
}
