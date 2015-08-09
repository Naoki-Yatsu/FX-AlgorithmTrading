package ny2.ats.indicator;

import java.util.EnumSet;
import java.util.Set;

import ny2.ats.indicator.indicators.BollingerBandEMAIndicator;
import ny2.ats.indicator.indicators.BollingerBandIndicator;
import ny2.ats.indicator.indicators.BollingerBandIndicator.BollingerPeriod;
import ny2.ats.indicator.indicators.ExponentialMovingAverageIndicator;
import ny2.ats.indicator.indicators.IchimokuIndicator;
import ny2.ats.indicator.indicators.IchimokuIndicator.IchimokuPeriod;
import ny2.ats.indicator.indicators.LinearRegressionIndicator;
import ny2.ats.indicator.indicators.LinearRegressionIndicator.LRPeriod;
import ny2.ats.indicator.indicators.MACDIndicator;
import ny2.ats.indicator.indicators.MACDIndicator.MACDPeriod;
import ny2.ats.indicator.indicators.MovingAverageIndicator;
import ny2.ats.indicator.indicators.MovingAverageIndicator.MAPeriod;
import ny2.ats.indicator.indicators.OHLCIndicator;
import ny2.ats.indicator.indicators.OHLCIndicator.OHLCType;
import ny2.ats.indicator.indicators.PriceRangeIndicator;
import ny2.ats.indicator.indicators.PriceRangeIndicator.PriceRangePeriod;
import ny2.ats.indicator.indicators.RSIIndicator;
import ny2.ats.indicator.indicators.RSIIndicator.RSIPeriod;
import ny2.ats.indicator.indicators.RankCorrelationIndexIndicator;
import ny2.ats.indicator.indicators.RankCorrelationIndexIndicator.RankCIPeriod;
import ny2.ats.indicator.indicators.StochasticsIndicator;
import ny2.ats.indicator.indicators.StochasticsIndicator.StochasticsPeriod;

/**
 * Indicatorの種別を表すenumです。
 */
public enum IndicatorType {

    // 4本値(Open-High-Low-Close)
    OHLC(OHLCIndicator.class, OHLCType.class),

    // 単純移動平均(Moving Average)
    MA(MovingAverageIndicator.class, MAPeriod.class),
    // 指数平滑移動平均
    EMA(ExponentialMovingAverageIndicator.class, MAPeriod.class),

    // RSI (Relative Strength index)
    RSI(RSIIndicator.class, RSIPeriod.class),

    // RCI (Rank Correlation Index)
    RCI(RankCorrelationIndexIndicator.class, RankCIPeriod.class),

    // MACD
    MACD(MACDIndicator.class, MACDPeriod.class),

    // ボリンジャーバンド
    BOLLINGER(BollingerBandIndicator.class, BollingerPeriod.class),
    BOLLINGER_EMA(BollingerBandEMAIndicator.class, BollingerPeriod.class),

    // ストキャスティクス
    STOCHASTICS(StochasticsIndicator.class, StochasticsPeriod.class),

    // 一目均衡表
    ICHIMOKU(IchimokuIndicator.class, IchimokuPeriod.class),

    // 線形回帰分析
    LINEAR_REG(LinearRegressionIndicator.class, LRPeriod.class),

    // 価格レンジ
    PRICE_RANGE(PriceRangeIndicator.class, PriceRangePeriod.class);


    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** Tick OHLC で計算するIndicator */
    public static final Set<IndicatorType> INDICATOR_FOR_TICK = EnumSet.of(IndicatorType.MA, IndicatorType.EMA, IndicatorType.LINEAR_REG);


    /** 対象Indicatorのクラス */
    private Class<? extends Indicator<? extends CalcPeriod>> indicatorClass;

    /** 対象IndicatorのCalcPeriodクラス */
    private Class<? extends CalcPeriod> calcPeriodClass;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    private IndicatorType(Class<? extends Indicator<? extends CalcPeriod>> indicatorClass, Class<? extends CalcPeriod> calcPeriodClass) {
        this.indicatorClass = indicatorClass;
        this.calcPeriodClass = calcPeriodClass;
    }

    /**
     * Stringの配列からSetを作成します。<br>
     * "ALL"が指定された場合はすべての要素を返します
     * @param types
     * @return
     */
    public static Set<IndicatorType> valueOfStringArray(String[] types) {
        Set<IndicatorType> indicatorTypeSet = EnumSet.noneOf(IndicatorType.class);
        for (String str : types) {
            if ("ALL".equals(str)) {
                indicatorTypeSet = EnumSet.allOf(IndicatorType.class);
            } else if (!str.isEmpty()) {
                try {
                    indicatorTypeSet.add(IndicatorType.valueOf(str));
                } catch (Exception e) {
                    // TODO: handle exception
                }

            }
        }
        return indicatorTypeSet;
    }


    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    public Class<? extends Indicator<? extends CalcPeriod>> getIndicatorClass() {
        return indicatorClass;
    }

    public Class<? extends CalcPeriod> getCalcPeriodClass() {
        return calcPeriodClass;
    }

}
