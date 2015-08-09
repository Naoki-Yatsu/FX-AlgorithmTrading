package ny2.ats.indicator.indicators;

import java.util.ArrayList;

import ny2.ats.core.common.Period;
import ny2.ats.core.common.Symbol;
import ny2.ats.indicator.CalcPeriod;
import ny2.ats.indicator.IndicatorType;
import ny2.ats.indicator.indicators.RankCorrelationIndexIndicator.RankCIPeriod;

/**
 * RCI(Rank Correlation Index) のクラスです<br>
 * 紛らわしいので、フル名称にしています
 */
public class RankCorrelationIndexIndicator extends SimpleIndicator<RankCIPeriod> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public RankCorrelationIndexIndicator(IndicatorType type, Symbol symbol, Period period) {
        super(RankCorrelationIndexIndicator.class, type, symbol, period);
    }

    @Override
    protected void initializeMap() {
        // valueMap初期化
        for (RankCIPeriod rciPeriod : RankCIPeriod.values()) {
            valueMap.put(rciPeriod, new ArrayList<>());
        }
    }

    // //////////////////////////////////////
    // Method (@Override)
    // //////////////////////////////////////

    @Override
    public int getCountCalcPeriod() {
        return RankCIPeriod.values().length;
    }

    @Override
    protected void reduceDatAdditionalItems(int holdDays, int remainFromIndex) {
        // Do nothing
    };

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

    // //////////////////////////////////////
    // Inner Class
    // //////////////////////////////////////

    /**
     * RCIの計算期間です
     * 日足では9, 26, 52を使いますが、時間足なので異なった数字にしています
     */
    public enum RankCIPeriod implements CalcPeriod {
        P12(12),
        P24(24),
        P48(48);
        private int periodCount;
        private RankCIPeriod(int periodCount) {
            this.periodCount = periodCount;
        }
        public int getPeriodCount() {
            return periodCount;
        }
        @Override
        public String getName() {
            return name();
        }
    }
}
