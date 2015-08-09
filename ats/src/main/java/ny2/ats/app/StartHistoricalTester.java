package ny2.ats.app;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ny2.ats.core.router.IEventRouter;
import ny2.ats.historical.HistoricalTesterSetup;
import ny2.ats.historical.IHistoricalTester;
import ny2.ats.information.ITimerManager;
import ny2.ats.market.connection.historical.HistoricalConnector;
import ny2.ats.market.order.impl.HistoricalOrderManagerImpl;
import ny2.ats.market.transport.impl.MarketManagerImpl;
import ny2.ats.model.impl.ModelManagerImpl;

/**
 * Entry Point for Historical(Back) Test
 */
public class StartHistoricalTester {

    private static final String SPRING_CONFIG_FILE = "classpath:applicationContextBacktest.xml";

    private static final Logger logger = LoggerFactory.getLogger(StartHistoricalTester.class);

    public static void main(String[] args) {
        // For rotating error log
        logger.error("START Application.");

        try {
            @SuppressWarnings("resource")
            ApplicationContext context = new ClassPathXmlApplicationContext(SPRING_CONFIG_FILE);

            // EventRouter設定
            IEventRouter eventRouter = context.getBean(IEventRouter.class);
            eventRouter.trimListenerLists();

            // Indicatorの計算対象設定
            // IIndicatorDataHolder indicatorDataHolder = context.getBean(IIndicatorDataHolder.class);
            // initializeは別途行う


            //
            // 以下 Historical Test 専用設定
            //

            // marketレイヤーのインスタンス入れえ(ConnectorとOrderManager)
            MarketManagerImpl marketManager = context.getBean(MarketManagerImpl.class);
            HistoricalConnector connector = context.getBean(HistoricalConnector.class);
            HistoricalOrderManagerImpl orderManager = context.getBean(HistoricalOrderManagerImpl.class);
            // marketManager.setupMarketForHistorical(MarketType.XXXX, connector, orderManager);

            // 通常のTimer停止
            ITimerManager timerEventGenerator = context.getBean(ITimerManager.class);
            timerEventGenerator.stopTimerForBacktest();

            // Model実行モード
            ModelManagerImpl modelManager = context.getBean(ModelManagerImpl.class);
            modelManager.setMultiThreadMode(false);

            // セットアップ
            HistoricalTesterSetup testerSetup = context.getBean(HistoricalTesterSetup.class);
            testerSetup.setupAll();

            // 特殊なデプロイが必要な場合
            //SetupOption.deployNoiseRangeModel(context, symbolSet);
            // SetupOption.deployIndicatorTradeModel(context, EnumSet.of(Symbol.USDJPY));

            //
            // 実行
            //

            // Historicalテスト開始
            logger.info("Start Test.");
            IHistoricalTester historicalTester = context.getBean(IHistoricalTester.class);
            historicalTester.startReplay();


        } catch (Exception e) {
            logger.error("", e);
        }
    }

}
