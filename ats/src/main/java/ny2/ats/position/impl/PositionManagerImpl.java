package ny2.ats.position.impl;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import ny2.ats.core.common.OrderAction;
import ny2.ats.core.common.OrderStatus;
import ny2.ats.core.common.OrderType;
import ny2.ats.core.common.Period;
import ny2.ats.core.common.Side;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.data.MarketData;
import ny2.ats.core.data.Order;
import ny2.ats.core.data.Order.OrderBuilder;
import ny2.ats.core.data.PLInformation;
import ny2.ats.core.data.Position;
import ny2.ats.core.event.EventType;
import ny2.ats.core.event.IEventListener;
import ny2.ats.core.event.MarketUpdateEvent;
import ny2.ats.core.event.OrderUpdateEvent;
import ny2.ats.core.event.PLInformationEvent;
import ny2.ats.core.event.PositionUpdateEvent;
import ny2.ats.core.event.TimerInformationEvent;
import ny2.ats.core.router.IEventRouter;
import ny2.ats.core.util.ExceptionUtility;
import ny2.ats.core.util.NumberUtility;
import ny2.ats.market.connection.MarketType;
import ny2.ats.model.ModelType;
import ny2.ats.model.ModelVersion;
import ny2.ats.position.IPositionHolder;
import ny2.ats.position.IPositionManager;

/**
 * ポジションサービスの管理クラスです。
 */
@Service
@ManagedResource(objectName="PositionService:name=PositionManager")
public class PositionManagerImpl implements IPositionManager, IEventListener {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // Logger
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** インスタンス識別用のUUID */
    private final UUID uuid = UUID.randomUUID();

    @Autowired
    private IEventRouter eventRouter;

    @Autowired
    private IPositionHolder positionHolder;

