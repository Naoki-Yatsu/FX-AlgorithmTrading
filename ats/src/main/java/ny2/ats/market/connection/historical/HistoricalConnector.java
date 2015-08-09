package ny2.ats.market.connection.historical;

import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import ny2.ats.core.common.Symbol;
import ny2.ats.core.data.MarketData;
import ny2.ats.core.event.MarketUpdateEvent;
import ny2.ats.core.exception.ATSRuntimeException;
import ny2.ats.market.connection.IMarketConnector;
import ny2.ats.market.connection.MarketType;
import ny2.ats.market.order.impl.HistoricalOrderManagerImpl;
import ny2.ats.market.transport.IMarketManager;

/**
 * Historical Test 用の Connector です
 */
// @Component("HistoricalConnector") -> xml
public class HistoricalConnector implements IMarketConnector {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final UUID uuid = UUID.randomUUID();

    /** Market種別 */
    public static final MarketType MARKET_TYPE = MarketType.HISTORICAL;

    @Autowired
    private IMarketManager marketManager;

    @Autowired
    @Qualifier("HistoricalOrderManager")
    private HistoricalOrderManagerImpl orderManager;

    /** ログイン状態 */
    private boolean isLoggedIn = false;


    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public HistoricalConnector() {
        logger.info("Create instance.");
    }

    @PostConstruct
    public void init() {
        logger.info("PostConstruct instance.");
    }


    // //////////////////////////////////////
    // Method (@Override)
    // //////////////////////////////////////

    @Override
    public MarketType getMarketType() {
        return MARKET_TYPE;
    }

    @Override
    public void sendLogin() {
        logger.info("sendLogin.");
    }

    @Override
    public void sendLogout() {
        logger.info("sendLogout.");
    }

    @Override
    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    @Override
    public void sendNewOrder(Object orderObj) {
        // このメソッドは呼ばれないはず
        throw new ATSRuntimeException("想定外のメソッドが呼び出されました。");
    }

    @Override
    public void sendAmendOrder(Object amendObj) {
        // このメソッドは呼ばれないはず
        throw new ATSRuntimeException("想定外のメソッドが呼び出されました。");
    }

    @Override
    public void sendCanceldOrder(Object cancelObj) {
        // このメソッドは呼ばれないはず
        throw new ATSRuntimeException("想定外のメソッドが呼び出されました。");
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    /**
     * Replay用のデータセットアップを行います。
     * @param symbolList
     */
    public void setupForReplay(Set<Symbol> symbolSet) {
        // Order Manager 初期化
        orderManager.setup(symbolSet);
    }

    public void updateMarketData(MarketData marketData) {
        // 執行用データ更新、指値チェック (執行に使うためイベント送信よりも先に実行する)
        orderManager.updateMarketAndCheckLimitStop(marketData);
        // イベント送信
        marketManager.updateFromMarket(new MarketUpdateEvent(uuid, getClass(), marketData));
    }

}
