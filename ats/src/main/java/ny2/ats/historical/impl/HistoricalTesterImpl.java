package ny2.ats.historical.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import ny2.ats.core.common.Symbol;
import ny2.ats.core.data.MarketData;
import ny2.ats.core.util.MarketTimeUtility;
import ny2.ats.core.util.SystemUtility;
import ny2.ats.database.HistoricalDao;
import ny2.ats.database.IDBConnectionManager;
import ny2.ats.database.impl.KdbHistoricalDao;
import ny2.ats.database.kdb.KdbByFunctionType;
import ny2.ats.historical.IHistoricalTester;
import ny2.ats.indicator.IIndicatorDataHolder;
import ny2.ats.information.IEventGenarator;
import ny2.ats.information.TimerChecker;
import ny2.ats.market.connection.historical.HistoricalConnector;

// @Service -> xml
public class HistoricalTesterImpl implements IHistoricalTester {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // Logger
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    @Qualifier("KdbHistoricalDao")
    private HistoricalDao historicalDao;

    @Autowired
    @Qualifier("HistoricalConnector")
    private HistoricalConnector historicalConnector;

    @Autowired
    private IDBConnectionManager dbConnectionManager;

    @Autowired
    private IEventGenarator eventGenarator;

    @Autowired
    private IIndicatorDataHolder IndicatorDataHolder;

    /** Timeチェッカー */
    @Autowired
    private TimerChecker timerChecker;


    /** モデル稼動状態 */
    private boolean isModelRunning = false;


    // //////////////////////////////////////
    // Field (@Value)
    // //////////////////////////////////////

    /** テスト開始日 */
    @Value("#{T(java.time.LocalDate).parse(\"${historical.tester.startdate}\")}")
    private LocalDate startDate;

    /** テスト終了日 */
    @Value("#{T(java.time.LocalDate).parse(\"${historical.tester.enddate}\")}")
    private LocalDate endDate;

    /** テスト対象symbol */
    @Value("#{T(ny2.ats.core.common.Symbol).valueOfStringArray('${historical.tester.symbols}'.split(','))}")
    private Set<Symbol> symbolSet;

    //
    // By条件
    //
    /** MarketData読み込みのByおまとめモード */
    @Value("#{T(ny2.ats.database.kdb.KdbByFunctionType).valueOf('${historical.tester.byfunction}')}")
    private KdbByFunctionType byFunctionType;

    /** byおまとめの単位 (PIPSでは不使用)*/
    @Value("${historical.tester.bycount}")
    private int byCount;

    //
    // 全体設定
    //

    /** データロード間隔(7の倍数がおすすめ) */
    @Value("${historical.tester.incrementdays:14}")
    private long incrementDays;

    /** 初期データを読み込むかどうか(default=true) */
    @Value("${historical.tester.loadInitial}")
    private boolean loadInitial;

    /** RTデータをディスクに書き込むかどうか */
    @Value("${historical.tester.writedisk}")
    private boolean writeDisk;

    /** Indicatorデータ保持日数(マイナスの場合は削除しない) */
    @Value("${historical.indicator.holddays}")
    private int indicatorDataHoldDays;

    //
    // 取引時刻
    //

    /** 取引開始時刻(月曜) */
    @Value("#{T(java.time.LocalTime).parse('${historical.trade.startmonday}')}")
    private LocalTime startTradeMonday;

    /** 取引終了時刻(土曜・夏) */
    @Value("#{T(java.time.LocalTime).parse('${historical.trade.stopsaturday.summer}')}")
    private LocalTime stopTradeSaturdaySummer;

