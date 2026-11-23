package club.persistencia;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import club.modelo.conjunto.Equipo;
import club.modelo.torneos.EstadoTorneo;
import club.modelo.torneos.InscripcionEquipo;
import club.modelo.torneos.InscripcionIndividual;
import club.modelo.torneos.InscripcionTorneo;
import club.modelo.torneos.Modalidad;
import club.modelo.torneos.Torneo;
import club.modelo.torneos.TorneoCompetitivo;
import club.modelo.torneos.TorneoRecreativo;
import club.modelo.usuarios.Usuario;
import club.servicios.ClubSeneca;
import club.servicios.ReglaDeNegocioException;

//se divide en cargar y guardar 

public class RepositorioTorneos {

    private static final String ARCHIVO_TORNEOS = "torneos.csv";
    private static final String ARCHIVO_INSCRIPCIONES = "inscripcionesTorneo.csv";

    private static final String[] ENC_TORNEOS = {
        "id", "nombre", "tipo", "modalidad", "disciplina",
        "categoria", "cupos", "fecha", "tarifa", "estado",
        "preferencialesLiberados"
    };

    private static final String[] ENC_INSCRIPCIONES = {
        "idTorneo", "tipo", "idEquipo", "participantes",
        "preferencial", "puntajeRanking"
    };

    private RepositorioTorneos() {
    }

    // CARGA

    public static void cargar(Path carpeta, ClubSeneca club) {
        cargarTorneos(carpeta, club);
        cargarInscripciones(carpeta, club);
    }

    private static void cargarTorneos(Path carpeta, ClubSeneca club) {
        int numeroFila = 1;

        for (String[] fila : Csv.leer(carpeta.resolve(ARCHIVO_TORNEOS))) {
            numeroFila++;

            validarColumnas(ARCHIVO_TORNEOS, fila, 11, numeroFila);

            try {
                String tipo = fila[2].trim().toUpperCase();

                String id = fila[0];
                String nombre = fila[1];
                Modalidad modalidad = Modalidad.valueOf(fila[3].trim().toUpperCase());
                String disciplina = fila[4];
                String categoria = fila[5];
                int cupos = Integer.parseInt(fila[6]);
                LocalDate fecha = LocalDate.parse(fila[7]);

                int tarifa = Integer.parseInt(fila[8]);

                Torneo torneo;

                if (tipo.equals("RECREATIVO")) {
                    torneo = new TorneoRecreativo(
                        id, nombre, modalidad, disciplina,
                        categoria, cupos, fecha
                    );

                    if (tarifa != 0) {
                        throw error(ARCHIVO_TORNEOS, numeroFila,
                            "un torneo recreativo debe tener tarifa 0");
                    }

                } else if (tipo.equals("COMPETITIVO")) {
                    torneo = new TorneoCompetitivo(
                        id, nombre, modalidad, disciplina,
                        categoria, cupos, fecha, tarifa
                    );

                } else {
                    throw error(ARCHIVO_TORNEOS, numeroFila,
                        "tipo de torneo desconocido: " + tipo);
                }

                EstadoTorneo estado =
                    EstadoTorneo.valueOf(fila[9].trim().toUpperCase());

                boolean preferencialesLiberados =
                    Boolean.parseBoolean(fila[10]);

                torneo.restaurarEstado(estado, preferencialesLiberados);

                club.getServicioTorneos().agregarTorneoCargado(torneo);

            } catch (IllegalArgumentException | DateTimeParseException e) {
                throw error(ARCHIVO_TORNEOS, numeroFila,
                    "tipo, modalidad, fecha, tarifa, cupos o estado inválido");
            }
        }
    }

