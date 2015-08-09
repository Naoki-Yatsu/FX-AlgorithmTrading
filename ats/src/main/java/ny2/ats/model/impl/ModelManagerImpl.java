package ny2.ats.model.impl;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import ny2.ats.core.common.Period;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.data.IndicatorInformation;
import ny2.ats.core.data.MarketData;
import ny2.ats.core.data.ModelInformation;
import ny2.ats.core.data.Order;
import ny2.ats.core.data.PLInformation;
import ny2.ats.core.event.EventType;
import ny2.ats.core.event.IEventListener;
import ny2.ats.core.event.IndicatorUpdateEvent;
import ny2.ats.core.event.MarketOrderEvent;
import ny2.ats.core.event.MarketUpdateEvent;
import ny2.ats.core.event.ModelInformationEvent;
import ny2.ats.core.event.OrderUpdateEvent;
import ny2.ats.core.event.PLInformationEvent;
import ny2.ats.core.event.PositionUpdateEvent;
import ny2.ats.core.event.SystemInformationEvent;
import ny2.ats.core.event.TimerInformationEvent;
import ny2.ats.core.router.IEventRouter;
import ny2.ats.information.IJmxManager;
import ny2.ats.information.JmxDomainType;
import ny2.ats.market.connection.MarketType;
import ny2.ats.model.IModel;
import ny2.ats.model.IModelIndicatorDataHolder;
import ny2.ats.model.IModelManager;
import ny2.ats.model.ModelPositionHolder;

@Service
@ManagedResource(objectName="ModelService:name=ModelManager")
public class ModelManagerImpl implements IModelManager, IEventListener {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // Logger
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** インスタンス識別用のUUID */
    private final UUID uuid = UUID.randomUUID();

    // @Autowired
    // private ApplicationContext applicationContext;

    @Autowired
    private IEventRouter eventRouter;

    @Autowired
    private IJmxManager jmxManager;

    @Autowired
    private IModelIndicatorDataHolder modelIndicatorDataHolder;


    // Model関連
    /** 処理対象通貨ペアのセット  */
    private final Set<Symbol> targetSymbolSet = EnumSet.noneOf(Symbol.class);

    /** 全モデル・InstanceのSet */
    private final Set<IModel> allModelSet = new HashSet<>();

    /** MarketUpdateイベント受信モデルのMap  */
    private final Map<Symbol, Set<IModel>> marketDataListenerMap = new EnumMap<>(Symbol.class);

    /** Indicatorイベント受信モデルのMap */
    private final Map<Symbol, Set<IModel>> indicatorListenerMap = new EnumMap<>(Symbol.class);

    /** Timerイベント受信モデル */
    private final Set<IModel> timerListenerSet = new HashSet<>();

    // その他
    /** Model処理e実行用のExecuter */
    private final ExecutorService modelExecutor = Executors.newFixedThreadPool(10);

    /** モデルとMBeanの名前Map */
    private final Map<IModel, String> modelMBeanMap = new HashMap<>();

    /** ModelのMBeanの通番 */
    private final AtomicInteger mbeanNoCounter = new AtomicInteger(1);

    /** 動作モード */
    private boolean multiThreadMode = true;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    private ModelManagerImpl() {
        logger.info("Create instance.");
    }

    @PostConstruct
    private void init() {
        logger.info("PostConstruct instance.");

        // subscribe event
        eventRouter.registerListeners(EventType.EVNET_TYPE_CLIENT_ALL, this);
    }

    // //////////////////////////////////////
    // Method (IEventListener)
    // //////////////////////////////////////

    @Override
    public void onEvent(MarketUpdateEvent marketUpdateEvent) {
        MarketData marketData = marketUpdateEvent.getContent();

        // DataHolderに登録
        modelIndicatorDataHolder.updateMarketData(marketData);

        // 該当の通貨ペアを取り扱うモデルを取得
        Set<IModel> modelSet = marketDataListenerMap.get(marketData.getSymbol());
        // 受信モデルがあればデータを送信する。
        if (modelSet != null) {
            if (multiThreadMode) {
                for (IModel model : modelSet) {
                    modelExecutor.execute(() -> model.receiveMarketData(marketData));
                }
            } else {
                for (IModel model : modelSet) {
                    model.receiveMarketData(marketData);
                }
            }
        }
    }

