package ny2.ats.historical;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import ny2.ats.core.common.Period;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.data.MarketData;
import ny2.ats.core.event.MarketUpdateEvent;
import ny2.ats.database.kdb.KdbByFunctionType;
import ny2.ats.indicator.IIndicatorDataHolder;
import ny2.ats.indicator.IndicatorType;
import ny2.ats.market.connection.MarketType;
import ny2.ats.market.order.impl.HistoricalOrderManagerImpl;
import ny2.ats.model.IModelFactory;
import ny2.ats.model.impl.ModelManagerImpl;
import ny2.ats.position.impl.PositionManagerImpl;

/**
 * Historicalテストの設定を行うクラスです
 */
// @Component -> xml
public class HistoricalTesterSetup {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // Logger
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final UUID uuid = UUID.randomUUID();

    @Autowired
    private IIndicatorDataHolder indicatorDataHolder;

    @Autowired
    private IModelFactory modelFactory;

    @Autowired
    private HistoricalOrderManagerImpl orderManager;


    // Position換算データ登録用
    @Autowired
    private ModelManagerImpl modelManager;

    @Autowired
    private PositionManagerImpl positionManager;


    // //////////////////////////////////////
    // Field (@Value)
    // //////////////////////////////////////

    //
    // 実行条件(HistoricalTesterImplにも定義)
    //

    /** テスト開始日 */
    @Value("#{T(java.time.LocalDate).parse(\"${historical.tester.startdate}\")}")
    private LocalDate startDate;

    /** テスト終了日 */
    @Value("#{T(java.time.LocalDate).parse(\"${historical.tester.enddate}\")}")
    private LocalDate endDate;

    /** テスト対象symbol */
    @Value("#{T(ny2.ats.core.common.Symbol).valueOfStringArray('${historical.tester.symbols}'.split(','))}")
    private Set<Symbol> symbolSet;

    /** MarketData読み込みのByおまとめモード */
    @Value("#{T(ny2.ats.database.kdb.KdbByFunctionType).valueOf('${historical.tester.byfunction}')}")
    private KdbByFunctionType byFunctionType;

    /** byおまとめの単位 */
    @Value("${historical.tester.bycount:0}")
    private int byCount;

    /** 初期データを読み込むかどうか(default=true) */
    @Value("${historical.tester.loadInitial}")
    private boolean loadInitial;

    /** RTデータをディスクに書き込むかどうか */
    @Value("${historical.tester.writedisk}")
    private boolean writeDisk;

    /** 取引開始時刻(月曜) */
    @Value("#{T(java.time.LocalTime).parse('${historical.trade.startmonday}')}")
    private LocalTime startTradeMonday;

    /** 取引終了時刻(土曜・夏) */
    @Value("#{T(java.time.LocalTime).parse('${historical.trade.stopsaturday.summer}')}")
    private LocalTime stopTradeSaturdaySummer;

    /** 取引終了時刻(土曜・冬) */
    @Value("#{T(java.time.LocalTime).parse('${historical.trade.stopsaturday.winter}')}")
    private LocalTime stopTradeSaturdayWinter;

    //
    // Indicator
    //
    /** Indicatorデータ保持日数(マイナスの場合は削除しない) */
    @Value("${historical.indicator.holddays}")
    private int indicatorDataHoldDays;

    /** Indicator作成対象Symbol(Historicalテスト対象symbolにあわせる) */
    @Value("#{T(ny2.ats.core.common.Symbol).valueOfStringArray('${historical.tester.symbols}'.split(','))}")
    private Set<Symbol> indicatorSymbols;

    /** Indicator作成対象Type */
    @Value("#{T(ny2.ats.indicator.IndicatorType).valueOfStringArray('${historical.indicator.indicatortypes}'.split(','))}")
    private Set<IndicatorType> indicatorTypes;

    /** Indicator作成対象Period (Time) */
    @Value("#{T(ny2.ats.core.common.Period).valueOfStringArray('${historical.indicator.periodtimes}'.split(','))}")
    private Set<Period> indicatorPeriodTimes;

    /** Indicator作成対象Period (Tick) */
    @Value("#{T(ny2.ats.core.common.Period).valueOfStringArray('${historical.indicator.periodticks}'.split(','))}")
    private Set<Period> indicatorPeriodTicks;


    //
    // Model
    //

    // Model1
    @Value("${historical.tester.model1:}")
    private String model1;
    @Value("#{'${historical.tester.versions1:}'.split(',')}")
    private String[] versions1;

    // Model2
    @Value("${historical.tester.model2:}")
    private String model2;
    @Value("#{'${historical.tester.versions2:}'.split(',')}")
    private String[] versions2;

    // Model3
    @Value("${historical.tester.model3:}")
    private String model3;
    @Value("#{'${historical.tester.versions3:}'.split(',')}")
    private String[] versions3;


    // Model4 & Model5 - IndicatorTradeModel Sub-Classes
    // Model4
    @Value("${historical.tester.model4:}")
    private String model4;
    @Value("#{'${historical.tester.versions4:}'.split(',')}")
    private String[] versions4;

    // Model5
    @Value("${historical.tester.model5:}")
    private String model5;
    @Value("#{'${historical.tester.versions5:}'.split(',')}")
    private String[] versions5;


    //
    // Initial Data
    //
    @Value("${kdb.bt.markettype}")
    private MarketType initialMarketType;

