package club.servicios;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import club.modelo.conjunto.AcumuladoJugador;
import club.modelo.conjunto.Categoria;
import club.modelo.conjunto.Competencia;
import club.modelo.conjunto.Deporte;
import club.modelo.conjunto.Equipo;
import club.modelo.conjunto.EstadisticaBasket;
import club.modelo.conjunto.EstadisticaFutbol;
import club.modelo.conjunto.EstadisticaJugador;
import club.modelo.conjunto.EstadisticaVoleibol;
import club.modelo.conjunto.FilaPosicion;
import club.modelo.conjunto.InscripcionCompetencia;
import club.modelo.conjunto.InstitucionRival;
import club.modelo.conjunto.Partido;
import club.modelo.conjunto.PartidoCompetencia;
import club.modelo.usuarios.Socio;
import club.modelo.conjunto.Fase;
import club.modelo.conjunto.TipoFase;
import club.modelo.conjunto.PartidoExterno;

// REGISTRAR COMPETENCIA, PARTIDOS, RIVALES, TABLAS DE POSICIONES
public class ServicioCompetencias {

    private final ServicioPartidos servicioPartidos;
    private final List<Competencia> competencias;
    private int contadorCompetencias;

    public ServicioCompetencias(ServicioPartidos servicioPartidos) {
        this.servicioPartidos = servicioPartidos;
        this.competencias = new ArrayList<>();
        this.contadorCompetencias = 0;
    }

    public List<Competencia> getCompetencias() {
        return new ArrayList<>(competencias);
    }

    public Competencia getCompetencia(String id) {
        for (Competencia competencia : competencias) {
            if (competencia.getId().equals(id)) {
                return competencia;
            }
        }
        return null;
    }

    // registrar competencia 

    public Competencia registrarCompetencia(String nombre, String organizador, String temporada,
                                            Deporte deporte, Categoria categoria) {
    	String id = String.format("CP-%03d", contadorCompetencias + 1);

        Competencia nueva = new Competencia(
            id, nombre, organizador, temporada, deporte, categoria
        );

        for (Competencia existente : competencias) {

            boolean mismoNombre =
                existente.getNombre().equalsIgnoreCase(nueva.getNombre());

            boolean mismoDeporte =
                existente.getDeporte().getNombre().equalsIgnoreCase(
                    nueva.getDeporte().getNombre()
                );

            boolean mismaTemporada =
                existente.getTemporada().equals(nueva.getTemporada());

            boolean mismaCategoria =
                existente.getCategoria() == nueva.getCategoria();

            if (mismoNombre && mismoDeporte
                    && mismaTemporada && mismaCategoria) {

                throw new ReglaDeNegocioException(
                    "Ya existe una competencia con ese nombre, deporte, temporada y categoría"
                );
            }
        }
        competencias.add(nueva);
        contadorCompetencias++;
        return nueva;
    }
    public void agregarCompetenciaCargada(Competencia competencia) {
        if (competencia == null) {
            throw new ReglaDeNegocioException("La competencia no puede ser nula");
        }
        if (getCompetencia(competencia.getId()) != null) {
            throw new ReglaDeNegocioException("Ya existe una competencia con el id " + competencia.getId());
        }
        int numero;
        try {
            numero = Integer.parseInt(competencia.getId().substring(3));
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            throw new ReglaDeNegocioException("Id de competencia inválido: " + competencia.getId());
        }
        competencias.add(competencia);
        if (numero > contadorCompetencias) {
            contadorCompetencias = numero;
        }
    }

    // Rivales

    public InstitucionRival registrarRival(Competencia competencia, String nombre) {
        exigirCompetencia(competencia);
        InstitucionRival rival = new InstitucionRival(nombre);
        competencia.agregarRival(rival);
        return rival;
    }

    //inscribir nomina

