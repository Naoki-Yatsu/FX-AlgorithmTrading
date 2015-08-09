package ny2.ats.information;

import java.time.LocalDateTime;

/**
 * JMXイベント作成クラスです
 */
public interface IEventGenarator {

    /**
     * StartEventを送信します。
     */
    public String sendStartEvent();

    /**
     * StopEventを送信します
     */
    public String sendStopEvent();

    /**
     * ModelStartEventを送信します。
     */
    public String sendModelStartEvent();

    /**
     * ModelStopEventを送信します
     */
    public String sendModelStopEvent();

    /**
     * 指定のMarketにログインします。
     * @param marketTypeName
     */
    public String loginMarket(String marketTypeName);

    /**
     * 指定のMarketからログアウトします。
     * @param marketTypeName
     */
    public String logoutMarket(String marketTypeName);


    /**
     * 週初通知イベントを送信します(JMX用)
     * @return
     */
    public String sendWeekStartEvent();

    /**
     * 週初通知イベントを送信します
     * @param reportDateTime
     * @return
     */
    public void sendWeekStartEvent(LocalDateTime reportDateTime);

    /**
     * 週末通知イベントを送信します(JMX用)
     * @return
     */
    public String sendWeekEndEvent();

    /**
     * 週末通知イベントを送信します(JMX用)
     * @param reportDateTime
     * @return
     */
    public void sendWeekEndEvent(LocalDateTime reportDateTime);


    // //////////////////////////////////////
    // For Historical
    // //////////////////////////////////////

    /**
     * ModelStartEventを送信します。送信時刻を指定できます。
     */
    public void sendModelStartEventForHistorical(LocalDateTime reportDateTime);

    /**
     * ModelStopEventを送信します。送信時刻を指定できます。
     */
    public void sendModelStopEventForHistorical(LocalDateTime reportDateTime);

}
