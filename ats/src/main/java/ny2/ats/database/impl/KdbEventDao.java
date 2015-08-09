package ny2.ats.database.impl;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.exxeleron.qjava.QBasicConnection;
import com.exxeleron.qjava.QException;

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
import ny2.ats.database.EventDao;
import ny2.ats.database.kdb.IKdbConverter;
import ny2.ats.database.kdb.IndicatorDetailKdbConverter;
import ny2.ats.database.kdb.IndicatorformationKdbConverter;
import ny2.ats.database.kdb.MarketDataKdbConverter;
import ny2.ats.database.kdb.MarketDataShortKdbConverter;
import ny2.ats.database.kdb.ModelInformationKdbConverter;
import ny2.ats.database.kdb.OptimizedExecutionKdbConverter;
import ny2.ats.database.kdb.OrderKdbConverter;
import ny2.ats.database.kdb.PLInformationKdbConverter;
import ny2.ats.database.kdb.PositionKdbConverter;
import ny2.ats.database.kdb.SystemInformationKdbConverter;
import ny2.ats.database.kdb.TimerInformationKdbConverter;

@Repository("KdbEventDao")
public class KdbEventDao implements EventDao {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // Logger
    private final Logger logger = LoggerFactory.getLogger(getClass());

    // connection parameter
    @Value("${kdb.tp.host}")
    protected String host;

    @Value("${kdb.tp.port}")
    protected int port;

    @Value("${kdb.tp.username}")
    protected String username;

    @Value("${kdb.tp.password}")
    protected String password;

    /** MarketDataのテーブル名(通常:MarketData、バックテスト:MarketDataShort) */
    @Value("${kdb.table.price}")
    protected String priceTable;


    /** insert function to ticker plant */
    private static final String Q_UPD = ".u.upd";

    // kdb converter
    // private MarketDataKdbConverter marketDataKdbConverter = new MarketDataKdbConverter();
    private MarketDataKdbConverter marketDataKdbConverter;
    private final OrderKdbConverter orderKdbConverter = new OrderKdbConverter();
    private final PositionKdbConverter positionKdbConverter = new PositionKdbConverter();
    private final IndicatorformationKdbConverter indicatorformationKdbConverter = new IndicatorformationKdbConverter();
    private final IndicatorDetailKdbConverter indicatorDetailKdbConverter = new IndicatorDetailKdbConverter();
    private final ModelInformationKdbConverter modelInformationKdbConverter = new ModelInformationKdbConverter();
    private final PLInformationKdbConverter plInformationKdbConverter = new PLInformationKdbConverter();
    private final OptimizedExecutionKdbConverter optimizedExecutionKdbConverter = new OptimizedExecutionKdbConverter();
    private final SystemInformationKdbConverter systemInformationKdbConverter = new SystemInformationKdbConverter();
    private final TimerInformationKdbConverter timerInformationKdbConverter = new TimerInformationKdbConverter();

    /** Executer for Insert data to database */
    // private ExecutorService queueExecutor = Executors.newSingleThreadExecutor();

    // Event Storing Queue
    // private BlockingQueue<IData> dataQueue = new LinkedBlockingQueue<>(100000);

    // Q connection
    private static final int CONNECTION_POOL_SIZE = 5;
    private BlockingQueue<QBasicConnection> connectionPool = new LinkedBlockingQueue<>(CONNECTION_POOL_SIZE);


    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public KdbEventDao() {
        logger.info("Create instance.");
    }

    @PostConstruct
    public void init() {
        logger.info("PostConstruct instance.");
        for (int i = 0; i < CONNECTION_POOL_SIZE; i++) {
            connectionPool.add(createConnection());
        }

        // set price table converter
        if (MarketDataShortKdbConverter.TABLE_NAME.equals(priceTable)) {
            marketDataKdbConverter = new MarketDataShortKdbConverter();
        } else {
            marketDataKdbConverter = new MarketDataKdbConverter();
        }
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public void insert(MarketData data) {
    }

    @Override
    public void insert(Order data) {
    }

    @Override
    public void insert(Position data) {
    }

    @Override
    public void insert(IndicatorInformation data) {
    }

    @Override
    public void insert(ModelInformation data) {
    }

    @Override
    public void insert(PLInformation data) {
    }

    @Override
    public void insert(OptimizedExecution data) {
    }

    @Override
    public void insert(TimerInformation data) {
    }

    @Override
    public void insert(SystemInformation data) {
    }

    @Override
    public <T extends IData> void insert(Class<T> clazz, List<T> dataList) {
        if (clazz == MarketData.class ) {
            insertList(dataList, marketDataKdbConverter);

        } else if (clazz == Order.class ) {
            insertList(dataList, orderKdbConverter);

        } else if (clazz == Position.class ) {
            insertList(dataList, positionKdbConverter);

        } else if (clazz == IndicatorInformation.class ) {
            insertList(dataList, indicatorformationKdbConverter);
            insertList(dataList, indicatorDetailKdbConverter);

        } else if (clazz == ModelInformation.class ) {
            insertList(dataList, modelInformationKdbConverter);

        } else if (clazz == PLInformation.class ) {
            insertList(dataList, plInformationKdbConverter);

        } else if (clazz == OptimizedExecution.class ) {
            insertList(dataList, optimizedExecutionKdbConverter);

        } else if (clazz == TimerInformation.class ) {
            insertList(dataList, timerInformationKdbConverter);

        } else if (clazz == SystemInformation.class ) {
            insertList(dataList, systemInformationKdbConverter);

        } else {
            logger.error("Unexpected class = " + clazz.getSimpleName());
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private <T extends IData> void insertList(List<T> dataList, IKdbConverter converter) {
        QBasicConnection qConnection = getConnection();
        try {
            qConnection.sync(Q_UPD, converter.getTableName(), converter.convert(dataList));
        } catch (Exception e) {
            logger.error("Error in inserting data " + converter.getTableName() + ". Reset connection just in case...", e);
            for (T data : dataList) {
                logger.error("Error Data : {}", data.toString());
            }
            try {
                qConnection.reset();
            } catch (IOException | QException e1) {
                logger.error("Error in reset connection", e);
            }
        } finally {
            // return connection to queue
            connectionPool.add(qConnection);
        }
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    public QBasicConnection getConnection() {
        try {
            QBasicConnection qConnection = connectionPool.take();

            // check connection
            while (!qConnection.isConnected()) {
                try {
                    qConnection.reset();
                    Thread.sleep(20);
                } catch (IOException | QException e) {
                    logger.error("", e);
                }
            };
            return qConnection;
        } catch (InterruptedException e) {
            logger.error("", e);
        }
        return null;
    }

    private QBasicConnection createConnection() {
        QBasicConnection qConnection = null;
        try {
            qConnection = new QBasicConnection(host, port, username, password);
            qConnection.open();
        } catch (QException | IOException e) {
            logger.error("Error in create connection.", e);
      }
        return qConnection;
    }


    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

    // //////////////////////////////////////
    // Inner Class
    // //////////////////////////////////////

//    class PublisherTask implements Runnable {
//        private final QConnection q;
//        boolean running = true;
//        public PublisherTask(final QConnection q) {
//            this.q = q;
//        }
//        public void stop() {
//            running = false;
//        }
//        public void run() {
//            while ( running ) {
//                try {
//                    q.sync(".u.upd", "ask", "");
//                } catch (final QException e1 ) {
//                    // q error, try again
//                    logger.error("", e1);
//                } catch ( final IOException e1 ) {
//                    // problem with connection
//                    running = false;
//                }
//            }
//        }
//    }

}


