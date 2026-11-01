package club.modelo.usuarios;

import club.servicios.ReglaDeNegocioException;
import java.util.ArrayList;
import modelo.ventas.venta;

public class Empleado extends Usuario implements Comprador {

    private String codigoDescuento;
    private int puntosFidelidad;
    private Socio socio; 
    private ArrayList<Turno> turnos;
    private ArrayList<SugerenciaProducto> sugerenciasProducto;
    private ArrayList<venta> ventas;
    private ArrayList<cambioTurno> cambiosTurno;

    public Empleado(String login, String nombre, String contrasenaHash, String codigoDescuento) {
        super(login, nombre, contrasenaHash);
        this.codigoDescuento = codigoDescuento;
        this.puntosFidelidad = 0;
        this.socio = null;
        this.turnos = new ArrayList<>();
        this.sugerenciasProducto = new ArrayList<>();
        this.ventas = new ArrayList<>();
        this.cambiosTurno = new ArrayList<>();
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
