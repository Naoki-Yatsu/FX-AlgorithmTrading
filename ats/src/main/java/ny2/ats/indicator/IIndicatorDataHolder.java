package ny2.ats.indicator;

import java.util.Map;
import java.util.Set;

import ny2.ats.core.common.Period;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.data.MarketData;
import ny2.ats.core.data.TimerInformation;
import ny2.ats.indicator.impl.IndicatorDataMap;
import ny2.ats.indicator.indicators.OHLCIndicator;

/**
 * Indicatorデータ管理のインターフェースです。
 */
public interface IIndicatorDataHolder {

    // /** Indicator作成の最小期間 */
    // public static final Period SHORTEST_PERIOD = Period.MIN_1;

    /**
     * Indicatorを初期化します(通常はこちらを使用します)
     */
    public void initialize();

    /**
     * 各種設定パラメータを上書きしてIndicatorを初期化します（Historicalテストなどで使用します）。<br>
     * nullが設定されている項目はデフォルト値を使用します
     *
     * @param symbols
     * @param indicatorTypes
     * @param indicatorPeriodsTime
     * @param indicatorPeriodsTick
     */
    public void initialize(Set<Symbol> symbols, Set<IndicatorType> indicatorTypes, Set<Period> indicatorPeriodTimes, Set<Period> indicatorPeriodTicks);

    public Set<Symbol> getIndicatorSymbols();

    public Set<IndicatorType> getIndicatorTypes();

    public Set<Period> getIndicatorPeriodsTime();

    public Set<Period> getIndicatorPeriodsTick();


    // //////////////////////////////////////
    // Update
    // //////////////////////////////////////

    /**
     * マーケットデータを更新します
     *
     * @param marketData 更新マーケットデータ
     */
    public void updateMarketData(MarketData marketData);

    /**
     * Indicatorの期間変更を通知します。Indicatorはlastの時刻で作成されます。
     * @param timerInformation period-変更対象の期間 / baseDateTime-変更前の基準時刻 / nextDateTime-次の基準時刻
     */
    public void changePeriod(TimerInformation timerInformation);


    // //////////////////////////////////////
    // Data Access
    // //////////////////////////////////////

    /**
     * 対象のIndicatorMapを返します。
     * @param indicatorType
     * @return
     */
    public IndicatorDataMap<?> getIndicatorDataMap(IndicatorType indicatorType);

    /**
     * 対象のIndicatorを返します。
     * @param indicatorType
     * @param symbol
     * @param period
     * @return
     */
    public Indicator<?> getIndicator(IndicatorType indicatorType, Symbol symbol, Period period);

    /**
     * すべてのIndicatorのMapを複製して返します。(OHLC含む)
     * @return
     */
    public Map<IndicatorType, IndicatorDataMap<?>> copyIndicatorDataMapWithOHLC();

    /**
     * IndicatorMapへの参照を返します(OHLCは含まない)
     * @return
     */
    public Map<IndicatorType, IndicatorDataMap<?>> getAllIndicatorDataMap();

    /**
     * OHLC Map への参照を返します
     * @return
     */
    public IndicatorDataMap<OHLCIndicator> getOHLCMap();


    /**
     * Indicatorの過去データを削減します
     *
     * @param holdDays 残す期間
     */
    public void reduceIndicatorData(int holdDays);

}
