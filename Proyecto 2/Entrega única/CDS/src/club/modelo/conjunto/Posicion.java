package club.modelo.conjunto;

public enum Posicion {

    PORTERO("FUTBOL"),
    DEFENSA("FUTBOL"),
    MEDIOCAMPISTA("FUTBOL"),
    DELANTERO("FUTBOL"),


    BASE("BASKET"),
    ESCOLTA("BASKET"),
    ALERO("BASKET"),
    ALA_PIVOT("BASKET"),
    PIVOT("BASKET"),


    ARMADOR("VOLEIBOL"),
    CENTRAL("VOLEIBOL"),
    OPUESTO("VOLEIBOL"),
    LIBERO("VOLEIBOL"),
    PUNTA("VOLEIBOL");

    private final String deporte;

    private Posicion(String deporte) {
        this.deporte = deporte;
    }

    public String getDeporte() {
        return deporte;
    }


    public boolean esDe(String nombreDeporte) {
        return deporte.equalsIgnoreCase(nombreDeporte);
    }
}