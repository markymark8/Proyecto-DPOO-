package club.modelo.usuarios;

import java.time.LocalDate;
import java.time.Period;
import club.servicios.ReglaDeNegocioException;

/**
 * Socio del club. Es tambien el "jugador": no existe una clase Jugador.
 * Puede estar asociado (opcionalmente) a un Empleado.
 */
public class Socio extends Usuario implements Comprador {

    private LocalDate fechaNacimiento;
    private int puntosFidelidad;
    private boolean bebidaCaliente;
    private boolean activo;
    private Empleado empleado; // puede ser null

    public Socio(String login, String nombre, String contrasenaHash, LocalDate fechaNacimiento) {
        super(login, nombre, contrasenaHash);
        this.fechaNacimiento = fechaNacimiento;
        this.puntosFidelidad = 0;
        this.bebidaCaliente = false;
        this.activo = true;
        this.empleado = null;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    @Override
    public int getPuntosFidelidad() {
        return puntosFidelidad;
    }

    public void setPuntosFidelidad(int puntosFidelidad) {
        this.puntosFidelidad = puntosFidelidad;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public boolean tieneBebidaCaliente() {
        return bebidaCaliente;
    }

    public void marcarBebidaCaliente(boolean bebidaCaliente) {
        this.bebidaCaliente = bebidaCaliente;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public int getEdad(LocalDate hoy) {
    	 if (hoy.isBefore(fechaNacimiento)) {
    	        throw new ReglaDeNegocioException("La fecha de consulta es anterior a la fecha de nacimiento");
    	    }
    	Period diferencia = Period.between(fechaNacimiento, hoy);
    	int edad = diferencia.getYears();
    	return edad; 
    }

  
    public boolean esEntrenador() {
        return empleado !=null && empleado instanceof Entrenador;
    }

    @Override
    public void sumarPuntos(int puntos) {
    	if (puntos < 0) {
    		throw new ReglaDeNegocioException("La cantidad de puntos a sumar debe ser positiva.");
    	}
    	puntosFidelidad += puntos;
    }
    
    
    @Override
    public void restarPuntos(int puntos) {
    	if (puntos < 0) {
    		
    		throw new ReglaDeNegocioException("La cantidad de puntos a restar debe ser positiva.");
    	}
    	if (puntos > puntosFidelidad) {
    		throw new ReglaDeNegocioException("Saldo insuficiente: intenta restar " + puntos + " y solo tiene " + puntosFidelidad + " puntos");
    	}
    	
    	puntosFidelidad -= puntos;
       
    }
}
