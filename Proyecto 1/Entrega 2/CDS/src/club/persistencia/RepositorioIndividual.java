package club.persistencia;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import club.modelo.individual.Academia;
import club.modelo.individual.BloqueHorario;
import club.modelo.individual.Disciplina;
import club.modelo.individual.EstadoReserva;
import club.modelo.individual.InscripcionAcademia;
import club.modelo.individual.Instalacion;
import club.modelo.individual.Modalidad;
import club.modelo.individual.Nivel;
import club.modelo.individual.PartidoPractica;
import club.modelo.individual.Reserva;
import club.modelo.individual.SesionEntrenamiento;
import club.modelo.individual.TipoSesion;
import club.modelo.usuarios.Entrenador;
import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Usuario;
import club.servicios.ClubSeneca;
import club.servicios.ReglaDeNegocioException;
import club.servicios.ServicioIndividual;


public class RepositorioIndividual {

    private static String ARCHIVO_ACADEMIAS = "academias.csv";
    private static String ARCHIVO_INSTALACIONES = "instalaciones.csv";
    private static String ARCHIVO_INSCRIPCIONES = "inscripciones_academia.csv";
    private static String ARCHIVO_RESERVAS = "reservas.csv";
    private static String ARCHIVO_PARTIDOS = "partidos_practica.csv";
    private static String ARCHIVO_SESIONES = "sesiones.csv";

    private static String[] ENC_ACADEMIAS = { "disciplina", "maxSimultaneas" };
    private static String[] ENC_INSTALACIONES = {
        "codigo", "nombre", "disciplina", "capacidad", "techada", "habilitada"
    };
    private static  String[] ENC_INSCRIPCIONES = {
        "loginSocio", "disciplina", "nivel", "puntajeRanking", "fechaInscripcion"
    };
    private static String[] ENC_RESERVAS = {
        "id", "codigoInstalacion", "fecha", "horaInicio", "horaFin", "loginResponsable", "estado",
        "numeroJugadores", "modalidad"
    };
    private static final String[] ENC_PARTIDOS = {
        "id", "codigoInstalacion", "fecha", "horaInicio", "horaFin", "loginResponsable", "estado",
        "ladoA", "ladoB"
    };
    private static final String[] ENC_SESIONES = {
        "id", "codigoInstalacion", "fecha", "horaInicio", "horaFin", "loginEntrenador", "estado",
        "tipoSesion", "convocados"
    };

    private RepositorioIndividual() {
    }

    // ===================== CARGAR =====================

    public static void cargar(Path carpeta, ClubSeneca club) {
        ServicioIndividual servicio = club.getServicioIndividual();
        servicio.getAcademias().clear();
        servicio.getInstalaciones().clear();
        servicio.getReservas().clear();

        cargarAcademias(carpeta.resolve(ARCHIVO_ACADEMIAS), servicio);
        cargarInstalaciones(carpeta.resolve(ARCHIVO_INSTALACIONES), servicio);
        cargarInscripciones(carpeta.resolve(ARCHIVO_INSCRIPCIONES), club);
        cargarReservas(carpeta.resolve(ARCHIVO_RESERVAS), club);
        cargarPartidos(carpeta.resolve(ARCHIVO_PARTIDOS), club);
        cargarSesiones(carpeta.resolve(ARCHIVO_SESIONES), club);

        
        servicio.getReservas().sort(Comparator.comparingInt(Reserva::getId));
        servicio.reiniciarContadorDeIds();
    }

    private static void cargarAcademias(Path archivo, ServicioIndividual servicio) {
        int numeroFila = 1;
        for (String[] fila : Csv.leer(archivo)) {
            numeroFila++;
            revisarColumnas(ARCHIVO_ACADEMIAS, numeroFila, fila, 2);
            try {
                servicio.getAcademias().add(new Academia(Disciplina.valueOf(fila[0]), Integer.parseInt(fila[1])));
            } catch (IllegalArgumentException e) {
                throw error(ARCHIVO_ACADEMIAS, numeroFila, "disciplina o valor numerico invalido");
            }
        }
    }

