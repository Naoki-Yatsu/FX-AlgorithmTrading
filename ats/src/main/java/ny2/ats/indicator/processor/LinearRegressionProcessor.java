package ny2.ats.indicator.processor;

import java.time.LocalDateTime;
import java.util.List;

import ny2.ats.core.common.Period;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.util.CollectionUtility;
import ny2.ats.core.util.NumberUtility;
import ny2.ats.indicator.IndicatorProcessor;
import ny2.ats.indicator.impl.IndicatorDataMap;
import ny2.ats.indicator.indicators.LinearRegressionIndicator;
import ny2.ats.indicator.indicators.LinearRegressionIndicator.LRPeriod;
import ny2.ats.indicator.indicators.OHLCIndicator;

/**
 * 線形回帰分析の計算を行うクラスです。
 *    y = a0 + a1 * x
 * として各係数を算出します。
 * xは指定Periodの最小単位を1とします、。（MIN_1であれば1分）
 */
public class LinearRegressionProcessor extends IndicatorProcessor<LinearRegressionIndicator, LRPeriod> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public LinearRegressionProcessor(IndicatorDataMap<OHLCIndicator> ohlcMap, IndicatorDataMap<LinearRegressionIndicator> valueMap) {
        super(ohlcMap, valueMap);
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public void updateOHLC(Symbol symbol, Period period, LocalDateTime dateTime) {
        OHLCIndicator ohlcIndicator = ohlcMap.getSymbolMap(symbol).getIndicator(period);
        LinearRegressionIndicator lrIndicator = indicatorMap.getSymbolMap(symbol).getIndicator(period);
        List<Double> closeList = ohlcIndicator.getCloseList();

        // add time / calculate value
        lrIndicator.addTimeData(dateTime);
        for (LRPeriod lrPeriod : LRPeriod.values()) {
            calcLR(lrPeriod, closeList, symbol, lrIndicator);
        }
    }

    /**
     * 回帰分析の値を計算します
     * @param lrPeriod
     * @param closeList
     * @param symbol
     * @param lrIndicator
     * @param dateTime
     */
    protected void calcLR(LRPeriod lrPeriod, List<Double> closeList, Symbol symbol, LinearRegressionIndicator lrIndicator) {
        // N期間のデータを使用して計算する
        if (closeList.size() < lrPeriod.getPeriodCount()) {
            // 期間が足りない場合はNaNを入れる
            lrIndicator.addValueData(lrPeriod, Double.NaN, Double.NaN);
            return;
        }

        int n = lrPeriod.getPeriodCount();
        List<Double> nList = CollectionUtility.lastSubListView(closeList, n);

        // 計算に使用する値
        // xは0からn-1を使います
        int sigmaX = 0;
        int sigmaXSq = 0;
        double sigmaY = 0.0;
        double sigmaXY = 0.0;

        for (int i = 0; i < n; i++) {
            sigmaX += i;
            sigmaXSq += i * i;
            sigmaY += nList.get(i);
            sigmaXY += i * nList.get(i);
        }

        // a0, a1を計算します。
        // 傾き
        double a1 = (n * sigmaXY - sigmaX * sigmaY) / (n * sigmaXSq - sigmaX * sigmaX);
        // y切片
        double a0 = (sigmaY - a1 * sigmaX) / n;
        // 現在時刻の切片
        double a0Current = a0 + a1 * (n - 1);

        // 丸め
        a1 = NumberUtility.roundFixedPrecision(a1, 3);
        a0Current = symbol.roundSubPips(a0Current);

        // データ追加
        lrIndicator.addValueData(lrPeriod, a1, a0Current);
    }

    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

}
