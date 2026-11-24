package club.modelo.individual;


public enum Nivel {
    PRINCIPIANTE, INTERMEDIO, AVANZADO;

    /**
     Usamos .ordinal() dado que asigna un valor segun la posicion en el enum, principiante sería 0, intermedio 1, y avanzado 2, 
     con lo que es mucho más fácil manejar la adyacencia
     */
    public static boolean sonCompatibles(Nivel a, Nivel b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Los niveles no pueden ser null");
        }
        return Math.abs(a.ordinal() - b.ordinal()) <= 1;
    }
}
