package club.servicios;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import club.modelo.conjunto.Equipo;
import club.modelo.conjunto.IntegranteEquipo;
import club.modelo.conjunto.Partido;
import club.modelo.conjunto.PartidoInterno;
import club.modelo.conjunto.TipoTarjeta;
import club.modelo.usuarios.Socio;
import club.modelo.conjunto.EstadisticaFutbol;
import club.modelo.conjunto.EstadisticaBasket;
import club.modelo.conjunto.EstadisticaVoleibol;

// PROGRAMAR PARTIDOS INTERNOS, REGISTRA TARJETAS, ESTADISTICAS Y CIERRA PARTIDOS CON LOS RESULTADOS 
public class ServicioPartidos {

    private final List<Partido> partidos;
    private int contadorPartidos;


    public ServicioPartidos() {
        this.partidos = new ArrayList<>();
        this.contadorPartidos = 0;
    }

    public List<Partido> getPartidos() {
        return new ArrayList<>(partidos);
    }

    public Partido getPartido(String id) {
    	if (id == null) {
    	    return null;
    	}
    	for (Partido partido : partidos) {

            if (partido.getId().equals(id)) {
                return partido;
            }
        }

        return null;
    }

    public PartidoInterno programarPartidoInterno(Equipo local, Equipo visitante, LocalDate fecha) {
    	if (fecha == null) {
            throw new ReglaDeNegocioException(
                "La fecha del partido no puede ser nula"
            );
        }

        if (local == null || visitante == null) {
            throw new ReglaDeNegocioException(
                "Los equipos no pueden ser nulos"
            );
        }

        if (!local.isActivo() || !visitante.isActivo()) {
            throw new ReglaDeNegocioException(
                "Ambos equipos deben estar activos"
            );
        }

        if (!local.tieneMinimoHabilitados()
                || !visitante.tieneMinimoHabilitados()) {
            throw new ReglaDeNegocioException(
                "Los equipos no tienen suficientes jugadores habilitados"
            );
        }
        String id = String.format("PT-%03d", contadorPartidos + 1);
        
        PartidoInterno partido = new PartidoInterno(
            id,
            local,
            visitante,
            fecha
        );
        contadorPartidos++;

        partidos.add(partido);

        return partido;
    }

    public boolean registrarTarjeta(PartidoInterno partido, Socio socio, TipoTarjeta tipo) {
    	if (partido == null || socio == null || tipo == null) {
            throw new ReglaDeNegocioException(
                "El partido, el socio y el tipo de tarjeta son obligatorios"
            );
        }

        if (partido.estaJugado()) {
            throw new ReglaDeNegocioException(
                "No se pueden registrar tarjetas en un partido jugado"
            );
        }

        if (!partido.getDeporte().usaTarjetas()) {
            throw new ReglaDeNegocioException(
                "Este deporte no permite registrar tarjetas"
            );
        }

        if (partido.getEstadisticaJugador(socio) != null) {
            throw new ReglaDeNegocioException(
                "Registra las tarjetas del jugador antes de sus estadísticas"
            );
        }

        IntegranteEquipo integranteEncontrado = null;

        for (IntegranteEquipo integrante : partido.getLocal().getIntegrantesActivos()) {

            if (integrante.getSocio().getLogin().equals(socio.getLogin())) {
                integranteEncontrado = integrante;
                break;
            }
        }
        if (integranteEncontrado == null) {

            for (IntegranteEquipo integrante : partido.getVisitante().getIntegrantesActivos()) {

                if (integrante.getSocio().getLogin().equals(socio.getLogin())) {
                    integranteEncontrado = integrante;
                    break;
                }
            }
        }

        if (integranteEncontrado == null) {
            throw new ReglaDeNegocioException(
                "El socio no pertenece a ninguno de los equipos del partido"
            );
        }
        if (!integranteEncontrado.isHabilitado()) {
            throw new ReglaDeNegocioException(
                "El jugador está suspendido y no puede recibir tarjetas"
            );
        }
        switch (tipo) {

        	case AMARILLA:
        		integranteEncontrado.registrarAmarilla();
        		partido.registrarAmarillaPartido(
                integranteEncontrado.getSocio().getLogin());
        		break;

            case ROJA:
                integranteEncontrado.registrarRoja();
                partido.registrarRojaPartido(integranteEncontrado.getSocio().getLogin());
                break;

            default:
                throw new ReglaDeNegocioException(
                    "Tipo de tarjeta no válido");
                
        }
        partido.marcarSancionado(
        	    integranteEncontrado.getSocio().getLogin()
        		);
    
        return !integranteEncontrado.isHabilitado();
    }

