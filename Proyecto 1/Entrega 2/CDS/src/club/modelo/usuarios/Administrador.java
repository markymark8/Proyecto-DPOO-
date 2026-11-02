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

    
    boolean esEntrenador = empleadoSolicitante.getTipoEmpleado().equals("Entrenador");

    boolean esFisioterapeuta = empleadoSolicitante.getTipoEmpleado().equals("Fisioterapeuta");

    
    Date fechaOriginal = turnoOriginal.getFecha();
    String horaInicioOriginal = turnoOriginal.getHoraInicio();
    String horaFinalOriginal = turnoOriginal.getHoraFinal();

    Date nuevaFecha = cambio.getNuevaFecha();
    String nuevaHoraInicio = cambio.getNuevaHoraInicio();
    String nuevaHoraFinal = cambio.getNuevaHoraFinal();
   
    int entrenadores = contarEmpleados(
    	    empleados, fechaOriginal, horaInicioOriginal, "Entrenador");

    int fisioterapeutas = contarEmpleados(
    	    empleados, fechaOriginal, horaInicioOriginal, "Fisioterapeuta");
    

    if (horaInicioOriginal.equals(nuevaHoraInicio) &&
    	    horaFinalOriginal.equals(nuevaHoraFinal) &&
    	    fechaOriginal.equals(nuevaFecha)) {

    	    throw new ReglaDeNegocioException(
    	        "El nuevo turno debe ser diferente al original");
    	}


    if (nuevaHoraInicio.compareTo(nuevaHoraFinal) >= 0) {
        throw new ReglaDeNegocioException(
            "La hora de inicio debe ser anterior a la hora final");
    }

    boolean cubreHoraOriginal =
    	    fechaOriginal.equals(nuevaFecha) &&
    	    nuevaHoraInicio.compareTo(horaInicioOriginal) <= 0 &&
    	    nuevaHoraFinal.compareTo(horaInicioOriginal) > 0;

    	if (!cubreHoraOriginal) {

    		if (esEntrenador && entrenadores <= 2) {
    	        throw new ReglaDeNegocioException(
    	            "No se puede aprobar el cambio porque quedaría sin entrenador");
    	    }

    	    if (esFisioterapeuta && fisioterapeutas <= 2) {
    	        throw new ReglaDeNegocioException(
    	            "No se puede aprobar el cambio porque quedaría sin fisioterapeuta");
    	    }
    	}
    

    }
    
    
    private int contarEmpleados(
            ArrayList<Empleado> empleados,
            Date fecha,
            String hora,
            String tipoEmpleado) {

        int cantidad = 0;

        for (Empleado empleado : empleados) {

            boolean estaTrabajando = false;

            for (Turno turno : empleado.getTurnos()) {

                if (turno.getFecha().equals(fecha) &&
                    turno.getHoraInicio().compareTo(hora) <= 0 &&
                    turno.getHoraFinal().compareTo(hora) > 0) {

                    estaTrabajando = true;
                }
            }

            if (estaTrabajando &&
                empleado.getTipoEmpleado().equals(tipoEmpleado)) {

                cantidad++;
            }
        }

        return cantidad;
    }
    
    private void validarCobertura(
            ArrayList<Empleado> empleados,
            Date fecha,
            String horaInicio,
            String horaFinal) {

        String hora = horaInicio;

        while (hora.compareTo(horaFinal) < 0) {

            int entrenadores = contarEmpleados(
                empleados, fecha, hora, "Entrenador");

            int fisioterapeutas = contarEmpleados(
                empleados, fecha, hora, "Fisioterapeuta");

            if (entrenadores < 2 || fisioterapeutas < 1) {
                throw new ReglaDeNegocioException(
                    "No se cumple la cobertura minima de empleados");
            }

            // Aquí falta avanzar a la siguiente hora.
        }
    }
}
