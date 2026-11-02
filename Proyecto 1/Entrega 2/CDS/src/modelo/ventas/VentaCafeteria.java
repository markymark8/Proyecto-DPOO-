package modelo.ventas;

import java.util.Date;
import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Empleado;
import java.util.ArrayList;
import modelo.cafeteria.ProductoCafeteria;
import club.servicios.ReglaDeNegocioException;
import club.modelo.usuarios.Comprador;
import modelo.cafeteria.Snack;
import modelo.cafeteria.Bebida;

public class VentaCafeteria extends venta {
	
	private double impuestoConsumo;
	private double propina;
	private ArrayList<ProductoCafeteria> productos;
	private ArrayList<Integer> cantidades;
	private boolean confirmada;
	private boolean alergenosInformados;
	
	public VentaCafeteria(Date fecha, double total, double descuento, double puntos, double impuestoConsumo, double propina, Socio socio, Empleado empleado) {
		super(fecha, total, descuento, puntos, socio, empleado);
		this.impuestoConsumo = impuestoConsumo;
		this.propina = propina;
		this.productos = new ArrayList<>();
		this.cantidades = new ArrayList<>();
		this.impuestoConsumo = 0.08;
		this.propina = 0.10;
		this.confirmada = false;
		this.alergenosInformados = false;
		}
	
		
		
		
public void agregarProducto(ProductoCafeteria producto, int cantidad) {

    if (confirmada) {
        throw new ReglaDeNegocioException(
            "No se pueden agregar productos a una venta confirmada");
    }

    if (producto == null) {
        throw new ReglaDeNegocioException(
            "El producto no puede ser nulo");
    }

    if (cantidad <= 0) {
        throw new ReglaDeNegocioException(
            "La cantidad debe ser mayor a cero");
    }

    int cantidadAcumulada = cantidad;

    for (int i = 0; i < productos.size(); i++) {
        if (productos.get(i) == producto) {
            cantidadAcumulada += cantidades.get(i);
        }
    }

    if (cantidadAcumulada > producto.getCantidadDisponible()) {
        throw new ReglaDeNegocioException(
            "No hay suficiente inventario");
    }

    productos.add(producto);
    cantidades.add(cantidad);
    
    this.alergenosInformados = false;
}


public double calcularSubtotal() {

    double subtotal = 0;

    for (int i = 0; i < productos.size(); i++) {
        ProductoCafeteria producto = productos.get(i);
        int cantidad = cantidades.get(i);

        subtotal += producto.getPrecio() * cantidad;
    }

    return subtotal;
}

public double calcularImpuestoConsumo() {
    return calcularSubtotal() * impuestoConsumo;
}

public double calcularPropina() {
    return calcularSubtotal() * propina;
}

public double calcularTotal() {
    return calcularSubtotal()
        + calcularImpuestoConsumo()
        + calcularPropina();
}



public void setPropina(double nuevaPropina) {

    if (confirmada) {
        throw new ReglaDeNegocioException(
            "No se puede modificar una venta confirmada");
    }

    if (!Double.isFinite(nuevaPropina) ||
        nuevaPropina < 0) {
        throw new ReglaDeNegocioException(
            "La propina no puede ser negativa ni invalida");
    }

    this.propina = nuevaPropina;
}



public void confirmarVenta(Comprador comprador, int puntosUsar) {

    if (confirmada) {
        throw new ReglaDeNegocioException(
            "La venta ya fue confirmada");
    }

    if (comprador == null) {
        throw new ReglaDeNegocioException(
            "El comprador no puede ser nulo");
    }

    if (productos.isEmpty()) {
        throw new ReglaDeNegocioException(
            "La venta debe tener al menos un producto");
        
    }  
        
boolean contieneSnack = false;

for (ProductoCafeteria producto : productos) {
    if (producto instanceof Snack) {
        contieneSnack = true;
    }
}

if (contieneSnack && !alergenosInformados) {
    throw new ReglaDeNegocioException(
        "Debe informar los alergenos antes de confirmar la venta");
}

    

    if (puntosUsar < 0 ||
        puntosUsar > comprador.getPuntosFidelidad()) {
        throw new ReglaDeNegocioException(
            "Cantidad de puntos invalida");
    }

    double totalVenta = calcularTotal();

    if (puntosUsar > totalVenta) {
        throw new ReglaDeNegocioException(
            "Los puntos superan el valor de la compra");
    }


    for (int i = 0; i < productos.size(); i++) {

        ProductoCafeteria producto = productos.get(i);
        int cantidadTotal = 0;

        for (int j = 0; j < productos.size(); j++) {
            if (productos.get(j) == producto) {
                cantidadTotal += cantidades.get(j);
            }
        }

        if (cantidadTotal > producto.getCantidadDisponible()) {
            throw new ReglaDeNegocioException(
                "Inventario insuficiente para "
                + producto.getNombre());
        }
    }


    double totalFinal = totalVenta - puntosUsar;


    for (int i = 0; i < productos.size(); i++) {
        productos.get(i).reducirStock(cantidades.get(i));
    }


    comprador.restarPuntos(puntosUsar);

    int puntosGanados = (int) (totalFinal * 0.02);
    comprador.sumarPuntos(puntosGanados);


    setTotal(totalFinal);
    setPuntos(puntosGanados);
    setDescuento(0);

    
    if (comprador instanceof Socio) {
        ((Socio) comprador).agregarVenta(this);
    } else if (comprador instanceof Empleado) {
        ((Empleado) comprador).agregarVenta(this);
    }

    { confirmada = true;
    
    }
}


public String consultarAlergenos() {

    String informacion = "";

    for (ProductoCafeteria producto : productos) {

        if (producto.getClass() == Snack.class) {

            Snack snack = (Snack) producto;

            informacion += snack.getNombre() + ": "
                    + snack.getAlergenos() + " ";
        }
    }

    return informacion;
}
    public boolean contieneBebidaCaliente() {

        for (ProductoCafeteria producto : productos) {

            if (producto.getClass() == Bebida.class) {

                Bebida bebida = (Bebida) producto;

                if (bebida.getTemperatura().equalsIgnoreCase("caliente")) {
                    return true;
                }
            }
        }

        return false;
    }


public void confirmarInformacionAlergenos() {

    if (confirmada) {
        throw new ReglaDeNegocioException(
            "La venta ya fue confirmada");
    }

    this.alergenosInformados = true;
}
}
