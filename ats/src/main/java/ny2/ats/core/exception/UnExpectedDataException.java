package ny2.ats.core.exception;

/**
 * 想定外のデータを受信した際に出力するException
 */
public class UnExpectedDataException extends ATSRuntimeException{

    private static final long serialVersionUID = 1L;

    /**
     * 想定外のイベント名を出力する
     * @param clazz event class
     */
    public UnExpectedDataException(Class<?> clazz) {
        super("Received Not Registered Data: " + clazz.toGenericString());
    }

    public UnExpectedDataException(String message) {
        super(message);
    }

}