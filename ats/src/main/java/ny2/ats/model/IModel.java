package ny2.ats.model;

import com.udojava.jmx.wrapper.JMXBean;
import com.udojava.jmx.wrapper.JMXBeanAttribute;
import com.udojava.jmx.wrapper.JMXBeanOperation;
import com.udojava.jmx.wrapper.JMXBeanParameter;

import ny2.ats.core.data.IndicatorInformation;
import ny2.ats.core.data.MarketData;
import ny2.ats.core.data.Order;
import ny2.ats.core.data.Position;
import ny2.ats.core.data.SystemInformation;
import ny2.ats.core.data.TimerInformation;

@JMXBean
public interface IModel {

    // //////////////////////////////////////
    // Method モデル情報
    // //////////////////////////////////////

    /**
     * モデルの種別を取得します。
     * @return
     */
    public ModelType getModelType();

    /**
     * モデルのバージョンを取得します。
     * @return
     */
    public ModelVersion getModelVersion();

    /**
     * モデル情報を取得します
     * @return
     */
    @JMXBeanAttribute
    public default String getModelInformation() {
        return getDisplayName() + " " + getModelParams();
    }

    /**
     * モデルの表示名を取得します。
     * @return
     */
    public String getDisplayName();

    /**
     * モデルのパラメータ情報を取得します。
     * @return
     */
    public String getModelParams();

    /**
     * モデルの稼動状況を取得します。
     * @return
     */
    public ModelStatus getModelStatus();

    /**
     * モデルのPL情報を取得します
     * @return
     */
    public int getPl();

    /**
     * モデルのPosition情報を取得します
     * @return position情報
     */
    public String showAllPosition();

    // //////////////////////////////////////
    // Method JMX
    // //////////////////////////////////////

    public static String NOT_IMPLEMENTED = "Not Implemented";

    public static String CONFIRMATION_MESSAGE = "ERROR: For executing this method, please input confirmation as true.";


    /**
     * モデルの稼動状態を取得します
     * @return
     */
    public String getRunningStatus();


    /**
     * 現在保持しているオーダー情報を取得します
     * @return
     */
    public default String showAllOrders() {
        return NOT_IMPLEMENTED;
    }

    /**
     * 現在保持しているポジションをすべて決済します。(JMX用Wrapper)
     * @param confirm
     * @return
     */
    @JMXBeanOperation
    public default String WARN_closeAllPosition(@JMXBeanParameter(name = "confirm", description = "Set true if you really do this.") boolean confirm) {
        if (!confirm) {
            return CONFIRMATION_MESSAGE;
        }
        return closeAllPosition();
    }

    /**
     *  現在保持しているポジションをすべて決済します。
     * @return JMX実行用のポジション情報
     */
    public String closeAllPosition();


    // //////////////////////////////////////
    // Method イベント処理
    // //////////////////////////////////////

    /**
     * マーケットデータの更新を受けて動作します
     *
     * @param marketData
     */
    public void receiveMarketData(MarketData marketData);

    /**
     * オーダーの更新を受けて動作します。
     *
     * @param order
     */
    public void receiveOrderUpdate(Order order);

    /**
     * Positionの更新を受けて動作します。
     *
     * @param position
     */
    public default void receivePositionUpdate(Position position) {
        // Do nothing.
    }

    /**
     * Indicatorの更新を受けて動作します。
     *
     * @param indicatorInformation
     */
    public default void receiveIndicatorUpdate(IndicatorInformation indicatorInformation) {
        // Do nothing.
    }

    /**
     * Timerを受信して動作します。
     * @param timerInformation
     */
    public default void onTimer(TimerInformation timerInformation) {
        // Do nothing.
    }

    /**
     * SystemInformationを受信して動作します。
     * @param systemInformation
     */
    public void onSystemEvent(SystemInformation systemInformation);

}
