package ny2.ats.database.kdb;

import java.util.List;

import ny2.ats.core.data.IndicatorInformation;

import com.exxeleron.qjava.QDateTime;

/**
 * Indicator用のkdb converter<br>
 * ※Indicatorは1つのデータを複数行に分割するため特殊処理を行う。
 */
public class IndicatorformationKdbConverter implements IKdbConverter<IndicatorInformation> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    private static final String TABLE_NAME = "IndicatorInformation";

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public Object[] convert(List<IndicatorInformation> dataList) {
        int rowCount = dataList.size();
        KdbIndicatorInformation kdbData = new KdbIndicatorInformation(rowCount);
        for (int rowIndex=0; rowIndex < rowCount; rowIndex++) {
            kdbData.addData(rowIndex, dataList.get(rowIndex));
        }
        return kdbData.toKdbDataObject();
    }

    @Override
    public Object[] convert(IndicatorInformation data) {
        KdbIndicatorInformation kdbData = new KdbIndicatorInformation(1);
        kdbData.addData(0, data);
        return kdbData.toKdbDataObject();
    }

    // //////////////////////////////////////
    // Inner Class
    // //////////////////////////////////////

    /**
     * IndicatorInformation
     *  c             | t f a
     *  --------------| -----
     *  time          | n
     *  sym           | s
     *  indicatorType | s
     *  period        | s
     *  dataString    | C
     *  reportDateTime| z
     *  createDateTime| z
     */
    private class KdbIndicatorInformation {
        // columns
        String[] symbolArray;
        String[] indicatorTypeArray;
        String[] periodArray;
        Object[] dataStringeArray;
        QDateTime[] reportDateTimeArray;
        QDateTime[] createDateTimeArray;

        public KdbIndicatorInformation(int rowCount) {

            symbolArray = new String[rowCount];
            indicatorTypeArray = new String[rowCount];
            periodArray = new String[rowCount];
            dataStringeArray = new Object[rowCount];
            reportDateTimeArray = new QDateTime[rowCount];
            createDateTimeArray = new QDateTime[rowCount];
        }

        public void addData(int rowIndex, IndicatorInformation data) {
            symbolArray[rowIndex] = KdbUtility.kdbValue(data.getSymbol());
            indicatorTypeArray[rowIndex] = KdbUtility.kdbValue(data.getType());
            periodArray[rowIndex] = KdbUtility.kdbValue(data.getPeriod());
            dataStringeArray[rowIndex] = KdbUtility.kdbValueCharList(data.getDataString());
            reportDateTimeArray[rowIndex] = KdbUtility.kdbValue(data.getDateTime());
            createDateTimeArray[rowIndex] = KdbUtility.kdbValue(data.getCreateDateTime());
        }

        public Object[] toKdbDataObject() {
            return new Object[] {
                    symbolArray,
                    indicatorTypeArray,
                    periodArray,
                    dataStringeArray,
                    reportDateTimeArray,
                    createDateTimeArray };
        }
    }
}