    @Override
    public void onEvent(OrderUpdateEvent event) {
        // 対象のモデルににオーダーを送信
        Order order = event.getContent();
        allModelSet.stream()
            .filter(model -> model.getModelType() == order.getModelType() && model.getModelVersion() == order.getModelVersion())
            .forEach(model -> model.receiveOrderUpdate(order));
    }

    @Override
    public void onEvent(PositionUpdateEvent event) {
        // 対象のモデルににオーダーを送信
        // Position position = event.getContent();

        // 各モデルに許容ポジション量を通知

    }

    @Override
    public void onEvent(IndicatorUpdateEvent event) {
        IndicatorInformation indicatorInformation = event.getContent();

        // DataHolderに登録
        modelIndicatorDataHolder.updateIndicator(indicatorInformation);

        // Get target models
        Set<IModel> modelSet = indicatorListenerMap.get(indicatorInformation.getSymbol());
        // 受信モデルがあればデータを送信する。
        if (modelSet != null) {
            if (multiThreadMode) {
                for (IModel model : modelSet) {
                    modelExecutor.execute(() -> model.receiveIndicatorUpdate(indicatorInformation));
                }
            } else {
                for (IModel model : modelSet) {
                    model.receiveIndicatorUpdate(indicatorInformation);
                }
            }
        }
    }

    @Override
    public void onEvent(TimerInformationEvent event) {
        // 対象のモデルのメソッドを実行
        if (multiThreadMode) {
            for (IModel model : timerListenerSet) {
                modelExecutor.execute(() -> model.onTimer(event.getContent()));
            }
            // Show Executor Status
            if (event.getPeriod() == Period.MIN_5) {
                logger.info("Executor status. modelExecutor : {}", modelExecutor.toString());
            }

        } else {
            for (IModel model : timerListenerSet) {
                model.onTimer(event.getContent());
            }
        }
    }

    @Override
    public void onEvent(SystemInformationEvent event) {
        // 対象のモデルのメソッドを実行(パフォーマンス不要)
        allModelSet.forEach(model -> model.onSystemEvent(event.getContent()));
    }


    // //////////////////////////////////////
    // Method - Model Register - IModelManager
    // //////////////////////////////////////

    @Override
    public Set<IModel> getAllModels() {
        return allModelSet;
    }

    @Override
    public ModelPositionHolder createPositionHolder() {
        return new ModelPositionHolder(modelIndicatorDataHolder);
    }

    @Override
    public IModelIndicatorDataHolder getModelIndicatorDataHolder() {
        return modelIndicatorDataHolder;
    }

    @Override
    public synchronized void registerModelForMarketUpdate(IModel model, Symbol symbol) {
        // 対象Symbol用のQueueがあれば使い、なければ作成
        Set<IModel> modelSet = marketDataListenerMap.get(symbol);
        if (modelSet == null) {
            modelSet =  new HashSet<>();
            marketDataListenerMap.put(symbol, modelSet);
        }
        modelSet.add(model);

        // 一覧になければ追加(Set, Map)
        allModelSet.add(model);

        // 処理対象通貨ペア一覧を更新
        targetSymbolSet.add(symbol);
    }

    @Override
    public void registerModelForMarketUpdate(IModel model, List<Symbol> symbolList) {
        symbolList.stream().forEach(symbol -> registerModelForMarketUpdate(model, symbol));
    }

    @Override
    public void registerModelForMarketUpdate(IModel model, List<Symbol> symbolList, List<MarketType> marketList) {
        // TODO MarketTypeの区別をつける
        registerModelForMarketUpdate(model, symbolList);
    }

    @Override
    public synchronized void registerIndicator(IModel model, Symbol symbol) {
        // Symbol
        Set<IModel> modelSet = indicatorListenerMap.get(symbol);
        if (modelSet == null) {
            modelSet = new HashSet<>();
            indicatorListenerMap.put(symbol, modelSet);
        }
        modelSet.add(model);
    }

    @Override
    public synchronized void registerTimer(IModel model, Period period) {
        timerListenerSet.add(model);
    }


    // //////////////////////////////////////
    // Method - Send Event
    // //////////////////////////////////////

    @Override
    public void sendOrderToMarket(Order order) {
        MarketOrderEvent marketOrderEvent = new MarketOrderEvent(uuid, this.getClass(), order);
        eventRouter.addEvent(marketOrderEvent);
    }

