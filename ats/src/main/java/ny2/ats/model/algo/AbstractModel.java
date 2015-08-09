package ny2.ats.model.algo;

import java.time.LocalDateTime;

import org.slf4j.Logger;

import com.udojava.jmx.wrapper.JMXBean;
import com.udojava.jmx.wrapper.JMXBeanAttribute;
import com.udojava.jmx.wrapper.JMXBeanOperation;

import ny2.ats.core.common.OrderAction;
import ny2.ats.core.common.OrderStatus;
import ny2.ats.core.common.OrderType;
import ny2.ats.core.common.Side;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.data.MarketData;
import ny2.ats.core.data.ModelInformation;
import ny2.ats.core.data.Order;
import ny2.ats.core.data.PLInformation;
import ny2.ats.core.data.SystemInformation;
import ny2.ats.core.exception.ModelInitializeException;
import ny2.ats.market.connection.MarketType;
import ny2.ats.model.IModel;
import ny2.ats.model.IModelIndicatorDataHolder;
import ny2.ats.model.IModelManager;
import ny2.ats.model.ModelPositionHolder;
import ny2.ats.model.ModelStatus;

/**
 * 基本的なモデルの抽象クラスです
 * 通常のモデルはこのクラスを拡張します。
 */
@JMXBean
public abstract class AbstractModel implements IModel {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // Logger
    protected final Logger logger;

    /** モデルマネージャー（逆参照） */
    protected final IModelManager modelManager;

    /** インディケーター情報 */
    protected final IModelIndicatorDataHolder modelIndicatorDataHolder;

    /** モデルのポジション管理 */
    protected final ModelPositionHolder modelPositionHolder;

    /** 稼動状態 */
    protected volatile boolean isRunning = false;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public AbstractModel(IModelManager modelManager, Logger logger) {
        this.logger = logger;
        this.modelManager = modelManager;
        this.modelIndicatorDataHolder = modelManager.getModelIndicatorDataHolder();
        this.modelPositionHolder = modelManager.createPositionHolder();
    }

    /**
     * モデルのファクトリーメソッド
     * JMXで呼ばれることを想定して、Stringを引数とする
     * @throws ModelInitializeException 引数に対応するVersionが存在しない場合など
     */
    //public abstract IModel createInstance(IModelManager modelManager, String modelVersionStr, Symbol symbol) throws ModelInitializeException;

    // //////////////////////////////////////
    // Method @Override
    // //////////////////////////////////////

    @Override
    public void onSystemEvent(SystemInformation systemInformation) {
        switch (systemInformation.getInfromationType()) {
            case MODEL_START:
                startModel();
                break;
            case MODEL_STOP:
                stopModel();
                break;
            case WEEK_START:
                // Do nothing
                break;
            case WEEK_END:
                onWeekend();
                break;
            default:
                break;
        }
    }

    /**
     * モデルを開始します
     */
    protected void startModel() {
        logger.info("{} Start Model.", getDisplayName());
        isRunning = true;
    }

    /**
     * モデルを停止します
     */
    protected void stopModel() {
        logger.info("{} Stop Model.", getDisplayName());
        isRunning = false;
    }

    /**
     * WEEK_ENDイベントを受信した際の処理を定義します。必要に応じてOverrideしてください。
     */
    protected void onWeekend() {
        // default - stop model
        stopModel();
    }

    @Override
    public ModelStatus getModelStatus() {
        if (isRunning) {
            return ModelStatus.RUNNING;
        } else {
            return ModelStatus.STOPPED;
        }
    }

    @Override
    @JMXBeanAttribute
    public int getPl() {
        return modelPositionHolder.getTotalPlJpy();
    }

    @Override
    public String getDisplayName() {
        StringBuilder sb = new StringBuilder();
        sb.append('[')
                .append(getModelType().name())
                .append('/')
                .append(getModelVersion().getName())
                .append(']');
        return sb.toString();
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    /**
     * 新規のMarketオーダーを作成します
     */
    protected Order createOpenMarketOrder(MarketData marketData, Symbol symbol, Side side, int amount) {
        Order order = Order.createNewOrderBuilder(
                marketData.getMarketType(),
                getModelType(),
                getModelVersion(),
                marketData.getQuoteId(),
                symbol,
                side,
                OrderType.MARKET,
                marketData.getPrice(side.getOpenBidAsk()),
                amount).createInstance();
        return order;
    }

    /**
     * 決済のMarketオーダーを作成します
     */
    protected Order createCloseMarketOrder(MarketData marketData, Order originalOrder) {
        Order order = Order.createNewOrderBuilderAsClose(
                marketData.getMarketType(),
                getModelType(),
                getModelVersion(),
                marketData.getQuoteId(),
                originalOrder.getSymbol(),
                originalOrder.getSide().getReverseSide(),
                OrderType.MARKET,
                marketData.getPrice(originalOrder.getSide().getCloseBidAsk()),
                originalOrder.getOrderAmount(),
                originalOrder.getOrderId(),
                originalOrder.getMarketPositionId()).createInstance();
        return order;
    }

    /**
     * ダミーの執行済みオーダーを作成します
     */
    protected Order createDummyOrder(MarketType marketType, Symbol symbol, Side side, double price, int amount) {
        Order order = Order.createNewOrderBuilder(
                marketType,
                getModelType(),
                getModelVersion(),
                "0",
                symbol,
                side,
                OrderType.MARKET,
                price,
                amount)
                .setOrderAction(OrderAction.FILL)
                .setOrderStatus(OrderStatus.FILLED)
                .setExecutePrice(price)
                .setExecuteAmount(amount)
                .createInstance();
        return order;
    }

    /**
     * PL更新および、ModelのPLInforamtionを作成して送信します
     *
     * @param reportDateTime 作成基準日時
     */
    protected void updatePLAndSendPLInformation(LocalDateTime reportDateTime) {
        // PL更新
        modelPositionHolder.updatePl();
        // PL情報送信
        PLInformation plInformation = new PLInformation(PLInformation.PLInformationType.MODEL,
                getModelType(), getModelVersion(),
                modelPositionHolder.getTotalPlJpy(),
                modelPositionHolder.getAllPlJpy(),
                modelPositionHolder.getNetAmountAll(),
                reportDateTime,
                modelIndicatorDataHolder.getMarketDataMap());
        modelManager.sendPLInformation(plInformation);
    }

    /**
     * ModelInformationを作成して送信します<br>
     * information1にはModelParamsの値を設定します
     *
     * @param information2
     * @param information3
     * @param information4
     * @param information5
     * @param reportDateTime
     */
    public void sendModelInfomation(String information2, String information3, String information4, String information5, LocalDateTime reportDateTime) {
        ModelInformation modelInformation = new ModelInformation(
                getModelType(),
                getModelVersion(),
                ModelInformation.ModelInfromationType.INFORMATION,
                getModelParams(),
                information2,
                information3,
                information4,
                information5,
                reportDateTime);
        // モデル情報を送信
        modelManager.sendModelInformation(modelInformation);
    }

    /**
     * 最新のMarketDataを返します
     * @param symbol
     * @return
     */
    public MarketData getLastMarketData(Symbol symbol) {
        return modelIndicatorDataHolder.getMarketData(symbol);
    }

    // //////////////////////////////////////
    // Method For JMX
    // //////////////////////////////////////

    @Override
    @JMXBeanAttribute
    public String getRunningStatus() {
        return getModelStatus().name();
    }

    @Override
    @JMXBeanOperation
    public String showAllPosition() {
        return "[All Position]\n" + modelPositionHolder.showAllPosition();
    }

}
