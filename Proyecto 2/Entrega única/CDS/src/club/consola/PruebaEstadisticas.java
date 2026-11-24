package club.consola;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import club.modelo.conjunto.Categoria;
import club.modelo.conjunto.Deporte;
import club.modelo.conjunto.Equipo;
import club.modelo.conjunto.EstadisticaFutbol;
import club.modelo.conjunto.PartidoInterno;
import club.modelo.conjunto.Posicion;
import club.modelo.conjunto.TipoTarjeta;
import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Usuario;
import club.persistencia.RepositorioConjunto;
import club.persistencia.RepositorioEstadisticas;
import club.persistencia.RepositorioPartidos;
import club.servicios.ClubSeneca;
import club.servicios.ReglaDeNegocioException;
import club.servicios.ServicioConjunto;
import club.servicios.ServicioPartidos;

/** Prueba de ida y vuelta de estadisticas y tarjetas por partido. */
public class PruebaEstadisticas {

    private static int fallas = 0;

    private static final Posicion[] POS = {
        Posicion.PORTERO, Posicion.DEFENSA, Posicion.DEFENSA, Posicion.DEFENSA,
        Posicion.MEDIOCAMPISTA, Posicion.MEDIOCAMPISTA, Posicion.DELANTERO, Posicion.DELANTERO
    };

    private static void verificar(String d, boolean c) {
        System.out.println((c ? "OK     " : "FALLA  ") + d);
        if (!c) {
            fallas++;
        }
    }

    private static void debeRechazar(String d, Runnable r) {
        try {
            r.run();
            verificar(d + " (debia rechazar y no lo hizo)", false);
        } catch (ReglaDeNegocioException e) {
            verificar(d + " -> " + e.getMessage(), true);
        }
    }

    private static void socios(ClubSeneca club, String prefijo, int n) {
        for (int i = 1; i <= n; i++) {
            club.registrarUsuario(new Socio(prefijo + i, "Jugador " + prefijo + i,
                    Usuario.hash("1234"), LocalDate.of(2000, 1, 1)));
        }
    }

    private static Equipo equipo(ClubSeneca club, String nombre, Deporte dep, String prefijo) {
        socios(club, prefijo, 8);
        ServicioConjunto sc = club.getServicioConjunto();
        Equipo e = sc.crearEquipo(nombre, dep, Categoria.MAYORES, 14);
        for (int i = 1; i <= 8; i++) {
            sc.inscribirJugador(e, club.getSocio(prefijo + i), POS[i - 1], i);
        }
        return e;
    }

    public static void main(String[] args) throws IOException {
        ClubSeneca c1 = new ClubSeneca();
        ServicioPartidos sp = c1.getServicioPartidos();
        Deporte futbol = new Deporte("FUTBOL", 7, 3, 1, 0, true, false, true);
        c1.getServicioConjunto().registrarDeporte(futbol);
        Equipo a = equipo(c1, "Aguilas", futbol, "a");
        Equipo b = equipo(c1, "Bravos", futbol, "b");

        PartidoInterno p = sp.programarPartidoInterno(a, b, LocalDate.of(2026, 11, 8));
        sp.registrarTarjeta(p, c1.getSocio("a2"), TipoTarjeta.AMARILLA);
        sp.registrarTarjeta(p, c1.getSocio("b3"), TipoTarjeta.ROJA);
        sp.registrarEstadisticaFutbol(p, c1.getSocio("a2"), 1, 0);
        sp.registrarEstadisticaFutbol(p, c1.getSocio("a3"), 2, 1);

        System.out.println("=== Reglas ===");
        debeRechazar("Tarjeta despues de estadistica",
            () -> sp.registrarTarjeta(p, c1.getSocio("a3"), TipoTarjeta.AMARILLA));
        debeRechazar("Estadistica duplicada",
            () -> sp.registrarEstadisticaFutbol(p, c1.getSocio("a3"), 1, 1));
        debeRechazar("Estadistica de jugador ajeno",
            () -> sp.registrarEstadisticaFutbol(p, new Socio("zz", "Zz", Usuario.hash("1"),
                LocalDate.of(2000, 1, 1)), 1, 1));
        sp.registrarResultado(p, 3, 0);
        debeRechazar("Estadistica con partido cerrado",
            () -> sp.registrarEstadisticaFutbol(p, c1.getSocio("a4"), 1, 1));

        System.out.println("=== Guardar ===");
        Path carpeta = Files.createTempDirectory("estad1");
        RepositorioConjunto.guardar(carpeta, c1);
        RepositorioPartidos.guardar(carpeta, c1);
        RepositorioEstadisticas.guardar(carpeta, c1);
        verificar("estadisticas.csv existe", Files.exists(carpeta.resolve("estadisticas.csv")));
        verificar("tarjetasPartido.csv existe", Files.exists(carpeta.resolve("tarjetasPartido.csv")));

        System.out.println("=== Cargar y comparar ===");
        ClubSeneca c2 = new ClubSeneca();
        socios(c2, "a", 8);
        socios(c2, "b", 8);
        RepositorioConjunto.cargar(carpeta, c2);
        RepositorioPartidos.cargar(carpeta, c2);
        RepositorioEstadisticas.cargar(carpeta, c2);
        PartidoInterno q = (PartidoInterno) c2.getServicioPartidos().getPartido("PT-001");
        verificar("2 estadisticas cargadas", q.getEstadisticas().size() == 2);
        EstadisticaFutbol e2 = (EstadisticaFutbol) q.getEstadisticaJugador(c2.getSocio("a2"));
        EstadisticaFutbol e3 = (EstadisticaFutbol) q.getEstadisticaJugador(c2.getSocio("a3"));
        verificar("a2: 1 gol, 0 asist, 1 amarilla",
            e2 != null && e2.getGoles() == 1 && e2.getAsistencias() == 0 && e2.getTarjetasAmarillas() == 1);
        verificar("a3: 2 goles, 1 asist", e3 != null && e3.getGoles() == 2 && e3.getAsistencias() == 1);
        verificar("Contador amarillas de a2 restaurado", q.getAmarillasPartido("a2") == 1);
        verificar("Roja de b3 restaurada", q.recibioRojaPartido("b3"));
        verificar("Estadistica apunta al socio cargado", e2.getJugador() == c2.getSocio("a2"));

        System.out.println("=== Estabilidad ===");
        Path carpeta2 = Files.createTempDirectory("estad2");
        RepositorioEstadisticas.guardar(carpeta2, c2);
        verificar("estadisticas.csv identico",
            Files.readString(carpeta.resolve("estadisticas.csv")).equals(Files.readString(carpeta2.resolve("estadisticas.csv"))));
        verificar("tarjetasPartido.csv identico",
            Files.readString(carpeta.resolve("tarjetasPartido.csv")).equals(Files.readString(carpeta2.resolve("tarjetasPartido.csv"))));

        System.out.println();
        System.out.println(fallas == 0 ? "TODO OK" : "HAY " + fallas + " FALLA(S)");
    }
}
