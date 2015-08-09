package ny2.ats.core.exception;

import ny2.ats.core.event.EventType;

/**
 * 想定外のイベントを受信した際に出力するException
 */
public class UnExpectedEventException extends ATSRuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 想定外のイベント名を出力する
     * @param eventType
     */
    public UnExpectedEventException(EventType eventType) {
        super("Received Not Registered Event: " + eventType.name());
    }
    
    public UnExpectedEventException(String message) {
        super(message);
    }

}