    /** 現在の最新レート */
    private final Map<Symbol, MarketData> marketDataMap = new ConcurrentHashMap<>();

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    @PostConstruct
    private void init() {
        logger.info("PostConstruct instance.");

        // listener登録
        eventRouter.registerListener(EventType.MARKET_UPDATE, this);
        eventRouter.registerListener(EventType.ORDER_UPDATE, this);
        eventRouter.registerListener(EventType.TIMER_INFORMATION, this);
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public void onEvent(MarketUpdateEvent event) {
        // 最新レートを保持
        MarketData marketData = event.getContent();
        marketDataMap.put(marketData.getSymbol(), marketData);
    }

    @Override
    public void onEvent(OrderUpdateEvent event) {
        Order order = event.getContent();
        if (order.getOrderStatus() != OrderStatus.FILLED) {
            return;
        }
        // Position更新
        Symbol symbol = order.getSymbol();
        Symbol ccy2Jpy = symbol.getCcy2JpySymbol();
        MarketData marketData = marketDataMap.get(symbol);
        MarketData ccy2JpyMarketData = marketDataMap.get(ccy2Jpy);
        // ccy2jpyが存在しない場合は、とりあえずUSDJPYで計算する
        if (ccy2JpyMarketData == null) {
            logger.error("Market Data : " + ccy2Jpy.name() + " is NOT exist.");
            ccy2JpyMarketData = marketDataMap.get(Symbol.USDJPY);
        }
        Position position = positionHolder.updatePosition(order, marketData, ccy2JpyMarketData);

        // Event作成
        PositionUpdateEvent positionUpdateEvent = new PositionUpdateEvent(uuid, getClass(), position);
        eventRouter.addEvent(positionUpdateEvent);

        logger.info("Create New Position : {}", position.toStringSummary());
    }

    @Override
    public void onEvent(TimerInformationEvent event) {
        // 1分ごとに計算する
        if (event.getPeriod()!=Period.MIN_1) {
            return;
        }

        // PLを再計算します
        calcPlAmountJpy();

        // 現在のPLイベントを送信(1分ごと)
        if (event.getPeriod() == Period.MIN_1) {
            PLInformation plInformation = new PLInformation(PLInformation.PLInformationType.ALL, null, null,
                    getPlAmountJpy(), positionHolder.getAllPlJpy(), positionHolder.getNetAmountAll(), event.getContent().getCurrentDateTime(), marketDataMap);
            eventRouter.addEvent(new PLInformationEvent(uuid, getClass(), plInformation));
        }
    }

    private void calcPlAmountJpy() {
        positionHolder.updatePl(marketDataMap);
    }

    @Override
    public int getPlAmountJpy() {
        return positionHolder.getTotalPlJpy();
    }

    // //////////////////////////////////////
    // Method (JMX)
    // //////////////////////////////////////

    /**
     * 現在のポジションを編集します。
     * ポジションの変更にはダミーオーダーを使用します。
     *
     * @param symbolStr
     * @param netAmount
     * @param averagePrice
     * @return
     */
    @ManagedOperation
    @ManagedOperationParameters({
        @ManagedOperationParameter(name = "symbolStr", description = "Symbol.name()"),
        @ManagedOperationParameter(name = "netAmount", description = "Signed(+-) Net amount of the symbol"),
        @ManagedOperationParameter(name = "averagePrice", description = "Average price of the position")})
    public String WARN_editPosition(String symbolStr, int netAmount, double averagePrice) {
        try {
            Symbol symbol = Symbol.valueOf(symbolStr);
            Position position = positionHolder.getPosition(symbol);
            if (position == null) {
                // ダミーのポジションを作成
                // amountはゼロで作成されるのでそのままでOK
                position = new Position(symbol);
            }

            // netAmountが変更後になるように差分のAmountを計算します
            int orderAmount = netAmount - position.getNetOpenAmount();
            Side side = null;
            if (orderAmount > 0 ) {
                side = Side.BUY;
            } else if (orderAmount < 0) {
                side = Side.SELL;
                orderAmount = Math.abs(orderAmount);
            } else {
                return "Error: New net amount and current net amount are the same.";
            }

            // averagePriceが変更後になるように差分のexecutePriceを計算します
            double executePrice = 0.0;
            if (NumberUtility.almostEquals(averagePrice, 0.0, 0.000001)) {
                // 指定がゼロの時は現在価格をexecutePriceとする。
                executePrice = marketDataMap.get(symbol).getMidPrice();
            } else {
                executePrice = Math.abs(netAmount * averagePrice - position.getNetOpenAmount() * position.getAveragePrice()) / orderAmount;
            }

            // ダミーオーダーを作成します。
            OrderBuilder orderBuilder = OrderBuilder.getBuilder()
                    // 全般
                    .setSymbol(symbol)
                    .setMarketType(MarketType.DUMMY)
                    .setOrderId(new Long(99999999))
                    .setOrderAction(OrderAction.FILL)
                    .setOrderStatus(OrderStatus.FILLED)
                    .setModelType(ModelType.POSITION)
                    .setModelVersion(ModelVersion.DUMMY_VERSION)
                    // 詳細
                    .setSide(side)
                    .setOrderType(OrderType.MARKET)
                    .setOrderPrice(0)
                    .setOrderAmount(orderAmount)
                    .setQuoteId(null)
                    // 約定
                    .setMarketPositionId(null)
                    .setExecutePrice(executePrice)
                    .setExecuteAmount(orderAmount)
                    .setOriginalOrderId(null)
                    .setOriginalMarketId(null)
                    .setMarketDateTime(null);

            // Event作成
            OrderUpdateEvent orderUpdateEvent = new OrderUpdateEvent(uuid, getClass(), orderBuilder.createInstance());
            eventRouter.addEvent(orderUpdateEvent);

            return "Create dummy order : " + orderUpdateEvent.getContent().toStringSummary();
        } catch (Exception e) {
            return ExceptionUtility.getStackTraceString(e);
        }
    }

    /**
     * 全ての通貨ペアのポジションを表示します
     * @return
     */
    @ManagedOperation
    public String showAllPosition() {
        StringBuilder sb = new StringBuilder("All Position : \n");
        for (Position position : positionHolder.getAllPosition().values()) {
            sb.append("[").append(position.getSymbol()).append("] ").append(position.getNetOpenAmount()).append(", ").append(position.getAveragePrice()).append("\n");
        }
        return sb.toString();
    }
}
