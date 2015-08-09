package ny2.ats.indicator.impl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ny2.ats.core.common.Period;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.data.MarketData;
import ny2.ats.core.data.TimerInformation;
import ny2.ats.indicator.IIndicatorDataHolder;
import ny2.ats.indicator.IIndicatorManager;
import ny2.ats.indicator.Indicator;
import ny2.ats.indicator.IndicatorProcessor;
import ny2.ats.indicator.IndicatorType;
import ny2.ats.indicator.indicators.BollingerBandEMAIndicator;
import ny2.ats.indicator.indicators.BollingerBandIndicator;
import ny2.ats.indicator.indicators.ExponentialMovingAverageIndicator;
import ny2.ats.indicator.indicators.IchimokuIndicator;
import ny2.ats.indicator.indicators.LinearRegressionIndicator;
import ny2.ats.indicator.indicators.MACDIndicator;
import ny2.ats.indicator.indicators.MovingAverageIndicator;
import ny2.ats.indicator.indicators.OHLCIndicator;
import ny2.ats.indicator.indicators.PriceRangeIndicator;
import ny2.ats.indicator.indicators.RSIIndicator;
import ny2.ats.indicator.indicators.RankCorrelationIndexIndicator;
import ny2.ats.indicator.indicators.StochasticsIndicator;
import ny2.ats.indicator.processor.BollingerBandEMAProcessor;
import ny2.ats.indicator.processor.BollingerBandProcessor;
import ny2.ats.indicator.processor.ExponentialMovingAverageProcessor;
import ny2.ats.indicator.processor.IchimokuProcessor;
import ny2.ats.indicator.processor.LinearRegressionProcessor;
import ny2.ats.indicator.processor.MACDProcessor;
import ny2.ats.indicator.processor.MovingAverageProcessor;
import ny2.ats.indicator.processor.PriceRangeProcessor;
import ny2.ats.indicator.processor.RSIProcessor;
import ny2.ats.indicator.processor.RankCorrelationIndexProcessor;
import ny2.ats.indicator.processor.StochasticsProcessor;

@Component
public class IndicatorDataHolderImpl implements IIndicatorDataHolder {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** Logger */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private IIndicatorManager indicatorManager;

    /** Indicator作成対象Symbol */
    @Value("#{T(ny2.ats.core.common.Symbol).valueOfStringArray('${indicator.dataholder.symbols}'.split(','))}")
    private Set<Symbol> indicatorSymbols;

    /** Indicator作成対象Type */
    @Value("#{T(ny2.ats.indicator.IndicatorType).valueOfStringArray('${indicator.dataholder.indicatortypes}'.split(','))}")
    private Set<IndicatorType> indicatorTypes;

    /** Indicator作成対象Period (Time) */
    @Value("#{T(ny2.ats.core.common.Period).valueOfStringArray('${indicator.dataholder.periodtimes}'.split(','))}")
    private Set<Period> indicatorPeriodTimes;

    /** Indicator作成対象Period (Tick) */
    @Value("#{T(ny2.ats.core.common.Period).valueOfStringArray('${indicator.dataholder.periodticks}'.split(','))}")
    private Set<Period> indicatorPeriodTicks;

    /** Indicator作成の最小期間 */
    private Period shortestTimePeriod;


    /** 通貨ペアごとの最新1期間OHLC */
    private final Map<Symbol, OHLCLatestMap> latestOHLCMap = new EnumMap<>(Symbol.class);

    /** OHLCデータ */
    private IndicatorDataMap<OHLCIndicator> ohlcMap = new IndicatorDataMap<OHLCIndicator>(IndicatorType.OHLC, Collections.emptySet(), Collections.emptySet());

    /** Indicator更新Processor */
    private final Map<IndicatorType, IndicatorProcessor<?, ?>> allProcessorMap = new EnumMap<>(IndicatorType.class);

    /** Indicatorデータ(OHLC除く) */
    private final Map<IndicatorType, IndicatorDataMap<?>> allIndicatorDataMap = new EnumMap<>(IndicatorType.class);

    //
    // 更新用Work
    //
    /** Periodごとの更新時刻 */
    private final Map<Period, LocalDateTime> lastUpdateTimeMap = new ConcurrentHashMap<>();

