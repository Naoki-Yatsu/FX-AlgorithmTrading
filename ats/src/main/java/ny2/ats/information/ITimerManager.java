package ny2.ats.information;

import java.time.LocalDateTime;

/**
 * TImerのイベント管理クラスです。
 */
public interface ITimerManager {

    /**
     * Timerを停止します。主にBacktestで使用します
     */
    public void stopTimerForBacktest();

    /**
     * 次の通知基準時刻を返します。
     * @return
     */
    public LocalDateTime getNextTimerTime();

    /**
     * 最後のTimerの通知基準時刻を返します。
     * @return
     */
    public LocalDateTime getLastTimerTime();

}
