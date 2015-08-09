package ny2.ats.indicator.indicators;

import ny2.ats.core.common.Period;
import ny2.ats.core.common.Symbol;
import ny2.ats.indicator.IndicatorType;

/**
 * Bollinger Band のクラスです
 * 計算にはEMAを使用します
 */
public class BollingerBandEMAIndicator extends BollingerBandIndicator {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public BollingerBandEMAIndicator(IndicatorType type, Symbol symbol, Period period) {
        super(BollingerBandEMAIndicator.class, type, symbol, period);
    }

    // //////////////////////////////////////
    // Method (@Override)
    // //////////////////////////////////////

}
