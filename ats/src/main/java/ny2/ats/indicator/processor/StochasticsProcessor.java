package ny2.ats.indicator.processor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import ny2.ats.core.common.Period;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.util.CollectionUtility;
import ny2.ats.core.util.NumberUtility;
import ny2.ats.indicator.IndicatorProcessor;
import ny2.ats.indicator.impl.IndicatorDataMap;
import ny2.ats.indicator.indicators.OHLCIndicator;
import ny2.ats.indicator.indicators.StochasticsIndicator;
import ny2.ats.indicator.indicators.StochasticsIndicator.StochasticsPeriod;

/**
 * Stochasticsの計算を行うクラスです。
 */
public class StochasticsProcessor extends IndicatorProcessor<StochasticsIndicator, StochasticsPeriod> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public StochasticsProcessor(IndicatorDataMap<OHLCIndicator> ohlcMap,
                IndicatorDataMap<StochasticsIndicator> valueMap) {
        super(ohlcMap, valueMap);
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public void updateOHLC(Symbol symbol, Period period, LocalDateTime dateTime) {
        OHLCIndicator ohlcIndicator = ohlcMap.getSymbolMap(symbol).getIndicator(period);
        StochasticsIndicator stochasticsIndicator = indicatorMap.getSymbolMap(symbol).getIndicator(period);
        List<Double> closeList = ohlcIndicator.getCloseList();

        // add time / calculate value
        stochasticsIndicator.addTimeData(dateTime);
        for (StochasticsPeriod stochasticsPeriod : StochasticsPeriod.values()) {
            calcStochastics(stochasticsPeriod, closeList, symbol, stochasticsIndicator);
        }
    }

    /**
     * Stochasticsを計算します。
     * @param stochasticsPeriod
     * @param closeList
     * @param symbol
     * @param stochasticsIndicator
     * @param dateTime
     */
    protected void calcStochastics(StochasticsPeriod stochasticsPeriod, List<Double> closeList, Symbol symbol, StochasticsIndicator stochasticsIndicator) {
        int kPeriod = stochasticsPeriod.getKPeriod();
        int dPeriod = stochasticsPeriod.getDPeriod();
        int slowDPeriod = stochasticsPeriod.getSlowDPeriod();

        // すべてのデータが揃うには、 kPeriod + dPeriod + slowDPeriod 期間が必要
        // まず %K には、 kPeriod期間が必要
        if (closeList.size() < kPeriod) {
            // 期間が足りない場合はNaNを入れる
            stochasticsIndicator.addValueData(stochasticsPeriod, Double.NaN, Double.NaN, Double.NaN,Double.NaN,Double.NaN);
            return;
        }
        List<Double> closeKSubList = CollectionUtility.lastSubListView(closeList, kPeriod);
        double max = Collections.max(closeKSubList);
        double min = Collections.min(closeKSubList);
        double closeMin = CollectionUtility.getLast(closeList).doubleValue() - min;
        double maxMin = max - min;
        // ゼロ割り対策
        double k = Math.min(100, 100 * (closeMin / maxMin));
        k = NumberUtility.roundFixedFraction(k, 1);

        // 次に %D には closeMinList が dPeriod-1 期間必要
        List<Double> closeMinList = stochasticsIndicator.getCloseMinList(stochasticsPeriod);
        List<Double> maxMinList = stochasticsIndicator.getMaxMinList(stochasticsPeriod);
        if (closeMinList.size() < dPeriod - 1) {
            // 期間が足りない場合はNaNを入れる
            stochasticsIndicator.addValueData(stochasticsPeriod, k, Double.NaN, Double.NaN, closeMin, maxMin);
            return;
        }
        double d = Math.min(100, 100 * (
                (closeMin + getLastTotal(dPeriod - 1, closeMinList))
                / (maxMin + getLastTotal(dPeriod - 1, maxMinList)) ));
        d = NumberUtility.roundFixedFraction(d, 1);

        // 最後に Slow %D には dList が slowDPeriod-1 必要
        List<Double> dList = stochasticsIndicator.getDList(stochasticsPeriod);
        if (dList.size() < slowDPeriod - 1) {
            // 期間が足りない場合はNaNを入れる
            stochasticsIndicator.addValueData(stochasticsPeriod, k, d, Double.NaN, closeMin, maxMin);
            return;
        }
        double slowD = (d + getLastTotal(slowDPeriod - 1, dList)) / slowDPeriod;
        slowD = NumberUtility.roundFixedFraction(slowD, 1);

        // データ更新
        stochasticsIndicator.addValueData(stochasticsPeriod, k, d, slowD, closeMin, maxMin);
    }

    /**
     * 最後のN期間の合計を返します。
     * @param count
     * @param list
     * @return
     */
    private double getLastTotal(int count, List<Double> list) {
        return CollectionUtility.lastSubListView(list, count).stream().mapToDouble(d -> d.doubleValue()).sum();
    }

    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

}
