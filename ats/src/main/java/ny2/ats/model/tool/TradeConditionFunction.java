package ny2.ats.model.tool;

import java.util.function.BiFunction;

import ny2.ats.core.util.CollectionUtility;
import ny2.ats.indicator.CalcPeriod;
import ny2.ats.indicator.Indicator;
import ny2.ats.indicator.indicators.BollingerBandIndicator;
import ny2.ats.indicator.indicators.BollingerBandIndicator.BollingerPeriod;

/**
 * Function Samples for TradeCondition
 */
public abstract class TradeConditionFunction {

    //
    // 全般
    //

    /**
     * Indicatorの最新データを取得します。デフォルトではこの関数が使用されます
     */
    public static final BiFunction<Indicator<?>, CalcPeriod, Double> LAST = (indicator, calcPeriod) -> {
        return indicator.getLastValueWithCast(calcPeriod);
    };

    /**
     * Indicatorの最新データの1期間前のデータを取得します。
     */
    public static final BiFunction<Indicator<?>, CalcPeriod, Double> LAST_BEFORE = (indicator, calcPeriod) -> {
        Double d = CollectionUtility.getLastBefore(indicator.getValueListWithCast(calcPeriod));
        if (d != null) {
            return d;
        } else {
            return Double.NaN;
        }
    };


    //
    // 特定Indicator向け
    //

    /**
     * Bollingerのsigmaを取得します
     */
    public static final BiFunction<Indicator<?>, CalcPeriod, Double> LAST_BOLLINGER_SIGMA = (indicator, calcPeriod) -> {
        return ((BollingerBandIndicator) indicator).getLastSigma((BollingerPeriod) calcPeriod);
    };


}
