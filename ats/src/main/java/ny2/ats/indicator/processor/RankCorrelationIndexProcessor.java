package ny2.ats.indicator.processor;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.apache.commons.math3.stat.ranking.NaNStrategy;
import org.apache.commons.math3.stat.ranking.NaturalRanking;
import org.apache.commons.math3.stat.ranking.TiesStrategy;

import ny2.ats.core.common.Period;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.util.CollectionUtility;
import ny2.ats.indicator.IndicatorProcessor;
import ny2.ats.indicator.impl.IndicatorDataMap;
import ny2.ats.indicator.indicators.OHLCIndicator;
import ny2.ats.indicator.indicators.RankCorrelationIndexIndicator;
import ny2.ats.indicator.indicators.RankCorrelationIndexIndicator.RankCIPeriod;

/**
 * RSIの計算を行うクラスです。
 */
public class RankCorrelationIndexProcessor extends IndicatorProcessor<RankCorrelationIndexIndicator, RankCIPeriod> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** Ranking: Exception in NaN, Averaging for ties */
    private NaturalRanking ranking = new NaturalRanking(NaNStrategy.FAILED, TiesStrategy.AVERAGE);

    /** SpearmansCorrelation */
    private SpearmansCorrelation correlation = new SpearmansCorrelation();

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public RankCorrelationIndexProcessor(IndicatorDataMap<OHLCIndicator> ohlcMap, IndicatorDataMap<RankCorrelationIndexIndicator> valueMap) {
        super(ohlcMap, valueMap);
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public void updateOHLC(Symbol symbol, Period period, LocalDateTime dateTime) {
        OHLCIndicator ohlcIndicator = ohlcMap.getSymbolMap(symbol).getIndicator(period);
        RankCorrelationIndexIndicator rciIndicator = indicatorMap.getSymbolMap(symbol).getIndicator(period);
        List<Double> closeList = ohlcIndicator.getCloseList();

        // add time / calculate value
        rciIndicator.addTimeData(dateTime);
        for (RankCIPeriod rciPeriod : RankCIPeriod.values()) {
            double rci = calcRCI(rciPeriod, closeList);
            rciIndicator.addValueData(rciPeriod, rci);
        }
    }

    /**
     * RCIの値を計算します
     * @param rciPeriod
     * @param closeList
     * @return
     */
    private double calcRCI(RankCIPeriod rciPeriod, List<Double> closeList) {
        // N期間のデータを使用して計算する
        if (closeList.size() < rciPeriod.getPeriodCount()) {
            // 期間が足りない場合はNaNを入れる
            return Double.NaN;
        }

        // Calculate price rank
        List<Double> subList = CollectionUtility.lastSubListView(closeList, rciPeriod.getPeriodCount());
        double[] ranks = ranking.rank(CollectionUtility.toPrimitiveDouble(subList));

        double[] dateRanks = new double[rciPeriod.getPeriodCount()];
        for (int i = 0; i < dateRanks.length; i++) {
            dateRanks[i] = i + 1;
        }

        double coefficient = correlation.correlation(dateRanks, ranks);
        double rci = 0.5 * (coefficient * 100 + 100);

        return Math.round(rci);
    }


    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

}
