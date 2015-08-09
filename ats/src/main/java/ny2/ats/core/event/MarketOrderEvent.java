package ny2.ats.core.event;

import java.util.UUID;

import ny2.ats.core.data.Order;

/**
 * 新規オーダーをあらわすイベントです
 */
public class MarketOrderEvent extends AbstractEvent<Order> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** Event種別 */
    public static final EventType eventType = EventType.MARKET_ORDER;

    /** オーダー情報 */
    private final Order order;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public MarketOrderEvent(UUID creatorUUID, Class<?> creatorClass, Order order) {
        super(creatorUUID, creatorClass);
        this.order = order;
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public EventType getEventType() {
        return eventType;
    }

    @Override
    public Order getContent() {
        return order;
    }

}
