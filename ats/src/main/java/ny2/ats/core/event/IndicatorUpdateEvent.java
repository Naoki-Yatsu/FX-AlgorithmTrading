package ny2.ats.core.event;

import java.util.UUID;

import ny2.ats.core.data.IndicatorInformation;

public class IndicatorUpdateEvent extends AbstractEvent<IndicatorInformation> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** Event種別 */
    public static final EventType eventType = EventType.INDICATOR_UPDATE;

    /** Indicator情報 */
    private final IndicatorInformation indicator;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public IndicatorUpdateEvent(UUID creatorUUID, Class<?> creatorClass, IndicatorInformation indicator) {
        super(creatorUUID, creatorClass);
        this.indicator = indicator;
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public EventType getEventType() {
        return eventType;
    }

    @Override
    public IndicatorInformation getContent() {
        return indicator;
    }

}
