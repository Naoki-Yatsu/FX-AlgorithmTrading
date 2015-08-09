package ny2.ats.information;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ny2.ats.core.common.Period;
import ny2.ats.core.event.TimerInformationEvent;
import ny2.ats.core.router.IEventRouter;
import ny2.ats.core.util.MarketTimeUtility;
import ny2.ats.core.util.NumberUtility;

/**
 * Timerのクラスです。
 */
@Component
public class TimerChecker {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** Timerで使用するPeriod */
    private static final List<Period> TIMER_PERIODS = Arrays.asList(Period.MIN_1, Period.MIN_5, Period.MIN_15, Period.HOUR_1, Period.DAILY);

    /** 最短の更新間隔 */
    private static final Period SHORTEST_PERIOD = Period.MIN_1;

    /** 週末イベントを週末時刻の何分前に送信するか */
    private static final int WEEKEND_BEFORE_MINUTES = 15;


    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final UUID uuid = UUID.randomUUID();

    /** イベント送信先ルーター */
    @Autowired
    private IEventRouter eventRouter;

    /** 特殊イベント作成/送信 */
    @Autowired
    private IEventGenarator eventGenarator;


    /** 最終更新時刻 */
    private final Map<Period, LocalDateTime> lastDateTimeMap = new EnumMap<>(Period.class);

    /** 次回更新時刻 */
    private final Map<Period, LocalDateTime> nextDateTimeMap = new EnumMap<>(Period.class);

    /** 週末状態、週末の間はtrueに変更する */
    private volatile boolean onWeekend = false;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public TimerChecker() {
        logger.info("Create instance.");
    }

    /**
     * Timerの時刻を設定します
     * @param baseDateTime
     */
    public void resetTimer(LocalDateTime baseDateTime) {
        logger.info("Reset timer. baseDateTime = {}", baseDateTime);

        // 最初データ時刻を設定
        LocalDateTime lastDateTime1min = baseDateTime.withNano(0).withSecond(0);
        LocalDateTime lastDateTime5min = lastDateTime1min.withMinute(NumberUtility.previousNumberByBase(lastDateTime1min.getMinute(), 5));
        LocalDateTime lastDateTime15min = lastDateTime5min.withMinute(NumberUtility.previousNumberByBase(lastDateTime1min.getMinute(), 15));
        LocalDateTime lastDateTime1hour = lastDateTime15min.withMinute(0);
        LocalDateTime lastDateTime1day = culcurateNextDailyUpdate(baseDateTime.toLocalDate().minusDays(1));
        if (lastDateTime1day.isBefore(lastDateTime1hour)) {
            lastDateTime1day = culcurateNextDailyUpdate(lastDateTime1day.toLocalDate());
        }

        lastDateTimeMap.put(Period.MIN_1, lastDateTime1min);
        lastDateTimeMap.put(Period.MIN_5, lastDateTime5min);
        lastDateTimeMap.put(Period.MIN_15, lastDateTime15min);
        lastDateTimeMap.put(Period.HOUR_1, lastDateTime1hour);
        lastDateTimeMap.put(Period.DAILY, lastDateTime1day);

        // 次回の更新タイミングを設定
        LocalDateTime nextDateTime1min = lastDateTime1min.plusMinutes(1);
        LocalDateTime nextDateTime5min = lastDateTime5min.plusMinutes(5);
        LocalDateTime nextDateTime15min = lastDateTime15min.plusMinutes(15);
        LocalDateTime nextDateTime1hour = lastDateTime1hour.plusHours(1);
        LocalDateTime nextDateTime1day = culcurateNextDailyUpdate(lastDateTime1day.toLocalDate());

        nextDateTimeMap.put(Period.MIN_1, nextDateTime1min);
        nextDateTimeMap.put(Period.MIN_5, nextDateTime5min);
        nextDateTimeMap.put(Period.MIN_15, nextDateTime15min);
        nextDateTimeMap.put(Period.HOUR_1, nextDateTime1hour);
        nextDateTimeMap.put(Period.DAILY, nextDateTime1day);
    }


    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    /**
     * TImerの更新をチェックします。
     *
     * @param updateDateTime
     */
    public synchronized void checkTime(LocalDateTime updateDateTime) {
        LocalDateTime nextDateTime1min = nextDateTimeMap.get(SHORTEST_PERIOD);

        // 最小更新間隔である1分の更新をチェックして、更新がなければ全部更新しない
        if (updateDateTime.isBefore(nextDateTime1min)) {
            return;
        }

        // Market Close 時間帯は更新しない
        if (!MarketTimeUtility.isMarketOpened(nextDateTime1min)) {
            // 次のOpen時刻まで進める
            putForwardTimeToNext(MarketTimeUtility.getNextOpenTime(nextDateTime1min));
            return;
        }

        // 土曜日は Market Close 15分前に WEEK_ENDイベントを送信する
        if (!onWeekend && updateDateTime.getDayOfWeek() == DayOfWeek.SATURDAY) {
            LocalTime closeTime = MarketTimeUtility.getCloseWeekTimeSaturday(updateDateTime.toLocalDate());
            if(!updateDateTime.toLocalTime().isBefore(closeTime.minusMinutes(WEEKEND_BEFORE_MINUTES))) {
                onWeekend = true;
                eventGenarator.sendWeekEndEvent(nextDateTime1min);
            }
        } else if (onWeekend && updateDateTime.getDayOfWeek() == DayOfWeek.MONDAY) {
            // 月曜はStart時刻に開始イベントを送信する
            LocalTime openTime = MarketTimeUtility.getOpenWeekTimeMonday(updateDateTime.toLocalDate());
            if(!updateDateTime.toLocalTime().isBefore(openTime)) {
                onWeekend = false;
                eventGenarator.sendWeekStartEvent(nextDateTime1min);
            }
        }

        // 各基準時刻を進める
        for (Period period : TIMER_PERIODS) {
            LocalDateTime lastDateTime = lastDateTimeMap.get(period);
            LocalDateTime nextDateTime = nextDateTimeMap.get(period);
            // 5min以降の比較用
            LocalDateTime lastDateTime1min = lastDateTimeMap.get(SHORTEST_PERIOD);

            // 現在時刻が次の更新予定時刻以降
            if (!updateDateTime.isBefore(nextDateTime)) {
                // その時刻のMIN_1が更新されていなければ抜ける（先にMIN_1を更新する）
                if (period != SHORTEST_PERIOD && lastDateTime1min.isBefore(nextDateTime)) {
                    return;
                }

                // 更新時刻、次回時刻を設定
                lastDateTime = nextDateTime;
                if (period != Period.DAILY) {
                    nextDateTime = nextDateTime.plus(period.getTimeInverval(), period.getChronoUnit());
                } else {
                    nextDateTime = culcurateNextDailyUpdate(nextDateTime.toLocalDate());
                }
                sendTimerEvent(period, lastDateTime, nextDateTime);

                // LocalDateTime は immutable なので入れなおす
                lastDateTimeMap.put(period, lastDateTime);
                nextDateTimeMap.put(period, nextDateTime);
            } else {
                // 更新が無い場合、より長い時刻は更新しない
                return;
            }
        }
    }

