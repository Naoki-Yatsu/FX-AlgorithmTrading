package ny2.ats.indicator.processor;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import ny2.ats.core.common.Period;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.util.CollectionUtility;
import ny2.ats.indicator.IndicatorProcessor;
import ny2.ats.indicator.impl.IndicatorDataMap;
import ny2.ats.indicator.indicators.BollingerBandEMAIndicator;
import ny2.ats.indicator.indicators.BollingerBandIndicator.BollingerPeriod;
import ny2.ats.indicator.indicators.OHLCIndicator;

/**
 * Bollinger Band EMA の計算を行うクラスです。
 */
public class BollingerBandEMAProcessor extends IndicatorProcessor<BollingerBandEMAIndicator, BollingerPeriod> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public BollingerBandEMAProcessor(IndicatorDataMap<OHLCIndicator> ohlcMap, IndicatorDataMap<BollingerBandEMAIndicator> valueMap) {
        super(ohlcMap, valueMap);
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public void updateOHLC(Symbol symbol, Period period, LocalDateTime dateTime) {
        OHLCIndicator ohlcIndicator = ohlcMap.getSymbolMap(symbol).getIndicator(period);
        BollingerBandEMAIndicator indicator = indicatorMap.getSymbolMap(symbol).getIndicator(period);
        Double lastClose = ohlcIndicator.getLastClose();

        // add time / calculate value
        indicator.addTimeData(dateTime);
        for (BollingerPeriod calcPeriod : BollingerPeriod.values()) {
            List<Double> emaList = indicator.getMAList(calcPeriod);
            calcBollingerBandEMA(calcPeriod, emaList, lastClose, symbol, indicator);
        }
    }

    /**
     * BollingerBandEMA の値を計算します
     * @param calcPeriod
     * @param emaList
     * @param lastClose
     * @param symbol
     * @param indicator
     * @param dateTime
     */
    private void calcBollingerBandEMA(BollingerPeriod calcPeriod, List<Double> emaList, Double lastClose, Symbol symbol, BollingerBandEMAIndicator indicator) {
        // EMA計算を単純化する
        if (emaList.isEmpty() || CollectionUtility.getLast(emaList).isNaN()) {
            // 最初は終値で単純化
            indicator.addValueData(calcPeriod, lastClose, Double.NaN, Double.NaN, Double.NaN);
            return;
        }
        Double lastEma = CollectionUtility.getLast(emaList);
        // EMA計算
        double alpha = 2.0 / (calcPeriod.getPeriod() + 1);
        double ema = lastEma + alpha * (lastClose - lastEma);

        // 計算期間が足りない場合はNaNを入れる
        if (emaList.size() < calcPeriod.getPeriod() - 1) {
            indicator.addValueData(calcPeriod, ema, Double.NaN, Double.NaN, Double.NaN);
            return;
        }

        List<Double> subList = CollectionUtility.lastSubListView(emaList, calcPeriod.getPeriod() - 1);
        DescriptiveStatistics statistics = new DescriptiveStatistics(calcPeriod.getPeriod());
        for (Double d : subList) {
            statistics.addValue(d);
        }
        statistics.addValue(ema);
        ema = symbol.roundSubPips(ema);
        double sigma = symbol.roundSubPips(statistics.getStandardDeviation());
        double plus = ema + sigma;
        double minus = ema - sigma;
        // add data
        indicator.addValueData(calcPeriod, ema, sigma, plus, minus);
    }

    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

}
