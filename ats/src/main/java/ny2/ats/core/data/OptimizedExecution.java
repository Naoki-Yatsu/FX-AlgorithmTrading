package ny2.ats.core.data;

import java.time.LocalDateTime;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import ny2.ats.core.common.Side;
import ny2.ats.core.common.Symbol;
import ny2.ats.market.connection.MarketType;
import ny2.ats.market.order.OptimizerMethod;

/**
 * class for data of execution optimization
 */
@Getter
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class OptimizedExecution extends AbstractData {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // 全般
    /** 通貨ペア */
    private Symbol symbol;

    private OptimizerMethod optimizerMethod;

    /** Market種別 */
    private MarketType marketType;

    /** OrderID */
    private Long orderId;

    // 詳細
    /** Buy/Sell */
    private Side side;

    // レート
    /** 注文レート */
    private double orderPrice;

    /** 執行用の基準価格 */
    private double operatePrice;

    /** 約定レート */
    private double executePrice;

    // ID
    /** オーダーの元のレートID */
    private String orderQuoteId;

    /** 実行執行用の基準レートID */
    private String operateQuoteId;

    // 執行モデル情報
    /** 注文時の執行モデル情報 */
    private double signalOrder;

    /** 執行時の注文モデル情報 */
    private double signalOperate;

    // 時刻
    /** モデルからの注文作成日時 */
    private LocalDateTime orderDateTime;

    /** 執行注文時刻 */
    private LocalDateTime operateDateTime;

    /** マーケット更新日時 */
    private LocalDateTime executeDateTime;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    /**
     * 新規オーダーの情報からインスタンスを作成します
     * @param order
     * @param signalOrder
     * @return
     */
    public static OptimizedExecution createFromNewOrder(OptimizerMethod method, Order order, double signalOrder) {
        OptimizedExecution optimizedExecution = new OptimizedExecution();

        optimizedExecution.optimizerMethod = method;
        optimizedExecution.symbol = order.getSymbol();
        optimizedExecution.marketType = order.getMarketType();
        optimizedExecution.orderId = order.getOrderId();

        optimizedExecution.side = order.getSide();
        optimizedExecution.orderPrice = order.getOrderPrice();
        optimizedExecution.orderQuoteId = order.getQuoteId();
        optimizedExecution.orderDateTime = order.getOrderDateTime();

        optimizedExecution.signalOrder = signalOrder;

        return optimizedExecution;
    }

//    public static OptimizedExecution createFromFilledOrder(OptimizerMethod method, Order order) {
//        OptimizedExecution optimizedExecution = new OptimizedExecution();
//
//        optimizedExecution.optimizerMethod = method;
//        optimizedExecution.symbol = order.getSymbol();
//        optimizedExecution.marketType = order.getMarketType();
//        optimizedExecution.orderId = order.getOrderId();
//
//        optimizedExecution.side = order.getSide();
//        optimizedExecution.orderPrice = order.getOrderPrice();
//        optimizedExecution.operatePrice = 0.0;
//        optimizedExecution.executePrice = order.getExecutePrice();
//
//        optimizedExecution.orderQuoteId = order.getQuoteId();
//        optimizedExecution.operateQuoteId = "";
//
//        optimizedExecution.signalOrder = 0.0;
//        optimizedExecution.signalOperate = 0.0;
//
//        optimizedExecution.orderDateTime = order.getOrderDateTime();
//        optimizedExecution.operateDateTime = order.getMarketDateTime();
//        optimizedExecution.executeDateTime = order.getMarketDateTime();
//
//        return optimizedExecution;
//    }


    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public String toStringSummary() {
        StringBuilder sb = new StringBuilder(200);
        sb.append("OptimizedExecution [")
                .append(optimizerMethod).append(TO_STRING_DELIMITER)
                .append(marketType).append(TO_STRING_DELIMITER)
                .append(symbol).append(TO_STRING_DELIMITER)
                .append(orderId)
                .append("]");
        return sb.toString();
    }

    /**
     * 執行実行時の情報を追記します。
     * @param operateQuoteId
     * @param operatePrice
     * @param signalOperate
     */
    public void supplyOperatingInfo(String operateQuoteId, double operatePrice, double signalOperate) {
        this.operateQuoteId = operateQuoteId;
        this.operatePrice = operatePrice;
        this.signalOperate = signalOperate;
        this.operateDateTime = LocalDateTime.now();
    }

    /**
     * 執行済みオーダーの情報を追記します。
     * @param order
     */
    public void supplyFromExecutedOrder(Order order) {
        this.executePrice = order.getExecutePrice();
        this.executeDateTime = order.getMarketDateTime();
        // 作成時間はこのタイミングで設定する ⇒ Done通知を処理した時間がわかるため
        this.createDateTime = LocalDateTime.now();
    }

    /**
     * 最適化メソッドを変更します
     * @param optimizerMethod
     */
    public void setOptimizerMethod(OptimizerMethod optimizerMethod) {
        this.optimizerMethod = optimizerMethod;
    }

    /**
     * 執行時間を記録します
     * @param executeDateTime
     */
    public void setExecuteDateTime(LocalDateTime executeDateTime) {
        this.executeDateTime = executeDateTime;
    }
}