    private static void cargarInstalaciones(Path archivo, ServicioIndividual servicio) {
        int numeroFila = 1;
        for (String[] fila : Csv.leer(archivo)) {
            numeroFila++;
            revisarColumnas(ARCHIVO_INSTALACIONES, numeroFila, fila, 6);
            try {
                servicio.getInstalaciones().add(new Instalacion(Integer.parseInt(fila[0]), fila[1], fila[2],
                        Integer.parseInt(fila[3]), Boolean.parseBoolean(fila[4]), Boolean.parseBoolean(fila[5])));
            } catch (IllegalArgumentException e) {
                throw error(ARCHIVO_INSTALACIONES, numeroFila, "valor numerico invalido");
            }
        }
    }

    private static void cargarInscripciones(Path archivo, ClubSeneca club) {
        int numeroFila = 1;
        for (String[] fila : Csv.leer(archivo)) {
            numeroFila++;
            revisarColumnas(ARCHIVO_INSCRIPCIONES, numeroFila, fila, 5);
            try {
                Socio socio = club.getSocio(fila[0]);
                Academia academia = buscarAcademia(club.getServicioIndividual(), Disciplina.valueOf(fila[1]), numeroFila);
                academia.agregarInscripcion(new InscripcionAcademia(socio, academia, Nivel.valueOf(fila[2]),
                        Integer.parseInt(fila[3]), LocalDate.parse(fila[4])));
            } catch (IllegalArgumentException | DateTimeParseException e) {
                throw error(ARCHIVO_INSCRIPCIONES, numeroFila, "disciplina, nivel, fecha o valor numerico invalido");
            }
        }
    }

    private static void cargarReservas(Path archivo, ClubSeneca club) {
        ServicioRaqueta servicio = club.getServicioRaqueta();
        int numeroFila = 1;
        for (String[] fila : Csv.leer(archivo)) {
            numeroFila++;
            revisarColumnas(ARCHIVO_RESERVAS, numeroFila, fila, 9);
            try {
                Reserva reserva = new Reserva(Integer.parseInt(fila[0]),
                        buscarInstalacion(servicio, fila[1], ARCHIVO_RESERVAS, numeroFila), bloque(fila),
                        buscarUsuario(club, fila[5], ARCHIVO_RESERVAS, numeroFila), Integer.parseInt(fila[7]),
                        fila[8].isEmpty() ? null : Modalidad.valueOf(fila[8]));
                reserva.setEstado(EstadoReserva.valueOf(fila[6]));
                servicio.getReservas().add(reserva);
            } catch (IllegalArgumentException | DateTimeParseException e) {
                throw error(ARCHIVO_RESERVAS, numeroFila, "fecha, hora, estado, modalidad o valor numerico invalido");
            }
        }
    }

    private static void cargarPartidos(Path archivo, ClubSeneca club) {
        ServicioIndividual servicio = club.getServicioRaqueta();
        int numeroFila = 1;
        for (String[] fila : Csv.leer(archivo)) {
            numeroFila++;
            revisarColumnas(ARCHIVO_PARTIDOS, numeroFila, fila, 9);
            try {
                List<Socio> ladoA = socios(club, fila[7]);
                List<Socio> ladoB = socios(club, fila[8]);
                Modalidad modalidad = (ladoA.size() + ladoB.size() == 2) ? Modalidad.SENCILLOS : Modalidad.DOBLES;
                PartidoPractica partido = new PartidoPractica(Integer.parseInt(fila[0]),
                        buscarInstalacion(servicio, fila[1], ARCHIVO_PARTIDOS, numeroFila), bloque(fila),
                        buscarUsuario(club, fila[5], ARCHIVO_PARTIDOS, numeroFila), ladoA, ladoB, modalidad);
                partido.setEstado(EstadoReserva.valueOf(fila[6]));
                partido.setJugado(partido.getEstado() == EstadoReserva.FINALIZADA);
                servicio.getReservas().add(partido);
            } catch (IllegalArgumentException | DateTimeParseException e) {
                throw error(ARCHIVO_PARTIDOS, numeroFila, "fecha, hora, estado o valor numerico invalido");
            }
        }
    }

