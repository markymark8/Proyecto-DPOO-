package club.modelo.tienda;

public class Articulo {
	
	private String nombre;
	private String categoria;
	private double precio;
	private int cantidadDisponible;
	
	public Articulo(String nombre, String categoria, double precio, int cantidadDisponible) {
		super();
		this.nombre = nombre;
		this.categoria = categoria;
		this.precio = precio;
		this.cantidadDisponible = cantidadDisponible;
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
	    if (precio >= 0) {
	        this.precio = precio;
	    }
	}

	public int getCantidadDisponible() {
		return cantidadDisponible;
	}
	
	public void setCantidadDisponible(int cantidadDisponible) {
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



