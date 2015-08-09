package ny2.ats.database;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import ny2.ats.core.common.Symbol;
import ny2.ats.core.data.MarketData;
import ny2.ats.database.kdb.KdbByFunctionType;
import ny2.ats.historical.MarketDataList;

/**
 * Historicalデータの読み書きを行います。
 */
public interface HistoricalDao {

    // //////////////////////////////////////
    // Method - MarketData
    // //////////////////////////////////////

    /**
     * データを検索します。
     * @param fromdate
     * @param todate
     * @return
     */
    public List<MarketData> findMarketData(LocalDate fromdate, LocalDate todate);

    /**
     * Symbolを指定して、データを検索します。
     * @param fromdate
     * @param todate
     * @param symbolSet list of symbol
     * @return
     */
    public List<MarketData> findMarketData(LocalDate fromdate, LocalDate todate, Set<Symbol> symbolSet);

    /**
     * Symbolを指定して、結果を一定間隔でまとめてデータを検索します。
     * @param fromdate
     * @param todate
     * @param symbolList
     * @param byCount
     * @param byFunctionType
     * @return
     */
    public List<MarketData> findMarketData(LocalDate fromdate, LocalDate todate, Set<Symbol> symbolSet, int byCount, KdbByFunctionType byFunctionType);


    // //////////////////////////////////////
    // Method - MarketDataList
    // //////////////////////////////////////

    /**
     * Symbolを指定して、データを検索します。
     * @param fromdate
     * @param todate
     * @param symbol
     */
    public MarketDataList findMarketDataList(LocalDate fromdate, LocalDate todate, Symbol symbol);

    /**
     * Symbolを指定して、結果を一定間隔でまとめてデータを検索します。
     * @param fromdate
     * @param todate
     * @param symbol
     * @param byCount
     * @param byFunctionType
     * @return
     */
    public MarketDataList findMarketDataList(LocalDate fromdate, LocalDate todate, Symbol symbol, int byCount, KdbByFunctionType byFunctionType);

    // //////////////////////////////////////
    // Method - Disk
    // //////////////////////////////////////

    /**
     * メモリデータをディスクに書き込みます。
     */
    public void writeToDisk();

    /**
     * ディスクデータの最終処理を行います。
     */
    public void finalizeDisk();

}