    public InscripcionCompetencia inscribirEquipo(Competencia competencia, Equipo equipo,
                                                  List<Socio> nomina) {
    	exigirCompetencia(competencia);

        if (equipo == null || nomina == null) {
            throw new ReglaDeNegocioException(
                "El equipo y la nómina son obligatorios"
            );
        }
        if (!equipo.isActivo()) {
            throw new ReglaDeNegocioException(
                "No se puede inscribir un equipo dado de baja"
            );
        }

        if (!equipo.getDeporte().getNombre().equalsIgnoreCase(
                competencia.getDeporte().getNombre())) {

            throw new ReglaDeNegocioException(
                "El deporte del equipo no coincide con el de la competencia"
            );
        }

        if (equipo.getCategoria() != competencia.getCategoria()) {
            throw new ReglaDeNegocioException(
                "La categoría del equipo no coincide con la de la competencia"
            );
        }

        if (nomina.size() < competencia.getDeporte().getMinJugadores()) {
            throw new ReglaDeNegocioException(
                "La nómina no alcanza el mínimo de jugadores requerido"
            );
        }

        List<String> loginsRegistrados = new ArrayList<>();

        for (Socio socio : nomina) {

            if (socio == null) {
                throw new ReglaDeNegocioException(
                    "La nómina no puede contener jugadores nulos"
                );
            }

            if (loginsRegistrados.contains(socio.getLogin())) {
                throw new ReglaDeNegocioException(
                    "La nómina contiene jugadores repetidos"
                );
            }

            loginsRegistrados.add(socio.getLogin());
        }
        for (Socio socio : nomina) {

            boolean encontrado = false;

            for (club.modelo.conjunto.IntegranteEquipo integrante
                    : equipo.getIntegrantesActivos()) {

                if (integrante.getSocio().getLogin().equals(socio.getLogin())
                        && integrante.isHabilitado()) {

                    encontrado = true;
                    break;
                }
            }

            if (!encontrado) {
                throw new ReglaDeNegocioException(
                    "Todos los jugadores de la nómina deben pertenecer al equipo y estar habilitados"
                );
            }
        }
        InscripcionCompetencia inscripcion =
            new InscripcionCompetencia(equipo, nomina);

        competencia.agregarInscripcion(inscripcion);

        return inscripcion;
    }

    // registrar partido de competencia.

    public PartidoCompetencia registrarPartido(Competencia competencia, Equipo equipo,
                                               String nombreRival, int jornada, boolean clubEsLocal,
                                               String escenario, LocalDate fecha) {
    	exigirCompetencia(competencia);

        if (equipo == null) {
            throw new ReglaDeNegocioException(
                "El equipo es obligatorio"
            );
        }

        InscripcionCompetencia inscripcion =
            competencia.getInscripcion(equipo);

        if (inscripcion == null) {
            throw new ReglaDeNegocioException(
                "El equipo no está inscrito en esta competencia"
            );
        }

        InstitucionRival rival = competencia.getRival(nombreRival);

        if (rival == null) {
            throw new ReglaDeNegocioException(
                "La institución rival no está registrada en esta competencia"
            );
        }
        String id = servicioPartidos.generarIdPartidoCompetencia();

        PartidoCompetencia partido = new PartidoCompetencia(
            id,
            competencia.getId(),
            jornada,
            inscripcion,
            rival,
            clubEsLocal,
            escenario,
            fecha
        );
        servicioPartidos.agregarPartidoCompetencia(partido);

        return partido;
    }

    public void registrarResultado(PartidoCompetencia partido, int marcadorClub, int marcadorRival) {
    	if (partido == null) {
            throw new ReglaDeNegocioException(
                "El partido es obligatorio"
            );
        }

        int marcadorLocal;
        int marcadorVisitante;

        if (partido.clubEsLocal()) {
            marcadorLocal = marcadorClub;
            marcadorVisitante = marcadorRival;
        } else {
            marcadorLocal = marcadorRival;
            marcadorVisitante = marcadorClub;
        }

        partido.cerrar(marcadorLocal, marcadorVisitante);
    }

    public List<PartidoCompetencia> getPartidosDe(Competencia competencia) {
        exigirCompetencia(competencia);
        List<PartidoCompetencia> resultado = new ArrayList<>();
        for (Partido partido : servicioPartidos.getPartidos()) {
            if (partido instanceof PartidoCompetencia
                    && ((PartidoCompetencia) partido).getIdCompetencia().equals(competencia.getId())) {
                resultado.add((PartidoCompetencia) partido);
            }
        }
        return resultado;
    }

    public PartidoExterno registrarPartidoExterno(
            Competencia competencia,
            String nombreRivalLocal,
            String nombreRivalVisitante,
            int jornada,
            String escenario,
            LocalDate fecha) {

        exigirCompetencia(competencia);

        InstitucionRival rivalLocal =
            competencia.getRival(nombreRivalLocal);

        InstitucionRival rivalVisitante =
            competencia.getRival(nombreRivalVisitante);

        if (rivalLocal == null || rivalVisitante == null) {
            throw new ReglaDeNegocioException(
                "Ambas instituciones deben estar registradas en esta competencia"
            );
        }
        String id = servicioPartidos.generarIdPartidoExterno();
        
        PartidoExterno partido = new PartidoExterno(
            id,
            competencia.getId(),
            jornada,
            competencia.getDeporte(),
            rivalLocal,
            rivalVisitante,
            escenario,
            fecha
        );

        servicioPartidos.agregarPartidoExterno(partido);

        return partido;
    }


