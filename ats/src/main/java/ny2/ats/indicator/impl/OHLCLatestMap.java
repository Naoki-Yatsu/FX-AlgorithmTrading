package ny2.ats.indicator.impl;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ny2.ats.core.common.Period;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.data.MarketData;
import ny2.ats.core.util.NumberUtility;

/**
 * 特定の通貨ペアの最新1期間の全てのPeriodデータを保持するクラスです。
 */
public class OHLCLatestMap {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // /** Logger */
    // private static final Logger logger = LoggerFactory.getLogger(OHLCLatestMap.class);

    /** 対象の通貨ペア */
    private final Symbol symbol;

    /** Indicator作成対象Period (Time) */
    private final Set<Period> indicatorPeriodsTime;

    /** Indicator作成対象Period (Time) */
    private final Set<Period> indicatorPeriodsTick;

    /** Indicator作成の最小期間 */
    private final Period shortestTimePeriod;


    /** OHLC用のMap */
    private final Map<Period, OHLC> ohlcDataMap = new EnumMap<>(Period.class);

    // Tick Count 用
    private final Map<Period, Integer> tickUpdateCountMap = new EnumMap<>(Period.class);

    // Tick pip 用
    private final Map<Period, Double> tickPipThresholdMap = new EnumMap<>(Period.class);
    private final Map<Period, Double> tickPipLastMap = new EnumMap<>(Period.class);

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public OHLCLatestMap(Symbol symbol, Set<Period> indicatorPeriodsTime, Set<Period> indicatorPeriodsTick) {
        this.symbol = symbol;
        this.indicatorPeriodsTime = indicatorPeriodsTime;
        this.indicatorPeriodsTick = indicatorPeriodsTick;
        this.shortestTimePeriod = Period.getShortestPeriod(indicatorPeriodsTime);

        // 全てのPeriodのDataを空で作成します。
        for (Period period : indicatorPeriodsTime) {
            ohlcDataMap.put(period, new OHLC(symbol, period));
        }
        for (Period period : indicatorPeriodsTick) {
            ohlcDataMap.put(period, new OHLC(symbol, period));
        }

        // TICK用に作成
        for (Period tickPeriod : indicatorPeriodsTick) {
            // Tick count 用
            if (Period.isTickCountPeriod(tickPeriod)) {
                tickUpdateCountMap.put(tickPeriod, 0);
            }

            // Tick pip 用
            if (Period.isTickPipPeriod(tickPeriod)) {
                tickPipLastMap.put(tickPeriod, Double.NaN);
                // 変更しきい値を計算誤差を含めて小さめに設定
                tickPipThresholdMap.put(tickPeriod, symbol.convertSubPipsToReal(tickPeriod.getTimeInverval()));
            }
        }
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    /**
     * マーケットデータを更新します。
     */
    public synchronized void updateMarketData(MarketData marketData) {
        // 都度の更新は最も更新間隔が多いもののみ実行する
        // それ以外のPeriodは期間変更時に更新する
        if (shortestTimePeriod != null) {
            ohlcDataMap.get(shortestTimePeriod).update(marketData.getBidPrice(), marketData.getAskPrice());
        }
    }

    /**
     * Tick足用にマーケットデータを更新します。足の更新があれば、OHLCを返します。
     * ※Round Tickは1種類のみに対応
     * @param marketData
     * @return
     */
    public synchronized List<OHLC> updateMarketDataForTick(MarketData marketData) {
        // 戻り値用のリスト(多くの場合は対象がないので、nullのままとする)
        List<OHLC> returnList = null;

        // 最新の値を取得
        double bid = marketData.getBidPrice();
        double ask = marketData.getAskPrice();
        double mid = marketData.getMidPrice();

        for (Period tickPeriod : indicatorPeriodsTick) {
            // Tick更新
            ohlcDataMap.get(tickPeriod).update(bid, ask);

            if (Period.isTickCountPeriod(tickPeriod)) {
                // 更新回数のincrement
                int updateCount = tickUpdateCountMap.get(tickPeriod).intValue() + 1;
                tickUpdateCountMap.put(tickPeriod, updateCount);

                // 規定回数以上の更新が無ければ抜ける
                if (updateCount < tickPeriod.getTimeInverval()) {
                    continue;
                }
                tickUpdateCountMap.put(tickPeriod, 0);

            } else if (Period.isTickPipPeriod(tickPeriod)) {
                double newPrice = checkPipChange(tickPeriod, mid);
                if (Double.isNaN(newPrice)) {
                    continue;
                }
                // Lastを更新
                // logger.debug("Update tick pip. {} / {}, price = {}", symbol, tickPeriod, newPrice);
                tickPipLastMap.put(tickPeriod, symbol.roundSubPips(newPrice));
            }

            // Indicator用OHLCを作成
            OHLC ohlc = ohlcDataMap.get(tickPeriod);
            ohlc.roundAll();
            // Tick では最終更新時刻を更新時刻とする
            ohlc.setBaseDateTime(marketData.getMarketDateTime());

            // 戻り値のリストに追加
            if (returnList == null) {
                returnList = new ArrayList<>();
            }
            returnList.add(ohlc);

            // 更新対象を入れ替え、時刻は次の秒から
            ohlcDataMap.put(tickPeriod, new OHLC(symbol, tickPeriod));
        }

        if (returnList == null) {
            return Collections.emptyList();
        } else {
            return returnList;
        }
    }

    /**
     * Tick Pip 用に指定以上のPip変化があったか確認します
     * @param tickPeriod
     * @param mid
     * @return
     */
    private double checkPipChange(Period tickPeriod, double mid) {
        double newPrice = Double.NaN;

        // 一定以上の変化があればデータ作成
        double lastBasePrice = tickPipLastMap.get(tickPeriod);
        double pipThreshold = tickPipThresholdMap.get(tickPeriod);
        double compareThreshold = symbol.getPipValue() * 0.001;
        if (Double.isNaN(lastBasePrice)) {
            // 基準単位で丸め
            return symbol.roundSubPipsBase(mid, tickPeriod.getTimeInverval(), RoundingMode.DOWN);
        }

        if (NumberUtility.almostGreaterEqual(mid, lastBasePrice + pipThreshold, compareThreshold)) {
            // Up
            for (int i = 1; i < 10000; i++) {
                // 丸め価格を作成
                newPrice = lastBasePrice + pipThreshold * i;
                if (!NumberUtility.almostGreater(mid, newPrice + pipThreshold, compareThreshold)) {
                    break;
                }
            }

        } else if (NumberUtility.almostLessEqual(mid, lastBasePrice - pipThreshold, compareThreshold)) {
            // Down
            for (int i = 1; i < 10000; i++) {
                // 丸め価格を作成
                newPrice = lastBasePrice - pipThreshold * i;
                if (!NumberUtility.almostLess(mid, newPrice - pipThreshold, compareThreshold)) {
                    break;
                }
            }
        }

        return newPrice;
    }

    /**
     * 対象Periodを次の期間に進みます。
     * @param period
     * @param nextDateTime
     * @return 前期間のOHLC
     */
    public OHLC moveNextDateTime(Period period, LocalDateTime nextDateTime) {
        // 進める期間より大きな期間のデータをupdateします。
        copyDataToLongerPeriod(period);

        // 戻り値用に最新データを退避
        OHLC ohlc = ohlcDataMap.get(period);

        // 対象のPeriodのデータ更新
        ohlcDataMap.put(period, new OHLC(symbol, period, nextDateTime));

        return ohlc;
    }

    /**
    * fromPeriodのOHLCデータを、より長いperiodにコピーします。
    */
   public void copyDataToLongerPeriod(Period fromPeriod) {
       OHLC ohlc = ohlcDataMap.get(fromPeriod);
       if (!ohlc.isIntialized()) {
           return;
       }
       for (Period period : indicatorPeriodsTime) {
           if (period.getOrder() > fromPeriod.getOrder()) {
               ohlcDataMap.get(period).update(ohlc);
           }
       }
   }

    public OHLC getOHLCData(Period period) {
        return ohlcDataMap.get(period);
    }

    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

    public Symbol getSymbol() {
        return symbol;
    }

}
