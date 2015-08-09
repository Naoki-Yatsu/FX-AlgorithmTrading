package ny2.ats.indicator.processor;

import java.time.LocalDateTime;
import java.util.List;

import ny2.ats.core.common.Period;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.util.CollectionUtility;
import ny2.ats.indicator.IndicatorProcessor;
import ny2.ats.indicator.impl.IndicatorDataMap;
import ny2.ats.indicator.indicators.ExponentialMovingAverageIndicator;
import ny2.ats.indicator.indicators.MovingAverageIndicator.MAPeriod;
import ny2.ats.indicator.indicators.OHLCIndicator;

/**
 * 指数平滑移動平均の計算を行うクラスです。
 */
public class ExponentialMovingAverageProcessor extends IndicatorProcessor<ExponentialMovingAverageIndicator, MAPeriod> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public ExponentialMovingAverageProcessor(IndicatorDataMap<OHLCIndicator> ohlcMap,
                IndicatorDataMap<ExponentialMovingAverageIndicator> valueMap) {
        super(ohlcMap, valueMap);
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public void updateOHLC(Symbol symbol, Period period, LocalDateTime dateTime) {
        OHLCIndicator ohlcIndicator = ohlcMap.getSymbolMap(symbol).getIndicator(period);
        ExponentialMovingAverageIndicator emaIndicator = indicatorMap.getSymbolMap(symbol).getIndicator(period);
        List<Double> closeList = ohlcIndicator.getCloseList();
        Double lastClose = CollectionUtility.getLast(closeList);

        // add time / calculate value
        emaIndicator.addTimeData(dateTime);
        for (MAPeriod maPeriod : MAPeriod.values()) {
            double ema = calcEMA(maPeriod, closeList, emaIndicator.getLastValue(maPeriod), lastClose, symbol);
            emaIndicator.addValueData(maPeriod, ema);
        }
    }

    /**
     * EMAの値を計算します
     * @param maPeriod
     * @param emaIndicator
     * @param lastClose
     * @return
     */
    private double calcEMA(MAPeriod maPeriod, List<Double> closeList, Double lastEma, Double lastClose, Symbol symbol) {
        // N+1期間のデータを使用して計算する
        if (closeList.size() < maPeriod.getPeriodCount()) {
            // 期間が足りない場合はNaNを入れる
            return Double.NaN;
        } else if (closeList.size() <= maPeriod.getPeriodCount()) {
            // N期間ちょうどであれば単純平均で計算する。
            double firstEma = closeList.stream().mapToDouble(Double::doubleValue).average().getAsDouble();
            return symbol.roundSubPips(firstEma);
        }
        double alpha = 2.0 / (maPeriod.getPeriodCount() + 1);
        // EMA計算
        double ema = lastEma + alpha * (lastClose - lastEma);
        return symbol.roundSubPips(ema);
    }

    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

}