    /** 取引終了時刻(土曜・冬) */
    @Value("#{T(java.time.LocalTime).parse('${historical.trade.stopsaturday.winter}')}")
    private LocalTime stopTradeSaturdayWinter;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////


    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    /**
     * 条件を指定してHistoricalTestを実行します
     */
    public void startReplay(LocalDate startDate, LocalDate endDate, Set<Symbol> symbolSet, int byCount, KdbByFunctionType byFunctionType) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.symbolSet = symbolSet;
        this.byCount = byCount;
        this.byFunctionType = byFunctionType;
        startReplay();
    }

    @Override
    public void startReplay() {
        // connectorセットアップ
        historicalConnector.setupForReplay(symbolSet);
        timerChecker.resetTimer(LocalDateTime.of(startDate, LocalTime.MIN));

        // (日～土) 単位になるようにデータロード実行(最初のみincrementDays+α)
        LocalDate nextStartDate = startDate;
        LocalDate nextEndDate = nextStartDate.plusDays(incrementDays - 1);
        while (nextEndDate.getDayOfWeek() != DayOfWeek.SATURDAY) {
            nextEndDate = nextEndDate.plusDays(1);
        }
        if (nextEndDate.isAfter(endDate)) {
            nextEndDate = endDate;
        }

        // 初期データロード
        if (loadInitial) {
            loadInitialData();
        }

        while (true) {
            // データロード
            logger.info("Data Load : {}-{}", nextStartDate.toString(), nextEndDate.toString());
            List<MarketData> marketDataList = null;
            if (byFunctionType == KdbByFunctionType.UNUSED || byCount <= 0) {
                marketDataList = historicalDao.findMarketData(nextStartDate, nextEndDate, symbolSet);
            } else {
                marketDataList = historicalDao.findMarketData(nextStartDate, nextEndDate, symbolSet, byCount, byFunctionType);
            }

            if (marketDataList.size() == 0) {
                logger.error("No Historical Data.");
            }

            // データ再生開始
            loadData(marketDataList);

            // Indicatorデータ削除
            if (indicatorDataHoldDays > 0) {
                logger.info("Reduce indicator data.");
                IndicatorDataHolder.reduceIndicatorData(indicatorDataHoldDays);
            }
            System.gc();

            // RT Disk書き込み
            if (writeDisk) {
                while (dbConnectionManager.checkdDataQueueSize() != 0 || dbConnectionManager.checkActiveThread() != 0) {
                    SystemUtility.waitSleep(1000);
                }
                logger.info("Writing kdb to disk.");
                historicalDao.writeToDisk();
                SystemUtility.waitSleep(1000);
            }

            // 次の日付を設定
            nextStartDate = nextEndDate.plusDays(1);
            nextEndDate = nextEndDate.plusDays(incrementDays);
            // 次の開始日がEnd以降なら終了
            if (nextStartDate.isAfter(endDate)) {
                break;
            }
            // 次の終了日がEnd以降だったらEndを終了日に設定
            if (nextEndDate.isAfter(endDate)) {
                nextEndDate = endDate;
            }
        }

        // RT Finalize
        if (writeDisk) {
            logger.info("Finalize kdb to disk.");
            historicalDao.finalizeDisk();
        }

        // ロードが終わったら5秒waitしてシステムを強制終了
        SystemUtility.waitSleep(5000);

        logger.info("### FINISHED ###");
        System.exit(0);
    }


    /**
     * テスト実行前の初期化用にデータを流します
     * @param startDate
     * @param symbolList
     * @param byCount
     * @param byFunctionType
     */
    private void loadInitialData() {
        // 1週間前のデータを流す
        LocalDate fromDate = startDate.minusDays(7);
        LocalDate toDate = startDate.minusDays(1);

        logger.info("Load initial Data : {}-{}", fromDate.toString(), toDate.toString());
        List<MarketData> marketDataList = null;
        if (byFunctionType == KdbByFunctionType.UNUSED || byCount <= 0) {
            marketDataList = ((KdbHistoricalDao)historicalDao).findMarketData(fromDate, toDate, symbolSet);
        } else {
            marketDataList = ((KdbHistoricalDao)historicalDao).findMarketData(fromDate, toDate, symbolSet, byCount, byFunctionType);
        }

        // load
        for (int index = 0; index < marketDataList.size(); index++) {
            MarketData marketData = marketDataList.get(index);
            marketData.setCreateDateTime(LocalDateTime.now());
            // Timer更新チェック
            timerChecker.proceedTimer(marketData.getMarketDateTime());
            // MarketData更新
            historicalConnector.updateMarketData(marketData);
        }
    }

    /**
     * データを再生する
     * @param targetDataList
     */
    private void loadData(List<MarketData> targetDataList) {
        for (int index = 0; index < targetDataList.size(); index++) {
            // 途中で停止しないようにtry-catchする
            try {
                MarketData marketData = targetDataList.get(index);
                marketData.setCreateDateTime(LocalDateTime.now());

                // 週末はモデルを止める
                if (!isMarketOpenedTradable(marketData.getMarketDateTime())) {
                    if (isModelRunning) {
                        logger.info("Stop models for weekend. {}", marketData.getMarketDateTime());
                        eventGenarator.sendModelStopEventForHistorical(marketData.getMarketDateTime());
                        isModelRunning = false;
                    }
                } else {
                    if (!isModelRunning) {
                        logger.info("Start models for Monday. {}", marketData.getMarketDateTime());
                        eventGenarator.sendModelStartEventForHistorical(marketData.getMarketDateTime());
                        isModelRunning = true;
                    }
                }
                // Timer更新チェック
                timerChecker.proceedTimer(marketData.getMarketDateTime());
                // MarketData更新
                historicalConnector.updateMarketData(marketData);

            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    /**
     * 取引を行う時刻かどうか判断します
     * @param jstDateTime
     * @return
     */
    public boolean isMarketOpenedTradable(LocalDateTime jstDateTime) {
        switch (jstDateTime.getDayOfWeek()) {
            case TUESDAY:
            case WEDNESDAY:
            case THURSDAY:
            case FRIDAY:
                return true;
            case MONDAY:
                return jstDateTime.toLocalTime().isBefore(startTradeMonday) ? false : true;
            case SATURDAY:
                if (MarketTimeUtility.isNYCSummer(jstDateTime.toLocalDate())) {
                    return jstDateTime.toLocalTime().isAfter(stopTradeSaturdaySummer) ? false : true;
                } else {
                    return jstDateTime.toLocalTime().isAfter(stopTradeSaturdayWinter) ? false : true;
                }
            case SUNDAY:
                return false;
            default:
                return false;
        }
    }

}
