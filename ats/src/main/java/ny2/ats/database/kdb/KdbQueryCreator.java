package ny2.ats.database.kdb;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import ny2.ats.core.common.Symbol;
import ny2.ats.core.util.DateTimeUtility;

/**
 * kdb用のQueryを作成するクラスです
 */
public class KdbQueryCreator {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** KDB Date formatter */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeUtility.createDateTimeFormatter(DateTimeUtility.DATE_FORMAT_KDB);

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    /**
     * 以下の形のクエリを作成します。<br>
     * select from [tablename] where date=date,
     *
     * @param selectBase
     * @param tablename
     * @param date
     * @return
     */
    public static String createQuery(String selectBase, String tablename, LocalDate date) {
        StringBuilder sb = new StringBuilder();
        sb.append(selectBase)
                .append(" from ")
                .append(tablename)
                .append(" where date=")
                .append(date.format(DATE_FORMATTER));
        return sb.toString();
    }

    /**
     * 以下の形のクエリを作成します。<br>
     * select from [tablename] where date within date1 date2
     *
     * @param selectBase
     * @param tablename
     * @param date1
     * @param date2
     * @return
     */
    public static String createQuery(String selectBase, String tablename, LocalDate date1, LocalDate date2) {
        StringBuilder sb = new StringBuilder();
        sb.append(selectBase)
                .append(" from ")
                .append(tablename)
                .append(" where date within ")
                .append(date1.format(DATE_FORMATTER))
                .append(" ")
                .append(date2.format(DATE_FORMATTER));
        return sb.toString();
    }

    /**
     * 以下の形のクエリを作成します。<br>
     * select from [tablename] where date=date, sym=`sym
     *
     * @param selectBase
     * @param tablename
     * @param date
     * @param symbol
     * @return
     */
    public static String createQuery(String selectBase, String tablename, LocalDate date, Symbol symbol) {
        StringBuilder sb = new StringBuilder();
        sb.append(selectBase)
                .append(" from ")
                .append(tablename)
                .append(" where date=")
                .append(date.format(DATE_FORMATTER))
                .append(", sym=`")
                .append(symbol.name());
        return sb.toString();
    }

    /**
     * 以下の形のクエリを作成します。<br>
     * select from [tablename] where date within date1 date2, sym=`symbol
     *
     * @param selectBase
     * @param tablename
     * @param date1
     * @param date2
     * @param symbol
     * @return
     */
    public static  String createQuery(String selectBase, String tablename, LocalDate date1, LocalDate date2, Symbol symbol) {
        StringBuilder sb = new StringBuilder();
        sb.append(selectBase)
                .append(" from ")
                .append(tablename)
                .append(" where date within ")
                .append(date1.format(DATE_FORMATTER))
                .append(" ")
                .append(date2.format(DATE_FORMATTER))
                .append(", sym=`")
                .append(symbol.name());
        return sb.toString();
    }

    /**
     * 以下の形のクエリを作成します。<br>
     * select from [tablename] where date within date1 date2, sym in `S1`S2..
     *
     * @param selectBase
     * @param tablename
     * @param date1
     * @param date2
     * @param date2
     * @return symbolList
     */
    public static  String createQuery(String selectBase, String tablename, LocalDate date1, LocalDate date2, Set<Symbol> symbolSet) {
        StringBuilder sb = new StringBuilder();
        sb.append(selectBase)
                .append(" from ")
                .append(tablename)
                .append(" where date within ")
                .append(date1.format(DATE_FORMATTER))
                .append(" ")
                .append(date2.format(DATE_FORMATTER))
                .append(", sym in ");
        for (Symbol symbol : symbolSet) {
            sb.append("`").append(symbol.name());
        }
        return sb.toString();
    }

}
