package ny2.ats.database.kdb;

import java.util.List;

import ny2.ats.core.data.SystemInformation;

import com.exxeleron.qjava.QDateTime;

public class SystemInformationKdbConverter implements IKdbConverter<SystemInformation> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    private static final String TABLE_NAME = "SystemInformation";

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
    public Object[] convert(List<SystemInformation> dataList) {
        int rowCount = dataList.size();
        KdbSystemInformation kdbData = new KdbSystemInformation(rowCount);
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            kdbData.addData(rowIndex, dataList.get(rowIndex));
        }
        return kdbData.toKdbDataObject();
    }

    @Override
    public Object[] convert(SystemInformation data) {
        KdbSystemInformation kdbData = new KdbSystemInformation(1);
        kdbData.addData(0, data);
        return kdbData.toKdbDataObject();
    }

    // //////////////////////////////////////
    // Inner Class
    // //////////////////////////////////////

    //
    //    c             | t f a
    //    --------------| -----
    //    time          | n
    //    sym           | s
    //    message       |
    //    reportDateTime| z
    //    createDateTime| z
    //
    private class KdbSystemInformation {
        // columns
        String[] symbolArray;
        Object[] messageArray;
        QDateTime[] reportDateTimeArray;
        QDateTime[] createDateTimeArray;

        public KdbSystemInformation(int rowCount) {
            symbolArray = new String[rowCount];
            messageArray = new Object[rowCount];
            reportDateTimeArray = new QDateTime[rowCount];
            createDateTimeArray = new QDateTime[rowCount];
        }

        public void addData(int rowIndex, SystemInformation data) {
            symbolArray[rowIndex] = KdbUtility.kdbValue(data.getInfromationType());
            messageArray[rowIndex] = KdbUtility.kdbValueCharList(data.getMessage());
            reportDateTimeArray[rowIndex] = KdbUtility.kdbValue(data.getReportDateTime());
            createDateTimeArray[rowIndex] = KdbUtility.kdbValue(data.getCreateDateTime());
        }

        public Object[] toKdbDataObject() {
            return new Object[] {
                    symbolArray,
                    messageArray,
                    reportDateTimeArray,
                    createDateTimeArray };
        }
    }
}
