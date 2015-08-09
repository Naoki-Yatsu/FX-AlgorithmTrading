package ny2.ats.model;

import org.slf4j.LoggerFactory;

import ny2.ats.core.data.Order;
import ny2.ats.core.data.Position;
import ny2.ats.position.impl.PositionHolderImpl;

/**
 * モデル用のポジション管理クラスです
 *
 */
public class ModelPositionHolder extends PositionHolderImpl {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    private final IModelIndicatorDataHolder modelIndicatorDataHolder;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public ModelPositionHolder(IModelIndicatorDataHolder marketIndicatorDataHolder) {
        super();
        // Loggerを変更する
        this.logger = LoggerFactory.getLogger(ModelPositionHolder.class);

        this.modelIndicatorDataHolder = marketIndicatorDataHolder;
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    /**
     * PL情報を更新します。
     */
    public void updatePl() {
        updatePl(modelIndicatorDataHolder.getMarketDataMap());
    }

    /**
     * 現在のポジションの状態を取得します。
     * @return
     */
    public String showAllPosition() {
        StringBuilder sb = new StringBuilder();
        for (Position position : getAllPosition().values()) {
            sb.append('[').append(position.getSymbol()).append("] ")
                .append(position.getNetOpenAmount()).append(", ")
                .append(position.getAveragePrice()).append('\n');
        }
        return sb.toString();
    }

    /**
     * 現在の最新レートを使用してPositionを更新します
     * @param filledOrder
     * @return
     */
    public void updatePosition(Order filledOrder) {
        updatePosition(filledOrder, modelIndicatorDataHolder.getMarketData(filledOrder.getSymbol()), modelIndicatorDataHolder.getMarketData(filledOrder.getSymbol().getCcy2JpySymbol()));
    }
}
