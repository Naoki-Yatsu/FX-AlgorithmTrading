package ny2.ats.database.impl;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.exxeleron.qjava.DateTime;
import com.exxeleron.qjava.QBasicConnection;
import com.exxeleron.qjava.QConnection;
import com.exxeleron.qjava.QDateTime;
import com.exxeleron.qjava.QException;
import com.exxeleron.qjava.QTable;
import com.exxeleron.qjava.QTable.Row;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.data.MarketData;
import ny2.ats.core.exception.ATSRuntimeException;
import ny2.ats.database.HistoricalDao;
import ny2.ats.database.kdb.KdbByFunctionType;
import ny2.ats.database.kdb.KdbQueryCreator;
import ny2.ats.database.kdb.KdbUtility;
import ny2.ats.historical.MarketDataList;
import ny2.ats.market.connection.MarketType;

@Repository("KdbHistoricalDao")
public class KdbHistoricalDao implements HistoricalDao {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // connection parameter
    @Value("${kdb.bt.host}")
    private String host;

    @Value("${kdb.bt.port}")
    private int port;

    @Value("${kdb.tp.rtport}")
    private int rtport;

    @Value("${kdb.username}")
    private String username;

    @Value("${kdb.password}")
    private String password;

    /** History Table */
    @Value("${kdb.table.history}")
    private String historyTable;

    /** MarketType of Historical Data */
    @Value("#{T(ny2.ats.market.connection.MarketType).valueOf('${kdb.bt.markettype}')}")
    private MarketType historicalMarketType;

    //
    // Others
    //
    /** select用のConnection */
    private QConnection connection;

    /** Date format (QDateTime -> LongString) */
    private final DateFormat formatQDataTime2Long = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    /** Date format (LocalDateTime -> LongString) for quote ID */
    private final DateTimeFormatter formatter2Long = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    /** IDの最後につける1文字(%10で使用) */
    private long IdAdditonal = 0;

    // //////////////////////////////////////
    // Field - Query
    // //////////////////////////////////////

    //
    // Query Table
    //  c             | t f a
    //  --------------| -----
    //  marketDateTime| z       or    timestamp | p
    //  sym           | s   p
    //  bid           | f
    //  ask           | f
    //
    /** Query for tick */
    @Value("${kdb.query.tick}")
    private String selectTableBase;

    /** Query by second $1:function, $2:seconds */
    @Value("${kdb.query.bysec}")
    private String selectTableBySec;

    /** Query by tick count $1:function, $2:count */
    @Value("${kdb.query.bytick}")
    private String selectTableByTick;

    //
    // Disk writing of realtime data
    //
    private static final String Q_WRITE = "writeAllTables[TPHOME]";
    private static final String Q_FINALIZE = "finalize[]";

    // //////////////////////////////////////
    // Constructor / setup
    // //////////////////////////////////////

    public KdbHistoricalDao() {
        logger.info("Create instance.");
    }

    @PostConstruct
    public void init() {
        logger.info("PostConstruct instance.");

        // 回数が少ないのでConnectionは都度作成する
        // createConnectionForSelect();
    }

    // //////////////////////////////////////
    // Method @@Override
    // //////////////////////////////////////

    @Override
    public List<MarketData> findMarketData(LocalDate fromdate, LocalDate todate) {
        String query = KdbQueryCreator.createQuery(selectTableBase, historyTable, fromdate, todate);
        logger.info("Qeury : Select historical MarketData. {} {}, all symbols \n{}", fromdate, todate, query);
        return selectHistoricalData(query);
    }

    @Override
    public List<MarketData> findMarketData(LocalDate fromdate, LocalDate todate, Set<Symbol> symbolSet) {
        String query = KdbQueryCreator.createQuery(selectTableBase, historyTable, fromdate, todate, symbolSet);
        logger.info("Qeury : Select historical Market Data. {} {}, {} \n{}", fromdate, todate, symbolSet, query);
        return selectHistoricalData(query);
    }

