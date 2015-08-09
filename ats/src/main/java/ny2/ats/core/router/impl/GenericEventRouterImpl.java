package ny2.ats.core.router.impl;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import ny2.ats.core.event.EventType;
import ny2.ats.core.event.ExecutionInformationEvent;
import ny2.ats.core.event.IEvent;
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

/**
 * 非同期型の Event Router です
 */
// @Service("GenericEventRouter") -> xml
@ManagedResource(objectName="EventRouter:name=GenericEventRouter")
public class GenericEventRouterImpl extends AbstraceEventRouter {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // Logger -> super class

    // wait time for add queue
    private static final int WAIT_MILISEC = 20;

    //
    // Queue
    //
    /** 全てのQueueのMap */
    private final Map<EventType, Queue<?>> queueMap = new EnumMap<>(EventType.class);

    /** EventQueue(MarketUpdateEvent) */
    private final BlockingQueue<MarketUpdateEvent> marketUpdateQueue = new LinkedBlockingQueue<>(10000);

    /** EventQueue(NewOrderEvent) */
    private final BlockingQueue<MarketOrderEvent> marketOrderQueue = new LinkedBlockingQueue<>(1000);

    /** EventQueue(OrderUpdateEvent) */
    private final BlockingQueue<OrderUpdateEvent> orderUpdateQueue = new LinkedBlockingQueue<>(1000);

    /** EventQueue(PositionUpdateEvent) */
    private final BlockingQueue<PositionUpdateEvent> positionUpdateQueue = new LinkedBlockingQueue<>(1000);

    /** EventQueue(PositionUpdateEvent) */
    private final BlockingQueue<IndicatorUpdateEvent> indicatorUpdateQueue = new LinkedBlockingQueue<>(1000);

    /** EventQueue(ModelInformationEvent) */
    private final BlockingQueue<ModelInformationEvent> modelInformationQueue = new LinkedBlockingQueue<>(1000);

    /** EventQueue(PLInformationEvent) */
    private final BlockingQueue<PLInformationEvent> plInformationQueue = new LinkedBlockingQueue<>(1000);

    /** EventQueue(ExecutionInformationEvent) */
    private final BlockingQueue<ExecutionInformationEvent> executionInfromationEventQueue = new LinkedBlockingQueue<>(1000);

    /** EventQueue(SystemInformationEvent) */
    private final BlockingQueue<SystemInformationEvent> systemInformationQueue = new LinkedBlockingQueue<>(1000);

    /** EventQueue(TimerEvent) */
    private final BlockingQueue<TimerInformationEvent> timerInformationQueue = new LinkedBlockingQueue<>(1000);


    // Executer
    /** EventQueue用のExecuter */
    private ExecutorService queueExecutor;

    /** Status Checker*/
    private ScheduledExecutorService statusChecker = Executors.newSingleThreadScheduledExecutor();

    /** Listenerへの event dispatch用のExecuter */
    private ThreadPoolExecutor eventDispatchExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(30);

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public GenericEventRouterImpl() {
        super(LoggerFactory.getLogger(GenericEventRouterImpl.class));
        logger.info("Create instance.");

        // Queue Map 作成
        queueMap.put(EventType.MARKET_UPDATE, marketUpdateQueue);
        queueMap.put(EventType.MARKET_ORDER, marketOrderQueue);
        queueMap.put(EventType.ORDER_UPDATE, orderUpdateQueue);
        queueMap.put(EventType.POSITION_UPDATE, positionUpdateQueue);
        queueMap.put(EventType.INDICATOR_UPDATE, indicatorUpdateQueue);
        queueMap.put(EventType.MODEL_INFORMATION, modelInformationQueue);
        queueMap.put(EventType.PL_INFORMATION, plInformationQueue);
        queueMap.put(EventType.EXECUTION_INFORMATION, executionInfromationEventQueue);
        queueMap.put(EventType.SYSTEM_INFORMATION, systemInformationQueue);
        queueMap.put(EventType.TIMER_INFORMATION, timerInformationQueue);
    }

