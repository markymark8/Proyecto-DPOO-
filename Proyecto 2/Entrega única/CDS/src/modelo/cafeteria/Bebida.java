package modelo.cafeteria;

public class Bebida extends ProductoCafeteria {
	
	private String temperatura;

	public Bebida(String nombre, double precio, int cantidadDisponible, String temperatura) {
		super(nombre, precio, cantidadDisponible);
		this.temperatura = temperatura;
	}
	

	public String getTemperatura() {
    return temperatura;
}

	public boolean esCaliente() {
    return temperatura != null &&
           temperatura.equalsIgnoreCase("Caliente");
}
	

}
