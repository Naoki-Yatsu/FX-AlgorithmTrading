package ny2.ats.indicator.indicators;

import java.util.ArrayList;

import ny2.ats.core.common.Period;
import ny2.ats.core.common.Symbol;
import ny2.ats.indicator.CalcPeriod;
import ny2.ats.indicator.IndicatorType;
import ny2.ats.indicator.indicators.RSIIndicator.RSIPeriod;

/**
 * RSIのクラスです
 */
public class RSIIndicator extends SimpleIndicator<RSIPeriod> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public RSIIndicator(IndicatorType type, Symbol symbol, Period period) {
        super(RSIIndicator.class, type, symbol, period);
    }

    @Override
    protected void initializeMap() {
        // valueMap初期化
        for (RSIPeriod rsiPeriod : RSIPeriod.values()) {
            valueMap.put(rsiPeriod, new ArrayList<>());
        }
    }

    // //////////////////////////////////////
    // Method (@Override)
    // //////////////////////////////////////

    @Override
    public int getCountCalcPeriod() {
        return RSIPeriod.values().length;
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

    public enum RSIPeriod implements CalcPeriod {
        P09(9),
        P11(11),
        P14(14),
        P20(20),
        P30(30);
        private int periodCount;
        private RSIPeriod(int periodCount) {
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