    /** 更新待ちPeriod Map (イベントの順序入れ違い対応) */
    private final Map<Period, TimerInformation> waitingUpdateMap = new ConcurrentHashMap<>();

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public IndicatorDataHolderImpl() {
        logger.info("Create instance.");
    }

    @PostConstruct
    private void init() {
        // logger.info("PostConstruct instance.");
        // initializeDefault();
    }

    @Override
    public void initialize(Set<Symbol> symbols, Set<IndicatorType> indicatorTypes, Set<Period> indicatorPeriodTimes, Set<Period> indicatorPeriodTicks) {
        // Indicator作成対象を更新 nullの場合はデフォルトを使用
        if (symbols != null) {
            this.indicatorSymbols = symbols;
        }
        if (indicatorTypes != null) {
            this.indicatorTypes = indicatorTypes;
        }
        if (indicatorPeriodTimes != null) {
            this.indicatorPeriodTimes = indicatorPeriodTimes;
        }
        if (indicatorPeriodTicks != null) {
            this.indicatorPeriodTicks = indicatorPeriodTicks;
        }
        initialize();
    }

    @Override
    public void initialize() {
        // 最小更新間隔
        this.shortestTimePeriod = Period.getShortestPeriod(indicatorPeriodTimes);

        // All Period
        Set<Period> indicatorPeriodAll = new TreeSet<>();
        indicatorPeriodAll.addAll(indicatorPeriodTimes);
        indicatorPeriodAll.addAll(indicatorPeriodTicks);

        // Last OHLC Mapの初期化
        for (Symbol symbol : indicatorSymbols) {
            this.latestOHLCMap.put(symbol, new OHLCLatestMap(symbol, indicatorPeriodTimes, indicatorPeriodTicks));
        }

        // Indicator OHLC
        // this.ohlcMap = new IndicatorDataMap<OHLCIndicator>(IndicatorType.OHLC, indicatorSymbols, indicatorPeriodAll);
        indicatorTypes.add(IndicatorType.OHLC);
        for (Symbol symbol : indicatorSymbols) {
            ohlcMap.addSymbol(symbol, indicatorPeriodAll);
        }

        // Indicatorデータ初期化
        initializeMap(indicatorSymbols, indicatorTypes, indicatorPeriodAll);

        // 更新時刻Map
        for (Period period : indicatorPeriodTimes) {
            this.lastUpdateTimeMap.put(period, LocalDateTime.MIN);
        }
    }

