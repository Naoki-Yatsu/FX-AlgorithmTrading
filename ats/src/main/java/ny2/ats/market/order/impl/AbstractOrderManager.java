package ny2.ats.market.order.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;

import ny2.ats.core.data.MarketData;
import ny2.ats.core.data.OptimizedExecution;
import ny2.ats.core.data.Order;
import ny2.ats.core.event.ExecutionInformationEvent;
import ny2.ats.market.order.IExecutionOptimizer;
import ny2.ats.market.order.IOrderManager;
import ny2.ats.market.transport.IMarketManager;

public abstract class AbstractOrderManager implements IOrderManager {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** インスタンス識別用のUUID */
    protected final UUID uuid = UUID.randomUUID();

    @Autowired
    protected IMarketManager marketManager;

    @Autowired
    protected IExecutionOptimizer executionOptimizer;

    /** Orderのマップ。 キーは内部のオーダーID */
    protected final Map<Long, Order> orderMap = new ConcurrentHashMap<>();

    /** 執行最適化の結果マップ。 キーは内部のオーダーID */
    protected final Map<Long, OptimizedExecution> executionMap = new ConcurrentHashMap<>();

    protected boolean enableOptimizer = true;

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public void newOrderToMarket(Order order) {
        // Execution Optimizer
        if (enableOptimizer && order.isUseOrderOptimizer()) {
            OptimizedExecution optimizedExecution = executionOptimizer.checkExecuteNowOrHoldForWaiting(order);
            if (optimizedExecution == null) {
                // 執行できないときは抜ける。OrderOptimizerがオーダー管理
                return;
            }
            // Add information
            registerOptimizedExecution(optimizedExecution);
        }

        newOrderToMarketInternal(order);
    }

    /**
     * 執行最適化後の送信処理を行います
     * @param order
     */
    protected abstract void newOrderToMarketInternal(Order order);

    @Override
    public void updateMarketForOptimizer(MarketData marketData) {
        // ExecutionOptimizer
        if (marketData.getMarketType() == executionOptimizer.getUseMarketType()) {
            List<Order> executeOrders = executionOptimizer.updateMarketDataAndCheckWaiting(marketData);
            // 執行対象があれば再執行
            if (executeOrders != null) {
                for (Order order : executeOrders) {
                    newOrderToMarketInternal(order);
                }
            }
        }
    }

    /**
     * 執行最適化情報を登録します
     * @param order
     */
    protected void registerOptimizedExecution(OptimizedExecution optimizedExecution) {
        executionMap.put(optimizedExecution.getOrderId(), optimizedExecution);
    }

    /**
     * 執行済みオーダーから執行最適化情報を追記します。
     * @param order
     */
    protected void updateOptimizedExecutionWithDoneOrder(Order order) {
        OptimizedExecution optimizedExecution = executionMap.get(order.getOrderId());
        if (optimizedExecution == null) {
            return;
        }
        // 情報を更新して送信
        optimizedExecution.supplyFromExecutedOrder(order);
        marketManager.updateFromMarket(new ExecutionInformationEvent(uuid, getClass(), optimizedExecution));

        // Mapから削除
        executionMap.remove(order.getOrderId());
    }

    /**
     * Optimizerの使用設定を行います。
     *  true : Orderに使用設定されている場合は使用します
     *  false: Orderの設定にかかわらず使用しません
     *
     * @param enableOptimizer
     */
    public void setEnableOptimizer(boolean enableOptimizer) {
        this.enableOptimizer = enableOptimizer;
    }

    public boolean isEnableOptimizer() {
        return enableOptimizer;
    }
}