    private static void cargarSesiones(Path archivo, ClubSeneca club) {
        ServicioRaqueta servicio = club.getServicioRaqueta();
        int numeroFila = 1;
        for (String[] fila : Csv.leer(archivo)) {
            numeroFila++;
            revisarColumnas(ARCHIVO_SESIONES, numeroFila, fila, 9);
            try {
                Usuario usuario = buscarUsuario(club, fila[5], ARCHIVO_SESIONES, numeroFila);
                if (!(usuario instanceof Entrenador)) {
                    throw error(ARCHIVO_SESIONES, numeroFila, "el usuario " + fila[5] + " no es un entrenador");
                }
                SesionEntrenamiento sesion = new SesionEntrenamiento(Integer.parseInt(fila[0]),
                        buscarInstalacion(servicio, fila[1], ARCHIVO_SESIONES, numeroFila), bloque(fila),
                        (Entrenador) usuario, TipoSesion.valueOf(fila[7]), null);
                for (Socio convocado : socios(club, fila[8])) {
                    sesion.convocar(convocado);
                }
                sesion.setEstado(EstadoReserva.valueOf(fila[6]));
                servicio.getReservas().add(sesion);
            } catch (IllegalArgumentException | DateTimeParseException e) {
                throw error(ARCHIVO_SESIONES, numeroFila, "fecha, hora, estado, tipo o valor numerico invalido");
            }
        }
    }

    // ===================== GUARDAR =====================

    public static void guardar(Path carpeta, ClubSeneca club) {
        ServicioIndividual servicio = club.getServicioRaqueta();
        guardarAcademias(carpeta.resolve(ARCHIVO_ACADEMIAS), servicio);
        guardarInstalaciones(carpeta.resolve(ARCHIVO_INSTALACIONES), servicio);
        guardarInscripciones(carpeta.resolve(ARCHIVO_INSCRIPCIONES), servicio);
        guardarReservas(carpeta.resolve(ARCHIVO_RESERVAS), servicio);
        guardarPartidos(carpeta.resolve(ARCHIVO_PARTIDOS), servicio);
        guardarSesiones(carpeta.resolve(ARCHIVO_SESIONES), servicio);
    }

    private static void guardarAcademias(Path archivo, ServicioIndividual servicio) {
        List<String[]> filas = new ArrayList<>();
        for (Academia academia : servicio.getAcademias()) {
            filas.add(new String[] { academia.getDisciplina().name(),
                    String.valueOf(academia.getMaxInstalacionesSimultaneas()) });
        }
        Csv.escribir(archivo, ENC_ACADEMIAS, filas);
    }

    private static void guardarInstalaciones(Path archivo, ServicioIndividual servicio) {
        List<String[]> filas = new ArrayList<>();
        for (Instalacion i : servicio.getInstalaciones()) {
            filas.add(new String[] { String.valueOf(i.getCodigo()), i.getNombre(), i.getDisciplina(),
                    String.valueOf(i.getCapacidad()), String.valueOf(i.isTechada()), String.valueOf(i.isHabilitada()) });
        }
        Csv.escribir(archivo, ENC_INSTALACIONES, filas);
    }

    private static void guardarInscripciones(Path archivo, ServicioIndividual servicio) {
        List<String[]> filas = new ArrayList<>();
        for (Academia academia : servicio.getAcademias()) {
            for (InscripcionAcademia i : academia.getInscripciones()) {
                filas.add(new String[] { i.getSocio().getLogin(), academia.getDisciplina().name(), i.getNivel().name(),
                        String.valueOf(i.getPuntajeRanking()), i.getFechaInscripcion().toString() });
            }
        }
        Csv.escribir(archivo, ENC_INSCRIPCIONES, filas);
    }

