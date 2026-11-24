package club.persistencia;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import club.modelo.conjunto.Categoria;
import club.modelo.conjunto.Competencia;
import club.modelo.conjunto.Deporte;
import club.modelo.conjunto.Equipo;
import club.modelo.conjunto.EstadoPartido;
import club.modelo.conjunto.Fase;
import club.modelo.conjunto.InscripcionCompetencia;
import club.modelo.conjunto.InstitucionRival;
import club.modelo.conjunto.Partido;
import club.modelo.conjunto.PartidoCompetencia;
import club.modelo.conjunto.PartidoExterno;
import club.modelo.conjunto.TipoFase;
import club.modelo.usuarios.Socio;
import club.servicios.ClubSeneca;
import club.servicios.ReglaDeNegocioException;

public class RepositorioCompetencias {

    private static final String ARCHIVO_COMPETENCIAS = "competencias.csv";
    private static final String ARCHIVO_FASES = "fases.csv";
    private static final String ARCHIVO_INSCRIPCIONES = "inscripciones.csv";
    private static final String ARCHIVO_PARTIDOS = "partidosCompetencia.csv";
    private static final String ARCHIVO_EXTERNOS = "partidosExternos.csv";

    private static final String[] ENC_COMPETENCIAS = {
        "id", "nombre", "organizador", "temporada", "deporte", "categoria", "rivales"
    };
    private static final String[] ENC_FASES = {
        "idCompetencia", "orden", "nombre", "tipo", "clasificados"
    };
    private static final String[] ENC_INSCRIPCIONES = {
        "idCompetencia", "idEquipo", "nomina"
    };
    private static final String[] ENC_PARTIDOS = {
    	    "id", "idCompetencia", "jornada", "ordenFase", "idEquipo", "rival", "clubEsLocal",
    	    "escenario", "fecha", "estado", "marcadorLocal", "marcadorVisitante"
    };
    private static final String[] ENC_EXTERNOS = {
        "id", "idCompetencia", "jornada", "ordenFase", "rivalLocal", "rivalVisitante",
        "escenario", "fecha", "estado", "marcadorLocal", "marcadorVisitante"
    };

    private RepositorioCompetencias() {
    }

    // carga


    public static void cargar(Path carpeta, ClubSeneca club) {
        cargarCompetencias(carpeta, club);
        cargarFases(carpeta, club);
        cargarInscripciones(carpeta, club);
        cargarPartidos(carpeta, club);
        cargarPartidosExternos(carpeta, club);
    }

    private static void cargarCompetencias(Path carpeta, ClubSeneca club) {
        int numeroFila = 1;
        for (String[] fila : Csv.leer(carpeta.resolve(ARCHIVO_COMPETENCIAS))) {
            numeroFila++;
            validarColumnas(ARCHIVO_COMPETENCIAS, fila, 7, numeroFila);
            Deporte deporte = club.getServicioConjunto().getDeporte(fila[4]);
            if (deporte == null) {
                throw error(ARCHIVO_COMPETENCIAS, numeroFila, "no existe el deporte " + fila[4]);
            }
            try {
                Competencia competencia = new Competencia(fila[0], fila[1], fila[2], fila[3],
                        deporte, Categoria.valueOf(fila[5]));
                for (String nombre : separar(fila[6])) {
                    competencia.agregarRivalCargado(new InstitucionRival(nombre));
                }
                club.getServicioCompetencias().agregarCompetenciaCargada(competencia);
            } catch (IllegalArgumentException e) {
                throw error(ARCHIVO_COMPETENCIAS, numeroFila, "categoría inválida: " + fila[5]);
            }
        }
    }

    private static void cargarFases(Path carpeta, ClubSeneca club) {
        int numeroFila = 1;
        for (String[] fila : Csv.leer(carpeta.resolve(ARCHIVO_FASES))) {
            numeroFila++;
            validarColumnas(ARCHIVO_FASES, fila, 5, numeroFila);
            Competencia competencia = competencia(fila[0], ARCHIVO_FASES, numeroFila, club);
            try {
                Fase fase = new Fase(Integer.parseInt(fila[1]), fila[2],
                        TipoFase.valueOf(fila[3]), Integer.parseInt(fila[4]));
                if (competencia.getFase(fase.getOrden()) != null) {
                    throw error(ARCHIVO_FASES, numeroFila,
                            "la fase " + fase.getOrden() + " está repetida en " + fila[0]);
                }
                competencia.agregarFaseCargada(fase);
            } catch (IllegalArgumentException e) {
                throw error(ARCHIVO_FASES, numeroFila, "orden, tipo o clasificados inválido");
            } catch (ReglaDeNegocioException e) {
                if (e.getMessage().startsWith("Error en ")) {
                    throw e;
                }
                throw error(ARCHIVO_FASES, numeroFila, e.getMessage());
            }
        }
    }

