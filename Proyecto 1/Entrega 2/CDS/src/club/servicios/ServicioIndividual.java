package club.servicios;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import club.modelo.individual.Academia;
import club.modelo.individual.BloqueHorario;
import club.modelo.individual.Disciplina;
import club.modelo.individual.EstadisticaIndividual;
import club.modelo.individual.EstadisticaTenisDeMesa;
import club.modelo.individual.EstadisticaTenisPadel;
import club.modelo.individual.EstadoAsistencia;
import club.modelo.individual.EstadoReserva;
import club.modelo.individual.FeedbackEntrenamiento;
import club.modelo.individual.InscripcionAcademia;
import club.modelo.individual.Instalacion;
import club.modelo.individual.Modalidad;
import club.modelo.individual.Nivel;
import club.modelo.individual.PartidoPractica;
import club.modelo.individual.RegistroRanking;
import club.modelo.individual.Reserva;
import club.modelo.usuarios.Empleado;
import club.modelo.usuarios.Entrenador;
import club.modelo.usuarios.Socio;
import club.modelo.usuarios.Usuario;

public class ServicioIndividual {

    public static int PUNTAJE_INICIAL = 1000;
    public static int K_ELO = 32;
    public static Nivel NIVEL_INICIAL = Nivel.PRINCIPIANTE;

    private ClubSeneca club;
    private List<Academia> academias = new ArrayList<>();
    private List<Instalacion> instalaciones = new ArrayList<>();
    private List<Reserva> reservas = new ArrayList<>();
    private int siguienteId = 1;

    public ServicioIndividual(ClubSeneca club) {
        this.club = club;
    }


    public ClubSeneca getClub() {
        return club;
    }

    public List<Academia> getAcademias() {
        return academias;
    }

    public List<Instalacion> getInstalaciones() {
        return instalaciones;
    }

    public List<Reserva> getReservas() {
        return reservas;
    }

    public int nuevoId() {
        return siguienteId++;
    }

    
    public void reiniciarContadorDeIds() {
        siguienteId = reservas.stream().mapToInt(Reserva::getId).max().orElse(0) + 1;
    }

    
    /* Este método verifica que si la condicion no se cumple, rechaza la operacion */
    private void exigir(boolean condicion, String motivo) {
        if (!condicion) {
            throw new ReglaDeNegocioException(motivo);
        }
    }

    private Academia buscarAcademia(Disciplina disciplina) {
        exigir(disciplina != null, "Debe indicar la disciplina");
        for (Academia a : academias) {
            if (a.getDisciplina() == disciplina) {
                return a;
            }
        }
        throw new ReglaDeNegocioException("No existe la academia de " + disciplina);
    }

    private Disciplina disciplinaDe(Instalacion instalacion) {
        try {
            return Disciplina.valueOf(instalacion.getDisciplina());
        } catch (IllegalArgumentException e) {
            throw new ReglaDeNegocioException(
                    "La instalacion " + instalacion.getNombre() + " no es de una disciplina de raqueta");
        }
    }

    private void validarBloque(BloqueHorario b) {
        exigir(b != null && b.getFecha() != null && b.getHoraInicio() != null && b.getHoraFin() != null,
                "El bloque horario esta incompleto");
        exigir(b.getHoraFin().isAfter(b.getHoraInicio()), "La hora de fin debe ser posterior a la de inicio");
    }

    private void validarPeriodo(LocalDate desde, LocalDate hasta) {
        exigir(desde != null && hasta != null && !hasta.isBefore(desde), "El periodo es invalido");
    }

   
    private Socio socioDe(Usuario usuario) {
        exigir(usuario != null, "Debe indicar el usuario");
        if (usuario instanceof Socio) {
            return (Socio) usuario;
        }
    }

    private boolean contiene(List<Socio> lista, Socio socio) {
        return lista.stream().anyMatch(s -> s.getLogin().equals(socio.getLogin()));
    }

