package modelo.ventas;
import java.util.Date;

public class venta {
	
	private Date fecha;
	private double total;
	private double descuento;
	private double puntos;
	
	public venta(Date fecha, double total, double descuento, double puntos) {
		super();
		this.fecha = fecha;
		this.total = total;
		this.descuento = descuento;
		this.puntos = puntos;
	}
	
	
	

}
