package club.persistencia;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import club.modelo.conjunto.EstadisticaBasket;
import club.modelo.conjunto.EstadisticaFutbol;
import club.modelo.conjunto.EstadisticaJugador;
import club.modelo.conjunto.EstadisticaVoleibol;
import club.modelo.conjunto.Partido;
import club.modelo.conjunto.PartidoInterno;
import club.modelo.usuarios.Socio;
import club.servicios.ClubSeneca;
import club.servicios.ReglaDeNegocioException;

/**
 * Persistencia de estadisticas por partido y de contadores de tarjetas por partido.
 * estadisticas.csv: idPartido;login;tipo;a;b;amarillas;roja
 *   FUTBOL: a=goles b=asistencias | BASKET: a=puntos b=rebotes | VOLEIBOL: a=aces b=bloqueos
 * tarjetasPartido.csv: idPartido;login;amarillas;roja
 */
public class RepositorioEstadisticas {

    private static final String ARCHIVO_ESTADISTICAS = "estadisticas.csv";
    private static final String ARCHIVO_TARJETAS = "tarjetasPartido.csv";

    private static final String[] ENC_ESTADISTICAS = {
        "idPartido", "loginSocio", "tipo", "a", "b", "amarillas", "roja"
    };
    private static final String[] ENC_TARJETAS = {
        "idPartido", "loginSocio", "amarillas", "roja"
    };

    private RepositorioEstadisticas() {
    }

    public static void cargar(Path carpeta, ClubSeneca club) {
        cargarTarjetas(carpeta, club);
        cargarEstadisticas(carpeta, club);
    }

    private static void cargarTarjetas(Path carpeta, ClubSeneca club) {
        int numeroFila = 1;
        for (String[] fila : Csv.leer(carpeta.resolve(ARCHIVO_TARJETAS))) {
            numeroFila++;
            validarColumnas(ARCHIVO_TARJETAS, fila, 4, numeroFila);
            try {
                PartidoInterno partido = partidoInterno(fila[0], ARCHIVO_TARJETAS, numeroFila, club);
                partido.restaurarTarjetasPartido(
                    fila[1], Integer.parseInt(fila[2]), Boolean.parseBoolean(fila[3]));
            } catch (NumberFormatException e) {
                throw new ReglaDeNegocioException(
                    "Error en " + ARCHIVO_TARJETAS + ", fila " + numeroFila + ": número inválido");
            }
        }
    }

    private static void cargarEstadisticas(Path carpeta, ClubSeneca club) {
        int numeroFila = 1;
        for (String[] fila : Csv.leer(carpeta.resolve(ARCHIVO_ESTADISTICAS))) {
            numeroFila++;
            validarColumnas(ARCHIVO_ESTADISTICAS, fila, 7, numeroFila);
            Partido partido = club.getServicioPartidos().getPartido(fila[0]);
            if (partido == null) {
                throw new ReglaDeNegocioException(
                    "Error en " + ARCHIVO_ESTADISTICAS + ", fila " + numeroFila
                    + ": no existe el partido " + fila[0]);
            }
            try {
                Socio socio = club.getSocio(fila[1]);
                int a = Integer.parseInt(fila[3]);
                int b = Integer.parseInt(fila[4]);
                EstadisticaJugador est;
                switch (fila[2]) {
                    case "FUTBOL":
                        est = new EstadisticaFutbol(socio, a, b,
                            Integer.parseInt(fila[5]), Boolean.parseBoolean(fila[6]));
                        break;
                    case "BASKET":
                        est = new EstadisticaBasket(socio, a, b);
                        break;
                    case "VOLEIBOL":
                        est = new EstadisticaVoleibol(socio, a, b);
                        break;
                    default:
                        throw new ReglaDeNegocioException(
                            "Error en " + ARCHIVO_ESTADISTICAS + ", fila " + numeroFila
                            + ": tipo no soportado: " + fila[2]);
                }
                partido.agregarEstadisticaCargada(est);
            } catch (NumberFormatException e) {
                throw new ReglaDeNegocioException(
                    "Error en " + ARCHIVO_ESTADISTICAS + ", fila " + numeroFila + ": número inválido");
            }
        }
    }

    public static void guardar(Path carpeta, ClubSeneca club) {
        List<String[]> filasEst = new ArrayList<>();
        List<String[]> filasTar = new ArrayList<>();

        for (Partido partido : club.getServicioPartidos().getPartidos()) {
            for (EstadisticaJugador e : partido.getEstadisticas()) {
                String login = e.getJugador().getLogin();
                if (e instanceof EstadisticaFutbol) {
                    EstadisticaFutbol f = (EstadisticaFutbol) e;
                    filasEst.add(new String[] { partido.getId(), login, "FUTBOL",
                        String.valueOf(f.getGoles()), String.valueOf(f.getAsistencias()),
                        String.valueOf(f.getTarjetasAmarillas()), String.valueOf(f.isTarjetaRoja()) });
                } else if (e instanceof EstadisticaBasket) {
                    EstadisticaBasket b = (EstadisticaBasket) e;
                    filasEst.add(new String[] { partido.getId(), login, "BASKET",
                        String.valueOf(b.getPuntos()), String.valueOf(b.getRebotes()), "0", "false" });
                } else if (e instanceof EstadisticaVoleibol) {
                    EstadisticaVoleibol v = (EstadisticaVoleibol) e;
                    filasEst.add(new String[] { partido.getId(), login, "VOLEIBOL",
                        String.valueOf(v.getAces()), String.valueOf(v.getBloqueos()), "0", "false" });
                }
            }
            if (partido instanceof PartidoInterno) {
                PartidoInterno pi = (PartidoInterno) partido;
                for (String login : pi.getLoginsConTarjetaPartido()) {
                    filasTar.add(new String[] { pi.getId(), login,
                        String.valueOf(pi.getAmarillasPartido(login)),
                        String.valueOf(pi.recibioRojaPartido(login)) });
                }
            }
        }
        Csv.escribir(carpeta.resolve(ARCHIVO_ESTADISTICAS), ENC_ESTADISTICAS, filasEst);
        Csv.escribir(carpeta.resolve(ARCHIVO_TARJETAS), ENC_TARJETAS, filasTar);
    }

    private static void validarColumnas(String archivo, String[] fila, int esperadas, int numeroFila) {
        if (fila.length != esperadas) {
            throw new ReglaDeNegocioException(
                "Error en " + archivo + ", fila " + numeroFila + ": se esperaban " + esperadas
                + " columnas y se encontraron " + fila.length);
        }
    }

    private static PartidoInterno partidoInterno(String id, String archivo, int fila, ClubSeneca club) {
        Partido p = club.getServicioPartidos().getPartido(id);
        if (!(p instanceof PartidoInterno)) {
            throw new ReglaDeNegocioException(
                "Error en " + archivo + ", fila " + fila + ": no existe el partido interno " + id);
        }
        return (PartidoInterno) p;
    }
}
