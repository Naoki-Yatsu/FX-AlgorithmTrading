package ny2.ats.core.event;

import ny2.ats.core.exception.UnExpectedEventException;

/**
 * イベントリスナーのインターフェースです。
 * イベントを受信するクラスはこのインターフェースを実装します。
 * 必要なメソッドだけ実装してください。
 */
public interface IEventListener {

    /**
     * MarketUpdateEvent 受信処理
     *
     * @param event - MarketUpdateEvent
     */
    public default void onEvent(MarketUpdateEvent event) {
        new UnExpectedEventException(event.getEventType());
    }

    /**
     * MarketOrderEvent 受信処理
     *
     * @param event - MarketOrderEvent
     */
    public default void onEvent(MarketOrderEvent event) {
        new UnExpectedEventException(event.getEventType());
    }

    /**
     * OrderUpdateEvent 受信処理
     *
     * @param event - OrderUpdateEvent
     */
    public default void onEvent(OrderUpdateEvent event) {
        new UnExpectedEventException(event.getEventType());
    }

    /**
     * PositionUpdateEvent 受信処理
     *
     * @param event - PositionUpdateEvent
     */
    public default void onEvent(PositionUpdateEvent event) {
        new UnExpectedEventException(event.getEventType());
    }

    /**
     * IndicatorUpdateEvent 受信処理
     *
     * @param event - IndicatorUpdateEvent
     */
    public default void onEvent(IndicatorUpdateEvent event) {
        new UnExpectedEventException(event.getEventType());
    }

    /**
     * ExecutionInformationEvent 受信処理
     *
     * @param event - ExecutionInformationEvent
     */
    public default void onEvent(ExecutionInformationEvent event) {
        new UnExpectedEventException(event.getEventType());
    }

    /**
     * PLInformationEvent 受信処理
     *
     * @param event - PLInformationEvent
     */
    public default void onEvent(PLInformationEvent event) {
        new UnExpectedEventException(event.getEventType());
    }

    /**
     * ModelInformationEvent 受信処理
     *
     * @param event - ModelInformationEvent
     */
    public default void onEvent(ModelInformationEvent event) {
        new UnExpectedEventException(event.getEventType());
    }

    /**
     * TimerEvent 受信処理
     *
     * @param event - TimerEvent
     */
    public default void onEvent(TimerInformationEvent event) {
        new UnExpectedEventException(event.getEventType());
    }

    /**
     * SystemInformationEvent 受信処理
     *
     * @param event - SystemInformationEvent
     */
    public default void onEvent(SystemInformationEvent event) {
        new UnExpectedEventException(event.getEventType());
    }

    /**
     * 未定義Event受信処理
     *
     * @param event - NewOrderEvent
     */
    public default void onEvent(IEvent<?> event) {
        EventType eventType = event.getEventType();
        switch (eventType) {
            case MARKET_UPDATE:
                onEvent((MarketUpdateEvent) event);
                break;
            case MARKET_ORDER:
                onEvent((MarketOrderEvent) event);
                break;
            case ORDER_UPDATE:
                onEvent((OrderUpdateEvent) event);
                break;
            case POSITION_UPDATE:
                onEvent((PositionUpdateEvent) event);
                break;
            case INDICATOR_UPDATE:
                onEvent((IndicatorUpdateEvent) event);
                break;
            case MODEL_INFORMATION:
                onEvent((ModelInformationEvent) event);
                break;
            case PL_INFORMATION:
                onEvent((PLInformationEvent) event);
                break;
            case EXECUTION_INFORMATION:
                onEvent((ExecutionInformationEvent) event);
                break;
            case TIMER_INFORMATION:
                onEvent((TimerInformationEvent) event);
                break;
            case SYSTEM_INFORMATION:
                onEvent((SystemInformationEvent) event);
                break;
            default:
                break;
        }
        // このメソッドは通常よばれないので、とりあえずexceptionを投げる
        new UnExpectedEventException(event.getEventType());
    }

}
