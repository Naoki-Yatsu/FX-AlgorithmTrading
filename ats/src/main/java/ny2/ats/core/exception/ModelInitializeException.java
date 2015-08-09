package ny2.ats.core.exception;

/**
 * Model initialization related exception
 */
public class ModelInitializeException extends ATSRuntimeException {

    private static final long serialVersionUID = 1L;

    public ModelInitializeException() {
        super("Model Initialize Error: ");
    }

    public ModelInitializeException(String message) {
        super(message);
    }

    public ModelInitializeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModelInitializeException(Throwable cause) {
        super(cause);
    }
}
