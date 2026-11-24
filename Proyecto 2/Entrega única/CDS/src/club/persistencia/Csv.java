package club.persistencia;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List; 

public class Csv {

    public static final String SEPARADOR = ";";
    public static final String SEPARADOR_LISTA = ",";

    private Csv() {
    }

    public static List<String[]> leer(Path archivo) {
        List<String[]> filas = new ArrayList<>();
        if (!Files.exists(archivo)) {
            return filas;
        }
        try (BufferedReader lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            String linea = lector.readLine();
            while ((linea = lector.readLine()) != null) {
                if (!linea.isBlank()) {
                    filas.add(linea.split(";", -1));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer " + archivo, e);
        }
        return filas;
    }

    public static void escribir(Path archivo, String[] encabezado, List<String[]> filas) {
    	try {
            Files.createDirectories(archivo.getParent());
            try (BufferedWriter w = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8)) {
                w.write(String.join(SEPARADOR, encabezado));
                w.newLine();
                for (String[] fila : filas) {
                    w.write(String.join(SEPARADOR, fila));
                    w.newLine();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo escribir " + archivo, e);
    }
}}
