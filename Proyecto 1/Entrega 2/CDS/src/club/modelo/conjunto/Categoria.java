package club.modelo.conjunto;

public enum Categoria {

    SUB_13(0, 13),
    SUB_15(14, 15),
    SUB_17(16, 17),
    MAYORES(18,999999999);

    private final int edadMinima;
    private final int edadMaxima;

    private Categoria(int edadMinima, int edadMaxima) {
        this.edadMinima = edadMinima;
        this.edadMaxima = edadMaxima;
    }

    public int getEdadMinima() {
        return edadMinima;
    }

    public int getEdadMaxima() {
        return edadMaxima;
    }

    public boolean admite(int edad) {
        return edad >= edadMinima && edad <= edadMaxima;
    }

    public static Categoria deEdad(int edad) {
        for (Categoria c : values()) {
            if (c.admite(edad)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Edad invalida: " + edad);
    }
}