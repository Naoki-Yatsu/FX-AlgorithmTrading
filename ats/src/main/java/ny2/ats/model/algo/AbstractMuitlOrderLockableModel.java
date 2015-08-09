package ny2.ats.model.algo;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;

import ny2.ats.core.common.OrderStatus;
import ny2.ats.core.data.MarketData;
import ny2.ats.core.data.Order;
import ny2.ats.model.IModelManager;

/**
 * 新規・決済オーダーにロック制御を用いた複数オーダー用の抽象モデルです
 */
public abstract class AbstractMuitlOrderLockableModel extends AbstractModel {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // ロック
    /** 新規ポジション処理用のロック */
    private ReentrantLock newOrderLock = new ReentrantLock();
    /** ポジションクローズ用のロック */
    private ReentrantLock closeOrderLock = new ReentrantLock();

    // オーダー
    /** OpenオーダーのMap。keyはOrderId。ポジションを立てた時点ではオーダーは削除せずに、クローズした時点で該当オーダーを削除。 */
    private ConcurrentHashMap<Long, Order> openOrderMap = new ConcurrentHashMap<>();

    /** 決済待ちオーダーのMap。オーダーの結果待ちのオーダーのみが入っている */
    private ConcurrentHashMap<Long, Order> closeOrderMap = new ConcurrentHashMap<>();

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public AbstractMuitlOrderLockableModel(IModelManager modelManager, Logger logger) {
        super(modelManager, logger);
    }

    // //////////////////////////////////////
    // Method @Override
    // //////////////////////////////////////

    @Override
    public void receiveMarketData(MarketData marketData) {
        // モデル停止中はは何もしない
        if (!isRunning) {
            return;
        }

        // 既存ポジジョンTPチェック
        if (openOrderMap.size() > 0) {
            checkForCloseOrder(marketData);
        }

        // 新規ポジションチェック
        checkForNewOrder(marketData);

        // Stop Loss
        checkForStopLoss(marketData);
    }

