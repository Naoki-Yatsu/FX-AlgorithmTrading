package ny2.ats.core.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateTimeUtility {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    public static final Logger LOGGER = LoggerFactory.getLogger(DateTimeUtility.class);

    public static final ZoneId DEFAULT_ZONEID = ZoneId.systemDefault();
    public static final ZoneId ZONEID_JST = ZoneId.of("Asia/Tokyo");
    public static final ZoneId ZONEID_UTC = ZoneId.of("UTC");

    public static final ZoneOffset ZONEOFFSET_JST = ZoneOffset.of("+09:00");
    public static final ZoneOffset ZONEOFFSET_UTC = ZoneOffset.UTC;

    public static final String DATE_FORMAT_KDB = "yyyy.MM.dd";
    public static final String DATETIME_FORMAT_KDB = "yyyy.MM.dd'T'HH:mm:ss.SSS";

    public static final DateTimeFormatter DATETIME_FORMATTER_FIX = DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss.SSS");

    public static final DateTimeFormatter DATETIME_FORMATTER_UNDER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");
    public static final DateTimeFormatter DATE_FORMATTER_SIMPLE = DateTimeFormatter.ofPattern("yyyyMMdd");

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    public static String toStringIfPresent(LocalDateTime dateTime) {
        if (dateTime != null) {
            return dateTime.toString();
        } else {
            return null;
        }
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), DEFAULT_ZONEID);
    }

    public static Date toDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        ZonedDateTime zonedDateTime = dateTime.atZone(DEFAULT_ZONEID);
        return Date.from(zonedDateTime.toInstant());
    }

    public static Date toDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        ZonedDateTime zonedDateTime = localDate.atStartOfDay(DEFAULT_ZONEID);
        return Date.from(zonedDateTime.toInstant());
    }

    public static Date toDateUTCFromJST(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        ZonedDateTime zonedDateTime = dateTime.atZone(ZONEID_JST);
        return Date.from(zonedDateTime.toInstant());
    }

    public static Date toDate(ZonedDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return Date.from(dateTime.toInstant());
    }

    public static Calendar toCalendar(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        Date date = toDate(localDate);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);;
        return cal;
    }

    public static Calendar toCalendar(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        Date date = toDate(dateTime);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);;
        return cal;
    }

    public static DateTimeFormatter createDateTimeFormatter(String format) {
        return DateTimeFormatter.ofPattern(format);
    }

    /**
     * FIXの時刻をUTCのLoalDataTimeで返します。
     *
     * @param fixTimestamp
     * @return
     */
    public static LocalDateTime parseFIXDateTimeUTC(String fixTimestamp) {
        return LocalDateTime.parse(fixTimestamp, DATETIME_FORMATTER_FIX);
    }

    /**
     * FIXの時刻をJSTのLoalDataTimeで返します。
     *
     * @param fixTimestamp
     * @return
     */
    public static LocalDateTime parseFIXDateTimeJST(String fixTimestamp) {
        return parseFIXDateTimeUTC(fixTimestamp).plusHours(9);
    }

    /**
     * FIXの時刻をUTCのZonedDateTimeで返します。
     *
     * @param fixTimestamp
     * @return
     */
    public static ZonedDateTime parseFIXDateTimeUTCZone(String fixTimestamp) {
        return ZonedDateTime.of(parseFIXDateTimeUTC(fixTimestamp), ZONEID_UTC);
    }

    // //////////////////////////////////////
    // Method (過去データ解析用)
    // //////////////////////////////////////

    /** longの下位32bitを取得するマスク */
    private static final long LONG_INT_MASK = 0x00000000ffffffffL;

    /**
     * LocalDateTimeをエポックミリ時刻に変換し、intの配列として返します。
     * @param dateTime
     * @return int[2] - 0:上位32bit, 1:下位32bit
     */
    public static int[] parseIntArray(LocalDateTime dateTime) {
        long l = dateTime.toInstant(ZONEOFFSET_JST).toEpochMilli();
        return devideLongToInt(l);
    }

    /**
     * longを上位、下位32bitずつ2つのintに分割します
     *
     * @param l
     * @return
     */
   private static int[] devideLongToInt(long l) {
       int lower = (int) (l & LONG_INT_MASK);
       int upper = (int) (l >>> 32);
       int[] devided = {upper, lower};
       return devided;
   }

   /**
    * 上位、下位32bitずつ2つintに分割されたlongを復元します。
    *
    * @param int1
    * @param int2
    * @return
    */
  public static long mergeIntToLong(int int1, int int2) {
      return ((long)int1 << 32) + Integer.toUnsignedLong(int2);
  }

   /**
    * int[2]に分割されたエポックミリ時刻からLocalDataTimeを復元します。
    * @param intEpochTime1 上位32bit
    * @param intEpochTime2 下位32bit
    * @return
    */
   public static LocalDateTime restoreFromIntArray(int intEpochTime1, int intEpochTime2) {
       long epochTime = mergeIntToLong(intEpochTime1, intEpochTime2);
       return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochTime), ZONEOFFSET_JST);
   }

   /**
    * int[2]に分割されたエポックミリ時刻間の時間差を計算します。
    * @param startTime1
    * @param startTime2
    * @param endTime1
    * @param endTime2
    * @return
    */
   public static long calcDiffrenceTime(int startTime1, int startTime2, int endTime1, int endTime2) {
       long startTime = mergeIntToLong(startTime1, startTime2);
       long endTime = mergeIntToLong(endTime1, endTime2);
       long diff = endTime - startTime;
       return diff;
   }
}
