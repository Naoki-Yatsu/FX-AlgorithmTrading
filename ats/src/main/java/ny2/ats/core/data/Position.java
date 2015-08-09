package ny2.ats.core.data;

import java.time.LocalDateTime;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import ny2.ats.core.common.Side;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.exception.ATSRuntimeException;

/**
 * ポジションをあらわすクラスです
 */
@Getter
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class Position extends AbstractData implements Cloneable {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** 対象のSymbol */
    private Symbol symbol;

    /** 未決済ポジションのNet Amount。BUYが多いほうがLongでプラス。 */
    private int netOpenAmount = 0;

    /** 未決済ポジションの平均プライス */
    private double averagePrice = 1.0;

    /** 未決済PLの計算価格 */
    private double calcPrice = 1.0;

    /** 全PL(未決済込み) */
    private int totalPlJpy = 0;

    /** 決済済みPL(JPY) */
    private int realizedPlJpy = 0;

    /** 決済済みPL(CCy2) */
    private double realizedPlCcy2 = 0;

    /** 決済済みPL Pips（Amount考慮しない） */
    private double realizedPlPips = 0;

    /** 最終執行オーダー */
    private Long lastExecuteOrderId;

    /** 最終約定Market日時(バックテストで重要) */
    private LocalDateTime executeDateTime;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public Position(Symbol symbol) {
        this.symbol = symbol;
    }

    @Override
    public Position clone() {
        try {
            Position position = (Position) super.clone();
            position.setCreateDateTime(LocalDateTime.now());
            return position;
        } catch (CloneNotSupportedException e) {
            throw new ATSRuntimeException(e);
        }
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public String toStringSummary() {
        StringBuilder sb = new StringBuilder(200);
        sb.append("Position [")
                .append(symbol.name()).append(TO_STRING_DELIMITER)
                .append(lastExecuteOrderId).append(TO_STRING_DELIMITER)
                .append(netOpenAmount).append(ITEM_DELIMITER).append(averagePrice).append(TO_STRING_DELIMITER)
                .append(totalPlJpy).append(TO_STRING_DELIMITER)
                .append(executeDateTime)
                .append("]");
        return sb.toString();
    }

    /**
     * Positionを登録します。
     * TODO JPYに換算の際にBidを使っているが、損失の場合はUSD等を買うのでaskを使うべき
     *
     * @param filledOrder
     * @param marketData
     * @param ccy2JpyMarketData
     */
    public synchronized void updatePosition(Order filledOrder, MarketData marketData, MarketData ccy2JpyMarketData) {

        // Update PL
        if (filledOrder.getSide() == Side.BUY) {
            // オーダーがBUYの場合
            // 現在ロングであればポジションを加算
            if (netOpenAmount >= 0) {
                averagePrice = (netOpenAmount * averagePrice + filledOrder.getOrderAmount() * filledOrder.getExecutePrice()) / (netOpenAmount + filledOrder.getOrderAmount());
                netOpenAmount += filledOrder.getOrderAmount();
            } else {
                // OrderAmountが現在のOpenAmountより小さければ全量決済、大きければ一部決済
                // Short-PositionのClose
                if (Math.abs(netOpenAmount) >= filledOrder.getOrderAmount()) {
                    updatePlJpy(filledOrder.getOrderAmount(), averagePrice - filledOrder.getExecutePrice(), ccy2JpyMarketData.getBidPrice());
                    netOpenAmount += filledOrder.getOrderAmount();
                } else {
                    updatePlJpy(Math.abs(netOpenAmount), averagePrice - filledOrder.getExecutePrice(), ccy2JpyMarketData.getBidPrice());
                    averagePrice = filledOrder.getExecutePrice();
                    netOpenAmount += filledOrder.getOrderAmount();
                }
            }
        } else {
            // オーダーがSELLの場合
            // 現在ショートであればポジションを加算
            if (netOpenAmount <= 0) {
                averagePrice = (Math.abs(netOpenAmount) * averagePrice + filledOrder.getOrderAmount() * filledOrder.getExecutePrice()) / (Math.abs(netOpenAmount) + filledOrder.getOrderAmount());
                netOpenAmount -= filledOrder.getOrderAmount();
            } else {
                // OrderAmountが現在のOpenAmountより小さければ全量決済、大きければ一部決済
                // Long-PositionのClose
                if (Math.abs(netOpenAmount) >= filledOrder.getOrderAmount()) {
                    updatePlJpy(filledOrder.getOrderAmount(), filledOrder.getExecutePrice() - averagePrice, ccy2JpyMarketData.getBidPrice());
                    netOpenAmount -= filledOrder.getOrderAmount();
                } else {
                    updatePlJpy(Math.abs(netOpenAmount), filledOrder.getExecutePrice() - averagePrice, ccy2JpyMarketData.getBidPrice());
                    averagePrice = filledOrder.getExecutePrice();
                    netOpenAmount -= filledOrder.getOrderAmount();
                }
            }
        }

        // TotalPL更新
        calcTotalPlJpy(marketData, ccy2JpyMarketData);
        // 計算価格
        calcPrice = getClosePrice(marketData);

        // 時刻更新など
        lastExecuteOrderId = filledOrder.getOrderId();
        executeDateTime = filledOrder.getMarketDateTime();
    }

    /**
     * 決済ポジションのPLを更新します。
     *
     * @param ccy2Pl Ccy2のPL
     * @param ccy2JpyPrice 通貨2/JPYのレート
     * @return
     */
    private void updatePlJpy(int ccy2Amount, double plRate, double ccy2JpyPrice) {
        realizedPlCcy2 += ccy2Amount * plRate;
        realizedPlPips += symbol.convertRealToPips(plRate);
        if (symbol.isContainJpy()) {
            realizedPlJpy += (int) ccy2Amount * plRate;
        } else {
            realizedPlJpy += (int) ccy2Amount * plRate * ccy2JpyPrice;
        }
    }

    /**
     * JPY換算のTotalのPLを計算します。
     *
     * @param currentMarketData
     * @param ccy2JpyMarketData
     * @return
     */
    public int calcTotalPlJpy(MarketData currentMarketData, MarketData ccy2JpyMarketData) {
        totalPlJpy = getOpenPositionPlJpy(currentMarketData, ccy2JpyMarketData) + realizedPlJpy;
        return totalPlJpy;
    }

    /**
     * JPY換算の未決済ポジションのPLを返します
     *
     * @param currentMarketData
     * @param ccy2JpyMarketData
     * @return
     */
    public int getOpenPositionPlJpy(MarketData currentMarketData, MarketData ccy2JpyMarketData) {
        int unrealizedPlJpy = 0;
        int unrealizedPlCcy2 = 0;
        // CCY2 PL
        // Long/Shortで場合分け
        if (netOpenAmount >= 0) {
            // Long
            unrealizedPlCcy2 = (int) ((getClosePrice(currentMarketData) - averagePrice) * netOpenAmount);
        } else {
            // Short - netOpenAmountがマイナスなので注意
            unrealizedPlCcy2 = (int) ((averagePrice - getClosePrice(currentMarketData)) * (- netOpenAmount));
        }

        // JPY PL
        // 対円かどうかで場合分け
        if (symbol.isContainJpy()) {
            unrealizedPlJpy = unrealizedPlCcy2;
        } else {
            unrealizedPlJpy = (int) (unrealizedPlCcy2 * ccy2JpyMarketData.getBidPrice());
        }
        return unrealizedPlJpy;
    }

    /**
     * JPY換算のTotalのPLを返します
     * @param currentMarketData
     * @return
     */
    public double getTotalPlPips(MarketData currentMarketData) {
        // Long/Shortで場合分け
        if (netOpenAmount >= 0) {
            return (getClosePrice(currentMarketData) - averagePrice) + realizedPlPips;
        } else {
            return (averagePrice - getClosePrice(currentMarketData)) + realizedPlPips;
        }
    }

    /**
     * ポジションのロング／ショートの状態に応じた決済に使用する価格を返します。
     * @param currentMarketData
     * @return
     */
    public double getClosePrice(MarketData currentMarketData) {
        // Long/Shortで場合分け
        if (netOpenAmount >= 0) {
            // ロングの時はSELLなのでBid
            return currentMarketData.getBidPrice();
        } else {
            // ショートの時はBUYなのでAsk
            return currentMarketData.getAskPrice();
        }
    }

    // //////////////////////////////////////
    // Builder
    // //////////////////////////////////////

//    /**
//     * Builder for creating Position.
//     */
//    @Getter
//    @ToString
//    public static class PositionBuilder implements DataBuilder<Position> {
//        private Symbol symbol;
//        private int netOpenAmount = 0;
//        private double averagePrice = 1.0;
//        private double calcPrice = 1.0;
//        private int totalPlJpy = 0;
//        private int realizedPlJpy = 0;
//        private double realizedPlCcy2 = 0;
//        private double realizedPlPips = 0;
//        private String lastExecuteOrderId;
//        private LocalDateTime executeDateTime;
//        private LocalDateTime updateDateTime;
//
//        public static PositionBuilder getBuilder() {
//            return new PositionBuilder();
//        }
//
//        public static PositionBuilder getBuilder(Position source) {
//            return new PositionBuilder()
//                    .setSymbol(source.getSymbol())
//                    .setNetOpenAmount(source.getNetOpenAmount())
//                    .setAveragePrice(source.getAveragePrice())
//                    .setCalcPrice(source.getCalcPrice())
//                    .setTotalPlJpy(source.getTotalPlJpy())
//                    .setRealizedPlJpy(source.getRealizedPlJpy())
//                    .setRealizedPlCcy2(source.getRealizedPlCcy2())
//                    .setRealizedPlPips(source.getRealizedPlPips())
//                    .setLastExecuteOrderId(source.getLastExecuteOrderId())
//                    .setExecuteDateTime(source.getExecuteDateTime())
//                    .setUpdateDateTime(source.getUpdateDateTime());
//        }
//
//        @Override
//        public Position createInstance() {
//            return null;
//
//        }
//        public PositionBuilder setSymbol(Symbol symbol) {
//            this.symbol = symbol;
//            return this;
//        }
//        public PositionBuilder setNetOpenAmount(int netOpenAmount) {
//            this.netOpenAmount = netOpenAmount;
//            return this;
//        }
//        public PositionBuilder setAveragePrice(double averagePrice) {
//            this.averagePrice = averagePrice;
//            return this;
//        }
//        public PositionBuilder setCalcPrice(double calcPrice) {
//            this.calcPrice = calcPrice;
//            return this;
//        }
//        public PositionBuilder setTotalPlJpy(int totalPlJpy) {
//            this.totalPlJpy = totalPlJpy;
//            return this;
//        }
//        public PositionBuilder setRealizedPlJpy(int realizedPlJpy) {
//            this.realizedPlJpy = realizedPlJpy;
//            return this;
//        }
//        public PositionBuilder setRealizedPlCcy2(double realizedPlCcy2) {
//            this.realizedPlCcy2 = realizedPlCcy2;
//            return this;
//        }
//        public PositionBuilder setRealizedPlPips(double realizedPlPips) {
//            this.realizedPlPips = realizedPlPips;
//            return this;
//        }
//        public PositionBuilder setLastExecuteOrderId(String lastExecuteOrderId) {
//            this.lastExecuteOrderId = lastExecuteOrderId;
//            return this;
//        }
//        public PositionBuilder setExecuteDateTime(LocalDateTime executeDateTime) {
//            this.executeDateTime = executeDateTime;
//            return this;
//        }
//        public PositionBuilder setUpdateDateTime(LocalDateTime updateDateTime) {
//            this.updateDateTime = updateDateTime;
//            return this;
//        }
//    }
}
