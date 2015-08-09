package ny2.ats.core.router.impl;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import ny2.ats.core.event.EventType;
import ny2.ats.core.event.ExecutionInformationEvent;
import ny2.ats.core.event.IEvent;
import ny2.ats.core.event.IEventListener;
import ny2.ats.core.event.IndicatorUpdateEvent;
import ny2.ats.core.event.MarketOrderEvent;
import ny2.ats.core.event.MarketUpdateEvent;
import ny2.ats.core.event.ModelInformationEvent;
import ny2.ats.core.event.OrderUpdateEvent;
import ny2.ats.core.event.PLInformationEvent;
import ny2.ats.core.event.PositionUpdateEvent;
import ny2.ats.core.event.SystemInformationEvent;
import ny2.ats.core.event.TimerInformationEvent;
import ny2.ats.core.exception.UnExpectedEventException;
import ny2.ats.core.router.IEventRouter;

/**
 * Event Routerの抽象クラスです
 */
public abstract class AbstraceEventRouter implements IEventRouter  {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // Logger
    protected final Logger logger;

    //
    // Listener
    //
    /** 全てのリスナーのMap */
    protected final Map<EventType, List<IEventListener>> listenerMap = new EnumMap<>(EventType.class);

    /** イベントリスナー(MarketUpdate) */
    protected final List<IEventListener> marketUpdateEventListeners = new ArrayList<>(10);

    /** イベントリスナー(NewOrder) */
    protected final List<IEventListener> marketOrderEventListeners = new ArrayList<>(10);

    /** イベントリスナー(OrderUpdate) */
    protected final List<IEventListener> orderUpdateEventListeners = new ArrayList<>(10);

    /** イベントリスナー(PositionUpdate) */
    protected final List<IEventListener> positionUpdateEventListeners = new ArrayList<>(10);

    /** イベントリスナー(IndicatorUpdate) */
    protected final List<IEventListener> indicatorUpdateEventListeners = new ArrayList<>(10);

    /** イベントリスナー(ModelInformation) */
    protected final List<IEventListener> modelInformationEventListeners = new ArrayList<>(10);

    /** イベントリスナー(PLInformation) */
    protected final List<IEventListener> plInformationEventListeners = new ArrayList<>(10);

    /** イベントリスナー(PLInformation) */
    protected final List<IEventListener> executionInfromationEventListeners = new ArrayList<>(10);

    /** イベントリスナー(SystemInformation) */
    protected final List<IEventListener> systemInformationEventListeners = new ArrayList<>(10);

    /** イベントリスナー(TimerInformation) */
    protected final List<IEventListener> timerInformationEventListeners = new ArrayList<>(10);


    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public AbstraceEventRouter(Logger logger) {
        this.logger = logger;

        // ListenerのList作成
        listenerMap.put(EventType.MARKET_UPDATE, marketUpdateEventListeners);
        listenerMap.put(EventType.MARKET_ORDER, marketOrderEventListeners);
        listenerMap.put(EventType.ORDER_UPDATE, orderUpdateEventListeners);
        listenerMap.put(EventType.POSITION_UPDATE, positionUpdateEventListeners);
        listenerMap.put(EventType.INDICATOR_UPDATE, indicatorUpdateEventListeners);
        listenerMap.put(EventType.MODEL_INFORMATION, modelInformationEventListeners);
        listenerMap.put(EventType.PL_INFORMATION, plInformationEventListeners);
        listenerMap.put(EventType.EXECUTION_INFORMATION, executionInfromationEventListeners);
        listenerMap.put(EventType.SYSTEM_INFORMATION, systemInformationEventListeners);
        listenerMap.put(EventType.TIMER_INFORMATION, timerInformationEventListeners);
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public synchronized void registerListener(EventType eventType, IEventListener listener) {
        List<IEventListener> eventListeners = searchEventListener(eventType);
        eventListeners.add(listener);
    }

    /**
     * envetTypeに応じたListenerのリストを返します
     *
     * @param eventType
     * @return
     */
    protected List<IEventListener> searchEventListener(EventType eventType) {
        switch (eventType) {
            case MARKET_UPDATE:
                return marketUpdateEventListeners;
            case MARKET_ORDER:
                return marketOrderEventListeners;
            case ORDER_UPDATE:
                return orderUpdateEventListeners;
            case POSITION_UPDATE:
                return positionUpdateEventListeners;
            case INDICATOR_UPDATE:
                return indicatorUpdateEventListeners;
            case MODEL_INFORMATION:
                return modelInformationEventListeners;
            case PL_INFORMATION:
                return plInformationEventListeners;
            case EXECUTION_INFORMATION:
                return executionInfromationEventListeners;
            case TIMER_INFORMATION:
                return timerInformationEventListeners;
            case SYSTEM_INFORMATION:
                return systemInformationEventListeners;
            default:
                // 想定外のイベント
                logger.error("Undefined Event. EventType = {}", eventType.name());
                throw new UnExpectedEventException("Undefined Event. " + eventType.name());
        }
    }

    @Override
    public synchronized void registerListeners(EnumSet<EventType> eventTypes, IEventListener listener) {
        for (EventType eventType : eventTypes) {
            registerListener(eventType, listener);
        }
    }


    @Override
    @Deprecated
    public void addEvent(IEvent<?> event) {
        switch (event.getEventType()) {
            case MARKET_UPDATE:
                addEvent((MarketUpdateEvent) event);
                break;
            case MARKET_ORDER:
                addEvent((MarketOrderEvent) event);
                break;
            case ORDER_UPDATE:
                addEvent((OrderUpdateEvent) event);
                break;
            case POSITION_UPDATE:
                addEvent((PositionUpdateEvent) event);
                break;
            case INDICATOR_UPDATE:
                addEvent((IndicatorUpdateEvent) event);
                break;
            case MODEL_INFORMATION:
                addEvent((ModelInformationEvent) event);
                break;
            case PL_INFORMATION:
                addEvent((PLInformationEvent) event);
                break;
            case EXECUTION_INFORMATION:
                addEvent((ExecutionInformationEvent) event);
                break;
            case TIMER_INFORMATION:
                addEvent((TimerInformationEvent) event);
                break;
            case SYSTEM_INFORMATION:
                addEvent((SystemInformationEvent) event);
                break;
            default:
                logger.error("Undefined Event. EventType = {}", event.getEventType().name());
        }
    }

    @Override
    public void trimListenerLists() {
        for (List<IEventListener> list : listenerMap.values()) {
            ((ArrayList<IEventListener>) list).trimToSize();
        }
    }







}
