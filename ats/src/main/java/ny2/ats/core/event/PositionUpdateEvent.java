package ny2.ats.core.event;

import java.util.UUID;

import ny2.ats.core.data.Position;

/**
 * Positionの更新を表すイベントです。
 */
public class PositionUpdateEvent extends AbstractEvent<Position> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** Event種別 */
    public static final EventType eventType = EventType.POSITION_UPDATE;

    /** Positionデータ */
    private final Position position;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public PositionUpdateEvent(UUID creatorUUID, Class<?> creatorClass, Position position) {
        super(creatorUUID, creatorClass);
        this.position = position.clone();
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public EventType getEventType() {
        return eventType;
    }

    @Override
    public Position getContent() {
        return position;
    }

}