    /**
     * IndicatorProcessor、indicatorDataMap初期化を行います。
     */
    private void initializeMap(Set<Symbol> symbols, Set<IndicatorType> indicatorTypes, Set<Period> indicatorPeriodAll) {
        // typeは使いまわす
        IndicatorType type = null;

        // OHLC(特別扱い)
        // type = IndicatorType.OHLC;

        // MA
        type = IndicatorType.MA;
        if (indicatorTypes.contains(type)) {
            IndicatorDataMap<MovingAverageIndicator> smaMap = new IndicatorDataMap<MovingAverageIndicator>(type, symbols, indicatorPeriodAll);
            allIndicatorDataMap.put(type, smaMap);
            allProcessorMap.put(type, new MovingAverageProcessor(ohlcMap, smaMap));
        }

        // EMA
        type = IndicatorType.EMA;
        if (indicatorTypes.contains(type)) {
            IndicatorDataMap<ExponentialMovingAverageIndicator> emaMap = new IndicatorDataMap<ExponentialMovingAverageIndicator>(type, symbols, indicatorPeriodAll);
            allIndicatorDataMap.put(type, emaMap);
            allProcessorMap.put(type, new ExponentialMovingAverageProcessor(ohlcMap, emaMap));
        }

        // RSI
        type = IndicatorType.RSI;
        if (indicatorTypes.contains(type)) {
            IndicatorDataMap<RSIIndicator> rsiMap = new IndicatorDataMap<RSIIndicator>(type, symbols, indicatorPeriodAll);
            allIndicatorDataMap.put(type, rsiMap);
            allProcessorMap.put(type, new RSIProcessor(ohlcMap, rsiMap));
        }

        // RCI (Rank Correlation Index)
        type = IndicatorType.RCI;
        if (indicatorTypes.contains(type)) {
            IndicatorDataMap<RankCorrelationIndexIndicator> rciMap = new IndicatorDataMap<RankCorrelationIndexIndicator>(type, symbols, indicatorPeriodAll);
            allIndicatorDataMap.put(type, rciMap);
            allProcessorMap.put(type, new RankCorrelationIndexProcessor(ohlcMap, rciMap));
        }

        // MACD
        type = IndicatorType.MACD;
        if (indicatorTypes.contains(type)) {
            IndicatorDataMap<MACDIndicator> macdMap = new IndicatorDataMap<MACDIndicator>(type, symbols, indicatorPeriodAll);
            allIndicatorDataMap.put(type, macdMap);
            allProcessorMap.put(type, new MACDProcessor(ohlcMap, macdMap));
        }

        // BOLLINGER
        type = IndicatorType.BOLLINGER;
        if (indicatorTypes.contains(type)) {
            IndicatorDataMap<BollingerBandIndicator> bollingerMap = new IndicatorDataMap<BollingerBandIndicator>(type, symbols, indicatorPeriodAll);
            allIndicatorDataMap.put(type, bollingerMap);
            allProcessorMap.put(type, new BollingerBandProcessor(ohlcMap, bollingerMap));
        }

        // BOLLINGER EMA
        type = IndicatorType.BOLLINGER_EMA;
        if (indicatorTypes.contains(type)) {
            IndicatorDataMap<BollingerBandEMAIndicator> bollingerEMAMap = new IndicatorDataMap<BollingerBandEMAIndicator>(type, symbols, indicatorPeriodAll);
            allIndicatorDataMap.put(type, bollingerEMAMap);
            allProcessorMap.put(type, new BollingerBandEMAProcessor(ohlcMap, bollingerEMAMap));
        }

        // STOCHASTIC FAST
        type = IndicatorType.STOCHASTICS;
        if (indicatorTypes.contains(type)) {
            IndicatorDataMap<StochasticsIndicator> stochasticsMap = new IndicatorDataMap<StochasticsIndicator>(type, symbols, indicatorPeriodAll);
            allIndicatorDataMap.put(type, stochasticsMap);
            allProcessorMap.put(type, new StochasticsProcessor(ohlcMap, stochasticsMap));
        }

        // ICHIMOKU
        type = IndicatorType.ICHIMOKU;
        if (indicatorTypes.contains(type)) {
            IndicatorDataMap<IchimokuIndicator> ichimokuMap = new IndicatorDataMap<IchimokuIndicator>(type, symbols, indicatorPeriodAll);
            allIndicatorDataMap.put(type, ichimokuMap);
            allProcessorMap.put(type, new IchimokuProcessor(ohlcMap, ichimokuMap));
        }

        // LIN_REG
        type = IndicatorType.LINEAR_REG;
        if (indicatorTypes.contains(type)) {
            IndicatorDataMap<LinearRegressionIndicator> lrMap = new IndicatorDataMap<LinearRegressionIndicator>(type, symbols, indicatorPeriodAll);
            allIndicatorDataMap.put(type, lrMap);
            allProcessorMap.put(type, new LinearRegressionProcessor(ohlcMap, lrMap));
        }

        // PRICE_RANGE
        type = IndicatorType.PRICE_RANGE;
        if (indicatorTypes.contains(type)) {
            IndicatorDataMap<PriceRangeIndicator> priceRangeMap = new IndicatorDataMap<PriceRangeIndicator>(type, symbols, indicatorPeriodAll);
            allIndicatorDataMap.put(type, priceRangeMap);
            allProcessorMap.put(type, new PriceRangeProcessor(ohlcMap, priceRangeMap));
        }
    }

    // //////////////////////////////////////
    // Method (Update)
    // //////////////////////////////////////

    @Override
    public void updateMarketData(MarketData marketData) {
        // 更新対象の通貨ペアのOHLCをupdate
        Symbol symbol = marketData.getSymbol();
        if (latestOHLCMap.containsKey(symbol)) {
            latestOHLCMap.get(symbol).updateMarketData(marketData);

            // Tick用の更新
            updateForTick(marketData, latestOHLCMap.get(symbol));
        }
    }

