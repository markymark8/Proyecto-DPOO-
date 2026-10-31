package club.modelo.conjunto;

public class Deporte {

    private final String nombre;
    private final int minJugadores;
    private final int puntosVictoria;
    private final int puntosEmpate;
    private final int puntosDerrota;
    private final boolean permiteEmpate;
    private final boolean partidoPorSets;
    private final boolean usaTarjetas;

    public Deporte(String nombre, int minJugadores, int puntosVictoria, int puntosEmpate,
            int puntosDerrota, boolean permiteEmpate, boolean partidoPorSets, boolean usaTarjetas) {
        this.nombre = nombre;
        this.minJugadores = minJugadores;
        this.puntosVictoria = puntosVictoria;
        this.puntosEmpate = puntosEmpate;
        this.puntosDerrota = puntosDerrota;
        this.permiteEmpate = permiteEmpate;
        this.partidoPorSets = partidoPorSets;
        this.usaTarjetas = usaTarjetas;
    }

    public String getNombre() { return nombre; }
    public int getMinJugadores() { return minJugadores; }
    public int getPuntosVictoria() { return puntosVictoria; }
    public int getPuntosEmpate() { return puntosEmpate; }
    public int getPuntosDerrota() { return puntosDerrota; }
    public boolean permiteEmpate() { return permiteEmpate; }
    public boolean usaSets() { return partidoPorSets; }
    public boolean usaTarjetas() { return usaTarjetas; }

    public boolean validaPosicion(Posicion posicion) {
    	return posicion != null && posicion.esDe(nombre);
    }

    public boolean validaMarcador(int marcadorA, int marcadorB) {
    	if (marcadorA < 0 || marcadorB < 0) {
    	    return false;
    	}

    	if (!permiteEmpate && marcadorA == marcadorB) {
    	    return false;
    	}
    	
    	return true;
    }
    public int puntosPara(int marcadorPropio, int marcadorRival) {
    	 if (marcadorPropio > marcadorRival) {
    	        return puntosVictoria;
    	    }

    	    if (marcadorPropio == marcadorRival) {
    	        return puntosEmpate;
    	    }

    	    return puntosDerrota;
    }
}