package club.consola;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

import club.modelo.usuarios.Administrador;
import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Usuario;
import club.servicios.ClubSeneca;

/**
 * Demostracion por consola del modulo de competencias. Recorre sesiones de administrador y de jugador
 * con el menu real (ConsolaConjunto), mostrando lo que se escribe y lo que responde el sistema.
 * Trabaja sobre una copia temporal de datos/: no modifica los archivos del proyecto.
 */
public class DemoCompetencias {

    private static final LocalDate[] NACIMIENTOS = {
        LocalDate.of(2000, 3, 15), LocalDate.of(1999, 7, 22), LocalDate.of(2001, 11, 2),
        LocalDate.of(1998, 1, 30), LocalDate.of(2002, 5, 9), LocalDate.of(2003, 9, 18),
        LocalDate.of(2010, 4, 12), LocalDate.of(2010, 8, 25), LocalDate.of(1995, 12, 5),
        LocalDate.of(1997, 6, 14)
    };

    /** Entrada que entrega una linea por lectura y la imprime, como si el usuario la escribiera. */
    private static class EntradaConEco extends InputStream {
        private final Iterator<String> lineas;
        private byte[] actual = new byte[0];
        private int posicion = 0;

        EntradaConEco(List<String> lineas) {
            this.lineas = lineas.iterator();
        }

        @Override
        public int read() {
            byte[] uno = new byte[1];
            return read(uno, 0, 1) == -1 ? -1 : uno[0] & 0xff;
        }

        @Override
        public int read(byte[] destino, int desde, int largo) {
            if (posicion >= actual.length) {
                if (!lineas.hasNext()) {
                    return -1;
                }
                String linea = lineas.next();
                System.out.println(linea);
                actual = (linea + "\n").getBytes(StandardCharsets.UTF_8);
                posicion = 0;
            }
            int n = Math.min(largo, actual.length - posicion);
            System.arraycopy(actual, posicion, destino, desde, n);
            posicion += n;
            return n;
        }
    }

    private static ClubSeneca nuevoClub(Path datos) {
        ClubSeneca club = new ClubSeneca();
        club.registrarUsuario(new Administrador("admin", "Administrador", Usuario.hash("admin123")));
        for (int i = 0; i < 22; i++) {
            String login = String.format("jug%02d", i + 1);
            club.registrarUsuario(new Socio(login, "Jugador " + login, Usuario.hash("1234"),
                    NACIMIENTOS[i % NACIMIENTOS.length]));
        }
        club.cargar(datos);
        return club;
    }

    private static void sesion(ClubSeneca club, String titulo, String login, String... entradas) {
        System.out.println();
        System.out.println("################ " + titulo + " (usuario: " + login + ") ################");
        Scanner in = new Scanner(new EntradaConEco(List.of(entradas)), StandardCharsets.UTF_8);
        new ConsolaConjunto(club, in).ejecutar(club.buscarUsuario(login));
    }

    private static void copiar(Path origen, Path destino) throws IOException {
        try (Stream<Path> archivos = Files.list(origen)) {
            for (Path archivo : archivos.toList()) {
                if (Files.isRegularFile(archivo)) {
                    Files.copy(archivo, destino.resolve(archivo.getFileName()));
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Path temporal = Files.createTempDirectory("cds-demo-competencias");
        copiar(Path.of("datos"), temporal);
        ClubSeneca club = nuevoClub(temporal);

        sesion(club, "ADMINISTRADOR: registrar competencia, rivales, inscripcion, partido y resultado", "admin",
            // HDU 7: registrar competencia y rechazo por duplicada
            "21",
            "14", "Copa Ciudad", "Liga Bogotana de Futbol", "2026", "FUTBOL", "MAYORES",
            "14", "Copa Ciudad", "Liga Bogotana de Futbol", "2026", "FUTBOL", "MAYORES",
            // rivales
            "15", "CP-002", "Colegio San Jorge",
            "15", "CP-002", "colegio san jorge",
            // HDU 10: inscribir equipo con nomina (nomina corta, otra categoria, valida, repetida)
            "16", "CP-002", "EQ-006", "jug15,jug16",
            "16", "CP-002", "EQ-004", "jug07,jug08",
            "16", "CP-002", "EQ-006", "jug15,jug16,jug17,jug18,jug19,jug20,jug21",
            "16", "CP-002", "EQ-006", "jug15,jug16,jug17,jug18,jug19,jug20,jug21",
            "21",
            // HDU 9: registrar partido (equipo no inscrito, rival no registrado, valido)
            "17", "CP-002", "EQ-001", "Colegio San Jorge", "1", "s", "Cancha Norte", "2026-11-08",
            "17", "CP-002", "EQ-006", "Rival Fantasma", "1", "s", "Cancha Norte", "2026-11-08",
            "17", "CP-002", "EQ-006", "Colegio San Jorge", "1", "s", "Cancha Norte", "2026-11-08",
            // HDU 9 y 11: estadisticas (jug22 es del equipo pero no esta en la nomina)
            "12", "PC-004", "jug22", "1", "0",
            "12", "PC-004", "jug21", "2", "1",
            "12", "PC-004", "jug15", "0", "1",
            "13", "PC-004",
            // HDU 8: tabla sin partidos jugados, resultado, tabla con resultado
            "19", "CP-002",
            "18", "PC-004", "3", "1",
            "18", "PC-004", "0", "0",
            "19", "CP-002",
            "20", "CP-002",
            "19", "CP-001",
            "2",
            "0");

        sesion(club, "JUGADOR en la nomina de CP-001 (con estadisticas)", "jug14",
            "3", "6", "CP-001", "14", "6", "CP-002", "0");

        sesion(club, "JUGADOR en la nomina de CP-002", "jug21",
            "6", "CP-002", "4", "0");

        sesion(club, "JUGADOR en la nomina de CP-001 (sin estadisticas)", "jug01",
            "6", "CP-001", "0");

        System.out.println();
        System.out.println("################ PERSISTENCIA: guardar y volver a cargar ################");
        club.guardar(temporal);
        System.out.println("Archivos guardados: competencias.csv, inscripciones.csv, partidosCompetencia.csv");
        System.out.println("--- competencias.csv");
        Files.readAllLines(temporal.resolve("competencias.csv")).forEach(System.out::println);
        System.out.println("--- inscripciones.csv");
        Files.readAllLines(temporal.resolve("inscripciones.csv")).forEach(System.out::println);
        System.out.println("--- partidosCompetencia.csv");
        Files.readAllLines(temporal.resolve("partidosCompetencia.csv")).forEach(System.out::println);

        ClubSeneca recargado = nuevoClub(temporal);
        sesion(recargado, "ADMINISTRADOR en una sesion nueva, con los datos recargados", "admin",
            "21", "19", "CP-002", "0");
    }
}