package club.consola;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import club.persistencia.Csv;

public class PruebaCsv {

    private static int fallas = 0;

    private static void verificar(String descripcion, boolean condicion) {
        System.out.println((condicion ? "OK     " : "FALLA  ") + descripcion);
        if (!condicion) {
            fallas++;
        }
    }

    public static void main(String[] args) throws Exception {
        Path archivo = Path.of("datos", "prueba", "prueba.csv");
        String[] encabezado = { "id", "nombre", "activo", "fecha" };

        List<String[]> filas = new ArrayList<>();
        filas.add(new String[] { "1", "Ana Perez", "true", "2026-10-31" });
        filas.add(new String[] { "2", "Luis Gomez", "false", "2026-11-01" });
        filas.add(new String[] { "3", "", "true", "" });   // columnas vacias

        System.out.println("Ruta: " + archivo.toAbsolutePath());

        System.out.println("=== Escribir ===");
        Csv.escribir(archivo, encabezado, filas);
        verificar("El archivo existe", Files.exists(archivo));
        verificar("Tiene 4 lineas (encabezado + 3 filas)", Files.readAllLines(archivo).size() == 4);
        verificar("Primera linea es el encabezado", Files.readAllLines(archivo).get(0).equals("id;nombre;activo;fecha"));

        System.out.println("=== Leer ===");
        List<String[]> leidas = Csv.leer(archivo);
        verificar("Lee 3 filas (sin encabezado)", leidas.size() == 3);
        verificar("Cada fila tiene 4 columnas", leidas.get(0).length == 4 && leidas.get(2).length == 4);
        verificar("Valor de texto", leidas.get(0)[1].equals("Ana Perez"));
        verificar("Valor booleano", !Boolean.parseBoolean(leidas.get(1)[2]));
        verificar("Columna vacia se conserva", leidas.get(2)[1].isEmpty() && leidas.get(2)[3].isEmpty());

        System.out.println("=== Sobrescribir ===");
        Csv.escribir(archivo, encabezado, filas.subList(0, 1));
        verificar("Reescribir deja solo 1 fila", Csv.leer(archivo).size() == 1);

        System.out.println("=== Archivo inexistente ===");
        List<String[]> vacio = Csv.leer(Path.of("datos", "prueba", "no_existe.csv"));
        verificar("Archivo que no existe devuelve lista vacia", vacio.isEmpty());

        System.out.println();
        System.out.println(fallas == 0 ? "TODO OK" : "HAY " + fallas + " FALLA(S)");
    }
}