    /**
     * TimerEventを登録します
     */
    private void sendTimerEvent(Period period, LocalDateTime currentDateTime, LocalDateTime nextDateTime) {
        TimerInformationEvent timerEvent = new TimerInformationEvent(uuid, getClass(), period, currentDateTime, nextDateTime);
        eventRouter.addEvent(timerEvent);
    }

    /**
     * 指定時刻までタイマーを進めイベントを送信します 。主にバックテストで使用します。
     *
     * @param updateDateTime 基準時刻
     */
    public void proceedTimer(LocalDateTime updateDateTime) {
        while (!updateDateTime.isBefore(nextDateTimeMap.get(SHORTEST_PERIOD))) {
            checkTime(updateDateTime);
        }
    }

    /**
     * すべてのスケールの基準時刻を指定時間まで進めます
     * @param nextBaseTime
     */
    private void putForwardTimeToNext(LocalDateTime nextBaseTime) {

        for (Period period : TIMER_PERIODS) {
            LocalDateTime lastDateTime = lastDateTimeMap.get(period);
            LocalDateTime nextDateTime = nextDateTimeMap.get(period);

            // 指定時刻まで進める
            while (nextDateTime.isBefore(nextBaseTime)) {
                lastDateTime = nextDateTime;
                nextDateTime = nextDateTime.plus(period.getTimeInverval(), period.getChronoUnit());
            }

            // LocalDateTime は immutable なので入れなおす
            lastDateTimeMap.put(period, lastDateTime);
            nextDateTimeMap.put(period, nextDateTime);
        }
    }

    private LocalDateTime culcurateNextDailyUpdate(LocalDate previousDate) {
        // 更新は火-土
        LocalDate nextDate = previousDate.plusDays(1);
        while (nextDate.getDayOfWeek() == DayOfWeek.SUNDAY || nextDate.getDayOfWeek() == DayOfWeek.MONDAY) {
            nextDate = nextDate.plusDays(1);
        }
        // 夏冬考慮
        return LocalDateTime.of(nextDate, MarketTimeUtility.getEODTime(nextDate));
    }

    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

    /**
     * 最終更新時刻のMapを返します
     * @return
     */
    public Map<Period, LocalDateTime> getLastDateTimeMap() {
        return lastDateTimeMap;
    }

    /**
     * 次の更新時刻のMapを返します
     * @return
     */
    public Map<Period, LocalDateTime> getNextDateTimeMap() {
        return nextDateTimeMap;
    }

    /**
     * 最終更新時刻を返します
     * @return
     */
    public LocalDateTime getLastTimerTime() {
        return lastDateTimeMap.get(SHORTEST_PERIOD);
    }

    /**
     * 次の更新時刻を返します
     * @return
     */
    public LocalDateTime getNextTimerTime() {
        return nextDateTimeMap.get(SHORTEST_PERIOD);
    }
}