    private <T extends Reserva> T agregar(T reserva) {
        reservas.add(reserva);
        return reserva;
    }

    
    private List<Reserva> activasQueSeCruzan(BloqueHorario bloque) {
        return reservas.stream().filter(r -> r.estaActiva() && r.getBloque().seSolapaCon(bloque))
                .collect(Collectors.toList());
    }

    private boolean hayChoque(Instalacion instalacion, BloqueHorario bloque) {
        return activasQueSeCruzan(bloque).stream()
                .anyMatch(r -> r.getInstalacion().getCodigo() == instalacion.getCodigo());
    }

    private long instalacionesOcupadas(Disciplina disciplina, BloqueHorario bloque) {
        return activasQueSeCruzan(bloque).stream()
                .filter(r -> r.getInstalacion().getDisciplina().equals(disciplina.name()))
                .map(r -> r.getInstalacion().getCodigo()).distinct().count();
    }

    
    
    private void validarUso(Instalacion instalacion, BloqueHorario bloque, Disciplina disciplina, int jugadores,
            Modalidad modalidad, List<Socio> socios) {
        exigir(instalacion.isHabilitada(), "La instalacion esta deshabilitada");
        exigir(instalacion.admite(jugadores, modalidad), "PADEL".equals(instalacion.getDisciplina())
                ? "El padel solo se juega en dobles con 4 jugadores (recibido: " + jugadores + " jugadores, " + modalidad + ")"
                : "Numero de jugadores invalido: " + jugadores + " no corresponde a " + modalidad);
        exigir(!hayChoque(instalacion, bloque), "La instalacion ya esta ocupada en ese horario");
        int max = buscarAcademia(disciplina).getMaxInstalacionesSimultaneas();
        exigir(instalacionesOcupadas(disciplina, bloque) < max,
                "Cupo lleno: todas las instaalciones de las disciplina estan ocupadas en ese bloque");
    }

    // Inscripciones 

    public InscripcionAcademia inscribirEnAcademia(Socio socio, Disciplina disciplina) {
        Academia academia = buscarAcademia(disciplina);
        exigir(socio.isActivo(), "El socio " + socio.getLogin() + " esta dado de baja");
        exigir(!academia.estaInscrito(socio),
                "El socio ya esta inscrito en esta academia");
        InscripcionAcademia inscripcion = new InscripcionAcademia(socio, academia, NIVEL_INICIAL, PUNTAJE_INICIAL,
                LocalDate.now());
        academia.agregarInscripcion(inscripcion);
        return inscripcion;
    }

    public void asignarNivel(Entrenador entrenador, Socio socio, Disciplina disciplina, Nivel nivel) {
        exigir(entrenador != null, "Solo un entrenador puede asignar niveles");
        InscripcionAcademia inscripcion = buscarAcademia(disciplina).getInscripcionDe(socio);
    }

    // ================= Reservas (P2) =================

  

    public Reserva reservar(Usuario usuario, Instalacion instalacion, BloqueHorario bloque, int jugadores,
            Modalidad modalidad) {
        Socio socio = socioDe(usuario);
        validarBloque(bloque);
        Disciplina disciplina = disciplinaDe(instalacion);
        exigir(buscarAcademia(disciplina).estaInscrito(socio),
                "El socio no esta inscrito en la academia ");
        validarUso(instalacion, bloque, disciplina, jugadores, modalidad, Collections.singletonList(socio));
        return agregar(new Reserva(nuevoId(), instalacion, bloque, usuario, jugadores, modalidad));
    }

    public void cancelarReserva(Usuario usuario, Reserva reserva) {
        exigir(reserva != null, "Debe indicar la reserva");
        reserva.cancelar(usuario);
    }

    public void registrarIngreso(Socio socio, Reserva reserva, LocalDateTime ahora) {
        exigir(reserva.estaActiva(),
                "La reserva no esta activa");
        exigir(reserva.getBloque().contiene(ahora),
                "El ingreso esta fuera del bloque horario de la reserva ");
        reserva.registrarIngreso();
    }

    // ================= Partidos de practica

