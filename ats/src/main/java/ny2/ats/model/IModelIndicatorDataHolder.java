package ny2.ats.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ny2.ats.core.common.Period;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.data.IndicatorInformation;
import ny2.ats.core.data.MarketData;
import ny2.ats.database.kdb.KdbByFunctionType;
import ny2.ats.indicator.Indicator;
import ny2.ats.indicator.IndicatorType;
import ny2.ats.indicator.impl.IndicatorDataHolderImpl;
import ny2.ats.indicator.indicators.BollingerBandEMAIndicator;
import ny2.ats.indicator.indicators.BollingerBandIndicator;
import ny2.ats.indicator.indicators.ExponentialMovingAverageIndicator;
import ny2.ats.indicator.indicators.LinearRegressionIndicator;
import ny2.ats.indicator.indicators.MACDIndicator;
import ny2.ats.indicator.indicators.MovingAverageIndicator;
import ny2.ats.indicator.indicators.OHLCIndicator;
import ny2.ats.indicator.indicators.PriceRangeIndicator;
import ny2.ats.indicator.indicators.RSIIndicator;
import ny2.ats.indicator.indicators.RankCorrelationIndexIndicator;
import ny2.ats.indicator.indicators.StochasticsIndicator;

/**
 * モデル用にMarketData, IndicatorDataを保持するクラスです。
 */
public interface IModelIndicatorDataHolder {

    /**
     * MarketDataを更新します。
     * @param marketData
     */
    public void updateMarketData(MarketData marketData);

    /**
     * 該当のMarketDataを返します
     * @param symbol
     * @return
     */
    public MarketData getMarketData(Symbol symbol);

    /**
     * MarketDataMapを返します。
     * @return
     */
    public Map<Symbol, MarketData> getMarketDataMap();

    /**
     * Indicatorを更新します。
     * @param indicatorInformation
     */
    public void updateIndicator(IndicatorInformation indicatorInformation);

    // //////////////////////////////////////
    // Indicator Getter
    // //////////////////////////////////////

    /**
     * OHLC Indicator を返します
     * @param symbol
     * @return
     */
    public OHLCIndicator getIndicatorOHLC(Symbol symbol, Period period);

    /**
     * 対象のIndicatorを返します。
     * @param indicatorType
     * @param symbol
     * @param period
     * @return
     * @see IndicatorDataHolderImpl
     */
    public Indicator<?> getIndicator(IndicatorType indicatorType, Symbol symbol, Period period);

    //
    // 各IndicatorのGetter
    //

    public MovingAverageIndicator getIndicatorMA(Symbol symbol, Period period);

    public ExponentialMovingAverageIndicator getIndicatorEMA(Symbol symbol, Period period);

    public RSIIndicator getIndicatorRSI(Symbol symbol, Period period);

    public RankCorrelationIndexIndicator getIndicatorRCI(Symbol symbol, Period period);

    public MACDIndicator getIndicatorMACD(Symbol symbol, Period period);

    public BollingerBandIndicator getIndicatorBollinger(Symbol symbol, Period period);

    public BollingerBandEMAIndicator getIndicatorBollingerEMA(Symbol symbol, Period period);

    public StochasticsIndicator getIndicatorStochastics(Symbol symbol, Period period);

    public LinearRegressionIndicator getIndicatorLinearRegression(Symbol symbol, Period period);

    public PriceRangeIndicator getIndicatorPriceRange(Symbol symbol, Period period);


    // //////////////////////////////////////
    // For Historical Data
    // //////////////////////////////////////

    /**
     * 過去データを読み込みます（ティック）
     *
     * @param date1
     * @param date2
     * @param symbolList
     * @return
     */
    public List<MarketData> loadHistoricalMarketData(LocalDate date1, LocalDate date2, Set<Symbol> symbolSet);


    /**
     * 過去データを読み込みます（指定単位）
     *
     * @param date1
     * @param date2
     * @param symbolList
     * @param byCount
     * @param byFunctionType
     * @return
     */
    public List<MarketData> loadHistoricalMarketData(LocalDate date1, LocalDate date2, Set<Symbol> symbolSet, int byCount, KdbByFunctionType byFunctionType);

}
