package ny2.ats.core.router.impl;

import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedResource;

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

/**
 * バックテスト用の完全同期型Routerです
 */
// @Service("SynchronizedEventRouter") -> xml
@ManagedResource(objectName="EventRouter:name=SynchronizedEventRouter")
public class SynchronizedEventRouterImpl extends AbstraceEventRouter {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // Logger -> super class

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public SynchronizedEventRouterImpl() {
        super(LoggerFactory.getLogger(SynchronizedEventRouterImpl.class));
        logger.info("Create instance.");
    }

    @PostConstruct
    private void init() {
        logger.info("PostConstruct instance.");
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public void addEvent(MarketUpdateEvent event) {
        sendEvent(event);
    }

    @Override
    public void addEvent(MarketOrderEvent event) {
        sendEvent(event);
    }

    @Override
    public void addEvent(OrderUpdateEvent event) {
        sendEvent(event);
    }

    @Override
    public void addEvent(PositionUpdateEvent event) {
        sendEvent(event);
    }

    @Override
    public void addEvent(IndicatorUpdateEvent event) {
        sendEvent(event);
    }

    @Override
    public void addEvent(ModelInformationEvent event) {
        sendEvent(event);
    }

    @Override
    public void addEvent(PLInformationEvent event) {
        sendEvent(event);
    }

    @Override
    public void addEvent(ExecutionInformationEvent event) {
        sendEvent(event);
    }

    @Override
    public void addEvent(TimerInformationEvent event) {
        sendEvent(event);
    }

    @Override
    public void addEvent(SystemInformationEvent event) {
        sendEvent(event);
    }
    private void sendEvent(IEvent<?> event) {
        List<IEventListener> eventListeners = searchEventListener(event.getEventType());
        for (int index = 0; index < eventListeners.size(); index++) {
            try {
                IEventListener listener = eventListeners.get(index);
                listener.onEvent(event);
            } catch (Throwable t) {
                logger.error(event.getEventType().name() + " Thread で Error が発生しました。 ", t);
            }
        }
    }

}