    public PartidoPractica programarPartido(Usuario usuario, Instalacion instalacion, BloqueHorario bloque,
            List<Socio> ladoA, List<Socio> ladoB) {
        socioDe(usuario); 
        validarBloque(bloque);
        List<Socio> todos = new ArrayList<>(ladoA);
        todos.addAll(ladoB);
        Disciplina disciplina = disciplinaDe(instalacion);
        Academia academia = buscarAcademia(disciplina);
        for (Socio s : todos) {
            exigir(academia.estaInscrito(s),
                    "El socio no esta inscrito en la academia ");
        }
        for (Socio a : ladoA) {
            for (Socio b : ladoB) {
                Nivel na = academia.getInscripcionDe(a).getNivel();
                Nivel nb = academia.getInscripcionDe(b).getNivel();
                exigir(Nivel.sonCompatibles(na, nb) || a.esEntrenador() || b.esEntrenador(),
                        "Niveles no compatibles: deben ser iguales o adyacentes salvo que alguno sea entrenador");
            }
        }
        Modalidad modalidad = (todos.size() == 2) ? Modalidad.SENCILLOS : Modalidad.DOBLES;
        validarUso(instalacion, bloque, disciplina, todos.size(), modalidad, todos);
        return agregar(new PartidoPractica(nuevoId(), instalacion, bloque, usuario, ladoA, ladoB, modalidad));
    }

    public void registrarResultado(PartidoPractica partido, List<EstadisticaIndividual> estadisticas) {
        exigir(!partido.isJugado(), "El partido ya fue jugado");
        Disciplina disciplina = disciplinaDe(partido.getInstalacion());
        Academia academia = buscarAcademia(disciplina);
        
        Set<String> vistos = new HashSet<>();
        int setsA = 0;
        int setsB = 0;
        for (EstadisticaIndividual e : estadisticas) {
            Socio s = e.getSocio();
            boolean enLadoA = contiene(partido.getLadoA(), s);
            e.validarValores();
            if (enLadoA) {
                setsA = Math.max(setsA, e.getSetsGanados());
            } else {
                setsB = Math.max(setsB, e.getSetsGanados());
            }
        }
        exigir(setsA != setsB, "El resultado no puede ser empate");

        // ELO simplificado: lo que gana un lado lo pierde el otro
        double ra = promedioPuntaje(academia, partido.getLadoA());
        double rb = promedioPuntaje(academia, partido.getLadoB());
        double esperadoA = 1.0 / (1.0 + Math.pow(10.0, (rb - ra) / 400.0));
        int deltaA = (int) Math.round(K_ELO * ((setsA > setsB ? 1.0 : 0.0) - esperadoA));

        partido.registrarResultado(estadisticas);
        aplicarCambio(partido, academia, disciplina, partido.getLadoA(), deltaA);
        aplicarCambio(partido, academia, disciplina, partido.getLadoB(), -deltaA);
    }

    private double promedioPuntaje(Academia academia, List<Socio> lado) {
        return lado.stream().mapToInt(s -> academia.getInscripcionDe(s).getPuntajeRanking()).average().orElse(0);
    }

    private void aplicarCambio(PartidoPractica partido, Academia academia, Disciplina disciplina, List<Socio> lado,
            int delta) {
        for (Socio s : lado) {
            InscripcionAcademia insc = academia.getInscripcionDe(s);
            int anterior = insc.getPuntajeRanking();
            RegistroRanking registro = new RegistroRanking(s, disciplina, partido.getBloque().getFecha(), anterior,
                    anterior + delta, partido);
            insc.actualizarPuntaje(anterior + delta);
            partido.agregarRegistro(registro);
            insc.agregarRegistro(registro);
        }
    }

    

    // ================= Consultas y reportes =================

    public int getRanking(Socio socio, Disciplina disciplina) {
        InscripcionAcademia insc = buscarAcademia(disciplina).getInscripcionDe(socio);
        exigir(insc != null,
                "El socio " + socio.getLogin() + " no esta inscrito en " + disciplina + ": no tiene ranking en ella");
        return insc.getPuntajeRanking();
    }

