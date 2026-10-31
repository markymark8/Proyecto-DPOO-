package club.modelo.usuarios;

import club.servicios.ReglaDeNegocioException;

/**
 * Empleado del club (entrenadores y fisioterapeutas). Puede comprar y acumular puntos.
 * Es dueno del codigo de descuento: 15% para el, 8% para los socios que lo reciben.
 * Puede tener un Socio asociado (opcional).
 */
public class Empleado extends Usuario implements Comprador {

    private String codigoDescuento;
    private int puntosFidelidad;
    private Socio socio; 

    public Empleado(String login, String nombre, String contrasenaHash, String codigoDescuento) {
        super(login, nombre, contrasenaHash);
        this.codigoDescuento = codigoDescuento;
        this.puntosFidelidad = 0;
        this.socio = null;
    }

    public String getCodigoDescuento() {
        return codigoDescuento;
    }

    public void setCodigoDescuento(String codigoDescuento) {
        this.codigoDescuento = codigoDescuento;
    }

    @Override
    public int getPuntosFidelidad() {
        return puntosFidelidad;
    }

    public void setPuntosFidelidad(int puntosFidelidad) {
        this.puntosFidelidad = puntosFidelidad;
    }

    public Socio getSocio() {
        return socio;
    }


    public void vincularSocio(Socio socio) {
    	if (socio == null) {
            throw new ReglaDeNegocioException("El socio a vincular no puede ser nulo");
        }
        if (socio.getEmpleado() != null && socio.getEmpleado() != this) {
            throw new ReglaDeNegocioException("El socio ya esta vinculado a otro empleado");
        }
    	this.socio = socio;
    	socio.setEmpleado(this);
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
