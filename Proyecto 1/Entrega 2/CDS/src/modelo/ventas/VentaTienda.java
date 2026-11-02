package modelo.ventas;

import java.util.Date;
import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Empleado;
import java.util.ArrayList;
import modelo.tienda.Articulo;
import club.servicios.ReglaDeNegocioException;
import club.modelo.usuarios.Comprador;
import club.modelo.usuarios.Empleado;

public class VentaTienda extends venta{
	
	private double iva;
	private ArrayList<Articulo> articulos;
	private ArrayList<Integer> cantidades;
	private double descuentoAplicado;
	private int puntosRedimidos;
	private boolean confirmada;

	public VentaTienda(Date fecha, double total, double descuento, double puntos, double iva, Socio socio, Empleado empleado) {
		super(fecha, total, descuento, puntos, socio, empleado);
		this.iva = iva;
		this.articulos = new ArrayList<>();
		this.cantidades = new ArrayList<>();
		this.iva = 0.19;
		this.descuentoAplicado = 0;
		this.puntosRedimidos = 0;
		this.confirmada = false;
}
	
	
public void agregarArticulo(Articulo articulo, int cantidad) {
	if (confirmada) {
	    throw new ReglaDeNegocioException(
	        "No se pueden agregar articulos a una venta confirmada");
	}
    if (articulo == null) {
        throw new ReglaDeNegocioException(
            "El articulo no puede ser nulo");
    }

    if (cantidad <= 0) {
        throw new ReglaDeNegocioException(
            "La cantidad debe ser mayor a cero");
    }

    int cantidadAcumulada = cantidad;

    for (int i = 0; i < articulos.size(); i++) {
        if (articulos.get(i) == articulo) {
            cantidadAcumulada += cantidades.get(i);
        }
    }

    if (cantidadAcumulada > articulo.getCantidadDisponible()) {
        throw new ReglaDeNegocioException(
            "No hay suficiente inventario");
    }

    articulos.add(articulo);
    cantidades.add(cantidad);
}

	public double calcularSubtotal() {

	    double subtotal = 0;

	    for (int i = 0; i < articulos.size(); i++) {

	        Articulo articulo = articulos.get(i);
	        int cantidad = cantidades.get(i);

	        subtotal += articulo.getPrecio() * cantidad;
	    }

	    return subtotal;
	}
	
	public double calcularIVA() {

	    double subtotal = calcularSubtotal();

	    return subtotal * iva;
	}
	
	public double calcularTotalConIVA() {

	    double subtotal = calcularSubtotal();
	    double impuesto = calcularIVA();

	    return subtotal + impuesto;
	}
	

public double calcularDescuento(Comprador comprador,
                                String codigoDescuento,
                                ArrayList<Empleado> empleados) {

    if (comprador == null) {
        throw new ReglaDeNegocioException(
            "El comprador no puede ser nulo");
    }

    double porcentaje = 0;

    if (comprador instanceof Empleado) {
        porcentaje = 0.15;
    } else if (codigoDescuento != null && empleados != null) {

        for (Empleado empleado : empleados) {
            if (empleado.getCodigoDescuento() != null &&
                empleado.getCodigoDescuento().equals(codigoDescuento)) {
                porcentaje = 0.08;
            }
        }
    }

    return calcularSubtotal() * porcentaje;
}


public double calcularTotalFinal(Comprador comprador,
                                String codigoDescuento,
                                ArrayList<Empleado> empleados,
                                int puntosUsar) {
	if (comprador == null) {
	    throw new ReglaDeNegocioException(
	        "El comprador no puede ser nulo");
	}
    if (puntosUsar < 0) {
        throw new ReglaDeNegocioException(
            "Los puntos no pueden ser negativos");
    }

    if (puntosUsar > comprador.getPuntosFidelidad()) {
        throw new ReglaDeNegocioException(
            "El comprador no tiene suficientes puntos");
    }

    double subtotal = calcularSubtotal();

    double descuento = calcularDescuento(
        comprador, codigoDescuento, empleados);

    double base = subtotal - descuento;
    double impuesto = base * 0.19;
    double total = base + impuesto;

    if (puntosUsar > total) {
        throw new ReglaDeNegocioException(
            "Los puntos superan el valor de la compra");
    }

    return total - puntosUsar;
}


public void confirmarVenta(Comprador comprador,
                          String codigoDescuento,
                          ArrayList<Empleado> empleados,
                          int puntosUsar) {

    if (confirmada) {
        throw new ReglaDeNegocioException(
            "Esta venta ya fue confirmada");
    }

    if (comprador == null) {
        throw new ReglaDeNegocioException(
            "El comprador no puede ser nulo");
    }

    if (articulos.isEmpty()) {
        throw new ReglaDeNegocioException(
            "La venta debe tener al menos un articulo");
    }

    // Verificar nuevamente el inventario
    for (int i = 0; i < articulos.size(); i++) {

        Articulo articulo = articulos.get(i);
        int cantidadTotal = 0;

        for (int j = 0; j < articulos.size(); j++) {
            if (articulos.get(j) == articulo) {
                cantidadTotal += cantidades.get(j);
            }
        }

        if (cantidadTotal > articulo.getCantidadDisponible()) {
            throw new ReglaDeNegocioException(
                "No hay suficiente inventario");
        }
    }

 
    double totalFinal = calcularTotalFinal(
        comprador, codigoDescuento, empleados, puntosUsar);

    double descuento = calcularDescuento(
        comprador, codigoDescuento, empleados);

  
    for (int i = 0; i < articulos.size(); i++) {
        articulos.get(i).reducirStock(cantidades.get(i));
    }


    comprador.restarPuntos(puntosUsar);

  
    int puntosGanados = (int) Math.floor(totalFinal * 0.02);
    comprador.sumarPuntos(puntosGanados);

 
    setTotal(totalFinal);
    setDescuento(descuento);
    setPuntos(puntosGanados);

    descuentoAplicado = descuento;
    puntosRedimidos = puntosUsar;


    if (comprador instanceof Socio) {
        ((Socio) comprador).agregarVenta(this);
    } else if (comprador instanceof Empleado) {
        ((Empleado) comprador).agregarVenta(this);
    }

    confirmada = true;
}

	

}
