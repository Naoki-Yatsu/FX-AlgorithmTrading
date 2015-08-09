package ny2.ats.core.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import ny2.ats.core.data.SystemInformation;
import ny2.ats.core.data.SystemInformation.SystemInfromationType;

public class SystemInformationEvent extends AbstractEvent<SystemInformation> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** Event種別 */
    public static final EventType eventType = EventType.SYSTEM_INFORMATION;

    /** InfromationT情報 */
    private final SystemInformation infromation;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public SystemInformationEvent(UUID creatorUUID, Class<?> creatorClass, SystemInfromationType infromationType, String message, List<String> parameters) {
        super(creatorUUID, creatorClass);
        this.infromation = new SystemInformation(infromationType, message, parameters, LocalDateTime.now());
    }

    public SystemInformationEvent(UUID creatorUUID, Class<?> creatorClass, SystemInfromationType infromationType, String message, List<String> parameters, LocalDateTime reportDateTime) {
        super(creatorUUID, creatorClass);
        this.infromation = new SystemInformation(infromationType, message, parameters, reportDateTime);
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public EventType getEventType() {
        return eventType;
    }

    @Override
    public SystemInformation getContent() {
        return infromation;
    }

}
