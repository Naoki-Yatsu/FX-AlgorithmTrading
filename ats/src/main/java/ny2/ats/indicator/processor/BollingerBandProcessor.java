package ny2.ats.indicator.processor;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import ny2.ats.core.common.Period;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.util.CollectionUtility;
import ny2.ats.indicator.IndicatorProcessor;
import ny2.ats.indicator.impl.IndicatorDataMap;
import ny2.ats.indicator.indicators.BollingerBandIndicator;
import ny2.ats.indicator.indicators.BollingerBandIndicator.BollingerPeriod;
import ny2.ats.indicator.indicators.OHLCIndicator;

/**
 * Bollinger Band の計算を行うクラスです。
 */
public class BollingerBandProcessor extends IndicatorProcessor<BollingerBandIndicator, BollingerPeriod> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public BollingerBandProcessor(IndicatorDataMap<OHLCIndicator> ohlcMap, IndicatorDataMap<BollingerBandIndicator> valueMap) {
        super(ohlcMap, valueMap);
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public void updateOHLC(Symbol symbol, Period period, LocalDateTime dateTime) {
        OHLCIndicator ohlcIndicator = ohlcMap.getSymbolMap(symbol).getIndicator(period);
        BollingerBandIndicator indicator = indicatorMap.getSymbolMap(symbol).getIndicator(period);
        List<Double> closeList = ohlcIndicator.getCloseList();

        // add time / calculate value
        indicator.addTimeData(dateTime);
        for (BollingerPeriod calcPeriod : BollingerPeriod.values()) {
            calcBollingerBand(calcPeriod, closeList, symbol, indicator);
        }
    }

    /**
     * MAの値を計算します
     * @param maPeriod
     * @param closeList
     * @param symbol
     * @return
     */
    private void calcBollingerBand(BollingerPeriod calcPeriod, List<Double> closeList, Symbol symbol, BollingerBandIndicator indicator) {
        // N期間のデータを使用して計算する
        if (closeList.size() < calcPeriod.getPeriod()) {
            // 期間が足りない場合はNaNを入れる
            indicator.addValueData(calcPeriod, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
            return;
        }

        List<Double> subList = CollectionUtility.lastSubListView(closeList, calcPeriod.getPeriod());
        DescriptiveStatistics statistics = new DescriptiveStatistics(calcPeriod.getPeriod());
        for (Double d : subList) {
            statistics.addValue(d);
        }
        double sma = symbol.roundSubPips(statistics.getMean());
        double sigma = symbol.roundSubPips(statistics.getStandardDeviation());
        double plus = sma + sigma;
        double minus = sma - sigma;
        // add data
        indicator.addValueData(calcPeriod, sma, sigma, plus, minus);
    }

    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

}
