package ny2.ats.indicator.processor;

import java.time.LocalDateTime;
import java.util.List;

import ny2.ats.core.common.Period;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.util.CollectionUtility;
import ny2.ats.indicator.IndicatorProcessor;
import ny2.ats.indicator.impl.IndicatorDataMap;
import ny2.ats.indicator.indicators.MACDIndicator;
import ny2.ats.indicator.indicators.MACDIndicator.MACDPeriod;
import ny2.ats.indicator.indicators.OHLCIndicator;

/**
 * MACDの計算を行うクラスです。
 */
public class MACDProcessor extends IndicatorProcessor<MACDIndicator, MACDPeriod> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public MACDProcessor(IndicatorDataMap<OHLCIndicator> ohlcMap,
                IndicatorDataMap<MACDIndicator> valueMap) {
        super(ohlcMap, valueMap);
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public void updateOHLC(Symbol symbol, Period period, LocalDateTime dateTime) {
        OHLCIndicator ohlcIndicator = ohlcMap.getSymbolMap(symbol).getIndicator(period);
        MACDIndicator macdIndicator = indicatorMap.getSymbolMap(symbol).getIndicator(period);
        List<Double> closeList = ohlcIndicator.getCloseList();
        Double lastClose = CollectionUtility.getLast(closeList);

        // add time / calculate value
        macdIndicator.addTimeData(dateTime);
        for (MACDPeriod macdPeriod : MACDPeriod.values()) {
            double shortEma = calcEMA(macdPeriod.getShortPeriod(), closeList, macdIndicator.getLastShort(macdPeriod), lastClose, symbol);
            double longEma = calcEMA(macdPeriod.getLongPeriod(), closeList, macdIndicator.getLastLong(macdPeriod), lastClose, symbol);
            double macd = calcMACD(shortEma, longEma, symbol);

            // MACD計算後にTriggerを計算する。LongPeriod分を除外する。
            List<Double> macdList = macdIndicator.getMACDList(macdPeriod);
            List<Double> macdSubList = CollectionUtility.lastSubListView(macdList, macdList.size() - macdPeriod.getLongPeriod());
            double triggerEma = 0.0;
            if (macdSubList.contains(Double.NaN)) {
                // NaNが含まれる場合は計算できないので、結果をNaNとする
                triggerEma = Double.NaN;
            } else {
                triggerEma = calcEMA(macdPeriod.getTriggerPriod(), macdSubList, macdIndicator.getLastTrigger(macdPeriod), CollectionUtility.getLast(macdList), symbol);
            }

            macdIndicator.addValueData(macdPeriod, macd, shortEma, longEma, triggerEma);
        }
    }

    /**
     * EMAの値を計算します
     * @param calcPeriodCount
     * @param closeList
     * @param lastEma
     * @param lastClose
     * @param symbol
     * @return
     */
    private double calcEMA(int calcPeriodCount, List<Double> closeList, Double lastEma, Double lastClose, Symbol symbol) {
        // N+1期間のデータを使用して計算する
        if (closeList.size() <= calcPeriodCount) {
            if (closeList.size() < calcPeriodCount) {
                // 期間が足りない場合はNaNを入れる
                return Double.NaN;
            } else {
                // N期間ちょうどであれば単純平均で計算する。
                double firstEma = closeList.stream().mapToDouble(Double::doubleValue).filter(s -> !Double.isNaN(s)).average().getAsDouble();
                return symbol.roundSubPips(firstEma);
            }
        }
        double alpha = 2.0 / (calcPeriodCount + 1);
        // EMA計算
        double ema = lastEma + alpha * (lastClose - lastEma);
        return symbol.roundSubPips(ema);
    }

    /**
     * 長短2つのEMAからMACDを計算します。
     * @param shortEma
     * @param longEma
     * @param symbol
     * @return
     */
    private double calcMACD(double shortEma, double longEma, Symbol symbol) {
        if (Double.isNaN(shortEma) || Double.isNaN(longEma)) {
            return Double.NaN;
        }
        return symbol.roundSubPips(shortEma - longEma);
    }

    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

}
