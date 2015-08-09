package ny2.ats.market.order.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import ny2.ats.core.common.OrderAction;
import ny2.ats.core.common.OrderStatus;
import ny2.ats.core.common.OrderType;
import ny2.ats.core.common.Side;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.data.MarketData;
import ny2.ats.core.data.Order;
import ny2.ats.core.data.Order.OrderBuilder;
import ny2.ats.core.event.OrderUpdateEvent;
import ny2.ats.core.util.PriceUtility;
import ny2.ats.market.connection.MarketType;

/**
 * Historicalテスト時のオーダーの管理執行を行うクラスです。
 */
// @Component("HistoricalOrderManager") -> xml
public class HistoricalOrderManagerImpl extends AbstractOrderManager {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** 執行モード */
    @Value("${historical.order.executionMode:LATENCY_WORSE}")
    private ExecutionMode executionMode;

    /** Latencyモードで使用するLatency */
    @Value("${historical.order.latencymillisecond:200}")
    private int latencyMillisecond;


    /** Symbolごとの執行待ちオーダーのMap */
    private final Map<Symbol, Map<Long, Order>> symbolOrderMap = new EnumMap<>(Symbol.class);

    /** OrderId ごと最悪執行価格のMap(LatencyModeで使用)  */
    private final Map<Long, Double> orderWorstPriceMap = new ConcurrentHashMap<>();
    private final Map<Long, LocalDateTime> orderTimeMap = new ConcurrentHashMap<>();

    /** 最新MarketDataのMap */
    private final Map<Symbol, MarketData> marketDataMap = new ConcurrentHashMap<>();

    // //////////////////////////////////////
    // Constructor / setup
    // //////////////////////////////////////

    public HistoricalOrderManagerImpl() {
        logger.info("Create instance.");
    }

    @PostConstruct
    public void init() {
        logger.info("PostConstruct instance.");
    }

    /**
     * 対象通貨ペアのセットアップを行います
     *
     * @param symbolList
     */
    public void setup(Set<Symbol> symbolSet) {
        for (Symbol symbol : symbolSet) {
            symbolOrderMap.put(symbol, new ConcurrentHashMap<>());
        }
    }

