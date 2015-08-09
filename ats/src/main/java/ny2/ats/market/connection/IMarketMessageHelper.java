package ny2.ats.market.connection;

import ny2.ats.core.data.Order;
import ny2.ats.core.event.MarketUpdateEvent;
import ny2.ats.core.event.OrderUpdateEvent;

/**
 * 各Marketのメッセージを変換するクラスです
 */
public interface IMarketMessageHelper {

    /**
     * MarketへのNewOrderのメッセージを作成します。
     * @return
     */
    public Object createMessageNewOrder(Order order);

    /**
     * MarketUpdateEvent を作成します。
     * @return
     */
    public OrderUpdateEvent createEventOrderUpdate(Object message, Order order);

    /**
     * MarketUpdateEvent を作成します。
     * @return
     */
    public MarketUpdateEvent createEventMarketUpdate(Object message);


    /**
     * 内部で把握していないExecutionに関してダミーのオーダーを作成します。
     * @return
     */
    public Order createDummyOrderFromExecution(Object executionMessage);

}
