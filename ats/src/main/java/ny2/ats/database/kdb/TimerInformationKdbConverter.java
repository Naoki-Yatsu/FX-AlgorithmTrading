package ny2.ats.database.kdb;

import java.util.List;

import ny2.ats.core.data.TimerInformation;

import com.exxeleron.qjava.QDateTime;

public class TimerInformationKdbConverter implements IKdbConverter<TimerInformation> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    private static final String TABLE_NAME = "TimerInformation";

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
    public Object[] convert(List<TimerInformation> dataList) {
        int rowCount = dataList.size();
        KdbTimerInformation kdbData = new KdbTimerInformation(rowCount);
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            kdbData.addData(rowIndex, dataList.get(rowIndex));
        }
        return kdbData.toKdbDataObject();
    }

    @Override
    public Object[] convert(TimerInformation data) {
        KdbTimerInformation kdbData = new KdbTimerInformation(1);
        kdbData.addData(0, data);
        return kdbData.toKdbDataObject();
    }

    // //////////////////////////////////////
    // Inner Class
    // //////////////////////////////////////

    //
    //    c              | t f a
    //    ---------------| -----
    //    time           | n
    //    sym            | s
    //    currentDateTime| z
    //    nextDateTime   | z
    //    createDateTime | z
    //
    private class KdbTimerInformation {
        // columns
        String[] symbolArray;
        QDateTime[] currentDateTimeArray;
        QDateTime[] nextDateTimeArray;
        QDateTime[] createDateTimeArray;

        public KdbTimerInformation(int rowCount) {
            symbolArray = new String[rowCount];
            currentDateTimeArray = new QDateTime[rowCount];
            nextDateTimeArray = new QDateTime[rowCount];
            createDateTimeArray = new QDateTime[rowCount];
        }

        public void addData(int rowIndex, TimerInformation data) {
            symbolArray[rowIndex] = KdbUtility.kdbValue(data.getPeriod());
            currentDateTimeArray[rowIndex] = KdbUtility.kdbValue(data.getCurrentDateTime());
            nextDateTimeArray[rowIndex] = KdbUtility.kdbValue(data.getNextDateTime());
            createDateTimeArray[rowIndex] = KdbUtility.kdbValue(data.getCreateDateTime());
        }

        public Object[] toKdbDataObject() {
            return new Object[]{
                    symbolArray,
                    currentDateTimeArray,
                    nextDateTimeArray,
                    createDateTimeArray};
        }
    }
}
