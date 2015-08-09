package ny2.ats.indicator;

import ny2.ats.core.event.IEventListener;

/**
 * Indicatorサービス全体を管理するインターフェースです。
 */
public interface IIndicatorManager extends IEventListener {

    /**
     * Send IndicatorInformation to EventRouter
     * @param indicator
     */
    public void sendIndicatorUpdate(Indicator<?> indicator);
}
