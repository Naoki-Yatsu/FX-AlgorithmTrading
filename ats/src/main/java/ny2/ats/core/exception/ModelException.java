package ny2.ats.core.exception;

/**
 * Model related exception
 */
public class ModelException extends ATSRuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * モデル名とエラーメッセージを出力
     */
    public ModelException(Class<?> clazz, String message) {
        super(clazz.getSimpleName() + " : " + message);
    }
}