    @Value("${historical.tester.usdjpy}")
    private double initialUSDJPY;
    @Value("${historical.tester.eurjpy}")
    private double initialEURJPY;
    @Value("${historical.tester.eurusd}")
    private double initialEURUSD;
    @Value("${historical.tester.gbpusd}")
    private double initialGBPUSD;
    @Value("${historical.tester.audusd}")
    private double initialAUDUSD;


    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    /**
     * 全てのセットアップを行います。個別のセットアップと重複して実行しないようにしてください
     */
    public void setupAll() {
        // Information表示
        showHistoricalTestInformation();

        // Indicator設定
        setupIndicator();

        // デプロイモデル
        deployModels();

        // Position換算データ
        setupInitilaData();
    }

    /**
     * Indicator作成対象を上書き設定します
     */
    public void setupIndicator() {
        logger.info("Indicator: Symbol = {}", indicatorSymbols);
        logger.info("Indicator: IndicatorType = {}", indicatorTypes);
        logger.info("Indicator: Period Times = {}", indicatorPeriodTimes);
        logger.info("Indicator: Period Ticks = {}", indicatorPeriodTicks);
        logger.info("Indicator: Data Hold Days = [{}]", indicatorDataHoldDays);
        indicatorDataHolder.initialize(indicatorSymbols, indicatorTypes, indicatorPeriodTimes, indicatorPeriodTicks);
    }

    /**
     * 設定ファイルにしたがってモデルをデプロイします
     * 設定にブランクがあるものは無視します
     */
    public void deployModels() {
        // Model 1,2,3 - Normal Model
        if (!StringUtils.isBlank(model1) && versions1.length > 0) {
            deployModel(model1, versions1);
        }
        if (!StringUtils.isBlank(model2) && versions2.length > 0) {
            deployModel(model2, versions2);
        }
        if (!StringUtils.isBlank(model3) && versions3.length > 0) {
            deployModel(model3, versions3);
        }

        // Model 4,5 - IndicatorTradeModel
        if (!StringUtils.isBlank(model4) && versions4.length > 0) {
            deployIndicatorTradeModel(model4, versions4);
        }
        if (!StringUtils.isBlank(model5) && versions5.length > 0) {
            deployIndicatorTradeModel(model5, versions5);
        }
    }

    private void deployModel(String modelTypeStr, String[] modelVetrsions) {
        for (Symbol symbol : indicatorSymbols) {
            for (String versionStr : modelVetrsions) {
                logger.info(modelFactory.deployModelJMX(modelTypeStr, versionStr, symbol.name()));
            }
        }
    }

    private void deployIndicatorTradeModel(String modelClassName, String[] vetrsions) {
        for (Symbol symbol : indicatorSymbols) {
            for (String versionName : vetrsions) {
                logger.info(modelFactory.deployIndicatorTradeModelJMX(modelClassName, versionName, symbol.name()));
            }
        }
    }

    /**
     * Position換算用の初期データを設定します
     */
    public void setupInitilaData() {
        List<MarketData> initialDataList = new ArrayList<>();
        initialDataList.add(createMarketDataForInitialize(Symbol.USDJPY, initialUSDJPY));
        initialDataList.add(createMarketDataForInitialize(Symbol.EURJPY, initialEURJPY));
        initialDataList.add(createMarketDataForInitialize(Symbol.EURUSD, initialEURUSD));
        initialDataList.add(createMarketDataForInitialize(Symbol.GBPUSD, initialGBPUSD));
        initialDataList.add(createMarketDataForInitialize(Symbol.AUDUSD, initialAUDUSD));

        // log出力用
        StringBuilder sb = new StringBuilder("[");
        for (MarketData marketData : initialDataList) {
            sb.append(marketData.getSymbol()).append("-").append(marketData.getMidPrice()).append(", ");
        }
        sb.delete(sb.length() - 2, sb.length()).append("]");
        logger.info("Position Conversion Initial Data to PositionManager/ModelManager : {}", sb.toString());

        // Position計算のみに使用するため、対象サービスを限定する
        for (MarketData marketData : initialDataList) {
            MarketUpdateEvent event = new MarketUpdateEvent(uuid, getClass(), marketData);
            positionManager.onEvent(event);
            modelManager.onEvent(event);
        }
    }

    private MarketData createMarketDataForInitialize(Symbol symbol, double price) {
        return new MarketData(initialMarketType,
                symbol,
                String.valueOf(0),
                price,
                price,
                0,
                0,
                true,
                LocalDateTime.of(startDate, LocalTime.of(0, 0)));
    }

    /**
     * Historicalテスト実行条件を表示します
     */
    public void showHistoricalTestInformation() {
        // 日付/symbol
        logger.info("Test Dates = [{} ~ {}]", startDate, endDate);
        logger.info("Test Symbols = {}", symbolSet.toString());

        // By条件
        if (byFunctionType == KdbByFunctionType.UNUSED) {
            logger.info("Use by mode = [{}]", byFunctionType);
        } else {
            logger.info("Use by mode = [{}, {}]", byFunctionType, byCount);
        }

        // 初期データロード
        logger.info("Load initial data = [{}]", loadInitial);

        // kdb書き込み
        logger.info("kdb historical writing mode = [{}]", writeDisk);

        // 執行モード
        logger.info("Order ExecutionMode = [{}]", orderManager.getExecutionMode().name());

        // 取引時刻
        logger.info("Trade Time = [MONDAY-{}, SATURDAY-{}/{}]", startTradeMonday, stopTradeSaturdaySummer, stopTradeSaturdayWinter);
    }

}
