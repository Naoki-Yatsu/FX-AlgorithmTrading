package ny2.ats.indicator.processor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import ny2.ats.core.common.Period;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.util.CollectionUtility;
import ny2.ats.indicator.IndicatorProcessor;
import ny2.ats.indicator.impl.IndicatorDataMap;
import ny2.ats.indicator.indicators.IchimokuIndicator;
import ny2.ats.indicator.indicators.IchimokuIndicator.IchimokuPeriod;
import ny2.ats.indicator.indicators.OHLCIndicator;

/**
 * MACDの計算を行うクラスです。
 */
public class IchimokuProcessor extends IndicatorProcessor<IchimokuIndicator, IchimokuPeriod> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public IchimokuProcessor(IndicatorDataMap<OHLCIndicator> ohlcMap,
                IndicatorDataMap<IchimokuIndicator> valueMap) {
        super(ohlcMap, valueMap);
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public void updateOHLC(Symbol symbol, Period period, LocalDateTime dateTime) {
        OHLCIndicator ohlcIndicator = ohlcMap.getSymbolMap(symbol).getIndicator(period);
        IchimokuIndicator ichimokuIndicator = indicatorMap.getSymbolMap(symbol).getIndicator(period);
        List<Double> closeList = ohlcIndicator.getCloseList();

        // add time / calculate value
        ichimokuIndicator.addTimeData(dateTime);
        for (IchimokuPeriod calcPeriod : IchimokuPeriod.values()) {
            calcIchimoku(ichimokuIndicator, calcPeriod, closeList);
        }
    }

    /**
     * Ichimokuの値を計算します
     * @param ichimokuIndicator
     * @param calcPeriod
     * @param closeList
     */
    private void calcIchimoku(IchimokuIndicator ichimokuIndicator, IchimokuPeriod calcPeriod, List<Double> closeList) {
        // NaNで初期化する
        double tenkan = Double.NaN;
        double kijun = Double.NaN;
        double senkou1 = Double.NaN;
        double senkou2 = Double.NaN;
        double chikou = CollectionUtility.getLast(closeList);
        // データが期間分足りていれば設定
        if (closeList.size() >= calcPeriod.getTenkanPeriod()) {
            tenkan = calcHighLowHalf(closeList, calcPeriod.getTenkanPeriod());
        }
        if (closeList.size() >= calcPeriod.getKijunPeriod()) {
            kijun = calcHighLowHalf(closeList, calcPeriod.getKijunPeriod());
            senkou1 = (tenkan + kijun) / 2;
        }
        if (closeList.size() >= calcPeriod.getSenkouPriod()) {
            senkou2 = calcHighLowHalf(closeList, calcPeriod.getSenkouPriod());
        }

        // double tenkan = calcHighLowHalf(closeList, calcPeriod.getTenkanPeriod());
        // double kijun = calcHighLowHalf(closeList, calcPeriod.getKijunPeriod());
        // double senkou1 = (tenkan + kijun) / 2;
        // double senkou2 = calcHighLowHalf(closeList, calcPeriod.getSenkouPriod());
        // double chikou = CollectionUtility.getLast(closeList);
        ichimokuIndicator.addValueData(calcPeriod, tenkan, kijun, senkou1, senkou2, chikou);
    }

    /**
     * 過去指定期間の (高値+安値)/2 を計算します
     * @param closeList
     * @param period
     * @return
     */
    private double calcHighLowHalf(List<Double> closeList, int period) {
        List<Double> subList = CollectionUtility.lastSubListView(closeList, period);
        double high = Collections.max(subList);
        double low = Collections.min(subList);
        return (high + low) / 2;
    }

    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

}
