package modelo.ventas;
import java.util.Date;
import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Empleado;

public class venta {
	
	private Date fecha;
	private double total;
	private double descuento;
	private double puntos;
	private Socio socio;
	private Empleado empleado;
	
	public venta(Date fecha, double total, double descuento, double puntos, Socio socio, Empleado empleado) {
		super();
		this.fecha = fecha;
		this.total = total;
		this.descuento = descuento;
		this.puntos = puntos;
		this.socio = socio;
	    this.empleado = empleado;
	}
	
	
	

}