    public void registrarResultadoExterno(PartidoExterno partido, int marcadorLocal, int marcadorVisitante) {
        if (partido == null) {
            throw new ReglaDeNegocioException("El partido es obligatorio");
        }
        partido.cerrar(marcadorLocal, marcadorVisitante);
    }

    public List<PartidoExterno> getPartidosExternosDe(Competencia competencia) {
        exigirCompetencia(competencia);
        List<PartidoExterno> resultado = new ArrayList<>();
        for (Partido partido : servicioPartidos.getPartidos()) {
            if (partido instanceof PartidoExterno
                    && ((PartidoExterno) partido).getIdCompetencia().equals(competencia.getId())) {
                resultado.add((PartidoExterno) partido);
            }
        }
        return resultado;
    }

    // tabla de posiciones

    public List<FilaPosicion> tablaPosiciones(Competencia competencia) {
        exigirCompetencia(competencia);
        List<FilaPosicion> filas = new ArrayList<>();

        for (InscripcionCompetencia inscripcion : competencia.getInscripciones()) {
            filas.add(new FilaPosicion(
                inscripcion.getEquipo().getNombre(), true
            ));
        }

        for (InstitucionRival rival : competencia.getRivales()) {
            filas.add(new FilaPosicion(rival.getNombre(), false));
        }

        List<PartidoCompetencia> partidos = getPartidosDe(competencia);
        List<PartidoExterno> partidosExternos = getPartidosExternosDe(competencia);

        boolean hayPartidosJugados = false;

        for (PartidoCompetencia partido : partidos) {
            if (partido.estaJugado()) {
                hayPartidosJugados = true;
                break;
            }
        }

        if (!hayPartidosJugados) {
            for (PartidoExterno partido : partidosExternos) {
                if (partido.estaJugado()) {
                    hayPartidosJugados = true;
                    break;
                }
            }
        }

        if (!hayPartidosJugados) {
            throw new ReglaDeNegocioException(
                "Aún no hay partidos jugados en esta competencia"
            );
        }

        for (PartidoCompetencia partido : partidos) {

            if (!partido.estaJugado()) {
                continue;
            }

            int marcadorClub = partido.getMarcadorClub();
            int marcadorRival = partido.getMarcadorRival();

            String nombreClub = partido.getEquipoClub().getNombre();
            String nombreRival = partido.getRival().getNombre();

            FilaPosicion filaClub = null;
            FilaPosicion filaRival = null;

            for (FilaPosicion fila : filas) {

                if (fila.esClub() && fila.getNombre().equals(nombreClub)) {
                    filaClub = fila;
                }

                if (!fila.esClub()
                        && fila.getNombre().equalsIgnoreCase(nombreRival)) {
                    filaRival = fila;
                }
            }

            if (filaClub == null || filaRival == null) {
                throw new ReglaDeNegocioException(
                    "No se encontraron los equipos del partido en la tabla"
                );
            }

            filaClub.registrar(
                marcadorClub,
                marcadorRival,
                competencia.getDeporte().puntosPara(marcadorClub, marcadorRival)
            );

            filaRival.registrar(
                marcadorRival,
                marcadorClub,
                competencia.getDeporte().puntosPara(marcadorRival, marcadorClub)
            );
        }

    for (PartidoExterno partido : partidosExternos) {

        if (!partido.estaJugado()) {
            continue;
        }

        int marcadorLocal = partido.getMarcadorLocal();
        int marcadorVisitante = partido.getMarcadorVisitante();

        String nombreLocal = partido.getRivalLocal().getNombre();
        String nombreVisitante = partido.getRivalVisitante().getNombre();

        FilaPosicion filaLocal = null;
        FilaPosicion filaVisitante = null;

        for (FilaPosicion fila : filas) {

            if (!fila.esClub()
                    && fila.getNombre().equalsIgnoreCase(nombreLocal)) {
                filaLocal = fila;
            }

            if (!fila.esClub()
                    && fila.getNombre().equalsIgnoreCase(nombreVisitante)) {
                filaVisitante = fila;
            }
        }

        if (filaLocal == null || filaVisitante == null) {
            throw new ReglaDeNegocioException(
                "No se encontraron los equipos del partido en la tabla"
            );
        }

        filaLocal.registrar(
            marcadorLocal,
            marcadorVisitante,
            competencia.getDeporte().puntosPara(marcadorLocal, marcadorVisitante)
        );

        filaVisitante.registrar(
            marcadorVisitante,
            marcadorLocal,
            competencia.getDeporte().puntosPara(marcadorVisitante, marcadorLocal)
        );
    }

    filas.sort((f1, f2) -> {

        int comparacionPuntos =
            Integer.compare(f2.getPuntos(), f1.getPuntos());

        if (comparacionPuntos != 0) {
            return comparacionPuntos;
        }

        int comparacionDiferencia =
            Integer.compare(f2.getDiferencia(), f1.getDiferencia());

        if (comparacionDiferencia != 0) {
            return comparacionDiferencia;
        }

        return Integer.compare(
            f2.getGolesFavor(),
            f1.getGolesFavor()
        );
    });

    return filas;
}


