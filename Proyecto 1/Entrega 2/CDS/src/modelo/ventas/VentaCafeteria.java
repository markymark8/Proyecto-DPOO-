package modelo.ventas;

import java.util.Date;

public class VentaCafeteria extends venta {
	
	private double impuestoConsumo;
	private double propina;
	
	public VentaCafeteria(Date fecha, double total, double descuento, double puntos, double impuestoConsumo,
			double propina) {
		super(fecha, total, descuento, puntos);
		this.impuestoConsumo = impuestoConsumo;
		this.propina = propina;
	}
	
	

}
