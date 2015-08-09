package ny2.ats.database;

public interface IDBConnectionManager {

    /**
     * データQueueのサイズを返します。
     * @return
     */
    public int checkdDataQueueSize();

    /**
     * データ登録スレッドのActive Countを返します。
     * @return
     */
    public int checkActiveThread();
}
