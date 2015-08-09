package ny2.ats.indicator.indicators;

import ny2.ats.core.common.Period;
import ny2.ats.core.common.Symbol;
import ny2.ats.indicator.IndicatorType;

/**
 * 指数平滑移動平均のクラスです。
 * SimpleIndicatoのvalueとして、EMAの値を持ちます。
 */
public class ExponentialMovingAverageIndicator extends MovingAverageIndicator {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public ExponentialMovingAverageIndicator(IndicatorType type, Symbol symbol, Period period) {
        super(ExponentialMovingAverageIndicator.class, type, symbol, period);
    }

    // //////////////////////////////////////
    // Method (@Override)
    // //////////////////////////////////////

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

}
