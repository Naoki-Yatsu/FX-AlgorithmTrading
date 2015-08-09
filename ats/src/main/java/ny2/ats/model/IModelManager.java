package ny2.ats.model;

import java.util.List;
import java.util.Set;

import ny2.ats.core.common.Period;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.data.ModelInformation;
import ny2.ats.core.data.Order;
import ny2.ats.core.data.PLInformation;
import ny2.ats.market.connection.MarketType;

/**
 * モデルの管理クラスです。
 */
public interface IModelManager {

    /**
     * モデル全量を返します。
     */
    public Set<IModel> getAllModels();

    /**
     * ModelPositionHolderのインスタンスを作成します。
     * @return
     */
    public ModelPositionHolder createPositionHolder();

    /**
     * IMarketIndicatorDataHolderのインスタンスを返します。
     * @return
     */
    public IModelIndicatorDataHolder getModelIndicatorDataHolder();

    // //////////////////////////////////////
    // Method - Model Register
    // //////////////////////////////////////

    /**
     * モデルを登録します。
     *
     * @param model
     * @param symbol
     */
    public void registerModelForMarketUpdate(IModel model, Symbol symbol);

    /**
     * モデルを登録します。複数の通貨ペアを登録する際に使用。
     *
     * @param model
     * @param symbolList
     */
    public void registerModelForMarketUpdate(IModel model, List<Symbol> symbolList);

    /**
     * モデルを登録します。複数の通貨ペアを登録する際に使用。レート取得先を指定します。
     *
     * @param model
     * @param symbolList
     * @param marketList
     */
    public void registerModelForMarketUpdate(IModel model, List<Symbol> symbolList, List<MarketType> marketList);

    /**
     * Indicator受信登録を行います。
     * @param model
     * @param symbol
     */
    public void registerIndicator(IModel model, Symbol symbol);

    /**
     * Timer受信登録を行います。
     * @param model
     * @param period
     */
    public void registerTimer(IModel model, Period period);

    // //////////////////////////////////////
    // Method - Send Event
    // //////////////////////////////////////

    /**
     * マーケットにオーダーを送信します
     *
     * @param order
     */
    public void sendOrderToMarket(Order order);

    /**
     * PLをイベントに変換して送信します。
     *
     * @param plInformation
     */
    public void sendPLInformation(PLInformation plInformation);

    /**
     * モデル情報をイベントに変換して送信します。
     *
     * @param modelInformation
     */
    public void sendModelInformation(ModelInformation modelInformation);

    // //////////////////////////////////////
    // Method - Send Event
    // //////////////////////////////////////

    /**
     * モデルのインスタンスをJMX登録します。
     * @param modelMBean
     */
    public void registerModelMBean(IModel model);

    /**
     * モデル削除時にJMXのMBeanを削除します
     * @param modelMBean
     */
    public void unregisterModelMBean(IModel model);

}