    @Override
    public void sendModelInformation(ModelInformation modelInformation) {
        ModelInformationEvent event = new ModelInformationEvent(uuid, getClass(), modelInformation);
        eventRouter.addEvent(event);
    }

    @Override
    public void sendPLInformation(PLInformation plInformation) {
        PLInformationEvent event = new PLInformationEvent(uuid, getClass(), plInformation);
        eventRouter.addEvent(event);
    }


    // //////////////////////////////////////
    // Method JMX
    // //////////////////////////////////////

    /**
     * モデルの一覧を表示します。
     * @return
     */
    @ManagedOperation
    public String showModelList() {
        StringBuilder sb = new StringBuilder();
        for (IModel model : allModelSet) {
            sb.append(getShowDisplayHeadder(model)).append(model.getModelParams()).append("\n");
        }
        return sb.toString();
    }

    /**
     * モデルの状態を取得します。
     * @return
     */
    @ManagedOperation
    public String showModelStatus() {
        StringBuilder sb = new StringBuilder();
        for (IModel model : allModelSet) {
            sb.append(getShowDisplayHeadder(model)).append(model.getModelStatus().name()).append("\n");
        }
        return sb.toString();
    }

    /**
     * モデルのPL情報を取得します。
     * @return
     */
    @ManagedOperation
    public String showModelPL() {
        StringBuilder sb = new StringBuilder();
        for (IModel model : allModelSet) {
            sb.append(getShowDisplayHeadder(model)).append(model.getPl()).append("\n");
        }
        return sb.toString();
    }
    /**
     * モデルのPosition情報を取得します。
     * @return
     */
    @ManagedOperation
    public String showModelPosition() {
        StringBuilder sb = new StringBuilder();
        for (IModel model : allModelSet) {
            sb.append(getShowDisplayHeadder(model)).append("\n").append(model.showAllPosition()).append("\n");
        }
        return sb.toString();
    }

    /**
     * モデル情報のヘッダーを作成します
     */
    private String getShowDisplayHeadder(IModel model) {
        String mBeanName = modelMBeanMap.get(model);
        StringBuilder sb = new StringBuilder();
        sb.append(mBeanName)
                .append(" ")
                .append(model.getDisplayName())
                .append(" ");
        return sb.toString();
    }

    /**
     * モデルのインスタンスをJMX登録します。
     * @param model
     */
    @Override
    public void registerModelMBean(IModel model) {
        // MBeanの名前 - 文字数短縮のため"Model"の文字を省略する
        // 同一クラスで複数インスタンスがあるため通番をつける
        String name = model.getClass().getSimpleName().replaceAll("Model", "") + "/" + model.getModelVersion().getName() + "_" + mbeanNoCounter.getAndIncrement();
        // String name = model.getModelType() + "/" + model.getModelVersion().getName() + "_" + mbeanNoCounter.getAndIncrement();
        // JMX登録
        jmxManager.registerMBean(model, JmxDomainType.MODEL_SERVICE_MODEL, name);
        modelMBeanMap.put(model, name);
    }

    /**
     * モデル削除時にJMXのMBeanを削除します
     * @param model
     */
    @Override
    public void unregisterModelMBean(IModel model) {
        // MBeanの名前を取得
        String name = modelMBeanMap.get(model);
        if (name == null) {
            logger.error("Unregister MBean failed. MBean is not exist. {}", model.toString());
        }
        // Model一覧から削除
        allModelSet.remove(model);
        for (Entry<Symbol, Set<IModel>> symbolModelEntry : marketDataListenerMap.entrySet()) {
            if (symbolModelEntry.getValue() == model) {
                marketDataListenerMap.remove(symbolModelEntry.getKey());
            }
        }
        for (Entry<Symbol, Set<IModel>> symbolModelEntry : indicatorListenerMap.entrySet()) {
            if (symbolModelEntry.getValue() == model) {
                indicatorListenerMap.remove(symbolModelEntry.getKey());
            }
        }
        timerListenerSet.remove(model);

        // JMX削除
        jmxManager.unregisterMBean(JmxDomainType.MODEL_SERVICE_MODEL, name);
        modelMBeanMap.remove(model);
    }

    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

    @ManagedAttribute(description="Multi thread mode")
    public boolean isMultiThreadMode() {
        return multiThreadMode;
    }

    public void setMultiThreadMode(boolean multiThreadMode) {
        this.multiThreadMode = multiThreadMode;
    }

}
