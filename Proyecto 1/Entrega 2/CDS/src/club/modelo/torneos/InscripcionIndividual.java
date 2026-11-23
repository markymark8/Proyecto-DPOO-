package club.modelo.torneos;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import club.modelo.usuarios.Empleado;
import club.modelo.usuarios.Usuario;
import club.servicios.ReglaDeNegocioException;

public class InscripcionIndividual extends InscripcionTorneo {

    private final List<Usuario> participantes;

    public InscripcionIndividual(List<Usuario> participantes,
                                 boolean preferencial, int puntajeRanking) {

        super(preferencial, puntajeRanking);

        validar(participantes);

        this.participantes = new ArrayList<>(participantes);
    }

    private static void validar(List<Usuario> participantes) {

        if (participantes == null || participantes.isEmpty()
                || participantes.size() > 2) {
            throw new ReglaDeNegocioException(
                    "La inscripción debe tener uno o dos participantes");
        }

        Set<String> identificadores = new HashSet<>();

        for (Usuario participante : participantes) {

            if (participante == null) {
                throw new ReglaDeNegocioException(
                        "La inscripción no puede contener participantes nulos");
            }

            if (!identificadores.add(participante.getLogin())) {
                throw new ReglaDeNegocioException(
                        "No se puede inscribir dos veces al mismo participante");
            }
        }
    }

    @Override
    public String getNombre() {
        return participantes.stream()
                .map(Usuario::getNombre)
                .collect(Collectors.joining(" / "));
    }

    @Override
    public String getIdentificador() {
        return participantes.stream()
                .map(Usuario::getLogin)
                .sorted()
                .collect(Collectors.joining("-"));
    }

    @Override
    public List<Usuario> getParticipantes() {
        return Collections.unmodifiableList(participantes);
    }

    public boolean tieneEmpleado() {
        for (Usuario participante : participantes) {
            if (participante instanceof Empleado) {
                return true;
            }
        }
        return false;
    }
}
