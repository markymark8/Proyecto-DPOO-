package club.modelo.conjunto;

public class AcumuladoJugador {

    private final String login;
    private final String nombre;
    private int partidos;
    private int a;
    private int b;

    public AcumuladoJugador(String login, String nombre) {
        this.login = login;
        this.nombre = nombre;
    }

    public void sumar(int valorA, int valorB) {
        partidos++;
        a += valorA;
        b += valorB;
    }

    public String getLogin() { return login; }
    public String getNombre() { return nombre; }
    public int getPartidos() { return partidos; }
    public int getA() { return a; }
    public int getB() { return b; }
}