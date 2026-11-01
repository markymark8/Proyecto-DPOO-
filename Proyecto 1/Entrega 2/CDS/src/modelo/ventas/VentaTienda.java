package modelo.ventas;

import java.util.Date;

public class VentaTienda extends venta{
	
	private double iva;

	public VentaTienda(Date fecha, double total, double descuento, double puntos, double iva) {
		super(fecha, total, descuento, puntos);
		this.iva = iva;
	}
	
	

}