    @PostConstruct
    private void init() {
        logger.info("PostConstruct instance.");

        // Start Executor Thread
        startThread();
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public void addEvent(MarketUpdateEvent event) {
        try {
            marketUpdateQueue.offer(event, WAIT_MILISEC, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.error("", e);
        }
    }

    @Override
    public void addEvent(MarketOrderEvent event) {
        try {
            marketOrderQueue.offer(event, WAIT_MILISEC, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.error("", e);
        }
    }

    @Override
    public void addEvent(OrderUpdateEvent event) {
        try {
            orderUpdateQueue.offer(event, WAIT_MILISEC, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.error("", e);
        }
    }

    @Override
    public void addEvent(PositionUpdateEvent event) {
        try {
            positionUpdateQueue.offer(event, WAIT_MILISEC, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.error("", e);
        }
    }

    @Override
    public void addEvent(IndicatorUpdateEvent event) {
        try {
            indicatorUpdateQueue.offer(event, WAIT_MILISEC, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.error("", e);
        }
    }

    @Override
    public void addEvent(ModelInformationEvent event) {
        try {
            modelInformationQueue.offer(event, WAIT_MILISEC, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.error("", e);
        }
    }

    @Override
    public void addEvent(PLInformationEvent event) {
        try {
            plInformationQueue.offer(event, WAIT_MILISEC, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.error("", e);
        }
    }

    @Override
    public void addEvent(ExecutionInformationEvent event) {
        try {
            executionInfromationEventQueue.offer(event, WAIT_MILISEC, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.error("", e);
        }
    }

    @Override
    public void addEvent(TimerInformationEvent event) {
        try {
            timerInformationQueue.offer(event, WAIT_MILISEC, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.error("", e);
        }
    }

    @Override
    public void addEvent(SystemInformationEvent event) {
        try {
            systemInformationQueue.offer(event, WAIT_MILISEC, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.error("", e);
        }
    }

    /**
     * threadを開始する
     */
    public void startThread() {
        // Threadを開始する
        queueExecutor = Executors.newFixedThreadPool(10);
        queueExecutor.execute(new QueueCrawler<>(marketUpdateQueue, marketUpdateEventListeners, MarketUpdateEvent.class.getSimpleName()));
        queueExecutor.execute(new QueueCrawler<>(marketOrderQueue, marketOrderEventListeners, MarketOrderEvent.class.getSimpleName()));
        queueExecutor.execute(new QueueCrawler<>(orderUpdateQueue, orderUpdateEventListeners, OrderUpdateEvent.class.getSimpleName()));
        queueExecutor.execute(new QueueCrawler<>(positionUpdateQueue, positionUpdateEventListeners, PositionUpdateEvent.class.getSimpleName()));
        queueExecutor.execute(new QueueCrawler<>(indicatorUpdateQueue, indicatorUpdateEventListeners, IndicatorUpdateEvent.class.getSimpleName()));
        queueExecutor.execute(new QueueCrawler<>(modelInformationQueue, modelInformationEventListeners, ModelInformationEvent.class.getSimpleName()));
        queueExecutor.execute(new QueueCrawler<>(plInformationQueue, plInformationEventListeners, PLInformationEvent.class.getSimpleName()));
        queueExecutor.execute(new QueueCrawler<>(executionInfromationEventQueue, executionInfromationEventListeners, ExecutionInformationEvent.class.getSimpleName()));
        queueExecutor.execute(new QueueCrawler<>(timerInformationQueue, timerInformationEventListeners, TimerInformationEvent.class.getSimpleName()));
        queueExecutor.execute(new QueueCrawler<>(systemInformationQueue, systemInformationEventListeners, SystemInformationEvent.class.getSimpleName()));

        // Status Checker
        statusChecker.scheduleAtFixedRate(() -> {
            try {
                // Executer
                logger.info("Executor status check. queueExecutor : " + queueExecutor.toString());
                logger.info("Executor status check. eventDispatchExecutor : " + eventDispatchExecutor.toString());
                // Queue
                logger.info("Queue size check. marketUpdateQueue      : " + marketUpdateQueue.size());
                logger.info("Queue size check. marketOrderQueue       : " + marketOrderQueue.size());
                logger.info("Queue size check. orderUpdateQueue       : " + orderUpdateQueue.size());
                logger.info("Queue size check. positionUpdateQueue    : " + positionUpdateQueue.size());
                logger.info("Queue size check. indicatorUpdateQueue   : " + indicatorUpdateQueue.size());
                logger.info("Queue size check. modelInformationQueue  : " + modelInformationQueue.size());
                logger.info("Queue size check. plInformationQueue     : " + plInformationQueue.size());
                logger.info("Queue size check. executionInfromationEventQueue : " + executionInfromationEventQueue.size());
                logger.info("Queue size check. timerInformationQueue  : " + timerInformationQueue.size());
                logger.info("Queue size check. systemInformationQueue : " + systemInformationQueue.size());
            } catch(Throwable t) {
                logger.error("", t);
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    /**
     * threadを停止する
     */
    public void stopThread() {
        logger.warn("ShutdownNow all thread.");
        queueExecutor.shutdownNow();
    }

    // //////////////////////////////////////
    // Method - Utility
    // //////////////////////////////////////

    /**
     * Queueの滞留数をチェックします
     */
    public int checkTotalQueueSize() {
        int totalSize = 0;
        for (Queue<?> queue : queueMap.values()) {
            totalSize += queue.size();
        }
        return totalSize;
    }

    /**
     * ActiveThread数をチェックします。
     */
    public int checkActiveThread() {
        return eventDispatchExecutor.getActiveCount();
    }

    /**
     * Queueの滞留状況をチェックします
     */
    @ManagedOperation
    public String checkQueueStatus() {
        StringBuilder sb = new StringBuilder("[Queue Status]\n");
        for (Entry<EventType, Queue<?>> entry : queueMap.entrySet()) {
            sb.append(entry.getKey().name()).append(" : ")
                .append(entry.getValue().size()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Executorの状況チェックします。
     */
    @ManagedOperation
    public String checkExecutorStatus() {
        StringBuilder sb = new StringBuilder("[Executor Status]\n");
        sb.append("QueueExecutor : ").append(queueExecutor.toString());
        sb.append("EventExecutor : ").append(eventDispatchExecutor.toString());
        return sb.toString();
    }

    /**
     * EventListenerの一覧を表示します
     */
    @ManagedOperation
    public String showListeners() {
        StringBuilder sb = new StringBuilder("[Executor Status]\n");
        for (Entry<EventType, List<IEventListener>> entry : listenerMap.entrySet()) {
            sb.append(entry.getKey()).append(" : ");
            for (IEventListener eventListener : entry.getValue()) {
                sb.append(eventListener.getClass().getSimpleName()).append(", ");
            }
            sb.delete(sb.length() - 2, sb.length()).append("\n");
        }
        return sb.toString();
    }

    // //////////////////////////////////////
    // Inner Class
    // //////////////////////////////////////

    /**
     * 汎用のQueueCrawler。各キューを対象のリスナーに配布する。
     */
    class QueueCrawler<E extends IEvent<?>> implements Runnable {

        private BlockingQueue<E> queue;
        private List<IEventListener> eventListeners;
        private String threadName;

        public QueueCrawler(BlockingQueue<E> queue, List<IEventListener> eventListeners, String eventClassName) {
            this.queue = queue;
            this.eventListeners = eventListeners;
            this.threadName = QueueCrawler.class.toString() + "<" + eventClassName + ">";
        }

        @Override
        public void run() {
            logger.info("{} started.", threadName);
            while (true) {
                try {
                    E event = queue.take();
                    // 対象のリスナーにイベントを送信
                    for (int index = 0; index < eventListeners.size(); index++) {
                        final IEventListener listener = eventListeners.get(index);
                        eventDispatchExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    listener.onEvent(event);
                                } catch (Throwable t) {
                                    logger.error(threadName + " Thread で Error が発生しました", t);
                                }
                            }
                        });
                    }
                } catch (Throwable t) {
                    logger.error(threadName + " Thread で Error が発生しました", t);
                }
            }
        }
    }

}
