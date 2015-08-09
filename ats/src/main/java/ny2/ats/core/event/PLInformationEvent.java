package ny2.ats.core.event;

import java.util.UUID;

import ny2.ats.core.data.PLInformation;

public class PLInformationEvent extends AbstractEvent<PLInformation> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** Event種別 */
    public static final EventType eventType = EventType.PL_INFORMATION;

    /** PL情報 */
    private final PLInformation plInformation;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public PLInformationEvent(UUID creatorUUID, Class<?> creatorClass, PLInformation plInformation) {
        super(creatorUUID, creatorClass);
        this.plInformation = plInformation;
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public EventType getEventType() {
        return eventType;
    }

    @Override
    public PLInformation getContent() {
        return plInformation;
    }
}
