package ny2.ats.information;

import ny2.ats.core.event.SystemInformationEvent;

/**
 * SystemInformationを管理するクラスです
 */
public interface IInformationManager {

    /**
     * イベントを送信します
     * @param informationEvent
     */
    public void sendEvent(SystemInformationEvent informationEvent);

}
