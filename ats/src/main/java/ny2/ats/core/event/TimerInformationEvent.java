package ny2.ats.core.event;

import java.time.LocalDateTime;
import java.util.UUID;

import ny2.ats.core.common.Period;
import ny2.ats.core.data.TimerInformation;

public class TimerInformationEvent extends AbstractEvent<TimerInformation> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** Event種別 */
    public static final EventType eventType = EventType.TIMER_INFORMATION;

    /** Timer情報 */
    private final TimerInformation timerData;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public TimerInformationEvent(UUID creatorUUID, Class<?> creatorClass, Period period, LocalDateTime currentDateTime, LocalDateTime nextDateTime) {
        super(creatorUUID, creatorClass);
        this.timerData = new TimerInformation(period, currentDateTime, nextDateTime);
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public EventType getEventType() {
        return eventType;
    }

    @Override
    public TimerInformation getContent() {
        return timerData;
    }

    public Period getPeriod() {
        return timerData.getPeriod();
    }
}
