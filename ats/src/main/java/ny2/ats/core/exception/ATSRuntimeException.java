package ny2.ats.core.exception;

public class ATSRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 想定外のイベント名を出力する
     */
    public ATSRuntimeException() {
        super("ATSRuntimeException : ");
    }
    
    public ATSRuntimeException(String message) {
        super(message);
    }

    public ATSRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ATSRuntimeException(Throwable cause) {
        super(cause);
    }
}