    @Override
    public List<MarketData> findMarketData(LocalDate fromdate, LocalDate todate, Set<Symbol> symbolSet, int byCount, KdbByFunctionType byFunctionType) {
        String query = null;
        switch (byFunctionType) {
            case FIRST_SEC:
            case LAST_SEC:
                String queryBase = String.format(selectTableBySec, byFunctionType.getFunction(), byCount);
                query = KdbQueryCreator.createQuery(queryBase, historyTable, fromdate, todate, symbolSet);
                break;
            case FIRST_TICK:
            case LAST_TICK:
                queryBase = String.format(selectTableByTick, byFunctionType.getFunction(), byCount);
                query = KdbQueryCreator.createQuery(queryBase, historyTable, fromdate, todate, symbolSet);
                break;
            case UNUSED:
                query = KdbQueryCreator.createQuery(selectTableBase, historyTable, fromdate, todate, symbolSet);
                break;
            default:
                throw new ATSRuntimeException("ByFunctionType is not selected. " + byFunctionType.name());
        }
        logger.debug("Qeury : Select historical Market Data. {} {}, {} \n{}", fromdate, todate, symbolSet, query);
        return selectHistoricalData(query);
    }

    @Override
    public MarketDataList findMarketDataList(LocalDate fromdate, LocalDate todate, Symbol symbol) {
        String query = KdbQueryCreator.createQuery(selectTableBase, historyTable, fromdate, todate, symbol);
        logger.info("Qeury : Select historical MarketData for List. {} {}, {} \n{}", fromdate, todate, symbol, query);
        return selectHistoricalDataList(query);
    }

    @Override
    public MarketDataList findMarketDataList(LocalDate fromdate, LocalDate todate, Symbol symbol, int byCount, KdbByFunctionType byFunctionType) {
        String query = null;
        switch (byFunctionType) {
            case FIRST_SEC:
            case LAST_SEC:
                String queryBase = String.format(selectTableBySec, byFunctionType.getFunction(), byCount);
                query = KdbQueryCreator.createQuery(queryBase, historyTable, fromdate, todate, symbol);
                break;
            case FIRST_TICK:
            case LAST_TICK:
                queryBase = String.format(selectTableByTick, byFunctionType.getFunction(), byCount);
                query = KdbQueryCreator.createQuery(queryBase, historyTable, fromdate, todate, symbol);
                break;
            case UNUSED:
                query = KdbQueryCreator.createQuery(selectTableBase, historyTable, fromdate, todate, symbol);
                break;
            default:
                throw new ATSRuntimeException("ByFunctionType is not selected. " + byFunctionType.name());
        }
        logger.debug("Qeury : Select historical MarketData for List. {} {}, {} \n{}", fromdate, todate, symbol, query);
        return selectHistoricalDataList(query);
    }

    @Override
    public void writeToDisk() {
        // ディスク書き込み
        logger.info("Write splayed table to Disk.");
        executeQuery(rtport, Q_WRITE);
    }

