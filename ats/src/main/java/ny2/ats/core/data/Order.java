package ny2.ats.core.data;

import java.time.LocalDateTime;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import ny2.ats.core.common.OrderAction;
import ny2.ats.core.common.OrderStatus;
import ny2.ats.core.common.OrderType;
import ny2.ats.core.common.Side;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.util.IdGenerator;
import ny2.ats.market.connection.MarketType;
import ny2.ats.model.ModelType;
import ny2.ats.model.ModelVersion;

/**
 * オーダーをあらわすクラスです
 */
@Getter
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class Order extends AbstractData {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // ID作成
    private static final IdGenerator idGenerator = new IdGenerator();

    // 全般
    /** 通貨ペア */
    private final Symbol symbol;

    /** Market種別 */
    private final MarketType marketType;

    /** OrderID */
    private final Long orderId;

    /** アクション */
    private final OrderAction orderAction;

    /** ステータス */
    private final OrderStatus orderStatus;

    // モデル
    /** モデル種別 */
    private final ModelType modelType;

    /** モデル種別 */
    private final ModelVersion modelVersion;

    // 詳細
    /** Buy/Sell */
    private final Side side;

    /** オーダー種別 */
    private final OrderType orderType;

    /** 注文レート */
    private final double orderPrice;

    /** 金額 */
    private final int orderAmount;

    /** オーダーの元のレートID */
    private final String quoteId;

    // 約定
    /** Market側のOrder/PositionID(約定後のポジションを識別するキー情報) */
    private final String marketPositionId;

    /** 約定レート */
    private final double executePrice;

    /** 約定金額 */
    private final int executeAmount;

    /** 決済注文として扱う場合の元のオーダーID */
    private final Long originalOrderId;

    /** 決済注文として扱う場合の元のオーダーのMarket側のId */
    private final String originalMarketId;

    // 時刻
    /** マーケット更新日時 */
    private final LocalDateTime marketDateTime;

    /** モデルからの注文作成日時 */
    private final LocalDateTime orderDateTime;

    //
    // 以降の項目はDBに入れない
    //
    /** 執行最適化機能の使用有無（デフォルトは使用する） ※デフォルト変更時はBuilderも変更する */
    private boolean useOrderOptimizer = true;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public Order(Symbol symbol, MarketType marketType, Long orderId, OrderAction orderAction, OrderStatus orderStatus,
            ModelType modelType, ModelVersion modelVersion, Side side, OrderType orderType, double orderPrice, int orderAmount, String quoteId,
            String marketPositionId, double executePrice, int executeAmount, Long originalOrderId, String originalMarketId,
            LocalDateTime marketDateTime, LocalDateTime orderDateTime, boolean useOrderOptimizer) {
        super();
        this.symbol = symbol;
        this.marketType = marketType;
        this.orderId = orderId;
        this.orderAction = orderAction;
        this.orderStatus = orderStatus;
        this.modelType = modelType;
        this.modelVersion = modelVersion;
        this.side = side;
        this.orderType = orderType;
        this.orderPrice = orderPrice;
        this.orderAmount = orderAmount;
        this.quoteId = quoteId;
        this.marketPositionId = marketPositionId;
        this.executePrice = executePrice;
        this.executeAmount = executeAmount;
        this.originalOrderId = originalOrderId;
        this.originalMarketId = originalMarketId;
        this.marketDateTime = marketDateTime;
        this.orderDateTime = orderDateTime;
        this.useOrderOptimizer = useOrderOptimizer;
    }

    /**
     * 新規オーダーを作成します。
     */
    public static OrderBuilder createNewOrderBuilder(MarketType marketType,  ModelType modelType, ModelVersion modelVersion,
                String quoteId, Symbol symbol, Side side, OrderType orderType, double orderPrice, int orderAmount) {
        OrderBuilder orderBuilder = new OrderBuilder();
        orderBuilder.setOrderId(numberNewID());

        // 全般
        orderBuilder.setSymbol(symbol);
        orderBuilder.setMarketType(marketType);
        orderBuilder.setOrderAction(OrderAction.SUBMIT);
        orderBuilder.setOrderStatus(OrderStatus.NEW);

        // モデル
        orderBuilder.setModelType(modelType);
        orderBuilder.setModelVersion(modelVersion);

        // 詳細
        orderBuilder.setSide(side);
        orderBuilder.setOrderType(orderType);
        orderBuilder.setOrderPrice(orderPrice);
        orderBuilder.setOrderAmount(orderAmount);
        orderBuilder.setQuoteId(quoteId);

        // 時刻
        orderBuilder.setOrderDateTime(LocalDateTime.now());

        return orderBuilder;
    }

    /**
     * 既存ポジションの決済注文としての新規オーダーを作成します。
     */
    public static OrderBuilder createNewOrderBuilderAsClose(MarketType marketType,  ModelType modelType, ModelVersion modelVersion,
                String quoteId, Symbol symbol, Side side, OrderType orderType, double orderPrice, int orderAmount, Long originalOrderId, String originalMarketId) {
        OrderBuilder orderBuilder = new OrderBuilder();
        orderBuilder.setOrderId(numberNewID());

        // 全般
        orderBuilder.setSymbol(symbol);
        orderBuilder.setMarketType(marketType);
        orderBuilder.setOrderAction(OrderAction.SUBMIT);
        orderBuilder.setOrderStatus(OrderStatus.NEW);

        // モデル
        orderBuilder.setModelType(modelType);
        orderBuilder.setModelVersion(modelVersion);

        // 詳細
        orderBuilder.setSide(side);
        orderBuilder.setOrderType(orderType);
        orderBuilder.setOrderPrice(orderPrice);
        orderBuilder.setOrderAmount(orderAmount);
        orderBuilder.setQuoteId(quoteId);

        // 決済注文の元オーダー
        orderBuilder.setOriginalOrderId(originalOrderId);
        orderBuilder.setOriginalMarketId(originalMarketId);

        // 時刻
        orderBuilder.setOrderDateTime(LocalDateTime.now());

        return orderBuilder;
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public String toStringSummary() {
        StringBuilder sb = new StringBuilder(200);
        sb.append("Order [")
                .append(orderId).append(TO_STRING_DELIMITER)
                .append(orderAction.name()).append(ITEM_DELIMITER).append(orderStatus.name()).append(TO_STRING_DELIMITER)
                .append(modelType.name()).append(ITEM_DELIMITER).append(modelVersion.getName()).append(TO_STRING_DELIMITER)
                .append(marketType.name()).append(TO_STRING_DELIMITER)
                .append(symbol.name()).append(TO_STRING_DELIMITER)
                .append(orderPrice).append(ITEM_DELIMITER).append(executePrice).append(TO_STRING_DELIMITER)
                .append(marketDateTime)
                .append("]");
        return sb.toString();
    }

    /**
     * 約定済みのオーダーかどうか判断します。
     * @return
     */
    public boolean isFilled(){
        if (orderStatus == OrderStatus.FILLED) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * IDを採番します
     * @return
     */
    private static Long numberNewID() {
        return idGenerator.createLongIdDateTime6Number();
    }

    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

    public void changeUseOrderOptimizer(boolean use) {
        useOrderOptimizer = use;
    }

    // //////////////////////////////////////
    // Builder
    // //////////////////////////////////////

    /**
     * Builder for creating Order.
     */
    @Getter
    @ToString
    public static class OrderBuilder implements DataBuilder<Order> {
        // 全般
        private Symbol symbol;
        private MarketType marketType;
        private Long orderId;
        private OrderAction orderAction;
        private OrderStatus orderStatus;
        private ModelType modelType;
        private ModelVersion modelVersion;
        // 詳細
        private Side side;
        private OrderType orderType;
        private double orderPrice;
        private int orderAmount;
        private String quoteId;
        // 約定
        private String marketPositionId;
        private double executePrice;
        private int executeAmount;
        private Long originalOrderId;
        private String originalMarketId;
        private LocalDateTime marketDateTime;
        private LocalDateTime orderDateTime;
        // その他
        private boolean useOrderOptimizer = true;

        public static OrderBuilder getBuilder() {
            return new OrderBuilder();
        }

        public static OrderBuilder getBuilder(Order source) {
            return new OrderBuilder()
                    .setSymbol(source.getSymbol())
                    .setMarketType(source.getMarketType())
                    .setOrderId(source.getOrderId())
                    .setOrderAction(source.getOrderAction())
                    .setOrderStatus(source.getOrderStatus())
                    .setModelType(source.getModelType())
                    .setModelVersion(source.getModelVersion())
                    // 詳細
                    .setSide(source.getSide())
                    .setOrderType(source.getOrderType())
                    .setOrderPrice(source.getOrderPrice())
                    .setOrderAmount(source.getOrderAmount())
                    .setQuoteId(source.getQuoteId())
                    // 約定
                    .setMarketPositionId(source.getMarketPositionId())
                    .setExecutePrice(source.getExecutePrice())
                    .setExecuteAmount(source.getExecuteAmount())
                    .setOriginalOrderId(source.getOriginalOrderId())
                    .setOriginalMarketId(source.getOriginalMarketId())
                    .setMarketDateTime(source.getMarketDateTime())
                    .setOrderDateTime(source.getOrderDateTime())
                    .setUseOrderOptimizer(source.isUseOrderOptimizer());
        }

        @Override
        public Order createInstance() {
            return new Order(
                    symbol,
                    marketType,
                    orderId,
                    orderAction,
                    orderStatus,
                    modelType,
                    modelVersion,
                    side,
                    orderType,
                    orderPrice,
                    orderAmount,
                    quoteId,
                    marketPositionId,
                    executePrice,
                    executeAmount,
                    originalOrderId,
                    originalMarketId,
                    marketDateTime,
                    orderDateTime,
                    useOrderOptimizer);
        }

        public OrderBuilder setSymbol(Symbol symbol) {
            this.symbol = symbol;
            return this;
        }
        public OrderBuilder setMarketType(MarketType marketType) {
            this.marketType = marketType;
            return this;
        }
        public OrderBuilder setOrderId(Long orderId) {
            this.orderId = orderId;
            return this;
        }
        public OrderBuilder setOrderAction(OrderAction orderAction) {
            this.orderAction = orderAction;
            return this;
        }
        public OrderBuilder setOrderStatus(OrderStatus orderStatus) {
            this.orderStatus = orderStatus;
            return this;
        }
        public OrderBuilder setModelType(ModelType modelType) {
            this.modelType = modelType;
            return this;
        }
        public OrderBuilder setModelVersion(ModelVersion modelVersion) {
            this.modelVersion = modelVersion;
            return this;
        }
        public OrderBuilder setSide(Side side) {
            this.side = side;
            return this;
        }
        public OrderBuilder setOrderType(OrderType orderType) {
            this.orderType = orderType;
            return this;
        }
        public OrderBuilder setOrderPrice(double orderPrice) {
            this.orderPrice = orderPrice;
            return this;
        }
        public OrderBuilder setOrderAmount(int orderAmount) {
            this.orderAmount = orderAmount;
            return this;
        }
        public OrderBuilder setQuoteId(String quoteId) {
            this.quoteId = quoteId;
            return this;
        }
        public OrderBuilder setMarketPositionId(String marketPositionId) {
            this.marketPositionId = marketPositionId;
            return this;
        }
        public OrderBuilder setExecutePrice(double executePrice) {
            this.executePrice = executePrice;
            return this;
        }
        public OrderBuilder setExecuteAmount(int executeAmount) {
            this.executeAmount = executeAmount;
            return this;
        }
        public OrderBuilder setOriginalOrderId(Long originalOrderId) {
            this.originalOrderId = originalOrderId;
            return this;
        }
        public OrderBuilder setOriginalMarketId(String originalMarketId) {
            this.originalMarketId = originalMarketId;
            return this;
        }
        public OrderBuilder setMarketDateTime(LocalDateTime marketDateTime) {
            this.marketDateTime = marketDateTime;
            return this;
        }
        public OrderBuilder setOrderDateTime(LocalDateTime orderDateTime) {
            this.orderDateTime = orderDateTime;
            return this;
        }
        public OrderBuilder setUseOrderOptimizer(boolean useOrderOptimizer) {
            this.useOrderOptimizer = useOrderOptimizer;
            return this;
        }
    }
}