    // estadisticas acumuladas

    public List<AcumuladoJugador> estadisticasAcumuladas(Competencia competencia) {
        exigirCompetencia(competencia);
        Map<String, AcumuladoJugador> porLogin = new LinkedHashMap<>();

        for (PartidoCompetencia partido : getPartidosDe(competencia)) {
            if (!partido.estaJugado()) {
                continue;
            }
            for (EstadisticaJugador estadistica : partido.getEstadisticas()) {
                Socio jugador = estadistica.getJugador();
                AcumuladoJugador acumulado = porLogin.get(jugador.getLogin());
                if (acumulado == null) {
                    acumulado = new AcumuladoJugador(jugador.getLogin(), jugador.getNombre());
                    porLogin.put(jugador.getLogin(), acumulado);
                }
                acumulado.sumar(primerCampo(estadistica), segundoCampo(estadistica));
            }
        }

        List<AcumuladoJugador> lista = new ArrayList<>(porLogin.values());
        lista.sort((x, y) -> {
            if (x.getA() != y.getA()) {
                return Integer.compare(y.getA(), x.getA());
            }
            if (x.getB() != y.getB()) {
                return Integer.compare(y.getB(), x.getB());
            }
            return x.getLogin().compareTo(y.getLogin());
        });
        return lista;
    }

    public boolean participa(Competencia competencia, Socio socio) {
        exigirCompetencia(competencia);
        if (socio == null) {
            return false;
        }
        for (InscripcionCompetencia inscripcion : competencia.getInscripciones()) {
            if (inscripcion.tieneJugador(socio.getLogin())) {
                return true;
            }
        }
        return false;
    }
    
    public AcumuladoJugador acumuladoDe(Competencia competencia, Socio socio) {
        for (AcumuladoJugador acumulado : estadisticasAcumuladas(competencia)) {
            if (acumulado.getLogin().equals(socio.getLogin())) {
                return acumulado;
            }
        }
        return null;
    }

    public int[] posicionesDe(Competencia competencia, Socio socio) {
        AcumuladoJugador propio = acumuladoDe(competencia, socio);
        if (propio == null) {
            return null;
        }
        int posicionA = 1;
        int posicionB = 1;
        for (AcumuladoJugador otro : estadisticasAcumuladas(competencia)) {
            if (otro.getA() > propio.getA()) {
                posicionA++;
            }
            if (otro.getB() > propio.getB()) {
                posicionB++;
            }
        }
        return new int[] { posicionA, posicionB };
    }

    private int primerCampo(EstadisticaJugador e) {
        if (e instanceof EstadisticaFutbol) {
            return ((EstadisticaFutbol) e).getGoles();
        }
        if (e instanceof EstadisticaBasket) {
            return ((EstadisticaBasket) e).getPuntos();
        }
        return ((EstadisticaVoleibol) e).getAces();
    }

    private int segundoCampo(EstadisticaJugador e) {
        if (e instanceof EstadisticaFutbol) {
            return ((EstadisticaFutbol) e).getAsistencias();
        }
        if (e instanceof EstadisticaBasket) {
            return ((EstadisticaBasket) e).getRebotes();
        }
        return ((EstadisticaVoleibol) e).getBloqueos();
    }

    private void exigirCompetencia(Competencia competencia) {
        if (competencia == null || !competencias.contains(competencia)) {
            throw new ReglaDeNegocioException("La competencia no existe");
        }
    }
    
    public Fase definirFase(Competencia competencia, int orden, String nombre,
                            TipoFase tipo, int clasificados) {
        exigirCompetencia(competencia);
        Fase fase = new Fase(orden, nombre, tipo, clasificados);
        competencia.agregarFase(fase);
        return fase;
    }
}
