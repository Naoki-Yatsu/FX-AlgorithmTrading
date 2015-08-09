package ny2.ats.model.tool;

import java.util.EnumMap;
import java.util.Map;

import ny2.ats.core.common.Symbol;
import ny2.ats.core.data.MarketData;

/**
 * Spread Checker (temporal implementation)
 */
public class SpreadChecker {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** Spread基準Map(狭い) */
    private static final Map<Symbol, Double> spreadNarrowMap = new EnumMap<>(Symbol.class);

    /** Spread基準Map(中程度) */
    private static final Map<Symbol, Double> spreadMiddleMap = new EnumMap<>(Symbol.class);

    /** Spread基準Map(広い) */
    private static final Map<Symbol, Double> spreadWidewMap = new EnumMap<>(Symbol.class);

    static {
        // add major for temporary
        addSpreadMap(Symbol.USDJPY, 3, 10, 20);
        addSpreadMap(Symbol.EURUSD, 3, 10, 20);

        // USD
        addSpreadMap(Symbol.GBPUSD, 5, 10, 20);
        addSpreadMap(Symbol.AUDUSD, 5, 10, 20);
        addSpreadMap(Symbol.NZDUSD, 5, 10, 20);
        addSpreadMap(Symbol.USDCAD, 5, 10, 20);
        addSpreadMap(Symbol.USDCHF, 5, 10, 20);

        // JPY
        addSpreadMap(Symbol.EURJPY, 5, 10, 20);
        addSpreadMap(Symbol.GBPJPY, 5, 10, 20);
        addSpreadMap(Symbol.AUDJPY, 5, 10, 20);
        addSpreadMap(Symbol.CADJPY, 5, 10, 20);
        addSpreadMap(Symbol.CHFJPY, 5, 10, 20);
        addSpreadMap(Symbol.NZDJPY, 5, 10, 20);

        // EUR
        addSpreadMap(Symbol.EURGBP, 5, 10, 20);
        addSpreadMap(Symbol.EURCHF, 5, 10, 20);

    }

    private static void addSpreadMap(Symbol symbol, int narrow, int middle, int wide) {
        spreadNarrowMap.put(symbol, narrow * symbol.getPipValue());
        spreadMiddleMap.put(symbol, middle * symbol.getPipValue());
        spreadWidewMap.put(symbol, wide * symbol.getPipValue());
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    /**
     * MarketDataのSpreadがNarrowの範囲内か判断します。
     * 登録外Symbolではtrueを返します
     *
     * @param marketData
     * @return
     */
    public static boolean checkSpreadNarrow(MarketData marketData) {
        if (spreadNarrowMap.containsKey(marketData.getSymbol())) {
            if (marketData.getSpread() >= spreadNarrowMap.get(marketData.getSymbol())) {
                return false;
            }
        }
        return true;
    }

    /**
     * MarketDataのSpreadがMidedleの範囲内か判断します。
     * 登録外Symbolではtrueを返します
     *
     * @param marketData
     * @return
     */
    public static boolean checkSpreadMiddle(MarketData marketData) {
        if (spreadMiddleMap.containsKey(marketData.getSymbol())) {
            if (marketData.getSpread() >= spreadMiddleMap.get(marketData.getSymbol())) {
                return false;
            }
        }
        return true;
    }

    /**
     * MarketDataのSpreadがWideの範囲内か判断します。
     * 登録外Symbolではtrueを返します
     *
     * @param marketData
     * @return
     */
    public static boolean checkSpreadWide(MarketData marketData) {
        if (spreadWidewMap.containsKey(marketData.getSymbol())) {
            if (marketData.getSpread() >= spreadWidewMap.get(marketData.getSymbol())) {
                return false;
            }
        }
        return true;
    }


    /**
     * 指定SymbolのNarrow-Spreadの閾値を返します。
     * 登録されていない場合は、Double.NaNを返します。
     *
     * @param symbol
     * @return
     */
    public static Double getSpreadNarrow(Symbol symbol) {
        if (spreadNarrowMap.containsKey(symbol)) {
            return spreadNarrowMap.get(symbol);
        } else {
            return Double.NaN;
        }
    }

    /**
     * 指定SymbolのMiddle-Spreadの閾値を返します。
     * 登録されていない場合は、Double.NaNを返します。
     *
     * @param symbol
     * @return
     */
    public static Double getSpreadMiddle(Symbol symbol) {
        if (spreadMiddleMap.containsKey(symbol)) {
            return spreadMiddleMap.get(symbol);
        } else {
            return Double.NaN;
        }
    }

    /**
     * 指定SymbolのWide-Spreadの閾値を返します。
     * 登録されていない場合は、Double.NaNを返します。
     *
     * @param symbol
     * @return
     */
    public static Double getSpreadWide(Symbol symbol) {
        if (spreadWidewMap.containsKey(symbol)) {
            return spreadWidewMap.get(symbol);
        } else {
            return Double.NaN;
        }
    }

    public static double getSpreadNarrowPips(Symbol symbol) {
        return symbol.convertRealToPips(getSpreadNarrow(symbol));
    }

    public static double getSpreadMiddlePips(Symbol symbol) {
        return symbol.convertRealToPips(getSpreadMiddle(symbol));
    }

    public static double getSpreadWidePips(Symbol symbol) {
        return symbol.convertRealToPips(getSpreadWide(symbol));
    }

}