    public void registrarResultado(PartidoInterno partido, int marcadorLocal, int marcadorVisitante) {
    	if (partido == null) {
            throw new ReglaDeNegocioException(
                "El partido no puede ser nulo."
            );
        }
    	if (partido.estaJugado()) {
            throw new ReglaDeNegocioException(
                    "El partido ya fue jugado"
                );
            }

        List<IntegranteEquipo> suspendidos = new ArrayList<>();

        for (IntegranteEquipo integrante :
                partido.getLocal().getIntegrantesActivos()) {

            if (integrante.isSuspendidoInterno()
                    && !partido.fueSancionado(
                        integrante.getSocio().getLogin())) {

                suspendidos.add(integrante);
            }
        }

        for (IntegranteEquipo integrante :
                partido.getVisitante().getIntegrantesActivos()) {

        	if (partido.getDeporte().usaTarjetas()
        	        && integrante.isSuspendidoInterno()
        	        && !partido.fueSancionado(
        	            integrante.getSocio().getLogin())) {

                suspendidos.add(integrante);
            }
        }
        partido.cerrar(marcadorLocal, marcadorVisitante);

        for (IntegranteEquipo integrante : suspendidos) {
            integrante.levantarSuspensionInterno();
        }
    }

    public void agregarPartidoCargado(Partido partido) {
    	if (partido == null) {
            throw new ReglaDeNegocioException("El partido no puede ser nulo");
        }
        if (getPartido(partido.getId()) != null) {
            throw new ReglaDeNegocioException("Ya existe un partido con el id " + partido.getId());
        }

        int numero;
        try {
            numero = Integer.parseInt(partido.getId().substring(3));
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            throw new ReglaDeNegocioException("Id de partido inválido: " + partido.getId());
        }

        partidos.add(partido);
        if (numero > contadorPartidos) {
            contadorPartidos = numero;
        }
    }
    public void registrarEstadisticaFutbol(
            PartidoInterno partido,
            Socio socio,
            int goles,
            int asistencias) {

        if (partido == null || socio == null) {
            throw new ReglaDeNegocioException(
                "El partido y el jugador son obligatorios"
            );
        }

        if (!partido.getDeporte().getNombre().equalsIgnoreCase("FUTBOL")) {
            throw new ReglaDeNegocioException(
                "Este método solo funciona para partidos de fútbol"
            );
        }

        int amarillas = partido.getAmarillasPartido(
            socio.getLogin()
        );

        boolean roja = partido.recibioRojaPartido(
            socio.getLogin()
        );

        EstadisticaFutbol estadistica = new EstadisticaFutbol(
            socio,
            goles,
            asistencias,
            amarillas,
            roja
        );

        partido.agregarEstadistica(estadistica);
    }
    
    public void registrarEstadisticaBasket(
            PartidoInterno partido,
            Socio socio,
            int puntos,
            int rebotes) {

        if (partido == null || socio == null) {
            throw new ReglaDeNegocioException(
                "El partido y el jugador son obligatorios"
            );
        }

        if (!partido.getDeporte().getNombre().equalsIgnoreCase("BASKET")) {
            throw new ReglaDeNegocioException(
                "Este método solo funciona para partidos de basket"
            );
        }

        EstadisticaBasket estadistica = new EstadisticaBasket(
            socio,
            puntos,
            rebotes
        );

        partido.agregarEstadistica(estadistica);
    }
    public void registrarEstadisticaVoleibol(
            PartidoInterno partido,
            Socio socio,
            int aces,
            int bloqueos) {

        if (partido == null || socio == null) {
            throw new ReglaDeNegocioException(
                "El partido y el jugador son obligatorios"
            );
        }

        if (!partido.getDeporte().getNombre().equalsIgnoreCase("VOLEIBOL")) {
            throw new ReglaDeNegocioException(
                "Este método solo funciona para partidos de voleibol"
            );
        }

        EstadisticaVoleibol estadistica = new EstadisticaVoleibol(
            socio,
            aces,
            bloqueos
        );

        partido.agregarEstadistica(estadistica);
    }

}