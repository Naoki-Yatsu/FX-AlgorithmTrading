package ny2.ats.indicator.processor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import ny2.ats.core.common.Period;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.util.CollectionUtility;
import ny2.ats.indicator.IndicatorProcessor;
import ny2.ats.indicator.impl.IndicatorDataMap;
import ny2.ats.indicator.indicators.OHLCIndicator;
import ny2.ats.indicator.indicators.PriceRangeIndicator;
import ny2.ats.indicator.indicators.PriceRangeIndicator.PriceRangePeriod;

/**
 * 価格レンジ(サポートライン)の 計算を行うクラスです。
 */
public class PriceRangeProcessor extends IndicatorProcessor<PriceRangeIndicator, PriceRangePeriod> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    private static final double THRESHOLD = 0;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public PriceRangeProcessor(IndicatorDataMap<OHLCIndicator> ohlcMap, IndicatorDataMap<PriceRangeIndicator> valueMap) {
        super(ohlcMap, valueMap);
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public void updateOHLC(Symbol symbol, Period period, LocalDateTime dateTime) {
        OHLCIndicator ohlc = ohlcMap.getSymbolMap(symbol).getIndicator(period);
        List<Double> highList = ohlc.getHighList();
        List<Double> lowList = ohlc.getLowList();
        PriceRangeIndicator indicator = indicatorMap.getSymbolMap(symbol).getIndicator(period);

        // add time / calculate value
        indicator.addTimeData(dateTime);
        for (PriceRangePeriod calcPeriod : PriceRangePeriod.values()) {
            calculatePriceRange(calcPeriod, highList, lowList, symbol, indicator);
        }
    }

    /**
     * Price Range の値を計算します
     * @param calcPeriod
     * @param highList
     * @param lowList
     * @param symbol
     * @param indicator
     */
    private void calculatePriceRange(PriceRangePeriod calcPeriod, List<Double> highList, List<Double> lowList, Symbol symbol, PriceRangeIndicator indicator) {
        // N期間のデータを使用して計算する
        if (highList.size() < calcPeriod.getPeriodCount()) {
            // 期間が足りない場合はNaNを入れる
            indicator.addValueData(calcPeriod, Double.NaN, Double.NaN, Boolean.FALSE, Boolean.FALSE, 0, 0);
            return;
        }

        // 期間リスト
        List<Double> highSubList = CollectionUtility.lastSubListView(highList, calcPeriod.getPeriodCount());
        List<Double> lowSubList = CollectionUtility.lastSubListView(lowList, calcPeriod.getPeriodCount());

        // 期間内の価格レンジを取得
//        double max = Collections.max(highSubList);
//        double min = Collections.min(lowSubList);
//        double length = max - min;

        // 最小値を探して、その前後をチェックしてdip/rallyになっているか確かめる
        double max = Double.MIN_VALUE;
        int maxIndex = -1;
        double min = Double.MAX_VALUE;
        int minIndex = -1;

        for (int i = 0; i < calcPeriod.getPeriodCount(); i++) {
            double high = highSubList.get(i);
            if (high > max) {
                max = high;
                maxIndex = i;
            }
            double low = lowSubList.get(i);
            if (low < min) {
                min = low;
                minIndex = i;
            }
        }

        // dip/rally が無ければ前回の値を使う
        double upperSupport = indicator.getLastUpperSupport(calcPeriod);
        double lowerSupport = indicator.getLastLowerSupport(calcPeriod);
        Boolean upperValid = indicator.getLastUpperSupportValid(calcPeriod);
        Boolean lowerValid = indicator.getLastLowerSupportValid(calcPeriod);

        // 前後をチェックしてdip/rallyになっているか確かめる
        // rallyの確認 - maxの前後に閾値以下の値があること
        if (maxIndex > 0 && maxIndex < calcPeriod.getPeriodCount() - 1
                && Collections.min(lowSubList.subList(0, maxIndex)) < max * (1 - calcPeriod.getExtremumRatio())
                && Collections.min(lowSubList.subList(maxIndex, lowSubList.size())) < max * (1 - calcPeriod.getExtremumRatio())) {
            upperSupport = max;
            upperValid = Boolean.TRUE;
        } else {
            // 有効性確認
            if (max > upperSupport) {
                upperValid = Boolean.FALSE;
            }
        }
        // dipの確認 - minの前後に閾値以上の値があること
        if (minIndex > 0 && minIndex < calcPeriod.getPeriodCount() - 1
                && Collections.max(highSubList.subList(0, minIndex)) > min * (1 + calcPeriod.getExtremumRatio())
                && Collections.max(highSubList.subList(minIndex, highSubList.size())) > min * (1 + calcPeriod.getExtremumRatio())) {
            lowerSupport = min;
            lowerValid = Boolean.TRUE;
        } else {
            // 有効性確認
            if (min < lowerSupport) {
                lowerValid = Boolean.FALSE;
            }
        }

        // 基準点が決まったら近づいた回数をカウントする




        // Indicatorに値を追加
        indicator.addValueData(calcPeriod, upperSupport, lowerSupport, upperValid, lowerValid, 1, 1);
    }



    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

}