    private static void cargarInscripciones(Path carpeta, ClubSeneca club) {
        int numeroFila = 1;

        for (String[] fila : Csv.leer(carpeta.resolve(ARCHIVO_INSCRIPCIONES))) {
            numeroFila++;

            validarColumnas(ARCHIVO_INSCRIPCIONES, fila, 6, numeroFila);

            Torneo torneo = club.getServicioTorneos().getTorneo(fila[0]);

            if (torneo == null) {
                throw error(ARCHIVO_INSCRIPCIONES, numeroFila,
                    "no existe el torneo " + fila[0]);
            }

            List<Usuario> participantes = new ArrayList<>();

            for (String login : separar(fila[3])) {
                Usuario usuario = club.buscarUsuario(login);

                if (usuario == null) {
                    throw error(ARCHIVO_INSCRIPCIONES, numeroFila,
                        "no existe el usuario " + login);
                }

                participantes.add(usuario);
            }

            try {
                String tipo = fila[1].trim().toUpperCase();
                boolean preferencial = Boolean.parseBoolean(fila[4]);
                int puntajeRanking = Integer.parseInt(fila[5]);

                InscripcionTorneo inscripcion;

                if (tipo.equals("EQUIPO")) {
                    Equipo equipo = club.getServicioConjunto()
                        .getEquipo(fila[2]);

                    if (equipo == null) {
                        throw error(ARCHIVO_INSCRIPCIONES, numeroFila,
                            "no existe el equipo " + fila[2]);
                    }

                    inscripcion = new InscripcionEquipo(
                        equipo, participantes, preferencial, puntajeRanking
                    );

                } else if (tipo.equals("INDIVIDUAL")) {
                    inscripcion = new InscripcionIndividual(
                        participantes, preferencial, puntajeRanking
                    );

                } else {
                    throw error(ARCHIVO_INSCRIPCIONES, numeroFila,
                        "tipo de inscripción desconocido: " + tipo);
                }

                torneo.agregarInscripcionCargada(inscripcion);

            } catch (IllegalArgumentException e) {
                throw error(ARCHIVO_INSCRIPCIONES, numeroFila,
                    "puntaje o datos de inscripción inválidos");
            }
        }
    }

    // GUARDAR

    public static void guardar(Path carpeta, ClubSeneca club) {
        List<String[]> filasTorneos = new ArrayList<>();
        List<String[]> filasInscripciones = new ArrayList<>();

        for (Torneo torneo : club.getServicioTorneos().getTorneos()) {

            String tipo;
            int tarifa;

            if (torneo instanceof TorneoCompetitivo) {
                tipo = "COMPETITIVO";
                tarifa = ((TorneoCompetitivo) torneo).getTarifaInscripcion();
            } else {
                tipo = "RECREATIVO";
                tarifa = 0;
            }

            filasTorneos.add(new String[] {
                torneo.getId(),
                torneo.getNombre(),
                tipo,
                torneo.getModalidad().name(),
                torneo.getDisciplina(),
                torneo.getCategoria(),
                String.valueOf(torneo.getCupos()),
                torneo.getFecha().toString(),
                String.valueOf(tarifa),
                torneo.getEstado().name(),
                String.valueOf(torneo.isPreferencialesLiberados())
            });

            for (InscripcionTorneo inscripcion : torneo.getInscripciones()) {
                String tipoInscripcion;
                String idEquipo = "";
                List<String> logins = new ArrayList<>();

                if (inscripcion instanceof InscripcionEquipo) {
                    InscripcionEquipo inscripcionEquipo =
                        (InscripcionEquipo) inscripcion;

                    tipoInscripcion = "EQUIPO";
                    idEquipo = inscripcionEquipo.getEquipo().getId();

                    for (Usuario jugador : inscripcionEquipo.getJugadores()) {
                        logins.add(jugador.getLogin());
                    }

                } else if (inscripcion instanceof InscripcionIndividual) {
                    tipoInscripcion = "INDIVIDUAL";

                    for (Usuario participante : inscripcion.getParticipantes()) {
                        logins.add(participante.getLogin());
                    }

                } else {
                    throw new ReglaDeNegocioException(
                        "Tipo de inscripción no reconocido en el torneo "
                        + torneo.getId()
                    );
                }

                filasInscripciones.add(new String[] {
                    torneo.getId(),
                    tipoInscripcion,
                    idEquipo,
                    String.join(",", logins),
                    String.valueOf(inscripcion.isPreferencial()),
                    String.valueOf(inscripcion.getPuntajeRanking())
                });
            }
        }

        Csv.escribir(
            carpeta.resolve(ARCHIVO_TORNEOS),
            ENC_TORNEOS,
            filasTorneos
        );

        Csv.escribir(
            carpeta.resolve(ARCHIVO_INSCRIPCIONES),
            ENC_INSCRIPCIONES,
            filasInscripciones
        );
    }

    //AYUDAS

    private static List<String> separar(String celda) {
        List<String> partes = new ArrayList<>();

        if (celda != null && !celda.trim().isEmpty()) {
            for (String parte : celda.split(",")) {
                if (!parte.trim().isEmpty()) {
                    partes.add(parte.trim());
                }
            }
        }

        return partes;
    }

    private static void validarColumnas(
        String archivo, String[] fila, int esperadas, int numeroFila
    ) {
        if (fila.length != esperadas) {
            throw error(archivo, numeroFila,
                "se esperaban " + esperadas
                + " columnas y se encontraron " + fila.length);
        }
    }

    private static ReglaDeNegocioException error(
        String archivo, int fila, String detalle
    ) {
        return new ReglaDeNegocioException(
            "Error en " + archivo + ", fila " + fila + ": " + detalle
        );
    }
}

