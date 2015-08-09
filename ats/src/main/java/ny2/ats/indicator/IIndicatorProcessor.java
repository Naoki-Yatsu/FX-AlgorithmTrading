package ny2.ats.indicator;

import java.time.LocalDateTime;

import ny2.ats.core.common.Period;
import ny2.ats.core.common.Symbol;
import ny2.ats.indicator.impl.OHLC;

public interface IIndicatorProcessor {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    /**
     * OHLCの更新に伴いIndicatorを更新します。
     * @param ohlc
     */
    public void updateOHLC(OHLC ohlc);

    /**
     * OHLCの更新に伴いIndicatorを更新します。通常こちらを実装します。
     * @param symbol
     * @param period
     * @param dateTime
     */
    public void updateOHLC(Symbol symbol, Period period, LocalDateTime dateTime);

}
