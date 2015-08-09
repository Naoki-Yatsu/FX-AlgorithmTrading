package ny2.ats.market.transport.impl;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import ny2.ats.core.data.Order;
import ny2.ats.core.data.SystemInformation.SystemInfromationType;
import ny2.ats.core.event.EventType;
import ny2.ats.core.event.ExecutionInformationEvent;
import ny2.ats.core.event.IEventListener;
import ny2.ats.core.event.MarketOrderEvent;
import ny2.ats.core.event.MarketUpdateEvent;
import ny2.ats.core.event.OrderUpdateEvent;
import ny2.ats.core.event.SystemInformationEvent;
import ny2.ats.core.router.IEventRouter;
import ny2.ats.market.connection.IMarketConnector;
import ny2.ats.market.connection.MarketType;
import ny2.ats.market.order.IOrderManager;
import ny2.ats.market.order.impl.AbstractOrderManager;
import ny2.ats.market.transport.IMarketManager;

@Service
@ManagedResource(objectName="MarketService:name=MarketManager")
public class MarketManagerImpl implements IMarketManager, IEventListener {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // Logger
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private IEventRouter eventRouter;

    /** MarketConnectorへの参照 */
    private final Map<MarketType, IMarketConnector> marketConnectorMap = new EnumMap<>(MarketType.class);

    /** OrderManagerへの参照 */
    private final Map<MarketType, IOrderManager> orderManagerMap = new EnumMap<>(MarketType.class);

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    private MarketManagerImpl() {
        logger.info("Create instance.");
    }

    @PostConstruct
    public void init() {
        logger.info("PostConstruct instance.");

        // 取得イベントを登録
        eventRouter.registerListener(EventType.MARKET_ORDER, this);
        eventRouter.registerListener(EventType.SYSTEM_INFORMATION, this);
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public void onEvent(MarketOrderEvent event) {
        Order order = event.getContent();
        IOrderManager manager = orderManagerMap.get(order.getMarketType());
        if (manager != null) {
            manager.sendOrderToMarket(order);
        } else {
            logger.error("{} is NOT registerd.", order.getMarketType());
        }
    }

    @Override
    public void onEvent(SystemInformationEvent event) {
        SystemInfromationType infromationType = event.getContent().getInfromationType();
        MarketType marketType = null;
        try {
            switch (infromationType) {
                case MARKET_LOGIN:
                    marketType = MarketType.valueOf(event.getContent().getMessage());
                    loginMarket(marketType);
                    break;
                case MARKET_LOGOUT:
                    marketType = MarketType.valueOf(event.getContent().getMessage());
                    logoutMarket(marketType);
                    break;
                default:
                    // Do nothing.
                    break;
            }
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    @Override
    public void updateFromMarket(MarketUpdateEvent marketUpdate) {
        eventRouter.addEvent(marketUpdate);

        // TODO Routerから受け取ったほうがよいかも or 別スレッド
        for (IOrderManager orderManager : orderManagerMap.values()) {
            orderManager.updateMarketForOptimizer(marketUpdate.getContent());
        }
    }

    @Override
    public void updateFromMarket(OrderUpdateEvent orderUpdateEvent) {
        eventRouter.addEvent(orderUpdateEvent);
    }

    @Override
    public void updateFromMarket(ExecutionInformationEvent executionInformationEvent) {
        eventRouter.addEvent(executionInformationEvent);
    }

    @Override
    public void loginMarket(MarketType marketType) {
        IMarketConnector connector = marketConnectorMap.get(marketType);
        if (connector == null) {
            setupMarket(marketType);
            connector = marketConnectorMap.get(marketType);
            if (connector == null) {
                logger.error("Market definition is NOT correctly definied. MarketType = {}", marketType.name());
                return;
            }
        }
        // Login
        connector.sendLogin();
    }

    @Override
    public void logoutMarket(MarketType marketType) {
        IMarketConnector connector = marketConnectorMap.get(marketType);
        if (connector == null) {
            logger.error("Failed to Logiout. Market is NOT registered : MarketType = {}", marketType.name());
            return;
        }
        // Logout
        connector.sendLogout();
    }


    private void setupMarket(MarketType marketType) {
        IMarketConnector connector = applicationContext.getBean(marketType.getConnectorClass());
        IOrderManager manager = applicationContext.getBean(marketType.getOrderManagerClass());
        marketConnectorMap.put(marketType, connector);
        orderManagerMap.put(marketType, manager);
    }

    // //////////////////////////////////////
    // JMX
    // //////////////////////////////////////

    /**
     * 各Marketへのログイン状態をチェックします。
     */
    @ManagedOperation
    public String checkMarketStatus() {
        StringBuilder sb = new StringBuilder("[Login Status]\n");
        for (Entry<MarketType, IMarketConnector> entry : marketConnectorMap.entrySet()) {
            sb.append(entry.getKey()).append(" : ");
            if (entry.getValue().isLoggedIn()) {
                sb.append("Login\n");
            } else {
                sb.append("Logout\n");
            }
        }
        return sb.toString();
    }

    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

    /**
     * Historical Test用にOrderManege, Connectorを入れ替えます
     * @param marketType
     * @param orderManager
     */
    public void setupMarketForHistorical(MarketType marketType, IMarketConnector marketConnector, IOrderManager orderManager) {
        marketConnectorMap.put(marketType, marketConnector);
        orderManagerMap.put(marketType, orderManager);
    }

    @Override
    public IMarketConnector getMarketConnector(MarketType marketType) {
        return marketConnectorMap.get(marketType);
    }

    @Override
    public AbstractOrderManager getOrderManager(MarketType marketType) {
        return (AbstractOrderManager) orderManagerMap.get(marketType);
    }

}
