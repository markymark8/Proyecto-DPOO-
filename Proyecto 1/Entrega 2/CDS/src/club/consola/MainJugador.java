package club.consola;

import club.modelo.usuarios.Socio;

public class MainJugador {

    public static void main(String[] args) {
        ArranqueClub.ejecutar("jugadores (socios)", u -> u instanceof Socio);
    }
}