package ny2.ats.database.kdb;

import java.util.List;

import ny2.ats.core.data.IData;
import ny2.ats.core.exception.ATSRuntimeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface of converter from Data to kdb table
 */
public interface IKdbConverter<T extends IData> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    public static final Logger logger = LoggerFactory.getLogger(IKdbConverter.class);

    public static final char[] EMPTY_CHAR_ARRAY = new char[0];

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    /**
     * Insert target table
     * @return
     */
    public String getTableName();

    /**
     * Convert data to kdb style.
     *
     * @param dataList
     * @return
     */
    public Object[] convert(List<T> dataList);

    /**
     * Convert one data to kdb style.
     *
     * @param data
     * @return
     */
    public Object[] convert(T data);

    /**
     * Convert kdb data to Java object.
     * 元データからdataの列を除いたものを基本とします。
     *
     * @param row
     * @return
     */
    public default T convertFromKdb(Object[] row) {
        new ATSRuntimeException("NOT implemented.");
        return null;
    };
}
