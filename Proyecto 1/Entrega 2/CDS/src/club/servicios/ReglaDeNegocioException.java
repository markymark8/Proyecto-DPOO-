package club.servicios;

/**
 * Se lanza cuando una operacion viola una regla de negocio.
 * Las consolas la capturan y muestran el motivo del rechazo.
 */
public class ReglaDeNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ReglaDeNegocioException(String motivo) {
        super(motivo);
    }
}