    private static void guardarReservas(Path archivo, ServicioIndividual servicio) {
        List<String[]> filas = new ArrayList<>();
        for (Reserva r : servicio.getReservas()) {
            if (r instanceof PartidoPractica || r instanceof SesionEntrenamiento) {
                continue; // esas van en su propio archivo
            }
            filas.add(fila(r, String.valueOf(r.getNumeroJugadores()),
                    r.getModalidad() == null ? "" : r.getModalidad().name()));
        }
        Csv.escribir(archivo, ENC_RESERVAS, filas);
    }

    private static void guardarPartidos(Path archivo, ServicioIndividual servicio) {
        List<String[]> filas = new ArrayList<>();
        for (PartidoPractica p : servicio.getHistorialPartidosPractica()) {
            filas.add(fila(p, logins(p.getLadoA()), logins(p.getLadoB())));
        }
        Csv.escribir(archivo, ENC_PARTIDOS, filas);
    }

    private static void guardarSesiones(Path archivo, ServicioIndividual servicio) {
        List<String[]> filas = new ArrayList<>();
        for (SesionEntrenamiento s : servicio.getHistorialSesiones()) {
            filas.add(fila(s, s.getTipo().name(), logins(s.getConvocados())));
        }
        Csv.escribir(archivo, ENC_SESIONES, filas);
    }

    // ===================== AYUDAS =====================

    /** Las 7 columnas comunes de una reserva, mas las 2 propias de cada archivo. */
    private static String[] fila(Reserva r, String extra1, String extra2) {
        return new String[] { String.valueOf(r.getId()), String.valueOf(r.getInstalacion().getCodigo()),
                r.getBloque().getFecha().toString(), r.getBloque().getHoraInicio().toString(),
                r.getBloque().getHoraFin().toString(), r.getResponsable().getLogin(), r.getEstado().name(),
                extra1, extra2 };
    }

    private static String logins(List<Socio> socios) {
        return socios.stream().map(Socio::getLogin).collect(Collectors.joining(Csv.SEPARADOR_LISTA));
    }

    private static List<Socio> socios(ClubSeneca club, String celda) {
        List<Socio> lista = new ArrayList<>();
        if (!celda.trim().isEmpty()) {
            for (String login : celda.split(Csv.SEPARADOR_LISTA)) {
                lista.add(club.getSocio(login.trim()));
            }
        }
        return lista;
    }

    /** Fecha, horaInicio y horaFin estan en las columnas 2, 3 y 4 de los tres archivos de reservas. */
    private static BloqueHorario bloque(String[] fila) {
        return new BloqueHorario(LocalDate.parse(fila[2]), LocalTime.parse(fila[3]), LocalTime.parse(fila[4]));
    }

    private static Academia buscarAcademia(ServicioIndividual servicio, Disciplina disciplina, int numeroFila) {
        for (Academia a : servicio.getAcademias()) {
            if (a.getDisciplina() == disciplina) {
                return a;
            }
        }
        throw error(ARCHIVO_INSCRIPCIONES, numeroFila, "no existe la academia de " + disciplina);
    }

    private static Instalacion buscarInstalacion(ServicioIndividual servicio, String codigo, String archivo,
            int numeroFila) {
        for (Instalacion i : servicio.getInstalaciones()) {
            if (String.valueOf(i.getCodigo()).equals(codigo)) {
                return i;
            }
        }
        throw error(archivo, numeroFila, "no existe la instalacion " + codigo);
    }

    private static Usuario buscarUsuario(ClubSeneca club, String login, String archivo, int numeroFila) {
        Usuario usuario = club.buscarUsuario(login);
        if (usuario == null) {
            throw error(archivo, numeroFila, "no existe el usuario " + login);
        }
        return usuario;
    }

    private static void revisarColumnas(String archivo, int numeroFila, String[] fila, int esperadas) {
        if (fila.length != esperadas) {
            throw error(archivo, numeroFila, "se esperaban " + esperadas + " columnas y se encontraron " + fila.length);
        }
    }

    private static ReglaDeNegocioException error(String archivo, int numeroFila, String motivo) {
        return new ReglaDeNegocioException("Error en " + archivo + ", fila " + numeroFila + ": " + motivo);
    }
}}
