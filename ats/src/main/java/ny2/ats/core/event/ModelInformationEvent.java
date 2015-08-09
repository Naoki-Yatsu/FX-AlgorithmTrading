package ny2.ats.core.event;

import java.util.UUID;

import ny2.ats.core.data.ModelInformation;

public class ModelInformationEvent extends AbstractEvent<ModelInformation> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** Event種別 */
    public static final EventType eventType = EventType.MODEL_INFORMATION;

    /** Model情報 */
    private final ModelInformation modelInformation;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public ModelInformationEvent(UUID creatorUUID, Class<?> creatorClass, ModelInformation modelInformation) {
        super(creatorUUID, creatorClass);
        this.modelInformation = modelInformation;
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public EventType getEventType() {
        return eventType;
    }

    @Override
    public ModelInformation getContent() {
        return modelInformation;
    }
}