    private static void cargarInscripciones(Path carpeta, ClubSeneca club) {
        int numeroFila = 1;
        for (String[] fila : Csv.leer(carpeta.resolve(ARCHIVO_INSCRIPCIONES))) {
            numeroFila++;
            validarColumnas(ARCHIVO_INSCRIPCIONES, fila, 3, numeroFila);
            Competencia competencia = competencia(fila[0], ARCHIVO_INSCRIPCIONES, numeroFila, club);
            Equipo equipo = equipo(fila[1], ARCHIVO_INSCRIPCIONES, numeroFila, club);
            List<Socio> nomina = new ArrayList<>();
            for (String login : separar(fila[2])) {
                nomina.add(club.getSocio(login));
            }
            competencia.agregarInscripcionCargada(new InscripcionCompetencia(equipo, nomina));
        }
    }

    private static void cargarPartidos(Path carpeta, ClubSeneca club) {
        int numeroFila = 1;
        for (String[] fila : Csv.leer(carpeta.resolve(ARCHIVO_PARTIDOS))) {
            numeroFila++;
            validarColumnas(ARCHIVO_PARTIDOS, fila, 12, numeroFila);
            Competencia competencia = competencia(fila[1], ARCHIVO_PARTIDOS, numeroFila, club);
            Equipo equipo = equipo(fila[4], ARCHIVO_PARTIDOS, numeroFila, club);
            InscripcionCompetencia inscripcion = competencia.getInscripcion(equipo);
            if (inscripcion == null) {
                throw error(ARCHIVO_PARTIDOS, numeroFila,
                        "el equipo " + fila[4] + " no está inscrito en " + fila[1]);
            }
            InstitucionRival rival = competencia.getRival(fila[5]);
            if (rival == null) {
                throw error(ARCHIVO_PARTIDOS, numeroFila,
                        "el rival " + fila[5] + " no está registrado en " + fila[1]);
            }

            if (competencia.getFase(Integer.parseInt(fila[3])) == null) {
                throw error(ARCHIVO_PARTIDOS, numeroFila, "la competencia no tiene la fase " + fila[3]);
            }
            try {
            	int ordenFase = Integer.parseInt(fila[3]);
            if (competencia.getFase(ordenFase) == null) {
                throw error(ARCHIVO_PARTIDOS, numeroFila, "la competencia no tiene la fase " + fila[3]);
            }
            	PartidoCompetencia partido = new PartidoCompetencia(fila[0], fila[1],
            	        Integer.parseInt(fila[2]), Integer.parseInt(fila[3]), inscripcion, rival,
            	        Boolean.parseBoolean(fila[6]), fila[7], LocalDate.parse(fila[8]),
            	        EstadoPartido.valueOf(fila[9]),
            	        Integer.parseInt(fila[10]), Integer.parseInt(fila[11]));
                club.getServicioPartidos().agregarPartidoCargado(partido);
            } catch (IllegalArgumentException | DateTimeParseException e) {
                throw error(ARCHIVO_PARTIDOS, numeroFila, "jornada,fase, fecha, estado o marcador inválido");
            }
        }
    }

    private static void cargarPartidosExternos(Path carpeta, ClubSeneca club) {
        int numeroFila = 1;
        for (String[] fila : Csv.leer(carpeta.resolve(ARCHIVO_EXTERNOS))) {
            numeroFila++;
            validarColumnas(ARCHIVO_EXTERNOS, fila, 11, numeroFila);
            Competencia competencia = competencia(fila[1], ARCHIVO_EXTERNOS, numeroFila, club);
            InstitucionRival rivalLocal = competencia.getRival(fila[4]);
            InstitucionRival rivalVisitante = competencia.getRival(fila[5]);
            if (rivalLocal == null) {
                throw error(ARCHIVO_EXTERNOS, numeroFila,
                        "el rival local " + fila[4] + " no está registrado en " + fila[1]);
            }
            if (rivalVisitante == null) {
                throw error(ARCHIVO_EXTERNOS, numeroFila,
                        "el rival visitante " + fila[5] + " no está registrado en " + fila[1]);
            }
            try {
                int ordenFase = Integer.parseInt(fila[3]);
                if (competencia.getFase(ordenFase) == null) {
                    throw error(ARCHIVO_EXTERNOS, numeroFila,
                            "la competencia no tiene la fase " + fila[3]);
                }
                PartidoExterno partido = new PartidoExterno(
                        fila[0], fila[1], Integer.parseInt(fila[2]), ordenFase,
                        competencia.getDeporte(),
                        rivalLocal, rivalVisitante, fila[6], LocalDate.parse(fila[7]),
                        EstadoPartido.valueOf(fila[8]),
                        Integer.parseInt(fila[9]), Integer.parseInt(fila[10]));
                club.getServicioPartidos().agregarPartidoCargado(partido);
            } catch (IllegalArgumentException | DateTimeParseException e) {
                throw error(ARCHIVO_EXTERNOS, numeroFila,
                        "jornada, fase, fecha, estado o marcador inválido");
            }
        }
    }

    // guardar

