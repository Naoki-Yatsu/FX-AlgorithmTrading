package ny2.ats.core.router;

import java.util.EnumSet;

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

/**
 * 全般のEventのルーティングをするクラスです。
 */
public interface IEventRouter {

    /**
     * EventListenerを登録します
     *
     * @param eventType 取得したいベント種別
     * @param listener リスナー(通常は自分自身)
     */
    public abstract void registerListener(EventType eventType, IEventListener listener);

    /**
     * EventListenerを複数のイベントに登録します
     *
     * @param eventTypes 取得したいベント種別セット
     * @param listener リスナー(通常は自分自身)
     */
    public abstract void registerListeners(EnumSet<EventType> eventTypes, IEventListener listener);

    /**
     * MarketUpdateEventを登録する
     */
    public abstract void addEvent(MarketUpdateEvent event);

    /**
     * MarketOrderEventを登録する
     */
    public abstract void addEvent(MarketOrderEvent event);

    /**
     * OrderUpdateEventを登録する
     */
    public abstract void addEvent(OrderUpdateEvent event);

    /**
     * PositionUpdateEvent を登録する
     */
    public void addEvent(PositionUpdateEvent event);

    /**
     * IndicatorUpdateEvent を登録する
     */
    public void addEvent(IndicatorUpdateEvent event);

    /**
     * ModelInformationEvent を登録する
     */
    public void addEvent(ModelInformationEvent event);

    /**
     * PLInformationEvent を登録する
     */
    public void addEvent(PLInformationEvent event);

    /**
     * ExecutionInformationEvent を登録する
     */
    public void addEvent(ExecutionInformationEvent event);

    /**
     * TimerEvent を登録する
     */
    public void addEvent(TimerInformationEvent event);

    /**
     * SystemInformationEvent を登録する
     */
    public void addEvent(SystemInformationEvent event);

    /**
     * Eventを登録する（汎用）
     * @param event イベント全般
     */
    public abstract void addEvent(IEvent<?> event);

    /**
     * Listenerのリストをトリムします。起動時のListener登録後に一度だけ実行します。
     */
    public void trimListenerLists();

}
