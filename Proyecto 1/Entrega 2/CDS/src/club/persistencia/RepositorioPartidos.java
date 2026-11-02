package club.persistencia;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import club.modelo.conjunto.Equipo;
import club.modelo.conjunto.EstadoPartido;
import club.modelo.conjunto.Partido;
import club.modelo.conjunto.PartidoInterno;
import club.servicios.ClubSeneca;
import club.servicios.ReglaDeNegocioException;
import club.servicios.ServicioPartidos;

public class RepositorioPartidos {

    private static final String ARCHIVO_PARTIDOS = "partidos.csv";

    private static final String[] ENC_PARTIDOS = {
        "id", "tipo", "idEquipoLocal", "idEquipoVisitante", "fecha",
        "estado", "marcadorLocal", "marcadorVisitante", "sancionados"
    };

    private RepositorioPartidos() {
    }

    // PA LA CARGA

    public static void cargar(Path carpeta, ClubSeneca club) {
        List<String[]> filas = Csv.leer(carpeta.resolve(ARCHIVO_PARTIDOS));
        int numeroFila = 1;

        for (String[] fila : filas) {
            numeroFila++;

            if (fila.length != 9) {
                throw new ReglaDeNegocioException(
                    "Error en partidos.csv, fila " + numeroFila
                    + ": se esperaban 9 columnas y se encontraron " + fila.length
                );
            }

            if (!fila[1].equals("INTERNO")) {
                throw new ReglaDeNegocioException(
                    "Error en partidos.csv, fila " + numeroFila
                    + ": tipo de partido no soportado: " + fila[1]
                );
            }

            try {
            	Equipo local = club.getServicioConjunto().getEquipo(fila[2]);
            	Equipo visitante = club.getServicioConjunto().getEquipo(fila[3]);

            	if (local == null) {
            	    throw new ReglaDeNegocioException(
            	        "Error en partidos.csv, fila " + numeroFila
            	        + ": no existe el equipo local con id " + fila[2]
            	    );
            	}

            	if (visitante == null) {
            	    throw new ReglaDeNegocioException(
            	        "Error en partidos.csv, fila " + numeroFila
            	        + ": no existe el equipo visitante con id " + fila[3]
            	    );
            	}

            	LocalDate fecha = LocalDate.parse(fila[4]);
            	EstadoPartido estado = EstadoPartido.valueOf(fila[5]);

            	int marcadorLocal = Integer.parseInt(fila[6]);
            	int marcadorVisitante = Integer.parseInt(fila[7]);

            	PartidoInterno partido = new PartidoInterno(
            		    fila[0],
            		    local,
            		    visitante,
            		    fecha,
            		    estado,
            		    marcadorLocal,
            		    marcadorVisitante
            		);

            	if (!fila[8].trim().isEmpty()) {
            	    String[] logins = fila[8].split(Csv.SEPARADOR_LISTA);

            	    for (String login : logins) {
            	        if (!login.trim().isEmpty()) {
            	            partido.marcarSancionado(login.trim());
            	        }
            	    }
            	}

            	club.getServicioPartidos().agregarPartidoCargado(partido);

            } catch (IllegalArgumentException | java.time.format.DateTimeParseException e) {
                throw new ReglaDeNegocioException(
                    "Error en partidos.csv, fila " + numeroFila
                    + ": fecha, estado o marcador inválido"
                );
            }
        }
    }

    // PA GUARDAR

    public static void guardar(Path carpeta, ClubSeneca club) {
        ServicioPartidos servicio = club.getServicioPartidos();
        List<String[]> filas = new ArrayList<>();

        for (Partido partido : servicio.getPartidos()) {
        	if (!(partido instanceof PartidoInterno)) {
        	    continue;
        	}

        	PartidoInterno interno = (PartidoInterno) partido;

        	String sancionados = String.join(
        	    Csv.SEPARADOR_LISTA,
        	    interno.getSancionados()
        	);

        	String[] fila = {
        	    interno.getId(),
        	    "INTERNO",
        	    interno.getLocal().getId(),
        	    interno.getVisitante().getId(),
        	    interno.getFecha().toString(),
        	    interno.getEstado().name(),
        	    String.valueOf(interno.getMarcadorLocal()),
        	    String.valueOf(interno.getMarcadorVisitante()),
        	    sancionados
        	};

        	filas.add(fila);
        }

        Csv.escribir(carpeta.resolve(ARCHIVO_PARTIDOS), ENC_PARTIDOS, filas);
    }
}
