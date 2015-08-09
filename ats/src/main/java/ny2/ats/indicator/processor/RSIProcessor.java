package ny2.ats.indicator.processor;

import java.time.LocalDateTime;
import java.util.List;

import ny2.ats.core.common.Period;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.util.CollectionUtility;
import ny2.ats.core.util.NumberUtility;
import ny2.ats.indicator.IndicatorProcessor;
import ny2.ats.indicator.impl.IndicatorDataMap;
import ny2.ats.indicator.indicators.OHLCIndicator;
import ny2.ats.indicator.indicators.RSIIndicator;
import ny2.ats.indicator.indicators.RSIIndicator.RSIPeriod;

/**
 * RSIの計算を行うクラスです。
 */
public class RSIProcessor extends IndicatorProcessor<RSIIndicator, RSIPeriod> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public RSIProcessor(IndicatorDataMap<OHLCIndicator> ohlcMap, IndicatorDataMap<RSIIndicator> valueMap) {
        super(ohlcMap, valueMap);
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public void updateOHLC(Symbol symbol, Period period, LocalDateTime dateTime) {
        OHLCIndicator ohlcIndicator = ohlcMap.getSymbolMap(symbol).getIndicator(period);
        RSIIndicator rsiIndicator = indicatorMap.getSymbolMap(symbol).getIndicator(period);
        List<Double> closeList = ohlcIndicator.getCloseList();

        // add time / calculate value
        rsiIndicator.addTimeData(dateTime);
        for (RSIPeriod rsiPeriod : RSIPeriod.values()) {
            double rsi = calcRSI(rsiPeriod, closeList);
            rsiIndicator.addValueData(rsiPeriod, rsi);
        }
    }

    /**
     * RSIの値を計算します
     * @param rsiPeriod
     * @param closeList
     * @return
     */
    private double calcRSI(RSIPeriod rsiPeriod, List<Double> closeList) {
        // N+1期間のデータを使用して計算する
        if (closeList.size() < rsiPeriod.getPeriodCount() + 1) {
            // 期間が足りない場合はNaNを入れる
            return Double.NaN;
        }
        List<Double> subList = CollectionUtility.lastSubListView(closeList, rsiPeriod.getPeriodCount() + 1);
        double totalGain = 0;
        double totalLoss = 0;

        for (int i = 0; i < subList.size() - 1; i++) {
            Double close1 = subList.get(i).doubleValue();
            Double close2 = subList.get(i + 1).doubleValue();
            totalGain += Math.max(close2 - close1, 0);
            totalLoss += Math.max(close1 - close2, 0);
        }
        // ゼロ割防止
        if (totalGain + totalLoss < 0.0001) {
            return Double.NaN;
        }
        double rsi = (totalGain / (totalGain + totalLoss)) * 100;
        return NumberUtility.roundFixedFraction(rsi, 1);
    }

    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

}
