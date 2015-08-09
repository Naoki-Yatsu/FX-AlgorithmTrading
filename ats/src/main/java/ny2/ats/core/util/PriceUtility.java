package ny2.ats.core.util;

import ny2.ats.core.common.Side;
import ny2.ats.core.data.MarketData;
import ny2.ats.core.data.Order;

public class PriceUtility {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    /**
     * Orderしようととしている価格より現在価格が有利もしくは同一かどうかか判断します。そのままオーダー可能かどうかの確認に使用します。
     *
     * @param side
     * @param orderPrice
     * @param currentPrice
     * @return
     */
    public static boolean isBetterOrEqualPrice(Side side, double orderPrice, double currentPrice) {
        if (side == Side.BUY) {
            // Buyの場合、現在価格が安かったら有利
            return (orderPrice >= currentPrice);
        } else {
            // Sellの場合、現在価格が高かったら有利
            return (orderPrice <= currentPrice);
        }
    }

    /**
     * より良い執行価格を返します
     * @param side
     * @param orderPrice
     * @param currentPrice
     * @return
     */
    public static double getBetterPrice(Side side, double price1, double prcie2) {
        if (side == Side.BUY) {
            // Buyの場合、価格が安いほうが有利
            return Math.min(price1, prcie2);
        } else {
            // Sellの場合、価格が高いほうが有利
            return Math.max(price1, prcie2);
        }
    }

    /**
     * より悪い執行価格を返します
     * @param side
     * @param price1
     * @param prcie2
     * @return
     */
    public static double getWorsePrice(Side side, double price1, double prcie2) {
        if (side == Side.BUY) {
            // Buyの場合、価格が高いほうが不利
            return Math.max(price1, prcie2);
        } else {
            // Sellの場合、価格が安いほうが不利
            return Math.min(price1, prcie2);
        }
    }

    /**
     * レートのスプレッドが指定範囲内か判断します。
     *
     * @param marketData
     * @param pipsSpread
     * @return
     */
    public static boolean isWithinSpread(MarketData marketData, double pipsSpread) {
        return marketData.getSpreadPips() <= pipsSpread;
    }


    // //////////////////////////////////////
    // Method - Order
    // //////////////////////////////////////

    /**
     * Orderの含み益をPips単位で計算します
     * @param order
     * @param marketData
     * @return
     */
    public static double calculateUnrealizedProfit(Order order, MarketData marketData) {
        double closePrice = marketData.getPrice(order.getSide().getCloseBidAsk());
        double profit = order.getSide() == Side.BUY ? closePrice - order.getExecutePrice() : order.getExecutePrice()- closePrice;
        return order.getSymbol().convertRealToPips(profit);
    }

}
