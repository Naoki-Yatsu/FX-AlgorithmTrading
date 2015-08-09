package ny2.ats.indicator.impl;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import ny2.ats.core.common.Period;
import ny2.ats.core.common.Symbol;
import ny2.ats.indicator.Indicator;
import ny2.ats.indicator.IndicatorType;

/**
 * IndicatorTypeごとのデータを保持するクラスです。<br>
 * データ保持の階層構造は以下の通りです。
 *    {@literal IndicatorDataHolderImpl > [#]IndicatorDataMap > IndicatorDataSymbolMap > Indicator}
 *
 * @param <V> Indicator
 */
public class IndicatorDataMap<V extends Indicator<?>> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** 対象のインディケーター */
    private final IndicatorType type;

    /** Symbolごとのデータ用のMap */
    private final Map<Symbol, IndicatorDataSymbolMap<V>> dataMap = new EnumMap<>(Symbol.class);

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public IndicatorDataMap(IndicatorType type, Set<Symbol> indicatorSymbols, Set<Period> indicatorPeriodAll) {
        this.type = type;
        for (Symbol symbol : indicatorSymbols) {
            IndicatorDataSymbolMap<V> symbolMap = new IndicatorDataSymbolMap<V>(type, symbol, indicatorPeriodAll);
            dataMap.put(symbol, symbolMap);
        }
    }

    /**
     * 対象のSymbolを追加します
     * @param symbol
     */
    public void addSymbol(Symbol symbol, Set<Period> indicatorPeriodAll) {
        IndicatorDataSymbolMap<V> symbolMap = new IndicatorDataSymbolMap<V>(type, symbol, indicatorPeriodAll);
        dataMap.put(symbol, symbolMap);
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    public IndicatorDataSymbolMap<V> getSymbolMap(Symbol symbol) {
        return dataMap.get(symbol);
    }

    /**
     * SymbolごとのIndicatorのMapを返します。
     * @param symbol
     * @return
     */
    public Map<Period, V> getSymbolMapPeriod(Symbol symbol) {
        return (Map<Period, V>) dataMap.get(symbol).getDataMap();
    }

    /**
     * 対象のIndicatorを返します。
     * @param symbol
     * @param period
     * @return
     */
    public V getIndicator(Symbol symbol, Period period) {
        return dataMap.get(symbol).getIndicator(period);
    }

    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

    public IndicatorType getType() {
        return type;
    }

    public Map<Symbol, IndicatorDataSymbolMap<V>> getDatalMap() {
        return dataMap;
    }
}
