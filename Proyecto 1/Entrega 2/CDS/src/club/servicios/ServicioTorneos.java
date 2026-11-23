package club.servicios;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import club.modelo.conjunto.Categoria;
import club.modelo.individual.Disciplina;
import club.modelo.individual.Nivel;
import club.modelo.torneos.EstadoTorneo;
import club.modelo.torneos.Modalidad;
import club.modelo.torneos.Torneo;
import club.modelo.torneos.TorneoCompetitivo;
import club.modelo.torneos.TorneoRecreativo;

public class ServicioTorneos {

    private final ClubSeneca club;
    private final List<Torneo> torneos;
    private int contadorTorneos;

    public ServicioTorneos(ClubSeneca club) {

        if (club == null) {
            throw new ReglaDeNegocioException("El club no puede ser nulo");
        }

        this.club = club;
        this.torneos = new ArrayList<>();
        this.contadorTorneos = 0;
    }

    // CONSULTAS

    public List<Torneo> getTorneos() {
        return new ArrayList<>(torneos);
    }

    public Torneo getTorneo(String id) {

        if (id == null) {
            return null;
        }

        for (Torneo torneo : torneos) {
            if (torneo.getId().equals(id)) {
                return torneo;
            }
        }

        return null;
    }

    public Torneo exigirTorneo(String id) {

        Torneo torneo = getTorneo(id);

        if (torneo == null) {
            throw new ReglaDeNegocioException(
                "No existe el torneo " + id
            );
        }

        return torneo;
    }

    public List<Torneo> getTorneosPorEstado(EstadoTorneo estado) {

        List<Torneo> resultado = new ArrayList<>();

        if (estado == null) {
            return resultado;
        }

        for (Torneo torneo : torneos) {
            if (torneo.getEstado() == estado) {
                resultado.add(torneo);
            }
        }

        return resultado;
    }

    // CREACIÓN DE TORNEOS

    public TorneoRecreativo crearTorneoRecreativo(
        String nombre,
        Modalidad modalidad,
        String disciplina,
        String categoria,
        int cupos,
        LocalDate fecha
    ) {

        validarDisciplinaYCategoria(modalidad, disciplina, categoria);
        validarFecha(fecha);

        String disciplinaNormalizada =
            disciplina.trim().toUpperCase(Locale.ROOT);

        String categoriaNormalizada =
            categoria.trim().toUpperCase(Locale.ROOT);

        String id = siguienteId();

        TorneoRecreativo torneo = new TorneoRecreativo(
            id,
            nombre,
            modalidad,
            disciplinaNormalizada,
            categoriaNormalizada,
            cupos,
            fecha
        );

        agregar(torneo);

        return torneo;
    }

    public TorneoCompetitivo crearTorneoCompetitivo(
        String nombre,
        Modalidad modalidad,
        String disciplina,
        String categoria,
        int cupos,
        LocalDate fecha,
        int tarifa
    ) {

        validarDisciplinaYCategoria(modalidad, disciplina, categoria);
        validarFecha(fecha);

        String disciplinaNormalizada =
            disciplina.trim().toUpperCase(Locale.ROOT);

        String categoriaNormalizada =
            categoria.trim().toUpperCase(Locale.ROOT);

        String id = siguienteId();

        TorneoCompetitivo torneo = new TorneoCompetitivo(
            id,
            nombre,
            modalidad,
            disciplinaNormalizada,
            categoriaNormalizada,
            cupos,
            fecha,
            tarifa
        );

        agregar(torneo);

        return torneo;
    }

    // CICLO DE VIDA

    public void iniciarTorneo(String idTorneo) {
        exigirTorneo(idTorneo).iniciar();
    }

    public void finalizarTorneo(String idTorneo) {
        exigirTorneo(idTorneo).finalizar();
    }

    // CARGA DESDE CSV

    public void agregarTorneoCargado(Torneo torneo) {

        if (torneo == null) {
            throw new ReglaDeNegocioException(
                "El torneo no puede ser nulo"
            );
        }

        String id = torneo.getId();

        if (id == null || !id.startsWith("TN-")) {
            throw new ReglaDeNegocioException(
                "El identificador del torneo no tiene un formato válido"
            );
        }

        int numero;

        try {
            numero = Integer.parseInt(id.substring(3));
        } catch (NumberFormatException e) {
            throw new ReglaDeNegocioException(
                "El identificador del torneo no tiene un número válido"
            );
        }

        if (numero <= 0) {
            throw new ReglaDeNegocioException(
                "El identificador del torneo no tiene un número válido"
            );
        }

        torneos.add(torneo);

        if (numero > contadorTorneos) {
            contadorTorneos = numero;
        }
    }

    // MÉTODOS AUXILIARES

    private String siguienteId() {
        return String.format(
            Locale.ROOT,
            "TN-%03d",
            contadorTorneos + 1
        );
    }

    private void agregar(Torneo nuevo) {

        for (Torneo existente : torneos) {

            boolean mismoNombre =
                existente.getNombre().equalsIgnoreCase(nuevo.getNombre());

            boolean mismaDisciplina =
                existente.getDisciplina().equalsIgnoreCase(nuevo.getDisciplina());

            boolean mismaCategoria =
                existente.getCategoria().equalsIgnoreCase(nuevo.getCategoria());

            boolean mismaFecha =
                existente.getFecha().equals(nuevo.getFecha());

            if (mismoNombre && mismaDisciplina &&
                mismaCategoria && mismaFecha) {

                throw new ReglaDeNegocioException(
                    "Ya existe un torneo con ese nombre, disciplina, categoría y fecha"
                );
            }
        }

        torneos.add(nuevo);
        contadorTorneos++;
    }

    private void validarDisciplinaYCategoria(
        Modalidad modalidad,
        String disciplina,
        String categoria
    ) {

        if (modalidad == null) {
            throw new ReglaDeNegocioException(
                "La modalidad no puede ser nula"
            );
        }

        if (disciplina == null || disciplina.trim().isEmpty()) {
            throw new ReglaDeNegocioException(
                "La disciplina no puede estar vacía"
            );
        }

        if (categoria == null || categoria.trim().isEmpty()) {
            throw new ReglaDeNegocioException(
                "La categoría no puede estar vacía"
            );
        }

        String disciplinaNormalizada =
            disciplina.trim().toUpperCase(Locale.ROOT);

        String categoriaNormalizada =
            categoria.trim().toUpperCase(Locale.ROOT);

        try {

            if (modalidad == Modalidad.CONJUNTO) {

                if (club.getServicioConjunto()
                        .getDeporte(disciplinaNormalizada) == null) {

                    throw new ReglaDeNegocioException(
                        "No existe el deporte " + disciplinaNormalizada
                    );
                }

                Categoria.valueOf(categoriaNormalizada);

            } else {

                Disciplina.valueOf(disciplinaNormalizada);
                Nivel.valueOf(categoriaNormalizada);
            }

        } catch (IllegalArgumentException e) {

            throw new ReglaDeNegocioException(
                "La disciplina o categoría no corresponde a una opción válida para la modalidad "
                + modalidad
            );
        }
    }

    private void validarFecha(LocalDate fecha) {

        if (fecha == null) {
            throw new ReglaDeNegocioException(
                "La fecha del torneo no puede ser nula"
            );
        }

        if (!fecha.isAfter(LocalDate.now())) {
            throw new ReglaDeNegocioException(
                "La fecha del torneo debe ser futura"
            );
        }
    }
}