    @Override
    public synchronized void changePeriod(TimerInformation timerInformation) {
        Period period = timerInformation.getPeriod();
        LocalDateTime baseDateTime = timerInformation.getCurrentDateTime();
        LocalDateTime nextDateTime = timerInformation.getNextDateTime();

        // logger.debug("Timer updated {}. Current = {}, Next = {}", period, baseDateTime, nextDateTime);
        if (!indicatorPeriodTimes.contains(period)) {
            // logger.debug("Skip calculate indicators. Period = {}", period.name());
            return;
        }

        // 対象より短いPeriodが更新されているか確認、更新されていない場合はwaitする
        if (period != shortestTimePeriod) {
            Period shorterPeriod = period.getShorterPeriod(indicatorPeriodTimes);
            LocalDateTime shorterUpdateTIme = lastUpdateTimeMap.get(shorterPeriod);
            if (shorterUpdateTIme.compareTo(baseDateTime) < 0) {
                logger.warn("Shorter period has not updated yet. Period = {}, Time = {}", shorterPeriod, shorterUpdateTIme);
                waitingUpdateMap.put(period, timerInformation);
                return;
            }
        }

        // Periodごとの更新時刻の更新
        lastUpdateTimeMap.put(period, baseDateTime);

        for (Symbol symbol : latestOHLCMap.keySet()) {
            // 最新データを次の期間に更新して、最新データ取得
            OHLC ohlc = latestOHLCMap.get(symbol).moveNextDateTime(period, nextDateTime);

            // 初回の更新は時刻がnullなので抜ける
            if (ohlc.getBaseDateTime() == null) {
                logger.debug("First update of OHLC for Period = {}, Symbol = {}", period, symbol);
                continue;
            }

            // 1度も更新が無い場合は前回のCloseを使用する
            if (!ohlc.isInitialized()) {
                // logger.debug("Skip indicator update. No market update, and OHLC is NOT initialized. DateTime = {}, Period = {}, Symbol = {}", ohlc.getBaseDateTime(), period, symbol);
                OHLC lastOHLC = ohlcMap.getIndicator(symbol, period).getLastOHLC();
                if (lastOHLC == null) {
                    continue;
                }
                ohlc.update(lastOHLC.getCloseBid(), lastOHLC.getCloseAsk());
            }

            // Notify to Indicators
            updateOHLCAndIndicators(symbol, period, ohlc);
        }

        // 更新待ち確認
        if (!waitingUpdateMap.isEmpty()) {
            for (TimerInformation waitingTimer : waitingUpdateMap.values()) {
                logger.warn("Waiting Update, Period = {}, time = {}", waitingTimer.getPeriod(), waitingTimer.getCurrentDateTime());
                if (waitingTimer.getPeriod().getOrder() > period.getOrder() && !baseDateTime.isBefore(waitingTimer.getCurrentDateTime())) {
                    changePeriod(waitingTimer);
                }
            }
        }
    }

    /**
     * OHLCの更新を受けて全てのIndicatorを更新します。
     * @param symbol
     * @param period
     * @param ohlc
     */
    private void updateOHLCAndIndicators(Symbol symbol, Period period, OHLC ohlc) {
        // Firstly, Update OHLC
        // Round OHLC values, then add DataMap.
        ohlc.roundAll();
        OHLCIndicator ohlcIndicator = ohlcMap.getSymbolMap(symbol).getIndicator(period);
        ohlcIndicator.addTimeData(ohlc.getBaseDateTime());
        ohlcIndicator.addValueData(ohlc);
        // Next, Update indicators
        for (Entry<IndicatorType, IndicatorProcessor<?, ?>> entry : allProcessorMap.entrySet()) {
            // Tickは対象Indicatorのみ
            if (period.isTickPeriod() && !IndicatorType.INDICATOR_FOR_TICK.contains(entry.getKey())) {
                continue;
            }
            // Indicator計算
            entry.getValue().updateOHLC(ohlc);
        }
//        // Indicatorをparallel計算 - indicatorが10個くらいなら不要
//        allProcessorMap.entrySet().parallelStream()
//                // Tickは対象Indicatorのみ
//                .filter(e -> !period.isTickPeriod() || IndicatorType.INDICATOR_FOR_TICK.contains(e.getKey()))
//                .forEach(e -> e.getValue().updateOHLC(ohlc));


        // すべてのIndicatorを計算してからEvent送信する
        // Send event (OHLC)
        indicatorManager.sendIndicatorUpdate(ohlcIndicator);
        // Send event (indicators)
        for (IndicatorDataMap<?> datalMap : allIndicatorDataMap.values()) {
            if (period.isTickPeriod() && !IndicatorType.INDICATOR_FOR_TICK.contains(datalMap.getType())) {
                continue;
            }
            // Event送信
            Indicator<?> indicator = datalMap.getSymbolMap(symbol).getIndicator(period);
            indicatorManager.sendIndicatorUpdate(indicator);
        }
    }

