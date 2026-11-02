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
	

public Date getFecha() {
    return fecha;
}

public double getTotal() {
    return total;
}

public double getDescuento() {
    return descuento;
}

public double getPuntos() {
    return puntos;
}

public Socio getSocio() {
    return socio;
}

public Empleado getEmpleado() {
    return empleado;
}

public void setTotal(double total) {
    if (!Double.isFinite(total) || total < 0) {
        throw new IllegalArgumentException(
            "El total no puede ser negativo ni invalido");
    }

    this.total = total;
}

public void setDescuento(double descuento) {
    if (!Double.isFinite(descuento) || descuento < 0) {
        throw new IllegalArgumentException(
            "El descuento no puede ser negativo ni invalido");
    }

    this.descuento = descuento;
}

public void setPuntos(double puntos) {
    if (!Double.isFinite(puntos) || puntos < 0) {
        throw new IllegalArgumentException(
            "Los puntos no pueden ser negativos ni invalidos");
    }

    this.puntos = puntos;
}
	

}
