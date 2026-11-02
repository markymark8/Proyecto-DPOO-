package club.modelo.usuarios;
import java.util.ArrayList;
import club.servicios.ReglaDeNegocioException;
import java.util.Date;

public class Administrador extends Usuario {
	
	private ArrayList<cambioTurno> cambiosTurno;
	private ArrayList<SugerenciaProducto> sugerenciasProducto;

    public Administrador(String login, String nombre, String contrasenaHash) {
        super(login, nombre, contrasenaHash);
        this.cambiosTurno = new ArrayList<>();
        this.sugerenciasProducto = new ArrayList<>();
    }

    @Override
    public boolean esAdministrador() {
        return true;
    }
    
    public ArrayList<SugerenciaProducto> getSugerenciasProducto() {
        return sugerenciasProducto;
    }
    
    public void agregarSugerenciaProducto(SugerenciaProducto sugerencia) {
        if (sugerencia == null) {
            throw new ReglaDeNegocioException("La sugerencia no puede ser nula");
        }

        if (sugerenciasProducto.contains(sugerencia)) {
            throw new ReglaDeNegocioException("La sugerencia ya está registrada");
        }

        this.sugerenciasProducto.add(sugerencia);
    }
    
    public ArrayList<cambioTurno> getCambiosTurno() {
        return cambiosTurno;
    }
    
    public void agregarCambioTurno(cambioTurno cambio) {
        if (cambio == null) {
            throw new ReglaDeNegocioException("El cambio de turno no puede ser nulo");
        }

        if (cambiosTurno.contains(cambio)) {
            throw new ReglaDeNegocioException("El cambio de turno ya está registrado");
        }

        this.cambiosTurno.add(cambio);
    }
    
    public void rechazarCambioTurno(cambioTurno cambio) {
        if (cambio == null) {
            throw new ReglaDeNegocioException("El cambio de turno no puede ser nulo");
        }

        if (!cambiosTurno.contains(cambio)) {
            throw new ReglaDeNegocioException("El cambio de turno no está registrado");
        }

        if (!cambio.getEstado().equals("Pendiente")) {
            throw new ReglaDeNegocioException("Solo se pueden rechazar solicitudes pendientes");
        }

        cambio.setEstado("Rechazado");
    }
    

public void aprobarCambioTurno(cambioTurno cambio,
        ArrayList<Empleado> empleados) {

    if (cambio == null) {
        throw new ReglaDeNegocioException(
            "El cambio de turno no puede ser nulo");
    }

    if (!cambiosTurno.contains(cambio)) {
        throw new ReglaDeNegocioException(
            "El cambio de turno no está registrado");
    }

    if (!cambio.getEstado().equals("Pendiente")) {
        throw new ReglaDeNegocioException(
            "Solo se pueden aprobar solicitudes pendientes");
    }

    if (empleados == null) {
        throw new ReglaDeNegocioException(
            "La lista de empleados no puede ser nula");
    }

    Turno turnoOriginal = cambio.getTurno();

    if (turnoOriginal == null) {
        throw new ReglaDeNegocioException(
            "El turno original no puede ser nulo");
    }

    Empleado empleadoSolicitante = null;

    for (Empleado empleado : empleados) {
        if (empleadoSolicitante == null &&
            empleado.getTurnos().contains(turnoOriginal)) {
            empleadoSolicitante = empleado;
        }
    }

    if (empleadoSolicitante == null) {
        throw new ReglaDeNegocioException(
            "No se encontró al empleado responsable del turno");
    }

    Date fechaOriginal = turnoOriginal.getFecha();
    String horaInicioOriginal = turnoOriginal.getHoraInicio();
    String horaFinalOriginal = turnoOriginal.getHoraFinal();

    Date nuevaFecha = cambio.getNuevaFecha();
    String nuevaHoraInicio = cambio.getNuevaHoraInicio();
    String nuevaHoraFinal = cambio.getNuevaHoraFinal();

    if (nuevaFecha == null ||
        nuevaHoraInicio == null ||
        nuevaHoraFinal == null) {
        throw new ReglaDeNegocioException(
            "Los datos del nuevo turno no pueden ser nulos");
    }

    if (fechaOriginal.equals(nuevaFecha) &&
        horaInicioOriginal.equals(nuevaHoraInicio) &&
        horaFinalOriginal.equals(nuevaHoraFinal)) {
        throw new ReglaDeNegocioException(
            "El nuevo turno debe ser diferente al original");
    }

    if (nuevaHoraInicio.compareTo(nuevaHoraFinal) >= 0) {
        throw new ReglaDeNegocioException(
            "La hora de inicio debe ser anterior a la hora final");
    }

    
    ArrayList<String> horas = new ArrayList<>();

    horas.add(horaInicioOriginal);
    horas.add(horaFinalOriginal);

    if (fechaOriginal.equals(nuevaFecha)) {
        horas.add(nuevaHoraInicio);
        horas.add(nuevaHoraFinal);
    }

    for (Empleado empleado : empleados) {
        for (Turno turno : empleado.getTurnos()) {
            if (turno.getFecha().equals(fechaOriginal)) {
                horas.add(turno.getHoraInicio());
                horas.add(turno.getHoraFinal());
            }
        }
    }

    java.util.Collections.sort(horas);

    
    for (int i = 0; i < horas.size() - 1; i++) {

        String hora = horas.get(i);
        String siguienteHora = horas.get(i + 1);

        if (!hora.equals(siguienteHora) &&
            hora.compareTo(horaFinalOriginal) < 0 &&
            siguienteHora.compareTo(horaInicioOriginal) > 0) {

            int entrenadores = 0;
            int fisioterapeutas = 0;

            for (Empleado empleado : empleados) {

                boolean trabajando = false;

                for (Turno turno : empleado.getTurnos()) {

                    if (turno != turnoOriginal &&
                        turno.getFecha().equals(fechaOriginal) &&
                        turno.getHoraInicio().compareTo(hora) <= 0 &&
                        turno.getHoraFinal().compareTo(hora) > 0) {
                        trabajando = true;
                    }
                }

                // Considerar el nuevo horario del solicitante
                if (empleado == empleadoSolicitante &&
                    nuevaFecha.equals(fechaOriginal) &&
                    nuevaHoraInicio.compareTo(hora) <= 0 &&
                    nuevaHoraFinal.compareTo(hora) > 0) {
                    trabajando = true;
                }

                if (trabajando) {
                    if (empleado.getTipoEmpleado().equals("Entrenador")) {
                        entrenadores++;
                    }

                    if (empleado.getTipoEmpleado().equals("Fisioterapeuta")) {
                        fisioterapeutas++;
                    }
                }
            }

            if (entrenadores < 2 || fisioterapeutas < 1) {
                throw new ReglaDeNegocioException(
                    "No se puede aprobar el cambio porque no se cumple "
                    + "la cobertura mínima de 2 entrenadores "
                    + "y 1 fisioterapeuta");
            }
        }
    }
    turnoOriginal.actualizarHorario(
        nuevaFecha,
        nuevaHoraInicio,
        nuevaHoraFinal
    );

    cambio.setEstado("Aprobado");
    }
}

