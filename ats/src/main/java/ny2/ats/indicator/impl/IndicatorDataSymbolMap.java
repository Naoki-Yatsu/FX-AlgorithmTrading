package ny2.ats.indicator.impl;

import java.lang.reflect.Constructor;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ny2.ats.core.common.Period;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.exception.ATSRuntimeException;
import ny2.ats.indicator.Indicator;
import ny2.ats.indicator.IndicatorType;

/**
 * IndicatorType, Symbol ごとのデータを保持するクラスです。<br>
 * データ保持の階層構造は以下の通りです。<br>
 *    {@literal IndicatorDataHolderImpl > IndicatorDataMap > [#]IndicatorDataSymbolMap > Indicator}
 * @param <V> Indicator
 */
public class IndicatorDataSymbolMap<V extends Indicator<?>> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** 対象のインディケーター */
    private final IndicatorType type;

    /** 対象の通貨ペア */
    private final Symbol symbol;

    /** Periodごとのデータ用のMap */
    private final Map<Period, V> dataMap;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    @SuppressWarnings("unchecked")
    public IndicatorDataSymbolMap(IndicatorType type, Symbol symbol, Set<Period> indicatorPeriodAll) {
        this.type = type;
        this.symbol = symbol;
        dataMap = new EnumMap<>(Period.class);

        // initializeMap
        for (Period period : indicatorPeriodAll) {
            dataMap.put(period, null);
        }

        // Indicator作成
        createValueInstance((Class<V>) type.getIndicatorClass());
    }


    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    /**
     * 対象のIndicatorを返します。
     * @param period
     * @return
     */
    public V getIndicator(Period period) {
        return dataMap.get(period);
    }

    /**
     * Indicatorのインスタンスを作成します。一度のみ呼び出します。
     * @param clazz
     */
    private void createValueInstance(Class<V> clazz) {
        Constructor<V> ct;
        try {
            ct = clazz.getConstructor(IndicatorType.class, Symbol.class, Period.class);
            for (Entry<Period, V> entry : dataMap.entrySet()) {
                // 既にインスタンスが作成済みであれば作成しない。
                if (entry.getValue() != null) {
                    continue;
                }
                Period period = entry.getKey();
                V instance = (V) ct.newInstance(type, symbol, period);
                dataMap.put(period, instance);
            }
        } catch (Exception e) {
            throw new ATSRuntimeException(clazz.getSimpleName() + " Indicatorインスタンス作成でエラーが発生しました。", e);
        }
    }

    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

    public IndicatorType getType() {
        return type;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public Map<Period, V> getDataMap() {
        return dataMap;
    }
}
