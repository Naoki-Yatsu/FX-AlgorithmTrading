package ny2.ats.database.kdb;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exxeleron.qjava.DateTime;
import com.exxeleron.qjava.QDate;
import com.exxeleron.qjava.QDateTime;
import com.exxeleron.qjava.QException;
import com.exxeleron.qjava.QTime;
import com.exxeleron.qjava.QTimespan;
import com.exxeleron.qjava.QTimestamp;
import com.exxeleron.qjava.QType;

import ny2.ats.core.common.Symbol;
import ny2.ats.core.exception.ATSRuntimeException;
import ny2.ats.core.util.DateTimeUtility;
import ny2.ats.model.ModelVersion;

/**
 * Utility for kdb
 */
public class KdbUtility {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    public static final Logger logger = LoggerFactory.getLogger(KdbUtility.class);

    /** Null String */
    public static final char[] EMPTY_CHAR_ARRAY = new char[0];

    // //////////////////////////////////////
    // Method (Java -> kdb)
    // //////////////////////////////////////

    // [Null Values]
    //
    // put(BOOL, false);
    // put(BYTE, (byte) 0);
    // put(GUID, new UUID(0, 0));
    // put(SHORT, Short.MIN_VALUE);
    // put(INT, Integer.MIN_VALUE);
    // put(LONG, Long.MIN_VALUE);
    // put(FLOAT, Float.NaN);
    // put(DOUBLE, Double.NaN);
    // put(CHAR, ' ');
    // put(SYMBOL, "");
    // put(TIMESTAMP, new QTimestamp(Long.MIN_VALUE));
    // put(MONTH, new QMonth(Integer.MIN_VALUE));
    // put(DATE, new QDate(Integer.MIN_VALUE));
    // put(DATETIME, new QDateTime(Double.NaN));
    // put(TIMESPAN, new QTimespan(Long.MIN_VALUE));
    // put(MINUTE, new QMinute(Integer.MIN_VALUE));
    // put(SECOND, new QSecond(Integer.MIN_VALUE));
    // put(TIME, new QTime(Integer.MIN_VALUE));

    /**
     * Convert from Java:Integer list to kdb:int.
     * @param integerValue
     * @return
     */
    public static Integer kdbValue(Integer integerValue) {
        if (integerValue == null) {
            try {
                return (Integer) QType.getQNull(QType.INT);
            } catch (QException e) {
                logger.error("", e);
            }
        }
        return integerValue;
    }

    /**
     * Convert from Java:int list to kdb:int.<br>
     * You don't need to use this method for primitive type
     * @param value
     * @return
     */
    @Deprecated
    public static int kdbValue(int value) {
        return value;
    }

    /**
     * Convert from Java:Long list to kdb:long.<br>
     * @param longValue
     * @return
     */
    public static Long kdbValue(Long longValue) {
        if (longValue == null) {
            try {
                return (Long) QType.getQNull(QType.LONG);
            } catch (QException e) {
                logger.error("", e);
            }
        }
        return longValue;
    }

    /**
     * Convert from Java:long list to kdb:long.<br>
     * You don't need to use this method for primitive type
     * @param value
     * @return
     */
    @Deprecated
    public static long kdbValue(long value) {
        return value;
    }

    /**
     * Convert from Java:Double list to kdb:float.
     * @param doubleValue
     * @return
     */
    public static Double kdbValue(Double doubleValue) {
        if (doubleValue == null) {
            try {
                return (Double) QType.getQNull(QType.DOUBLE);
            } catch (QException e) {
                logger.error("", e);
            }
        }
        return doubleValue;
    }

    /**
     * Convert from Java:double list to kdb:float.<br>
     * You don't need to use this method for primitive type
     * @deprecated
     * @param value
     * @return
     */
    @Deprecated
    public static double kdbValue(double value) {
        return value;
    }

    /**
     * Convert from Java:enum to kdb:symbol.
     * @param enumValue
     * @return
     */
    public static String kdbValue(Enum<?> enumValue) {
        if (enumValue == null) {
            try {
                return (String) QType.getQNull(QType.SYMBOL);
            } catch (QException e) {
                logger.error("", e);
            }
        }
        return enumValue.name();
    }

