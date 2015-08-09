package ny2.ats.model.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ny2.ats.core.common.Period;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.data.IndicatorInformation;
import ny2.ats.core.data.MarketData;
import ny2.ats.database.HistoricalDao;
import ny2.ats.database.kdb.KdbByFunctionType;
import ny2.ats.indicator.IIndicatorDataHolder;
import ny2.ats.indicator.Indicator;
import ny2.ats.indicator.IndicatorType;
import ny2.ats.indicator.impl.IndicatorDataMap;
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
import ny2.ats.model.IModelIndicatorDataHolder;

/**
 * モデル用にMarketData, IndicatorDataを保持するクラスです。
 */
@Service
public class ModelIndicatorDataHolderImpl implements IModelIndicatorDataHolder {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // Logger
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** Historicalデータ参照DAO */
    @Autowired
    private HistoricalDao historicalDao;

    /** Indicatorデータ */
    // TODO サーバー分割時は、独自に収集すること
    @Autowired
    private IIndicatorDataHolder indicatorDataHolder;

    /** Market Data */
    private Map<Symbol, MarketData> marketDataMap = new ConcurrentHashMap<>();

    /** OHLCデータ */
    private IndicatorDataMap<OHLCIndicator> ohlcMap;

    /** Indicatorデータ(OHLCを含まない) */
    private Map<IndicatorType, IndicatorDataMap<?>> allIndicatorDataMap;



    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    @PostConstruct
    private void init() {
        logger.info("PostConstruct instance.");

        // TODO サーバー分割時は、独自に収集すること
        this.allIndicatorDataMap = indicatorDataHolder.getAllIndicatorDataMap();
        this.ohlcMap = indicatorDataHolder.getOHLCMap();
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public void updateMarketData(MarketData marketData) {
        marketDataMap.put(marketData.getSymbol(), marketData);
    }

    @Override
    public MarketData getMarketData(Symbol symbol) {
        return marketDataMap.get(symbol);
    }

    @Override
    public Map<Symbol, MarketData> getMarketDataMap() {
        return marketDataMap;
    }

    @Override
    public void updateIndicator(IndicatorInformation indicatorInformation) {
        // 現在は直接IndicatorDataHolderを参照しているため更新不要
    }


    // //////////////////////////////////////
    // Method - Indicator
    // //////////////////////////////////////

    @Override
    public OHLCIndicator getIndicatorOHLC(Symbol symbol, Period period) {
        return ohlcMap.getIndicator(symbol, period);
    }

    @Override
    public Indicator<?> getIndicator(IndicatorType indicatorType, Symbol symbol, Period period) {
        if (indicatorType == IndicatorType.OHLC) {
            return getIndicatorOHLC(symbol, period);
        } else {
            return allIndicatorDataMap.get(indicatorType).getIndicator(symbol, period);
        }
    }

    //
    // 各IndicatorのGetter
    //

    @Override
    public MovingAverageIndicator getIndicatorMA(Symbol symbol, Period period) {
        return (MovingAverageIndicator) getIndicator(IndicatorType.MA, symbol, period);
    }

    @Override
    public ExponentialMovingAverageIndicator getIndicatorEMA(Symbol symbol, Period period) {
        return (ExponentialMovingAverageIndicator) getIndicator(IndicatorType.EMA, symbol, period);
    }

    @Override
    public RSIIndicator getIndicatorRSI(Symbol symbol, Period period) {
        return (RSIIndicator) getIndicator(IndicatorType.RSI, symbol, period);
    }

    @Override
    public RankCorrelationIndexIndicator getIndicatorRCI(Symbol symbol, Period period) {
        return (RankCorrelationIndexIndicator) getIndicator(IndicatorType.RCI, symbol, period);
    }

    @Override
    public MACDIndicator getIndicatorMACD(Symbol symbol, Period period) {
        return (MACDIndicator) getIndicator(IndicatorType.MACD, symbol, period);
    }

    @Override
    public BollingerBandIndicator getIndicatorBollinger(Symbol symbol, Period period) {
        return (BollingerBandIndicator) getIndicator(IndicatorType.BOLLINGER, symbol, period);
    }

    @Override
    public BollingerBandEMAIndicator getIndicatorBollingerEMA(Symbol symbol, Period period) {
        return (BollingerBandEMAIndicator) getIndicator(IndicatorType.BOLLINGER_EMA, symbol, period);
    }

    @Override
    public StochasticsIndicator getIndicatorStochastics(Symbol symbol, Period period) {
        return (StochasticsIndicator) getIndicator(IndicatorType.STOCHASTICS, symbol, period);
    }

    @Override
    public LinearRegressionIndicator getIndicatorLinearRegression(Symbol symbol, Period period) {
        return (LinearRegressionIndicator) getIndicator(IndicatorType.LINEAR_REG, symbol, period);
    }

    @Override
    public PriceRangeIndicator getIndicatorPriceRange(Symbol symbol, Period period) {
        return (PriceRangeIndicator) getIndicator(IndicatorType.PRICE_RANGE, symbol, period);
    }

    // //////////////////////////////////////
    // Method - Historical Data
    // //////////////////////////////////////

    @Override
    public List<MarketData> loadHistoricalMarketData(LocalDate date1, LocalDate date2, Set<Symbol> symbolSet) {
        return historicalDao.findMarketData(date1, date2, symbolSet);
    }

    @Override
    public List<MarketData> loadHistoricalMarketData(LocalDate date1, LocalDate date2, Set<Symbol> symbolSet, int byCount, KdbByFunctionType byFunctionType) {
        return historicalDao.findMarketData(date1, date2, symbolSet, byCount, byFunctionType);
    }


}
