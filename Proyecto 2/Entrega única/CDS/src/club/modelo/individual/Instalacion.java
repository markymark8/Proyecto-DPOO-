package club.modelo.individual;

public class Instalacion {

    private int codigo;
    private String nombre;
    private String disciplina;
    private int capacidad;
    private boolean techada;
    private boolean habilitada;

    public Instalacion(int codigo, String nombre, String disciplina, int capacidad,
            boolean techada, boolean habilitada) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.disciplina = disciplina;
        this.capacidad = capacidad;
        this.techada = techada;
        this.habilitada = habilitada;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDisciplina() {
        return disciplina;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public boolean isTechada() {
        return techada;
    }

    public boolean isHabilitada() {
        return habilitada;
    }

    public void deshabilitar() {
        this.habilitada = false;
    }

    public void habilitar() {
        this.habilitada = true;
    }

    /**
     MÈTODOS IMPLEMENTADOS, verfica que la capacidad sea congruente con la modalidad que se va a jugar
     */
    public boolean admite(int jugadores, Modalidad modalidad) {
        if (modalidad == null) {
            return false;
        }
        int esperado = (modalidad == Modalidad.SENCILLOS) ? 2 : 4;
        if (jugadores != esperado || jugadores > capacidad) {
            return false;
        }
        if ("PADEL".equals(disciplina)) {
            return modalidad == Modalidad.DOBLES && jugadores == 4;
        }
        return true;
    }

    
}

