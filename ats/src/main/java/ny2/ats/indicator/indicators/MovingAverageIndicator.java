package ny2.ats.indicator.indicators;

import java.util.ArrayList;

import ny2.ats.core.common.Period;
import ny2.ats.core.common.Symbol;
import ny2.ats.indicator.CalcPeriod;
import ny2.ats.indicator.IndicatorType;
import ny2.ats.indicator.indicators.MovingAverageIndicator.MAPeriod;

/**
 * 移動平均のクラスです。
 * SimpleIndicatoのvalueとして、移動平均の値を持ちます。
 */
public class MovingAverageIndicator extends SimpleIndicator<MAPeriod> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public MovingAverageIndicator(IndicatorType type, Symbol symbol, Period period) {
        this(MovingAverageIndicator.class, type, symbol, period);
    }

    // For sub-class
    public MovingAverageIndicator(Class<?> indicatorClass, IndicatorType type, Symbol symbol, Period period) {
        super(indicatorClass, type, symbol, period);
    }

    @Override
    protected void initializeMap() {
        // valueMap初期化
        for (MAPeriod maPeriod : MAPeriod.values()) {
            valueMap.put(maPeriod, new ArrayList<>());
        }
    }

    // //////////////////////////////////////
    // Method (@Override)
    // //////////////////////////////////////

    @Override
    public int getCountCalcPeriod() {
        return MAPeriod.values().length;
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

    public enum MAPeriod implements CalcPeriod {
        P003(3),
        P005(5),
        P010(10),
        P020(20),
        P050(50),
        P100(100),
        P200(200);

        private int periodCount;

        private MAPeriod(int periodCount) {
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
