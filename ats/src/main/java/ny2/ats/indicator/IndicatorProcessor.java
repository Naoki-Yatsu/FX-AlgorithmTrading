package ny2.ats.indicator;

import ny2.ats.indicator.impl.IndicatorDataMap;
import ny2.ats.indicator.impl.IndicatorDataSymbolMap;
import ny2.ats.indicator.impl.OHLC;
import ny2.ats.indicator.indicators.OHLCIndicator;

public abstract class IndicatorProcessor<I extends Indicator<T>, T extends CalcPeriod> implements IIndicatorProcessor {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** OHLCデータ */
    protected final IndicatorDataMap<OHLCIndicator> ohlcMap;

    /** Indicatorデータ */
    protected final IndicatorDataMap<I> indicatorMap;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public IndicatorProcessor(IndicatorDataMap<OHLCIndicator> ohlcMap, IndicatorDataMap<I> indicatorMap) {
        this.ohlcMap = ohlcMap;
        this.indicatorMap = indicatorMap;
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public void updateOHLC(OHLC ohlc) {
        IndicatorDataSymbolMap<OHLCIndicator> symbolMap = ohlcMap.getSymbolMap(ohlc.getSymbol());
        if (symbolMap == null) {
            return;
        }
        updateOHLC(ohlc.getSymbol(), ohlc.getPeriod(), ohlc.getBaseDateTime());
    }

    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

    public IndicatorDataMap<OHLCIndicator> getOhlcMap() {
        return ohlcMap;
    }

    public IndicatorDataMap<I> getIndicatorMap() {
        return indicatorMap;
    }
}
