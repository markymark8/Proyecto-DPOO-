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
    if (puntosFidelidad < 0) {
        throw new ReglaDeNegocioException(
            "Los puntos de fidelidad no pueden ser negativos");
    }

    this.puntosFidelidad = puntosFidelidad;
}

    public Socio getSocio() {
        return socio;
    }
    
    public String getTipoEmpleado() {
        return "Empleado";
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
    
    public ArrayList<Turno> getTurnos() {
        return turnos;
    }
    
    public void agregarTurno(Turno turno) {
        if (turno == null) {
            throw new ReglaDeNegocioException("El turno no puede ser nulo");
        }

        if (turnos.contains(turno)) {
            throw new ReglaDeNegocioException("El turno ya está registrado");
        }

        this.turnos.add(turno);
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
    
    public ArrayList<venta> getVentas() {
        return ventas;
    }
    
    public void agregarVenta(venta nuevaVenta) {
        if (nuevaVenta == null) {
            throw new ReglaDeNegocioException("La venta no puede ser nula");
        }

        if (ventas.contains(nuevaVenta)) {
            throw new ReglaDeNegocioException("La venta ya está registrada");
        }

        this.ventas.add(nuevaVenta);
    }
}