    public List<EstadisticaRaqueta> getEstadisticas(Socio socio, Disciplina disciplina) {
        exigir(buscarAcademia(disciplina).estaInscrito(socio),
                "El socio " + socio.getLogin() + " no esta inscrito en " + disciplina + ": no tiene estadisticas en ella");
        List<EstadisticaRaqueta> resultado = new ArrayList<>();
        for (PartidoPractica p : getHistorialPartidosPractica()) {
            if (p.getInstalacion().getDisciplina().equals(disciplina.name())) {
                for (EstadisticaRaqueta e : p.getEstadisticas()) {
                    if (e.getSocio().getLogin().equals(socio.getLogin())) {
                        resultado.add(e);
                    }
                }
            }
        }
        return resultado;
    }

    public List<Reserva> getHistorialReservas() {
        return new ArrayList<>(reservas);
    }

    public List<PartidoPractica> getHistorialPartidosPractica() {
        return reservas.stream().filter(r -> r instanceof PartidoPractica).map(r -> (PartidoPractica) r)
                .collect(Collectors.toList());
    }

    public List<SesionEntrenamiento> getHistorialSesiones() {
        return reservas.stream().filter(r -> r instanceof SesionEntrenamiento).map(r -> (SesionEntrenamiento) r)
                .collect(Collectors.toList());
    }

    public String reporteAsistencia(Disciplina disciplina, LocalDate desde, LocalDate hasta) {
        validarPeriodo(desde, hasta);
        Map<String, List<FeedbackEntrenamiento>> porSocio = new TreeMap<>();
        for (SesionEntrenamiento s : getHistorialSesiones()) {
            LocalDate fecha = s.getBloque().getFecha();
            if (s.getInstalacion().getDisciplina().equals(disciplina.name()) && !fecha.isBefore(desde)
                    && !fecha.isAfter(hasta)) {
                for (FeedbackEntrenamiento f : s.getFeedbacks()) {
                    porSocio.computeIfAbsent(f.getSocio().getLogin(), k -> new ArrayList<>()).add(f);
                }
            }
        }
        exigir(!porSocio.isEmpty(),
                "No hay datos de asistencia de " + disciplina + " entre " + desde + " y " + hasta);
        StringBuilder sb = new StringBuilder("Asistencia " + disciplina + " (" + desde + " a " + hasta + "):");
        for (Map.Entry<String, List<FeedbackEntrenamiento>> e : porSocio.entrySet()) {
            List<FeedbackEntrenamiento> fs = e.getValue();
            OptionalDouble promedio = fs.stream().filter(f -> f.getNota() != null).mapToInt(FeedbackEntrenamiento::getNota)
                    .average();
            sb.append("\n  ").append(e.getKey())
                    .append(": asistio=").append(contar(fs, EstadoAsistencia.ASISTIO))
                    .append(", tarde=").append(contar(fs, EstadoAsistencia.TARDE))
                    .append(", falto=").append(contar(fs, EstadoAsistencia.FALTO))
                    .append(", nota promedio=")
                    .append(promedio.isPresent() ? String.format(Locale.ROOT, "%.1f", promedio.getAsDouble()) : "sin notas");
        }
        return sb.toString();
    }

    private long contar(List<FeedbackEntrenamiento> lista, EstadoAsistencia asistencia) {
        return lista.stream().filter(f -> f.getAsistencia() == asistencia).count();
    }

    public List<RegistroRanking> reporteEvolucionRanking(Disciplina disciplina, LocalDate desde, LocalDate hasta) {
        validarPeriodo(desde, hasta);
        List<RegistroRanking> lista = new ArrayList<>();
        for (PartidoPractica p : getHistorialPartidosPractica()) {
            for (RegistroRanking r : p.getRegistros()) {
                if (r.getDisciplina() == disciplina && !r.getFecha().isBefore(desde) && !r.getFecha().isAfter(hasta)) {
                    lista.add(r);
                
             
    }

   