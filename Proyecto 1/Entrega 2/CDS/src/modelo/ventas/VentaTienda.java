package modelo.ventas;

import java.util.Date;
import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Empleado;
import java.util.ArrayList;
import modelo.tienda.Articulo;

public class VentaTienda extends venta{
	
	private double iva;
	private ArrayList<Articulo> articulos;

	public VentaTienda(Date fecha, double total, double descuento, double puntos, double iva, Socio socio, Empleado empleado) {
		super(fecha, total, descuento, puntos, socio, empleado);
		this.iva = iva;
		this.articulos = new ArrayList<>();
}
	
	

}