    @Override
    public void receiveOrderUpdate(Order updateOrder) {
        logger.debug("{} Received Update Order. {}", getDisplayName(), updateOrder.toStringSummary());
        // オーダー情報を更新
        updateOrder(updateOrder);
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    /**
     * 現在のプライスをチェックして、必要があれば、NewOrderを出します。
     * @param marketData 最新レート
     */
    protected void checkForNewOrder(MarketData marketData) {
        // 既にロックされている場合は何もしない
        if (newOrderLock.tryLock()) {
            try {
                // Lock後に再度チェック
                if (newOrderLock == null) {
                    checkForNewOrderInternal(marketData);
                }
            } finally {
                newOrderLock.unlock();
            }
        } else {
            logger.warn("{} Skipped check for new order as already locked.", getDisplayName());
            return;
        }
    }

    /**
     * checkForNewOrderの内部処理です
     */
    protected abstract void checkForNewOrderInternal(MarketData marketData);


    /**
     * Openポジションから決済すべきオーダーを判別し、決済注文をだします。
     * @param marketData 最新レート
     */
    protected void checkForCloseOrder(MarketData marketData) {
        // 既にロックされている場合は何もしない
        if (closeOrderLock.tryLock()) {
            try {
                // Lock後に再度チェック
                if (openOrderMap.size() > 0) {
                    checkForCloseOrderInternal(marketData);
                }
            } finally {
                closeOrderLock.unlock();
            }
        } else {
            logger.warn("{} Skipped check for close order as already locked.", getDisplayName());
            return;
        }
    }

    /**
     * checkForCloseOrderの内部処理です
     */
    protected abstract void checkForCloseOrderInternal(MarketData marketData);

    /**
     * StopLossのチェックを行います
     */
    protected abstract void checkForStopLoss(MarketData marketData);

    /**
     * オーダー情報を更新します
     *
     * @param updateOrder
     */
    private synchronized void updateOrder(Order updateOrder) {
        OrderStatus updateOrderStatus = updateOrder.getOrderStatus();

        // search order
        Order openOrder = openOrderMap.get(updateOrder.getOrderId());
        Order closeOrder = closeOrderMap.get(updateOrder.getOrderId());

        // Open
        if (openOrder != null) {
            switch (updateOrderStatus) {
                case NEW:
                case OPENED:
                    // DO Nothing
                    break;
                case REJECTED:
                case CANCELED:
                    logger.warn("{} Open Order is rejected/canceled. {}", getDisplayName(), updateOrder.toStringSummary());
                    openOrderMap.remove(updateOrder.getOrderId());
                    break;
                case FILLED:
                    logger.info("{} Open Order is filled. {}", getDisplayName(), updateOrder.toStringSummary());
                    openOrderMap.put(updateOrder.getOrderId(), updateOrder);
                    // Position計算
                    modelPositionHolder.updatePosition(updateOrder);
                    break;
                default:
                    logger.error("{} Unexpected OrderStatus. {}", getDisplayName(), updateOrder.toString());
                    break;
            }

        // Close
        } else if (closeOrder != null) {
            switch (updateOrderStatus) {
                case NEW:
                case OPENED:
                    // DO Nothing
                    break;
                case REJECTED:
                case CANCELED:
                    logger.warn("{} Close Order is rejected/canceled. {}", getDisplayName(), updateOrder.toStringSummary());
                    closeOrderMap.remove(updateOrder.getOrderId());
                    break;
                case FILLED:
                    logger.info("{} Close Order is filled. {}", getDisplayName(), updateOrder.toStringSummary());
                    closeOrderMap.remove(updateOrder.getOrderId());
                    openOrder = openOrderMap.remove(updateOrder.getOriginalOrderId());
                    if (openOrder == null) {
                        logger.warn("{} Open Order of Close Order is NOT exist. OriginalOrderId = {}", getDisplayName(), updateOrder.getOriginalOrderId());
                    }
                    // Position計算
                    modelPositionHolder.updatePosition(updateOrder);
                    break;
                default:
                    logger.error("{} Unexpected OrderStatus. {}", getDisplayName(), updateOrder.toString());
                    break;
            }
        }
    }

    // //////////////////////////////////////
    // Method (JMX)
    // //////////////////////////////////////

    /**
     * ロックの状態を表示します。JMXでアクセスするためにはラッパーメソッドを作成してください。
     * @return ロック状態
     */
    public String showLockStatus() {
        StringBuilder sb = new StringBuilder("[Lock Status]\n");
        sb.append("newOrderLock   : ").append(newOrderLock.isLocked()).append("\n");
        sb.append("closeOrderLock : ").append(closeOrderLock.isLocked()).append("\n");
        return sb.toString();
    }

    /**
     * オーダーのロックを解放します。JMXでアクセスするためにはラッパーメソッドを作成してください。
     * @return 実行前後のロック状態
     */
    public String unlockAllLock(boolean confirm) {
        if (!confirm) {
            return CONFIRMATION_MESSAGE;
        }
        logger.warn("{} Unlock all locks.", getDisplayName());
        StringBuilder sb = new StringBuilder("[[Before]]\n");
        sb.append(showLockStatus());

        // unlock
        newOrderLock.unlock();
        closeOrderLock.unlock();

        sb.append("\n\n[[After]]\n").append(showLockStatus());
        return sb.toString();
    }

    /**
     * オーダーの状態を表示します。JMXでアクセスするためにはラッパーメソッドを作成してください。
     * @return 現在のオーダー状態
     */
    public String showOrderStatus() {
        StringBuilder sb = new StringBuilder("[Order Status]\n");
        sb.append("\nopenOrders : ");
        for (Order order : openOrderMap.values()) {
            sb.append(order.toStringSummary()).append("\n");
        }
        sb.append("\ncloseOrders : ");
        for (Order order : closeOrderMap.values()) {
            sb.append(order.toStringSummary()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public String closeAllPosition() {
        logger.warn("{} Close position.", getDisplayName());
        StringBuilder sb = new StringBuilder("[Target Orders]\n");

        // 決済注文
        try {
            if (closeOrderLock.tryLock(5, TimeUnit.SECONDS)) {
                try {
                    if (openOrderMap.size() == 0) {
                        sb.append("No Open Order.");
                    } else {
                        Collection<Order> orders = openOrderMap.values();
                        for (Order openOrder : orders) {
                            Order closeOrder = createCloseMarketOrder(modelIndicatorDataHolder.getMarketData(openOrder.getSymbol()), openOrder);
                            logger.info("{} Send close Order. {}", getDisplayName(), closeOrder.toStringSummary());
                            modelManager.sendOrderToMarket(closeOrder);
                            sb.append(closeOrder.toStringSummary()).append("\n");
                        }
                    }
                } finally {
                    closeOrderLock.unlock();
                }
            } else {
                sb.append("ERROR: Failed to get lock.");
            }
        } catch (InterruptedException e) {
            logger.error("", e);
        }
        return sb.toString();
    }
}