    /**
     * Convert from Java:enum list to kdb:symbol list.
     * @param symbolList
     * @return
     */
    public static String[] kdbValues(Set<Symbol> symbolSet) {
        if (symbolSet == null) {
            return new String[0];
        }
        String[] array = new String[symbolSet.size()];
        int i = 0;
        for (Symbol symbol : symbolSet) {
            array[i] = symbol.name();
            i++;
        }
        return array;
    }

    /**
     * Convert from Java:enum list to kdb:symbol list.
     * @param symbolArray
     * @return
     */
    public static String[] kdbValues(Symbol[] symbolArray) {
        if (symbolArray == null) {
            return new String[0];
        }
        String[] array = new String[symbolArray.length];
        for (int i = 0; i < symbolArray.length; i++) {
            array[i] = symbolArray[i].name();
        }
        return array;
    }

    /**
     * Convert from Java:"ModelVersion" to kdb:symbol.
     * @param modelVersion
     * @return
     */
    public static String kdbValue(ModelVersion modelVersion) {
        if (modelVersion == null) {
            try {
                return (String) QType.getQNull(QType.SYMBOL);
            } catch (QException e) {
                logger.error("", e);
            }
        }
        return modelVersion.getName();
    }

    /**
     * Convert from Java:String to kdb:symbol.
     * @param stringValue
     * @return
     */
    public static String kdbValue(String stringValue) {
        if (stringValue == null) {
            try {
                return (String) QType.getQNull(QType.SYMBOL);
            } catch (QException e) {
                logger.error("", e);
            }
        }
        return stringValue;
    }

    /**
     * Convert from Java:String to kdb:string(char list).
     * @param stringValue
     * @return
     */
    public static char[] kdbValueCharList(String stringValue) {
        if (stringValue == null) {
            return EMPTY_CHAR_ARRAY;
        }
        return stringValue.toCharArray();
    }

    /**
     * Convert from Java:LocalDateTime to kdb:datetime.
     * @param dateTime
     * @return
     */
    public static QDateTime kdbValue(LocalDateTime dateTime) {
        if (dateTime == null) {
            try {
                return (QDateTime) QType.getQNull(QType.DATETIME);
            } catch (QException e) {
                logger.error("", e);
            }
        }
        return new QDateTime(DateTimeUtility.toDate(dateTime));
    }

    /**
     * Convert from Java:LocalDateTime array to kdb:datetime list.
     * @param dateTimes
     * @return
     */
    public static QDateTime[] kdbValues(LocalDateTime[] dateTimes) {
        if (dateTimes == null) {
            return new QDateTime[0];
        }
        QDateTime[] qDateTimes = new QDateTime[dateTimes.length];
        for (int i = 0; i < dateTimes.length; i++) {
            qDateTimes[i] = new QDateTime(DateTimeUtility.toDate(dateTimes[i]));
        }
        return qDateTimes;
    }

    /**
     * Convert from Java:LocalDate to kdb:date.
     * @param localDate
     * @return
     */
    public static QDate kdbValue(LocalDate localDate) {
        if (localDate == null) {
            try {
                return (QDate) QType.getQNull(QType.DATE);
            } catch (QException e) {
                logger.error("", e);
            }
        }
        return new QDate(DateTimeUtility.toDate(localDate));
    }

    /**
     * Convert from Java:LocalDate(2) to kdb:date list.
     * @param fromDate
     * @param toDate
     * @return
     */
    public static QDate[] kdbValues(LocalDate fromDate, LocalDate toDate) {
        QDate fromQDate = null;
        QDate toQDate = null;
        if (fromDate == null || toDate == null) {
            // 片方の値があれば、それを両方に使う
            if (fromDate != null) {
                fromQDate = new QDate(DateTimeUtility.toDate(fromDate));
                toQDate = fromQDate;
            } else if (toDate != null) {
                toQDate = new QDate(DateTimeUtility.toDate(toDate));
                fromQDate = toQDate;
            } else {
                try {
                    fromQDate = (QDate) QType.getQNull(QType.DATE);
                    toQDate = (QDate) QType.getQNull(QType.DATE);
                } catch (QException e) {
                    logger.error("", e);
                }
            }
        } else {
            fromQDate = new QDate(DateTimeUtility.toDate(fromDate));
            toQDate = new QDate(DateTimeUtility.toDate(toDate));
        }
        // create array
        QDate[] dates = {fromQDate, toQDate};
        return dates;
    }

