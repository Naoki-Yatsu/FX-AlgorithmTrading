package ny2.ats.core.common;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

/**
 * 期間を表すEnumです
 */
public enum Period {

    // Tickに関しては、TimeUnitはダミー, intervalはtick数
    TICK  (0, "tick", TimeUnit.MILLISECONDS, ChronoUnit.MILLIS, 1),
    TICK_50 (1, "50 ticks", TimeUnit.MILLISECONDS, ChronoUnit.MILLIS, 50),
    TICK_100 (2, "100 ticks" , TimeUnit.MILLISECONDS, ChronoUnit.MILLIS, 100),

    // Tick Pip は一定pip以上動いたら更新する, intervalは基準sub pip
    TICK_PIPH (3, "0.5 pip", TimeUnit.MILLISECONDS, ChronoUnit.MILLIS, 5),
    TICK_PIP (4, "1 pip", TimeUnit.MILLISECONDS, ChronoUnit.MILLIS, 10),

    SEC_30(5, "30 secs", TimeUnit.SECONDS, ChronoUnit.SECONDS, 30),

    MIN_1 (11, "1 min", TimeUnit.MINUTES, ChronoUnit.MINUTES, 1),
    MIN_5 (12, "5 mins", TimeUnit.MINUTES, ChronoUnit.MINUTES, 5),
    MIN_15(13, "1 mins", TimeUnit.MINUTES, ChronoUnit.MINUTES, 15),

    HOUR_1(21, "1 hour", TimeUnit.HOURS, ChronoUnit.HOURS, 1),
    // HOUR_4(22, "4 hours", TimeUnit.HOURS, ChronoUnit.HOURS, 4),

    DAILY (31, "daily", TimeUnit.DAYS, ChronoUnit.DAYS, 1);
    // DAILY_5(32, "daily 5", TimeUnit.DAYS, ChronoUnit.DAYS, 5),
    // WEEKLY (33, "weekly", TimeUnit.DAYS, ChronoUnit.DAYS, 7);


    // //////////////////////////////////////
    // Field (constant)
    // //////////////////////////////////////

    /** TICKの更新回数 */
    public static final int TICK_COUNT = 100;

    /** TICK(Round)の更新回数 */
    public static final int TICK_ROUND_COUNT = 50;

    /** TICKを丸めて評価する場合の最低移動 */
    public static final double TICK_ROUND_CHANGE = 0.19;

    /** TICK関連PeriodのSet  */
    private static final Set<Period> PERIOD_TICKS = new TreeSet<>(
            Arrays.asList(Period.TICK, Period.TICK_50, Period.TICK_100, Period.TICK_PIPH, Period.TICK_PIP));

    /** Indicator計算対象(Tick) */
    public static final Set<Period> PERIOD_INDICATOR_TICK_DEFAULT = new TreeSet<Period>(
            Arrays.asList(Period.TICK_100, Period.TICK_PIP));

    /** Indicator計算対象(Time関連) */
    public static final Set<Period> PERIOD_INDICATOR_TIME_DEFAULT = new TreeSet<Period>(
            Arrays.asList(Period.MIN_1, Period.MIN_5, Period.HOUR_1));

    ///** Indicator計算対象 全量 */
    //@SuppressWarnings("serial")
    //public static final Set<Period> PERIOD_INDICATOR_ALL_DEFAULT = new TreeSet<Period>() {{addAll(PERIOD_INDICATOR_TICK_DEFAULT); addAll(PERIOD_INDICATOR_TIME_DEFAULT);}};


    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** 期間の長短をあらわす。小さいほうが期間が短い。数値は比較でのみ使用すること */
    private int order;

    /** 文字列名称 */
    private String stringName;

    /** TimeUnit単位 */
    private TimeUnit timeUnit;

    /** ChronoUnit単位 */
    private ChronoUnit chronoUnit;

    /** 単位期間 */
    private int inverval;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    private Period(int order, String stringName, TimeUnit timeUnit, ChronoUnit chronoUnit, int inverval) {
        this.order = order;
        this.stringName = stringName;
        this.timeUnit = timeUnit;
        this.chronoUnit = chronoUnit;
        this.inverval = inverval;
    }

    /**
     * Stringの配列からSetを作成します
     * @param periods
     * @return
     */
    public static Set<Period> valueOfStringArray(String[] periods) {
        Set<Period> periodSet = EnumSet.noneOf(Period.class);
        for (String str : periods) {
            if (!str.isEmpty()) {
                periodSet.add(Period.valueOf(str));
            }
        }
        return periodSet;
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    public static boolean isTickPeriod(Period period) {
        return PERIOD_TICKS.contains(period);
    }

    public boolean isTickPeriod() {
        return PERIOD_TICKS.contains(this);
    }

    public static boolean isTickCountPeriod(Period period) {
        if (period == TICK_50 || period == TICK_100) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isTickPipPeriod(Period period) {
        if (period == TICK_PIPH || period == TICK_PIP) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Return shorter period from this
     * For tick periods, return TICK
     * @return
     */
    public Period getShorterPeriod() {
        switch (this) {
            case DAILY:
                return Period.HOUR_1;
            case HOUR_1:
                return Period.MIN_15;
            case MIN_15:
                return Period.MIN_5;
            case MIN_5:
                return Period.MIN_1;
            case MIN_1:
                return Period.TICK;
            default:
                return Period.TICK;
        }
    }

    /**
     * Return shorter period from target set.
     * If this is shortest, return null
     * @param periodSet
     * @return
     */
    public Period getShorterPeriod(Set<Period> periodSet) {
        Period shorterPeriod = null;
        for (Period period : periodSet) {
            if (period.order < order) {
                // Max order is 1 shorter
                if (shorterPeriod != null && period.order > shorterPeriod.order) {
                    shorterPeriod = period;
                } else {
                    shorterPeriod = period;
                }
            }
        }
        return shorterPeriod;
    }

    /**
     * Return shortest period from target set.
     * @param periodSet
     * @return
     */
    public static Period getShortestPeriod(Set<Period> periodSet) {
        Period shortestPeriod = null;
        for (Period period : periodSet) {
            if (shortestPeriod == null) {
                shortestPeriod = period;
            } else if (period.order < shortestPeriod.order) {
                shortestPeriod = period;
            }
        }
        return shortestPeriod;
    }

    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

    public int getOrder() {
        return order;
    }

    public String getStringName() {
        return stringName;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public ChronoUnit getChronoUnit() {
        return chronoUnit;
    }

    public int getTimeInverval() {
        return inverval;
    }
}
