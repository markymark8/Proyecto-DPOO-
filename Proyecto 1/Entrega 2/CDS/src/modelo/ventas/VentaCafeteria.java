package modelo.ventas;

import java.util.Date;
import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Empleado;
import java.util.ArrayList;
import modelo.cafeteria.ProductoCafeteria;

public class VentaCafeteria extends venta {
	
	private double impuestoConsumo;
	private double propina;
	private ArrayList<ProductoCafeteria> productos;
	
	public VentaCafeteria(Date fecha, double total, double descuento, double puntos, double impuestoConsumo, double propina, Socio socio, Empleado empleado) {
		super(fecha, total, descuento, puntos, socio, empleado);
		this.impuestoConsumo = impuestoConsumo;
		this.propina = propina;
		this.productos = new ArrayList<>();
}
	
	

}
