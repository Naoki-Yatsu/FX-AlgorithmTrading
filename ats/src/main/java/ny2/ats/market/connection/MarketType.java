package ny2.ats.market.connection;

import ny2.ats.market.connection.historical.HistoricalConnector;
import ny2.ats.market.order.IOrderManager;
import ny2.ats.market.order.impl.HistoricalOrderManagerImpl;

/**
 * 接続先Marketをあらわすenumです
 */
public enum MarketType {

    HISTORICAL(HistoricalConnector.class, HistoricalOrderManagerImpl.class),
    DUMMY(null, null);


    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    private Class<? extends IMarketConnector> connectorClass;

    private Class<?  extends IOrderManager> orderManagerClass;


    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    private MarketType(Class<?  extends IMarketConnector> connectorClass, Class<?  extends IOrderManager> orderManagerClass) {
        this.connectorClass = connectorClass;
        this.orderManagerClass = orderManagerClass;
    }


    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    public Class<? extends IMarketConnector> getConnectorClass() {
        return connectorClass;
    }

    public Class<? extends IOrderManager> getOrderManagerClass() {
        return orderManagerClass;
    }
}
