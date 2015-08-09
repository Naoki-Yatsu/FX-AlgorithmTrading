package ny2.ats.market.transport;

import ny2.ats.core.event.ExecutionInformationEvent;
import ny2.ats.core.event.MarketUpdateEvent;
import ny2.ats.core.event.OrderUpdateEvent;
import ny2.ats.market.connection.IMarketConnector;
import ny2.ats.market.connection.MarketType;
import ny2.ats.market.order.impl.AbstractOrderManager;

/**
 * Market/Order関連サービスの入り口となるクラスです
 */
public interface IMarketManager {

    /**
     * マーケットから更新を受けます（MarketUpeate）
     */
    public void updateFromMarket(MarketUpdateEvent marketUpdate);

    /**
     * マーケットから更新を受けます（OrderUpdateEvent）
     */
    public void updateFromMarket(OrderUpdateEvent orderUpdateEvent);

    /**
     * マーケットから更新を受けます（ExecutionInformationEvent）
     */
    public void updateFromMarket(ExecutionInformationEvent executionInformationEvent);

    /**
     * 指定の接続先にLoginします
     */
    public void loginMarket(MarketType marketType);

    /**
     * 指定の接続先からLogoutします
     */
    public void logoutMarket(MarketType marketType);

    /**
     * 対象のMarketのConnectorを返します
     *
     * @param marketType
     * @return
     */
    public IMarketConnector getMarketConnector(MarketType marketType);

    /**
     * 対象のMarketのOrderManagerを返します
     *
     * @param marketType
     * @return
     */
    public AbstractOrderManager getOrderManager(MarketType marketType);

}
