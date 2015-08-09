package ny2.ats.market.order;

import java.util.List;

import ny2.ats.core.data.MarketData;
import ny2.ats.core.data.OptimizedExecution;
import ny2.ats.core.data.Order;
import ny2.ats.market.connection.MarketType;

/**
 * 執行の最適化を行うクラスのインターフェースです。
 * TODO シングルトンではなく、各Marketごとに作るべきかも
 */
public interface IExecutionOptimizer {

    /**
     * オーダーが現在執行可能か判断します。執行できない場合は、内部のMapに待ちオーダーとして保持します
     * 執行可能な場合は、最適化執行情報を返します。
     *
     * @param order
     * @return OptimizedExecution
     */
    public OptimizedExecution checkExecuteNowOrHoldForWaiting(Order order);

    /**
     * マーケットデータを更新し、執行待ちオーダーの状況を確認します
     *
     * @param marketData
     * @return
     */
    public List<Order> updateMarketDataAndCheckWaiting(MarketData marketData);

//    /**
//     * Optimize処理を開始します
//     */
//    public void startOptimizer();
//
//    /**
//     * Optimize処理を停止します。週末、EODなどで使用します
//     */
//    public void stopOprimizer();

    /**
     * 執行最適化の判断に使用するMarketを取得します
     *
     * @return
     */
    public MarketType getUseMarketType();

}
