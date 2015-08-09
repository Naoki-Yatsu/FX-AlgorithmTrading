package ny2.ats.core.event;

import java.util.UUID;

import ny2.ats.core.data.OptimizedExecution;

/**
 * 新規オーダーをあらわすイベントです
 */
public class ExecutionInformationEvent extends AbstractEvent<OptimizedExecution> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** Event種別 */
    public static final EventType eventType = EventType.EXECUTION_INFORMATION;

    /** オーダー情報 */
    private final OptimizedExecution optimizedExecution;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public ExecutionInformationEvent(UUID creatorUUID, Class<?> creatorClass, OptimizedExecution optimizedExecution) {
        super(creatorUUID, creatorClass);
        this.optimizedExecution = optimizedExecution;
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public EventType getEventType() {
        return eventType;
    }

    @Override
    public OptimizedExecution getContent() {
        return optimizedExecution;
    }

}