    /**
     * Tick足用に更新処理を行います。
     * @param marketData
     * @param latestMap
     */
    private void updateForTick(MarketData marketData, OHLCLatestMap latestMap) {
        List<OHLC> ohlcList = latestMap.updateMarketDataForTick(marketData);
        for (OHLC ohlc : ohlcList) {
            updateOHLCAndIndicators(marketData.getSymbol(), ohlc.getPeriod(), ohlc);
        }
    }

    // //////////////////////////////////////
    // Method (Data Access)
    // //////////////////////////////////////

    @Override
    public IndicatorDataMap<?> getIndicatorDataMap(IndicatorType indicatorType) {
        return allIndicatorDataMap.get(indicatorType);
    }

    @Override
    public Indicator<?> getIndicator(IndicatorType indicatorType, Symbol symbol, Period period) {
        return allIndicatorDataMap.get(indicatorType).getIndicator(symbol, period);
    }

    @Override
    public Map<IndicatorType, IndicatorDataMap<?>> copyIndicatorDataMapWithOHLC() {
        Map<IndicatorType, IndicatorDataMap<?>> map = new EnumMap<>(IndicatorType.class);
        // put OHLC
        map.put(IndicatorType.OHLC, ohlcMap);
        // put others
        for (Entry<IndicatorType, IndicatorDataMap<?>> entry : allIndicatorDataMap.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    @Override
    public IndicatorDataMap<OHLCIndicator> getOHLCMap() {
        return ohlcMap;
    }

    @Override
    public Map<IndicatorType, IndicatorDataMap<?>> getAllIndicatorDataMap() {
        return allIndicatorDataMap;
    }


    @Override
    public void reduceIndicatorData(int holdDays) {
        logger.info("Reduce indicator data : holdDays = {}", holdDays);

        // OHLC
        for (IndicatorDataSymbolMap<?> symbolMap : ohlcMap.getDatalMap().values()) {
            for (Indicator<?> indicator : symbolMap.getDataMap().values()) {
                indicator.reduceData(holdDays);
            }
        }
        // Others
        for (IndicatorDataMap<?> indicatorDataMap : allIndicatorDataMap.values()) {
            for (IndicatorDataSymbolMap<?> symbolMap : indicatorDataMap.getDatalMap().values()) {
                for (Entry<Period, ? extends Indicator<?>> indicatorPeriodEntry : symbolMap.getDataMap().entrySet()) {
                    if (indicatorPeriodEntry.getKey() != Period.DAILY) {
                        // Daily data are not reduced
                        indicatorPeriodEntry.getValue().reduceData(holdDays);
                    }
                }
            }
        }
    }

    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

    public Map<Symbol, OHLCLatestMap> getLatestOHLCMap() {
        return latestOHLCMap;
    }

    public Map<IndicatorType, IndicatorProcessor<?, ?>> getAllProcessorMap() {
        return allProcessorMap;
    }

    public Set<Symbol> getIndicatorSymbols() {
        return indicatorSymbols;
    }

    public Set<IndicatorType> getIndicatorTypes() {
        return indicatorTypes;
    }

    public Set<Period> getIndicatorPeriodsTime() {
        return indicatorPeriodTimes;
    }

    public Set<Period> getIndicatorPeriodsTick() {
        return indicatorPeriodTicks;
    }
}
