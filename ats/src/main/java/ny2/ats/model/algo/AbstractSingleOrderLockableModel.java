package ny2.ats.model.algo;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;

import com.udojava.jmx.wrapper.JMXBean;
import com.udojava.jmx.wrapper.JMXBeanOperation;
import com.udojava.jmx.wrapper.JMXBeanParameter;

import ny2.ats.core.common.OrderStatus;
import ny2.ats.core.common.Side;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.data.MarketData;
import ny2.ats.core.data.Order;
import ny2.ats.core.exception.ModelException;
import ny2.ats.model.IModelManager;

/**
 * 新規・決済オーダーにロック制御を用いた単一オーダー用の抽象モデルです
 */
@JMXBean
public abstract class AbstractSingleOrderLockableModel extends AbstractModel{

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // ロック
    /** オーダー操作用のロック */
    protected volatile ReentrantLock orderLock = new ReentrantLock();

    // オーダー
    /** Openオーダー。CloseオーダーがFillするとnullになります。 */
    protected volatile Order openOrder;
    /** Closeオーダー。Closeオーダーが執行するとOpen/Closeともnullになります */
    protected volatile Order closeOrder;


    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public AbstractSingleOrderLockableModel(IModelManager modelManager, Logger logger) {
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

        // 既存ポジジョンTP/SLチェック
        if (getPositionStatus() == ModelPositionStatus.OPEN) {
            checkForCloseOrder(marketData);
        }

        // 新規ポジションチェック
        if (getPositionStatus() == ModelPositionStatus.NONE) {
            checkForNewOrder(marketData);
        }
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
        if (orderLock.tryLock()) {
            try {
                // Lock後に再度チェック
                if (getPositionStatus() == ModelPositionStatus.NONE) {
                    checkForNewOrderInternal(marketData);
                }
            } finally {
                orderLock.unlock();
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
        if (orderLock.tryLock()) {
            try {
                // Lock後に再度チェック
                if (getPositionStatus() == ModelPositionStatus.OPEN) {
                    checkForCloseOrderInternal(marketData);
                }
            } finally {
                orderLock.unlock();
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
     * オーダー情報を更新します
     *
     * @param updateOrder
     */
    private synchronized void updateOrder(Order updateOrder) {
        OrderStatus updateOrderStatus = updateOrder.getOrderStatus();
        // Open
        if (openOrder != null && openOrder.getOrderId().equals(updateOrder.getOrderId())) {
            switch (updateOrderStatus) {
                case NEW:
                case OPENED:
                    // DO Nothing
                    break;
                case REJECTED:
                case CANCELED:
                    logger.warn("{} Open Order is rejected/canceled. {}", getDisplayName(), updateOrder.toStringSummary());
                    openOrder = null;
                    withOpenOrderCanceled(updateOrder);
                    break;
                case FILLED:
                    logger.info("{} Open Order is filled. {}", getDisplayName(), updateOrder.toStringSummary());
                    openOrder = updateOrder;
                    // Position計算
                    modelPositionHolder.updatePosition(updateOrder);
                    withOpenOrderFilled(updateOrder);
                    break;
                default:
                    logger.error("{} Unexpected OrderStatus. {}", getDisplayName(), updateOrder.toString());
                    break;
            }

        // Close
        } else if (closeOrder != null && closeOrder.getOrderId().equals(updateOrder.getOrderId())) {
            switch (updateOrderStatus) {
                case NEW:
                case OPENED:
                    // DO Nothing
                    break;
                case REJECTED:
                case CANCELED:
                    logger.warn("{} Close Order is rejected/canceled. {}", getDisplayName(), updateOrder.toStringSummary());
                    closeOrder = null;
                    withCloseOrderCanceled(updateOrder);
                    break;
                case FILLED:
                    logger.info("{} Close Order is filled. {}", getDisplayName(), updateOrder.toStringSummary());
                    closeOrder = null;
                    openOrder = null;
                    // Position計算
                    modelPositionHolder.updatePosition(updateOrder);
                    withCloseOrderFilled(updateOrder);
                    break;
                default:
                    logger.error("{} Unexpected OrderStatus. {}", getDisplayName(), updateOrder.toString());
                    break;
            }
        }
    }

    /**
     * OpenオーダーがCancel/Rejectした際に処理を行う場合はこのメソッドをオーバーライドしてください
     */
    protected void withOpenOrderCanceled(Order canceledOrder) {
        // Do nothing
    }

    /**
     * OpenオーダーがFillした際に処理を行う場合はこのメソッドをオーバーライドしてください
     */
    protected void withOpenOrderFilled(Order filleddOrder) {
        // Do nothing
    }

    /**
     * CloseオーダーがCancel/Rejectした際に処理を行う場合はこのメソッドをオーバーライドしてください
     */
    protected void withCloseOrderCanceled(Order canceledOrder) {
        // Do nothing
    }

    /**
     * CloseオーダーがFillした際に処理を行う場合はこのメソッドをオーバーライドしてください
     */
    protected void withCloseOrderFilled(Order filleddOrder) {
        // Do nothing
    }

    /**
     * Open Orderを作成して送信します
     */
    protected void sendOpenOrder(MarketData marketData, Symbol symbol, Side side, int amount) {
        openOrder = createOpenMarketOrder(getLastMarketData(symbol), symbol, side, amount);
        logger.info("{} Send open order. {}", getDisplayName(), openOrder.toStringSummary());
        modelManager.sendOrderToMarket(openOrder);
    }

    /**
     * Close Orderを作成して送信します
     */
    protected void sendCloseOrder(MarketData marketData, Order originalOrder) {
        closeOrder = createCloseMarketOrder(marketData, openOrder);
        logger.info("{} Send close order. {}", getDisplayName(), closeOrder.toStringSummary());
        modelManager.sendOrderToMarket(closeOrder);
    }

    /**
     * 現在のポジション状態を返します
     * @return
     */
    protected ModelPositionStatus getPositionStatus() {
        if (openOrder == null) {
            return ModelPositionStatus.NONE;
        } else if (openOrder != null && openOrder.getOrderStatus() != OrderStatus.FILLED) {
            return ModelPositionStatus.OPENING;
        } else if (openOrder != null && openOrder.getOrderStatus() == OrderStatus.FILLED && closeOrder == null) {
            return ModelPositionStatus.OPEN;
        } else if (openOrder != null && openOrder.getOrderStatus() == OrderStatus.FILLED && closeOrder != null) {
            return ModelPositionStatus.CLOSING;
        } else {
            // 想定外
            throw new ModelException(getClass(), "Unexcepted position status");
        }
    }

    // //////////////////////////////////////
    // Method (JMX)
    // //////////////////////////////////////

    /**
     * ロックの状態を表示します
     * @return ロック状態
     */
    @JMXBeanOperation
    public String showLockStatus() {
        StringBuilder sb = new StringBuilder("[Lock Status]\n");
        sb.append("orderLock : ").append(orderLock.isLocked()).append("\n");
        return sb.toString();
    }

    /**
     * オーダーのロックを解放します
     * @return 実行前後のロック状態
     */
    @JMXBeanOperation
    public String WARN_unlockAllLock(@JMXBeanParameter(name = "confirm", description = "Set true if you really do this.") boolean confirm) {
        if (!confirm) {
            return CONFIRMATION_MESSAGE;
        }
        logger.warn("{} Unlock all locks.", getDisplayName());
        StringBuilder sb = new StringBuilder("[[Before]]\n");
        sb.append(showLockStatus());

        // unlock
        orderLock.unlock();

        sb.append("\n\n[[After]]\n").append(showLockStatus());
        return sb.toString();
    }

    /**
     * オーダーの状態を表示します
     * @return 現在のオーダー状態
     */
    @JMXBeanOperation
    public String showOrderStatus() {
        StringBuilder sb = new StringBuilder("[Order Status]\n");
        sb.append("openOrder  : ").append(openOrder != null ? openOrder.toStringSummary() : "null").append("\n");
        sb.append("closeOrder : ").append(closeOrder != null ? closeOrder.toStringSummary() : "null").append("\n");
        sb.append("\n[Position Status]\n").append(getPositionStatus());
        return sb.toString();
    }

    @Override
    public String closeAllPosition() {
        logger.warn("{} Close position.", getDisplayName());
        StringBuilder sb = new StringBuilder();
        sb.append(showOrderStatus());

        sb.append("\n[Target Orders]\n");
        // 決済注文
        try {
            if (orderLock.tryLock(5, TimeUnit.SECONDS)) {
                try {
                    if (getPositionStatus() == ModelPositionStatus.NONE) {
                        sb.append("No Open Order.");
                    } else if (getPositionStatus() == ModelPositionStatus.OPENING) {
                        sb.append("WARN: Open Order has not filled yet.").append("\n");
                    } else if (getPositionStatus() == ModelPositionStatus.CLOSING) {
                        sb.append("WARN: Open Order is closing.").append("\n");
                        sb.append(showOrderStatus());
                    } else {
                        closeOrder = createCloseMarketOrder(modelIndicatorDataHolder.getMarketData(openOrder.getSymbol()), openOrder);
                        logger.info("{} Send close Order. {}", getDisplayName(), closeOrder.toStringSummary());
                        modelManager.sendOrderToMarket(closeOrder);
                        sb.append(closeOrder.toStringSummary());
                    }
                } finally {
                    orderLock.unlock();
                }
            } else {
                sb.append("ERROR: Failed to get lock.");
            }
        } catch (InterruptedException e) {
            logger.error("", e);
        }
        return sb.toString();
    }

    // //////////////////////////////////////
    // Inner Class
    // //////////////////////////////////////

    /**
     * ポジションの状態をあらわします
     * {@literal NONE -> OPENING -> OPEN -> CLOSING }
     */
    protected enum ModelPositionStatus {
        /** ポジションなし */
        NONE,
        /** 新規オーダー結果待ち */
        OPENING,
        /** ポジションを持っている */
        OPEN,
        /** 決済オーダー結果待ち */
        CLOSING;
    }

}
