package ny2.ats.database.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import ny2.ats.core.data.IData;
import ny2.ats.core.data.IndicatorInformation;
import ny2.ats.core.data.MarketData;
import ny2.ats.core.data.ModelInformation;
import ny2.ats.core.data.OptimizedExecution;
import ny2.ats.core.data.Order;
import ny2.ats.core.data.PLInformation;
import ny2.ats.core.data.Position;
import ny2.ats.core.data.SystemInformation;
import ny2.ats.core.data.TimerInformation;
import ny2.ats.core.event.EventType;
import ny2.ats.core.event.ExecutionInformationEvent;
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
import ny2.ats.core.exception.UnExpectedDataException;
import ny2.ats.core.router.IEventRouter;
import ny2.ats.database.EventDao;
import ny2.ats.database.IDBConnectionManager;

@Service
@ManagedResource(objectName="DatabaseService:name=DBManager")
public class DBConnectionManagerImpl implements IDBConnectionManager, IEventListener {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** Logger */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private IEventRouter eventRouter;

    @Autowired
    @Qualifier("KdbEventDao")
    private EventDao eventDao;

    /** Priceデータを保存するかどうか、バックテストでのみ必要に応じてfalseに設定します */
    @Value("${kdb.table.storeprice:true}")
    private boolean isStorePrice = true;