    @Autowired
    @Override
    public void setEnableOptimizer(@Value("${historical.order.optimizer:false}") boolean enableOptimizer) {
        super.setEnableOptimizer(enableOptimizer);
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    protected void newOrderToMarketInternal(Order order) {
        logger.info("New Order : {}", order.toStringSummary());

        // Marketオーダーで即時執行の場合は即執行
        if ((order.getOrderType() == OrderType.MARKET) &&
                (executionMode == ExecutionMode.IMMEDIATE || executionMode == ExecutionMode.IMMEDIATE_DISADVANTAGE)) {
            MarketData marketData = marketDataMap.get(order.getSymbol());

            // Optimizerの可能性があるため、執行はMarketRateを使う
            executeOrder(order, marketData.getPrice(order.getSide().getOpenBidAsk()), marketData.getMarketDateTime());

        } else {
            Map<Long, Order> orderMap = symbolOrderMap.get(order.getSymbol());
            orderMap.put(order.getOrderId(), order);
            if (executionMode == ExecutionMode.LATENCY_WORSE) {
                orderWorstPriceMap.put(order.getOrderId(), order.getOrderPrice());
                orderTimeMap.put(order.getOrderId(), marketDataMap.get(order.getSymbol()).getMarketDateTime());
            }
        }
    }

    @Override
    public void amendOrderToMarket(Order order) {
        // Auto-generated method stub
    }

    @Override
    public void cancelOrderToMarket(Order order) {
        // Auto-generated method stub
    }

    @Override
    public void execExecutionReport(MarketType marketType, Object message) {
        // Historical Testでは不要
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    /**
     * オーダーが執行対象かチェックし、執行します。
     * 排他制御されていません
     *
     * @param marketData
     */
    public void updateMarketAndCheckLimitStop(MarketData marketData) {
        // 対象通貨ペアのオーダー取得
        Map<Long, Order> orderMap = symbolOrderMap.get(marketData.getSymbol());
        if (orderMap == null) {
            return;
        }

        // オーダー一覧表示
        // logger.debug(orderMap.keySet().toString());

        // オーダー執行
        checkLimitStopOrderInternal(orderMap, marketData);

        // Update MarketData - 執行で前回レートを使うため最後に入れる
        marketDataMap.put(marketData.getSymbol(), marketData);
    }

    /**
     * オーダーが執行対象かチェックし、執行します。(内部処理用)
     *
     * @param marketData
     */
    public void checkLimitStopOrderInternal(Map<Long, Order> orderMap, MarketData marketData) {
        for (Order order : orderMap.values()) {
            // Market / LimitStop で場合わけ
            if (order.getOrderType() == OrderType.MARKET) {
                // Market Order
                double executePrice = marketData.getPrice(order.getSide().getOpenBidAsk());
                if (executionMode == ExecutionMode.NEXT_WORSE) {
                    double previoutPrice = marketDataMap.get(marketData.getSymbol()).getPrice(order.getSide().getOpenBidAsk());
                    executePrice = PriceUtility.getWorsePrice(order.getSide(), previoutPrice, executePrice);

                } else if (executionMode == ExecutionMode.LATENCY_WORSE) {
                    executePrice = PriceUtility.getWorsePrice(order.getSide(), executePrice, orderWorstPriceMap.get(order.getOrderId()));
                    if (Duration.between(orderTimeMap.get(order.getOrderId()), marketData.getMarketDateTime()).toMillis() < latencyMillisecond ) {
                        // Latency以内なら執行しない
                        orderWorstPriceMap.put(order.getOrderId(), executePrice);
                        continue;
                    } else {
                        orderWorstPriceMap.remove(order.getOrderId());
                        orderTimeMap.remove(order.getOrderId());
                    }
                }
                // Mapからremoveして執行
                orderMap.remove(order.getOrderId());
                executeOrder(order, executePrice, marketData.getMarketDateTime());

            } else {
                // Limit or Stop Order
                double orderPrice = order.getOrderPrice();
                if (order.getSide() == Side.BUY) {
                    // Buyの場合
                    if (order.getOrderType() == OrderType.LIMIT) {
                        // Limit
                        if (orderPrice >= marketData.getAskPrice()) {
                            orderMap.remove(order.getOrderId());
                            executeOrder(order, orderPrice, marketData.getMarketDateTime());
                        }
                    } else {
                        // Stop (SLのずれ考慮)
                        if (orderPrice <= marketData.getAskPrice()) {
                            orderMap.remove(order.getOrderId());
                            executeOrder(order, marketData.getAskPrice(), marketData.getMarketDateTime());
                        }
                    }

                } else {
                    // Sellの場合
                    if (order.getOrderType() == OrderType.LIMIT) {
                        // Limit
                        if (orderPrice <= marketData.getBidPrice()) {
                            orderMap.remove(order.getOrderId());
                            executeOrder(order, orderPrice, marketData.getMarketDateTime());
                        }
                    } else {
                        // Stop (SLのずれ考慮)
                        if (orderPrice >= marketData.getBidPrice()) {
                            orderMap.remove(order.getOrderId());
                            executeOrder(order, marketData.getBidPrice(), marketData.getMarketDateTime());
                        }
                    }
                }
            }
        }
    }

    /**
     * オーダーの執行処理を行います
     *
     * @param order
     * @param executePrice
     * @param priceDateTime
     */
    private void executeOrder(Order order, double executePrice, LocalDateTime priceDateTime) {
        // コピーしてBuilder作成
        OrderBuilder orderBuilder = OrderBuilder.getBuilder(order);

        // 時刻データ更新（レート更新時刻を設定）
        orderBuilder.setMarketDateTime(priceDateTime);

        // オーダー情報更新
        orderBuilder.setOrderAction(OrderAction.FILL);
        orderBuilder.setOrderStatus(OrderStatus.FILLED);
        orderBuilder.setMarketPositionId(orderBuilder.getOrderId() + "E");

        if (executionMode == ExecutionMode.IMMEDIATE_DISADVANTAGE || executionMode == ExecutionMode.NEXT_DISADVANTAGE) {
            double disadvantagePrice = orderBuilder.getSymbol().convertPipsToReal(executionMode.getDisadvantagePips());
            if (order.getSide() == Side.BUY) {
                executePrice += disadvantagePrice;
            } else {
                executePrice -= disadvantagePrice;
            }
        }
        orderBuilder.setExecutePrice(executePrice);
        orderBuilder.setExecuteAmount(orderBuilder.getOrderAmount());

        // イベント作成
        OrderUpdateEvent event = new OrderUpdateEvent(uuid, getClass(), orderBuilder.createInstance());
        marketManager.updateFromMarket(event);

        // 執行情報を更新
        updateOptimizedExecutionWithDoneOrder(event.getContent());

        logger.info("Update Order : {}", event.getContent().toStringSummary());
    }

    /**
     * 執行モードの設定を行います
     *
     * @param executionMode
     */
    public void setExecutionMode(ExecutionMode executionMode) {
        this.executionMode = executionMode;
    }

    public ExecutionMode getExecutionMode() {
        return executionMode;
    }

    // //////////////////////////////////////
    // Inner Class
    // //////////////////////////////////////

    public enum ExecutionMode {
        /** 即時執行 */
        IMMEDIATE(0),
        /** 即時執行 */
        IMMEDIATE_DISADVANTAGE(0.1),

        /** 次のレートで執行 */
        NEXT(0),
        /** 次のレートを固定値の不利なプライスで執行 */
        NEXT_DISADVANTAGE(0.1),
        /** 前のレートと次のレートの悪いほうで執行 */
        NEXT_WORSE(0),

        /** 固定Latencyを考慮して、その中の最悪価格で執行 */
        LATENCY_WORSE(0);

        private double disadvantagePips;

        ExecutionMode(double disadvantagePips) {
            this.disadvantagePips = disadvantagePips;
        }

        public double getDisadvantagePips() {
            return disadvantagePips;
        }
    }

}

