package ny2.ats.market.connection;


public interface IMarketConnector {

    /**
     * 対象のECNの情報を返す
     */
    public MarketType getMarketType();

    /**
     * ログインする
     */
    public void sendLogin();

    /**
     * ログアウトする
     */
    public void sendLogout();

    /**
     * ログイン状態を取得します。
     */
    public boolean isLoggedIn();

    /**
     * 新規オーダーを送信する
     * @param orderObj MarketごとのOrder-Object
     */
    public void sendNewOrder(Object orderObj);

    /**
     * オーダの変更を送信する
     * @param amendObj MarketごとのOrder-Object
     */
    public void sendAmendOrder(Object amendObj);

    /**
     * オーダの取り消しを送信する
     * @param cancelObj MarketごとのOrder-Object
     */
    public void sendCanceldOrder(Object cancelObj);

}
