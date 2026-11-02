package club.modelo.conjunto;

import club.servicios.ReglaDeNegocioException;


// no modelamos plantilla externa, solo nombre
public class InstitucionRival {

    private final String nombre;

    public InstitucionRival(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDeNegocioException("El nombre de la institución rival es obligatorio");
        }
        // ';' y ',' son separadores del CSV: no pueden venir dentro del nombre (recomendación de la IA)
        if (nombre.contains(";") || nombre.contains(",")) {
            throw new ReglaDeNegocioException("El nombre de la institución no puede contener ';' ni ','");
        }
        this.nombre = nombre.trim();
    }

    public String getNombre() {
        return nombre;
    }

    public boolean tieneNombre(String otro) {
        return otro != null && nombre.equalsIgnoreCase(otro.trim());
    }
}