    /**
     * Convert from Java:LocalTime to kdb:timespan.
     * @param localTime
     * @return
     */
    public static QTimespan kdbValueTimespan(LocalTime localTime) {
        if (localTime == null) {
            try {
                return (QTimespan) QType.getQNull(QType.TIMESPAN);
            } catch (QException e) {
                logger.error("", e);
            }
        }
        return new QTimespan(localTime.toNanoOfDay());
    }

    /**
     * Convert from Java:LocalDateTime to kdb:timestamp.
     * @param dateTime
     * @return
     */
    public static QTimestamp kdbValueTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            try {
                return (QTimestamp) QType.getQNull(QType.TIMESTAMP);
            } catch (QException e) {
                logger.error("", e);
            }
        }
        return new QTimestamp(DateTimeUtility.toDate(dateTime));
    }

    /**
     * nullに使用する値を取得します。
     * @return
     */
    public static Double getNullValueDouble() {
        try {
            return (Double) QType.getQNull(QType.DOUBLE);
        } catch (QException e) {
            logger.error("", e);
        }
        return null;
    }

    /**
     * nullに使用する値を取得します。
     * @return
     */
    public static String getNullValueSymbol() {
        try {
            return (String) QType.getQNull(QType.SYMBOL);
        } catch (QException e) {
            logger.error("", e);
        }
        return null;
    }

    /**
     * nullに使用する値を取得します。
     * @return
     */
    public static char[] getNullValueCharList() {
        return EMPTY_CHAR_ARRAY;
    }

    /**
     * nullに使用する値を取得します。
     * @return
     */
    public static QDateTime getNullValueDateTime() {
        try {
            return (QDateTime) QType.getQNull(QType.DATETIME);
        } catch (QException e) {
            logger.error("", e);
        }
        return null;
    }



    // //////////////////////////////////////
    // Method (kdb -> Java)
    // //////////////////////////////////////

    protected static final long DAY_MILLIS = 86400000L;
    protected static final long Q_BASE_MILLIS = 10957 * DAY_MILLIS;
    protected static final long Q_BASE_SECS = Q_BASE_MILLIS / 1000;

    /**
     * Kdb-DateTime を LocalDateTime に変換します
     * @param dateTime QDateTime or QTimestamp
     * @return
     */
    public static LocalDateTime convertToLocalDateTime(DateTime dateTime) {
        if (dateTime instanceof QDateTime) {
            return convertToLocalDateTime((QDateTime) dateTime);
        } else if (dateTime instanceof QTimestamp) {
            return convertToLocalDateTime((QTimestamp) dateTime);
        } else {
            throw new ATSRuntimeException(dateTime.getClass().getSimpleName() + " cannot be converted to LocalDateTime.");
        }
    }

    /**
     * kdbオブジェクトをJavaオブジェクトに変換します
     * @param qDateTime
     * @return
     */
    public static LocalDateTime convertToLocalDateTime(QDateTime qDateTime) {
        return LocalDateTime.ofInstant(qDateTime.toDateTime().toInstant(), ZoneId.systemDefault());
    }

    /**
     * kdbオブジェクトをJavaオブジェクトに変換します
     * @param qTimestamp
     * @return
     */
    public static LocalDateTime convertToLocalDateTime(QTimestamp qTimestamp) {
        long seconds = qTimestamp.getValue() / 1_000_000_000;
        int nanos = (int) (qTimestamp.getValue()  - seconds * 1_000_000_000);
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds + Q_BASE_SECS, nanos), DateTimeUtility.ZONEID_UTC);
    }

    /**
     * kdbオブジェクトをJavaオブジェクトに変換します
     * @param qDate
     * @return
     */
    public static LocalDate convertToLocalDate(QDate qDate) {
        return LocalDateTime.ofInstant(qDate.toDateTime().toInstant(), ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * kdbオブジェクトをJavaオブジェクトに変換します
     * @param qTime
     * @return
     */
    public static LocalTime convertToLocalTime(QTime qTime) {
        return LocalDateTime.ofInstant(qTime.toDateTime().toInstant(), ZoneId.systemDefault()).toLocalTime();
    }

    /**
     * kdbオブジェクトをJavaオブジェクトに変換します
     * @param qTimespan
     * @return
     */
    public static LocalTime convertToLocalTime(QTimespan qTimespan) {
        return LocalDateTime.ofInstant(qTimespan.toDateTime().toInstant(), ZoneId.systemDefault()).toLocalTime();
    }

}