    public static void guardar(Path carpeta, ClubSeneca club) {
        List<String[]> filasCompetencias = new ArrayList<>();
        List<String[]> filasFases = new ArrayList<>();
        List<String[]> filasInscripciones = new ArrayList<>();
        List<String[]> filasPartidos = new ArrayList<>();
        List<String[]> filasExternos = new ArrayList<>();

        for (Competencia competencia : club.getServicioCompetencias().getCompetencias()) {
            List<String> rivales = new ArrayList<>();
            for (InstitucionRival rival : competencia.getRivales()) {
                rivales.add(rival.getNombre());
            }
            filasCompetencias.add(new String[] {
                competencia.getId(), competencia.getNombre(), competencia.getOrganizador(),
                competencia.getTemporada(), competencia.getDeporte().getNombre(),
                competencia.getCategoria().name(), String.join(Csv.SEPARADOR_LISTA, rivales)
            });

            for (Fase fase : competencia.getFases()) {
                filasFases.add(new String[] {
                    competencia.getId(), String.valueOf(fase.getOrden()), fase.getNombre(),
                    fase.getTipo().name(), String.valueOf(fase.getClasificados())
                });
            }

            for (InscripcionCompetencia inscripcion : competencia.getInscripciones()) {
                List<String> logins = new ArrayList<>();
                for (Socio socio : inscripcion.getNomina()) {
                    logins.add(socio.getLogin());
                }
                filasInscripciones.add(new String[] {
                    competencia.getId(), inscripcion.getEquipo().getId(),
                    String.join(Csv.SEPARADOR_LISTA, logins)
                });
            }
        }

        for (Partido partido : club.getServicioPartidos().getPartidos()) {
            if (partido instanceof PartidoCompetencia) {
                PartidoCompetencia pc = (PartidoCompetencia) partido;
                filasPartidos.add(new String[] {
                    pc.getId(), pc.getIdCompetencia(), String.valueOf(pc.getJornada()),
                    String.valueOf(pc.getOrdenFase()),
                    pc.getEquipoClub().getId(), pc.getRival().getNombre(),
                    String.valueOf(pc.clubEsLocal()), pc.getEscenario(), pc.getFecha().toString(),
                    pc.getEstado().name(), String.valueOf(pc.getMarcadorLocal()),
                    String.valueOf(pc.getMarcadorVisitante())
                });
            } else if (partido instanceof PartidoExterno) {
                PartidoExterno pe = (PartidoExterno) partido;
                filasExternos.add(new String[] {
                    pe.getId(), pe.getIdCompetencia(), String.valueOf(pe.getJornada()),
                    String.valueOf(pe.getOrdenFase()), pe.getRivalLocal().getNombre(),
                    pe.getRivalVisitante().getNombre(), pe.getEscenario(), pe.getFecha().toString(),
                    pe.getEstado().name(), String.valueOf(pe.getMarcadorLocal()),
                    String.valueOf(pe.getMarcadorVisitante())
                });
            }
        }

        Csv.escribir(carpeta.resolve(ARCHIVO_COMPETENCIAS), ENC_COMPETENCIAS, filasCompetencias);
        Csv.escribir(carpeta.resolve(ARCHIVO_FASES), ENC_FASES, filasFases);
        Csv.escribir(carpeta.resolve(ARCHIVO_INSCRIPCIONES), ENC_INSCRIPCIONES, filasInscripciones);
        Csv.escribir(carpeta.resolve(ARCHIVO_PARTIDOS), ENC_PARTIDOS, filasPartidos);
        Csv.escribir(carpeta.resolve(ARCHIVO_EXTERNOS), ENC_EXTERNOS, filasExternos);
    }

    //  ayudas (hechas con IA)

    private static List<String> separar(String celda) {
        List<String> partes = new ArrayList<>();
        if (!celda.trim().isEmpty()) {
            for (String parte : celda.split(Csv.SEPARADOR_LISTA)) {
                if (!parte.trim().isEmpty()) {
                    partes.add(parte.trim());
                }
            }
        }
        return partes;
    }

    private static Competencia competencia(String id, String archivo, int fila, ClubSeneca club) {
        Competencia competencia = club.getServicioCompetencias().getCompetencia(id);
        if (competencia == null) {
            throw error(archivo, fila, "no existe la competencia " + id);
        }
        return competencia;
    }

    private static Equipo equipo(String id, String archivo, int fila, ClubSeneca club) {
        Equipo equipo = club.getServicioConjunto().getEquipo(id);
        if (equipo == null) {
            throw error(archivo, fila, "no existe el equipo " + id);
        }
        return equipo;
    }

    private static void validarColumnas(String archivo, String[] fila, int esperadas, int numeroFila) {
        if (fila.length != esperadas) {
            throw error(archivo, numeroFila,
                    "se esperaban " + esperadas + " columnas y se encontraron " + fila.length);
        }
    }

    private static ReglaDeNegocioException error(String archivo, int fila, String detalle) {
        return new ReglaDeNegocioException("Error en " + archivo + ", fila " + fila + ": " + detalle);
    }
}