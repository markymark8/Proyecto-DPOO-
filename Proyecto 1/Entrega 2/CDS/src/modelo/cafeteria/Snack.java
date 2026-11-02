package modelo.cafeteria;

public class Snack extends ProductoCafeteria {
	
	private String alergenos;

	public Snack(String nombre, double precio, int cantidadDisponible, String alergenos) {
		super(nombre, precio, cantidadDisponible);
		this.alergenos = alergenos;
	}
	
	public String getAlergenos() {
	    return alergenos;
	}

}