    @Override
    public void finalizeDisk() {
        // 作成した Splayed table の終了処理
        logger.info("Finalize splayed table.");
        executeQuery(rtport, Q_FINALIZE);
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    /**
     * データの検索を実行します。
     *
     * @param query
     * @return
     */
    private List<MarketData> selectHistoricalData(String query, Object... parameters) {
        QTable table = query(query, parameters);
        List<MarketData> dateList = new ArrayList<>(table.getRowsCount());
        for (Row row : table) {
            dateList.add(convertToData(row.toArray(), historicalMarketType));
        }
        return dateList;
    }

    /**
     * データの検索を実行します。
     *
     * @param query
     * @return
     */
    private MarketDataList selectHistoricalDataList(String query, Object... parameters) {
        QTable table = query(query, parameters);
        return convertToMarketDataList(table);
    }

    /**
     * Queryを実行します
     *
     * @param query
     * @param parameters
     * @return
     */
    private QTable query(String query, Object... parameters) {
        QConnection connection = createConnection();
        try {
            Object response = connection.sync(query, parameters);
            return (QTable) response;
        } catch (QException | IOException e) {
            logger.error("Error in query.", e);
            throw new ATSRuntimeException(e);
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * クエリの結果を MarketDataList に変換します
     * ※Symbolが1つの場合のみ
     *
     * @param table
     * @return
     */
    private MarketDataList convertToMarketDataList(QTable table) {
        int size = table.getRowsCount();
        Object[] data = table.getData();
        // col0: QDateTime
        QDateTime[] qDateTimes = (QDateTime[]) data[0];
        List<LocalDateTime> dateTimeList = new ArrayList<>(size);
        for (QDateTime qDateTime : qDateTimes) {
            dateTimeList.add(KdbUtility.convertToLocalDateTime(qDateTime));
        }
        // col1: sym
        String ccypair = ((String[]) data[1])[0];
        Symbol symbol = Symbol.valueOf(ccypair);
        // col2: bid
        DoubleList bidList = new DoubleArrayList((double[]) data[2]);
        // col3: ask
        DoubleList askList = new DoubleArrayList((double[]) data[3]);
        // mid
        double[] midArray = new double[size];
        for (int i = 0; i < size; i++) {
            midArray[i] = (bidList.getDouble(i) + askList.getDouble(i)) / 2;
        }
        DoubleList midList = new DoubleArrayList(midArray);

        // create MarketDataList
        MarketDataList marketDataList = new MarketDataList(symbol, dateTimeList, bidList, askList, midList);
        return marketDataList;
    }

    /**
     * q) meta selectByTick[...]
     *  c             | t f a
     *  --------------| -----
     *  marketDateTime| z
     *  sym           | s   p
     *  bid           | f
     *  ask           | f
     *
     * @param row
     * @param marketType
     * @return
     */
    private MarketData convertToData(Object[] row, MarketType marketType) {
        Symbol symbol = Symbol.valueOf((String) row[1]);
        LocalDateTime dateTime = KdbUtility.convertToLocalDateTime((DateTime) row[0]);
        return new MarketData(
                marketType,
                symbol,
                formatter2Long.format(dateTime)+ (IdAdditonal++ % 10),
                symbol.roundSubSubPips((double) row[2]),
                symbol.roundSubSubPips((double) row[3]),
                1_000_000,
                1_000_000,
                true,
                dateTime);
    }


    /**
     * Query to RT
     * @param port
     * @param query
     * @param parameters
     * @return
     */
    private Object executeQuery(int port, String query, final Object... parameters) {
        QBasicConnection con = openConnection(host, port, username, password);
        try {
            Object response = con.sync(query, parameters);
            return response;
        } catch (Exception e) {
            logger.error("Error in query. " + query, e);
        } finally {
            try {
                con.close();
            } catch (Exception e) {
            }
        }
        return null;
    }

    // //////////////////////////////////////
    // Method - Connection
    // //////////////////////////////////////

    private QConnection createConnection() {
        // すでに接続済みであればcloseする
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                // Do nothing.
            }
        }
        connection = openConnection(host, port, username, password);

        // Prepare query
        // try {
        // // Create Connection
        // connection = openConnection(host, port, username, password);
        // connection.sync(Q_SELECT_TICK_PREPARE);
        // } catch (QException | IOException e) {
        // logger.error("Error in query. " + Q_SELECT_TICK_PREPARE, e);
        // }
        return connection;
    }

    private QBasicConnection openConnection(String host, int port, String username, String password) {
        QBasicConnection connection = null;
        try {
            connection = new QBasicConnection(host, port, username, password);
            connection.open();
        } catch (QException | IOException e) {
            logger.error("Cannnot create connection. host=" + host + "port=" + port, e);
        }
        return connection;
    }

    private void closeConnection(QConnection connection) {
        if (connection == null) {
            return;
        }
        try {
            if (connection.isConnected()) {
                connection.close();
            }
        } catch (IOException e) {
            logger.error("Error in closing connection.", e);
        }
    }


    // //////////////////////////////////////
    // Test
    // //////////////////////////////////////

    public static void main(final String[] args) throws Exception {
        KdbHistoricalDao dao = new KdbHistoricalDao();
        dao.host = "192.168.10.212";
        dao.port = 5031;
        dao.username = "";
        dao.password = "";
        dao.historyTable = "FX";
        // dao.historyPipsTable = "FXPips";
        dao.testQuery();

//        Object res = dao.createConnection().sync(".z.P");
//        LocalDateTime localDateTime = KdbUtility.convertToLocalDateTime((DateTime) res);
//        System.out.println(localDateTime);
    }

    private void testQuery() throws Exception {

//        QDate date = QDate.fromString("2014.10.03");
//        QDate[] dates = {date, date };
//
//        Object response = q.sync("testFunc", "TrueFX", dates);
//        QTable table = (QTable) response;
//        System.out.println(table.toString());

        List<MarketData> dateList2 = findMarketData(LocalDate.of(2014, 1, 2), LocalDate.of(2014, 1, 2), EnumSet.of(Symbol.USDJPY, Symbol.EURUSD));
        dateList2.stream().forEach(data -> System.out.println(data.toStringSummary()));
    }
}
