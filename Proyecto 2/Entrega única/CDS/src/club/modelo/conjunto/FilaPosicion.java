package club.modelo.conjunto;

// FILA DE LA TABLA DE POSICIONES
public class FilaPosicion {

    private final String nombre;
    private final boolean esClub;
    private int jugados;
    private int ganados;
    private int empatados;
    private int perdidos;
    private int golesFavor;
    private int golesContra;
    private int puntos;

    public FilaPosicion(String nombre, boolean esClub) {
        this.nombre = nombre;
        this.esClub = esClub;
    }

  // Suma del partido jugado
    public void registrar(int favor, int contra, int puntosGanados) {
        jugados++;
        golesFavor += favor;
        golesContra += contra;
        puntos += puntosGanados;
        if (favor > contra) {
            ganados++;
        } else if (favor == contra) {
            empatados++;
        } else {
            perdidos++;
        }
    }

    public String getNombre() { return nombre; }
    public boolean esClub() { return esClub; }
    public int getJugados() { return jugados; }
    public int getGanados() { return ganados; }
    public int getEmpatados() { return empatados; }
    public int getPerdidos() { return perdidos; }
    public int getGolesFavor() { return golesFavor; }
    public int getGolesContra() { return golesContra; }
    public int getDiferencia() { return golesFavor - golesContra; }
    public int getPuntos() { return puntos; }
}
