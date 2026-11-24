package modelo.cafeteria;
import club.servicios.ReglaDeNegocioException;

public class ProductoCafeteria {
	
	private String nombre;
	private double precio;
	private int cantidadDisponible;
	
	public ProductoCafeteria(String nombre, double precio,
	        int cantidadDisponible) {

	    if (nombre == null || nombre.trim().isEmpty()) {
	        throw new ReglaDeNegocioException(
	            "El nombre no puede estar vacio");
	    }

	    if (!Double.isFinite(precio) || precio < 0) {
	        throw new ReglaDeNegocioException(
	            "El precio no puede ser negativo ni invalido");
	    }

	    if (cantidadDisponible < 0) {
	        throw new ReglaDeNegocioException(
	            "La cantidad disponible no puede ser negativa");
	    }

	    this.nombre = nombre;
	    this.precio = precio;
	    this.cantidadDisponible = cantidadDisponible;
	}
	

public String getNombre() {
    return nombre;
}

public double getPrecio() {
    return precio;
}

public int getCantidadDisponible() {
    return cantidadDisponible;
}

public boolean reducirStock(int cantidad) {
    if (cantidad > 0 && cantidad <= cantidadDisponible) {
        cantidadDisponible -= cantidad;
        return true;
    }

    return false;
}

public boolean aumentarStock(int cantidad) {
    if (cantidad > 0) {
        cantidadDisponible += cantidad;
        return true;
    }

    return false;
}
	
	
	

}
