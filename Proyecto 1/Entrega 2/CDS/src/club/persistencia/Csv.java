package club.persistencia;

import java.nio.file.Path;
import java.util.List;

/**
 * Utilidad comun de persistencia en CSV, usada por los tres modulos.
 *
 * Convencion: columnas separadas por ';', listas dentro de una celda separadas por ',',
 * UTF-8, primera fila de encabezado, fechas yyyy-MM-dd, horas HH:mm, booleanos true/false.
 */
public class Csv {

    public static final String SEPARADOR = ";";
    public static final String SEPARADOR_LISTA = ",";

    private Csv() {
    }

    /**
     * TODO (a mano): leer el archivo (UTF-8) y devolver las filas ya separadas por ';'.
     * Debe omitir la fila de encabezado y las lineas vacias.
     */
    public static List<String[]> leer(Path archivo) {
        throw new UnsupportedOperationException("TODO: Csv.leer");
    }

    /**
     * TODO (a mano): escribir el encabezado y luego cada fila unida con ';' (UTF-8).
     * Debe crear el archivo si no existe y sobrescribirlo si existe.
     */
    public static void escribir(Path archivo, String[] encabezado, List<String[]> filas) {
        throw new UnsupportedOperationException("TODO: Csv.escribir");
    }
}