    /** Executer for Insert data to database */
    private ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    private ThreadPoolExecutor queueExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);

    // Event Storing Queue
    private BlockingQueue<IData> dataQueue = new LinkedBlockingQueue<>(100000);

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    @PostConstruct
    private void init() {
        logger.info("PostConstruct instance.");

        // Listener登録。全てのイベント
        eventRouter.registerListeners(EventType.EVNET_TYPE_ALL, this);

        scheduledExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    queueExecutor.execute(new BatchInsertWorker(dataQueue, eventDao));
                } catch (Throwable t) {
                    logger.error("Unexpected error had occured.", t);
                }
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public void onEvent(MarketUpdateEvent event) {
        if (isStorePrice) {
            dataQueue.add(event.getContent());
        }
    }

    @Override
    public void onEvent(MarketOrderEvent event) {
        dataQueue.add(event.getContent());
    }

    @Override
    public void onEvent(OrderUpdateEvent event) {
        dataQueue.add(event.getContent());
    }

    @Override
    public void onEvent(PositionUpdateEvent event) {
        dataQueue.add(event.getContent());
    }

    @Override
    public void onEvent(IndicatorUpdateEvent event) {
        dataQueue.add(event.getContent());
    }

    @Override
    public void onEvent(PLInformationEvent event) {
        dataQueue.add(event.getContent());
    }

    @Override
    public void onEvent(ExecutionInformationEvent event) {
        dataQueue.add(event.getContent());
    }

    @Override
    public void onEvent(ModelInformationEvent event) {
        dataQueue.add(event.getContent());
    }

    @Override
    public void onEvent(TimerInformationEvent event) {
        dataQueue.add(event.getContent());
    }

    @Override
    public void onEvent(SystemInformationEvent event) {
        dataQueue.add(event.getContent());
    }

    @Override
    public int checkdDataQueueSize() {
        return dataQueue.size();
    }

    @Override
    public int checkActiveThread() {
        return queueExecutor.getActiveCount();
    }

    /**
     * Queueの滞留状況をチェックします
     */
    @ManagedOperation
    public String checkQueueStatus() {
        StringBuilder sb = new StringBuilder("[Queue Status]\n");
        sb.append("Data Queue : ").append(dataQueue.size());
        return sb.toString();
    }

    /**
     * Executorの状況チェックします。
     */
    @ManagedOperation
    public String checkExecutorStatus() {
        StringBuilder sb = new StringBuilder("[Executor Status]\n");
        sb.append(scheduledExecutor.toString());
        sb.append(queueExecutor.toString());
        return sb.toString();
    }

    @ManagedAttribute
    public boolean isStorePrice() {
        return isStorePrice;
    }

    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////


    // //////////////////////////////////////
    // Inner Class
    // //////////////////////////////////////

    private class BatchInsertWorker implements Runnable {
        private EventDao eventDao;
        private BlockingQueue<IData> dataQueue;

        public BatchInsertWorker(BlockingQueue<IData> dataQueue, EventDao eventDao) {
            this.dataQueue = dataQueue;
            this.eventDao = eventDao;
        }

        @Override
        public void run() {
            // drain from queue and create each list
            int queueSize = dataQueue.size();
            List<IData> allDataList = new ArrayList<>(queueSize);
            dataQueue.drainTo(allDataList, queueSize);

            // each data list
            List<MarketData> marketDatas = new ArrayList<>(queueSize);
            List<Order> orders = new ArrayList<>();
            List<Position> positions = new ArrayList<>();
            List<IndicatorInformation> indicatorInformations = new ArrayList<>();
            List<ModelInformation> modelInformations = new ArrayList<>();
            List<PLInformation> plInformations = new ArrayList<>();
            List<OptimizedExecution> executionInformations = new ArrayList<>();
            List<SystemInformation> systemInformations = new ArrayList<>();
            List<TimerInformation> timerInformations = new ArrayList<>();

            try {
                // Create List
                for (IData data : allDataList) {
                    if (data instanceof MarketData) {
                        marketDatas.add((MarketData) data);

                    } else if (data instanceof Order) {
                        orders.add((Order) data);

                    } else if (data instanceof Position) {
                        positions.add((Position) data);

                    } else if (data instanceof IndicatorInformation) {
                        indicatorInformations.add((IndicatorInformation) data);

                    } else if (data instanceof ModelInformation) {
                        modelInformations.add((ModelInformation) data);

                    } else if (data instanceof PLInformation) {
                        plInformations.add((PLInformation) data);

                    } else if (data instanceof OptimizedExecution) {
                        executionInformations.add((OptimizedExecution) data);

                    } else if (data instanceof SystemInformation) {
                        systemInformations.add((SystemInformation) data);

                    } else if (data instanceof TimerInformation) {
                        timerInformations.add((TimerInformation) data);

                    } else {
                        throw new UnExpectedDataException(data.getClass());
                    }
                }

                // insert
                if (!marketDatas.isEmpty()) {
                    // logger.debug("Insert List : MarketData - size = " + marketDatas.size());
                    eventDao.insert(MarketData.class, marketDatas);
                }
                if (!orders.isEmpty()) {
                    // logger.debug("Insert List : Order - size = " + orders.size());
                    eventDao.insert(Order.class, orders);
                }
                if (!positions.isEmpty()) {
                    // logger.debug("Insert List : Position - size = " + positions.size());
                    eventDao.insert(Position.class, positions);
                }
                if (!indicatorInformations.isEmpty()) {
                    // logger.debug("Insert List : IndicatorInformation - size = " + indicatorInformations.size());
                    eventDao.insert(IndicatorInformation.class, indicatorInformations);
                }
                if (!modelInformations.isEmpty()) {
                    // logger.debug("Insert List : ModelInformation - size = " + modelInformations.size());
                    eventDao.insert(ModelInformation.class, modelInformations);
                }
                if (!plInformations.isEmpty()) {
                    // logger.debug("Insert List : PLInformation - size = " + plInformations.size());
                    eventDao.insert(PLInformation.class, plInformations);
                }
                if (!executionInformations.isEmpty()) {
                    // logger.debug("Insert List : OptimizedExecution - size = " + executionInformations.size());
                    eventDao.insert(OptimizedExecution.class, executionInformations);
                }
                if (!systemInformations.isEmpty()) {
                    // logger.debug("Insert List : SystemInformation - size = " + systemInformations.size());
                    eventDao.insert(SystemInformation.class, systemInformations);
                }
                if (!timerInformations.isEmpty()) {
                    // logger.debug("Insert List : TimerInformation - size = " + timerInformations.size());
                    eventDao.insert(TimerInformation.class, timerInformations);
                }

            } catch (Exception e) {
                logger.error("Error in inserting data.", e);
            }
        }
    }

}
