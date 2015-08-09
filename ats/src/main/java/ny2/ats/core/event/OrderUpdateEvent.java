package ny2.ats.core.event;

import java.util.UUID;

import ny2.ats.core.common.OrderStatus;
import ny2.ats.core.data.Order;

/**
 * Orderの更新を表すイベントです。
 */
public class OrderUpdateEvent extends AbstractEvent<Order> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** Event種別 */
    public static final EventType eventType = EventType.ORDER_UPDATE;

    /** オーダーデータ */
    private final Order order;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public OrderUpdateEvent(UUID creatorUUID, Class<?> creatorClass, Order order) {
        super(creatorUUID, creatorClass);
//        this.order = order.clone();
        this.order = order;
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

    @Override
    public EventType getEventType() {
        return eventType;
    }

    @Override
    public Order getContent() {
        return order;
    }

    public OrderStatus getOrderStatus() {
        return order.getOrderStatus();
    }

}
