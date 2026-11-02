package modelo.tienda;

import club.servicios.ReglaDeNegocioException;

public class Articulo {
	
	private String nombre;
	private String categoria;
	private double precio;
	private int cantidadDisponible;
	
	public Articulo(String nombre, String categoria, double precio, int cantidadDisponible) {
		super();
		this.nombre = nombre;
		this.categoria = categoria;
		this.setPrecio(precio);
		this.setCantidadDisponible(cantidadDisponible);
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}

	public double getPrecio() {
		return precio;
	}

	public void setPrecio(double precio) {
	    if (!Double.isFinite(precio) || precio < 0) {
	        throw new ReglaDeNegocioException(
	            "El precio debe ser un valor valido y no negativo");
	    }

	    this.precio = precio;
	}

	public int getCantidadDisponible() {
		return cantidadDisponible;
	}
	
	public void setCantidadDisponible(int cantidadDisponible) {
	    if (cantidadDisponible < 0) {
	        throw new ReglaDeNegocioException(
	            "La cantidad disponible no puede ser negativa");
	    }

	    this.cantidadDisponible = cantidadDisponible;
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



