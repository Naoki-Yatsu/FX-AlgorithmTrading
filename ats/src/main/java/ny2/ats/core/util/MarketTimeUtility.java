package ny2.ats.core.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TimeZone;

/**
 * Marketの日付・時間を判断するUtilityです
 */
public class MarketTimeUtility {

    // //////////////////////////////////////
    // Field (static)
    // //////////////////////////////////////

    /** 月曜のOpen時刻 */
    private static final LocalTime OPEN_WEEEK_MONDAY_SUMMER = LocalTime.of(6, 0);
    private static final LocalTime OPEN_WEEEK_MONDAY_WINTER = LocalTime.of(7, 0);

    /** 土曜のClose時刻 */
    private static final LocalTime CLOSE_WEEEK_SATURDAY_SUMMER = LocalTime.of(6, 0);
    private static final LocalTime CLOSE_WEEEK_SATURDAY_WINTER = LocalTime.of(7, 0);

    //
    // EOD関連
    //
    /** EOD時刻(夏) */
    private static final LocalTime EOD_SUMMER = LocalTime.of(6, 0);
    /** EOD時刻(冬) */
    private static final LocalTime EOD_WINTER = LocalTime.of(7, 0);

    /** EOD開始(夏) */
    private static final LocalTime EOD_START_SUMMER = LocalTime.of(5, 55);
    /** EOD終了(夏) */
    private static final LocalTime EOD_END_SUMMER = LocalTime.of(7, 5);

    /** EOD開始(冬) */
    private static final LocalTime EOD_START_WINTER = LocalTime.of(6, 55);
    /** EOD終了(冬) */
    private static final LocalTime EOD_END_WINTER = LocalTime.of(8, 5);


    /** NYC Time Zone */
    private static final TimeZone NYC_TIMEZONE = TimeZone.getTimeZone("America/New_York");
    /** LDN Time Zone */
    private static final TimeZone LDN_TIMEZONE = TimeZone.getTimeZone("Europe/London");

    // //////////////////////////////////////
    // Method (Utility)
    // //////////////////////////////////////

    /**
     * MarketがOpenしている時間かどうか判断します
     *
     * @param jstDateTime
     * @return
     */
    public static boolean isMarketOpened(LocalDateTime jstDateTime) {
        switch (jstDateTime.getDayOfWeek()) {
            case TUESDAY:
            case WEDNESDAY:
            case THURSDAY:
            case FRIDAY:
                return true;
            case MONDAY:
                if (isNYCSummer(jstDateTime.toLocalDate())) {
                    return jstDateTime.toLocalTime().isBefore(OPEN_WEEEK_MONDAY_SUMMER) ? false : true;
                } else {
                    return jstDateTime.toLocalTime().isBefore(OPEN_WEEEK_MONDAY_WINTER) ? false : true;
                }
            case SATURDAY:
                if (isNYCSummer(jstDateTime.toLocalDate())) {
                    return jstDateTime.toLocalTime().isAfter(CLOSE_WEEEK_SATURDAY_SUMMER) ? false : true;
                } else {
                    return jstDateTime.toLocalTime().isAfter(CLOSE_WEEEK_SATURDAY_WINTER) ? false : true;
                }
            case SUNDAY:
                return false;
            default:
                return false;
        }
    }

    /**
     * 次の Market Open 時刻を取得します
     * @param jstDateTime
     * @return
     */
    public static LocalDateTime getNextOpenTime(LocalDateTime jstDateTime) {
        // まずSummerで計算する
        LocalDateTime nextOpenTime = LocalDateTime.of(jstDateTime.getYear(), jstDateTime.getMonth(), jstDateTime.getDayOfMonth(),
                OPEN_WEEEK_MONDAY_SUMMER.getHour(), OPEN_WEEEK_MONDAY_SUMMER.getMinute());
        while (nextOpenTime.getDayOfWeek() != DayOfWeek.MONDAY) {
            nextOpenTime = nextOpenTime.plusDays(1);
        }
        // 日付が確定したら夏冬判定する
        if (isNYCSummer(nextOpenTime.toLocalDate())) {
            return nextOpenTime;
        } else {
            return nextOpenTime.withHour(OPEN_WEEEK_MONDAY_WINTER.getHour());
        }
    }


    /**
     * NYC夏時間かどうかを判定します
     * @param date
     * @return
     */
    public static boolean isNYCSummer(LocalDate date) {
        return NYC_TIMEZONE.inDaylightTime(DateTimeUtility.toDate(date));
    }

    /**
     * LDN夏時間かどうかを判定します
     * @param date
     * @return
     */
    public static boolean isLDNSummer(LocalDate date) {
        return LDN_TIMEZONE.inDaylightTime(DateTimeUtility.toDate(date));
    }

    /**
     * EOD時間かどうか判定します
     * @param dateTime
     * @return
     */
    public static boolean isEOD(LocalDateTime dateTime) {
        if (isNYCSummer(dateTime.toLocalDate())) {
            if (dateTime.toLocalTime().isBefore(EOD_START_SUMMER) || dateTime.toLocalTime().isAfter(EOD_END_SUMMER)) {
                return false;
            } else {
                return true;
            }
        } else {
            if (dateTime.toLocalTime().isBefore(EOD_START_WINTER) || dateTime.toLocalTime().isAfter(EOD_END_WINTER)) {
                return false;
            } else {
                return true;
            }
        }
    }

    // //////////////////////////////////////
    // Method (Utility)
    // //////////////////////////////////////

    /**
     * 月曜日のMarket開始時刻を返します
     * @param date
     * @return
     */
    public static LocalTime getOpenWeekTimeMonday(LocalDate date) {
        return isNYCSummer(date) ? OPEN_WEEEK_MONDAY_SUMMER : OPEN_WEEEK_MONDAY_WINTER;
    }

    /**
     * 土曜日のMarket終了時刻を返します
     * @param date
     * @return
     */
    public static LocalTime getCloseWeekTimeSaturday(LocalDate date) {
        return isNYCSummer(date) ? CLOSE_WEEEK_SATURDAY_SUMMER : CLOSE_WEEEK_SATURDAY_WINTER;
    }

    /**
     * EOD時刻を返します
     * @param date
     * @return
     */
    public static LocalTime getEODTime(LocalDate date) {
        return isNYCSummer(date) ? EOD_SUMMER : EOD_WINTER;
    }

}
