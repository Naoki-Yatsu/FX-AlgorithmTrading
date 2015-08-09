package ny2.ats.market.order;

import ny2.ats.core.data.MarketData;
import ny2.ats.core.data.Order;
import ny2.ats.core.exception.MarketException;
import ny2.ats.market.connection.MarketType;

/**
 * Interface for Order Management classes
 */
public interface IOrderManager {

    /**
     * Marketにオーダーを送信します。
     * @param order
     */
    public default void sendOrderToMarket(Order order) {
        switch (order.getOrderAction()) {
            case SUBMIT:
                newOrderToMarket(order);
                break;
            case REPLACE:
                amendOrderToMarket(order);
                break;
            case CANCEL:
                cancelOrderToMarket(order);
                break;
            default:
                throw new MarketException(order.getMarketType(), "Received unexpected OrderAction. " + order.toString());
        }
    }

    /**
     * Marketに新規オーダーを送信します。
     * @param order
     */
    public void newOrderToMarket(Order order);

    /**
     * Marketにオーダーの変更を送信します。
     * @param order
     */
    public void amendOrderToMarket(Order order);

    /**
     * Marketにオーダーの取り消しを送信します。
     * @param order
     */
    public void cancelOrderToMarket(Order order);

    /**
     * Marketより受信したExecutionReportを処理します。
     */
    public void execExecutionReport(MarketType marketType, Object message);


    /**
     * Optimizer用にMarketデータの更新を行います
     * @param marketData
     */
    public void updateMarketForOptimizer(MarketData marketData);

}
