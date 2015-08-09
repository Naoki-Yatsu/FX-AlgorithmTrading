package ny2.ats.indicator.processor;

import java.time.LocalDateTime;
import java.util.List;

import ny2.ats.core.common.Period;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.util.CollectionUtility;
import ny2.ats.indicator.IndicatorProcessor;
import ny2.ats.indicator.impl.IndicatorDataMap;
import ny2.ats.indicator.indicators.MovingAverageIndicator;
import ny2.ats.indicator.indicators.MovingAverageIndicator.MAPeriod;
import ny2.ats.indicator.indicators.OHLCIndicator;

/**
 * 移動平均の計算を行うクラスです。
 */
public class MovingAverageProcessor extends IndicatorProcessor<MovingAverageIndicator, MAPeriod> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public MovingAverageProcessor(IndicatorDataMap<OHLCIndicator> ohlcMap, IndicatorDataMap<MovingAverageIndicator> valueMap) {
        super(ohlcMap, valueMap);
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public void updateOHLC(Symbol symbol, Period period, LocalDateTime dateTime) {
        OHLCIndicator ohlcIndicator = ohlcMap.getSymbolMap(symbol).getIndicator(period);
        MovingAverageIndicator maIndicator = indicatorMap.getSymbolMap(symbol).getIndicator(period);
        List<Double> closeList = ohlcIndicator.getCloseList();

        // add time / calculate value
        maIndicator.addTimeData(dateTime);
        for (MovingAverageIndicator.MAPeriod maPeriod : MovingAverageIndicator.MAPeriod.values()) {
            Double ma = calcMA(maPeriod, closeList, symbol);
            maIndicator.addValueData(maPeriod, ma);
        }
    }

    /**
     * MAの値を計算します
     * @param maPeriod
     * @param closeList
     * @param symbol
     * @return
     */
    private double calcMA(MAPeriod maPeriod, List<Double> closeList, Symbol symbol) {
        // N期間のデータを使用して計算する
        if (closeList.size() < maPeriod.getPeriodCount()) {
            // 期間が足りない場合はNaNを入れる
            return Double.NaN;
        }
        List<Double> subList = CollectionUtility.lastSubListView(closeList, maPeriod.getPeriodCount());
        double ma = subList.stream().mapToDouble(Double::doubleValue).average().getAsDouble();
        return symbol.roundSubPips(ma);
    }

    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

}
