package ny2.ats.database.kdb;

import java.util.List;

import ny2.ats.core.data.PLInformation;

import com.exxeleron.qjava.QDateTime;

public class PLInformationKdbConverter implements IKdbConverter<PLInformation> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    private static final String TABLE_NAME = "PLInformation";

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
    public Object[] convert(List<PLInformation> dataList) {
        int rowCount = dataList.size();
        KdbPLInformation kdbData = new KdbPLInformation(rowCount);
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            kdbData.addData(rowIndex, dataList.get(rowIndex));
        }
        return kdbData.toKdbDataObject();
    }

    @Override
    public Object[] convert(PLInformation data) {
        KdbPLInformation kdbData = new KdbPLInformation(1);
        kdbData.addData(0, data);
        return kdbData.toKdbDataObject();
    }

    // //////////////////////////////////////
    // Inner Class
    // //////////////////////////////////////

    //
    //    c                  | t f a
    //    -------------------| -----
    //    time               | n
    //    sym                | s
    //    modelType          | s
    //    modelVersion       | s
    //    plJpy              | i
    //    plDetail           |
    //    netAmountDetail    |
    //    netAmountCcy       |
    //    reportDateTime     | z
    //    createDateTime     | z
    //
    private class KdbPLInformation {
        // columns
        String[] symbolArray;
        String[] modelTypeArrayArray;
        String[] modelVersionArrayArray;

        int[] plJpyArray;
        Object[] plDetailArray;
        Object[] netAmountDetailArray;
        Object[] netAmountCcyArray;

        QDateTime[] reportDateTimeArray;
        QDateTime[] createDateTimeArray;

        public KdbPLInformation(int rowCount) {
            symbolArray = new String[rowCount];
            modelTypeArrayArray = new String[rowCount];
            modelVersionArrayArray = new String[rowCount];

            plJpyArray = new int[rowCount];
            plDetailArray = new Object[rowCount];
            netAmountDetailArray = new Object[rowCount];
            netAmountCcyArray = new Object[rowCount];

            reportDateTimeArray = new QDateTime[rowCount];
            createDateTimeArray = new QDateTime[rowCount];
        }

        public void addData(int rowIndex, PLInformation data) {
            symbolArray[rowIndex] = KdbUtility.kdbValue(data.getPlInformationType());
            modelTypeArrayArray[rowIndex] = KdbUtility.kdbValue(data.getModelType());
            modelVersionArrayArray[rowIndex] = KdbUtility.kdbValue(data.getModelVersion());

            plJpyArray[rowIndex] = data.getPlJpy();
            plDetailArray[rowIndex] = KdbUtility.kdbValueCharList(data.getPlDetail());
            netAmountDetailArray[rowIndex] = KdbUtility.kdbValueCharList(data.getNetAmountDetail());
            netAmountCcyArray[rowIndex] = KdbUtility.kdbValueCharList(data.getNetAmountCcy());

            reportDateTimeArray[rowIndex] = KdbUtility.kdbValue(data.getReportDateTime());
            createDateTimeArray[rowIndex] = KdbUtility.kdbValue(data.getCreateDateTime());
        }

        public Object[] toKdbDataObject() {
            return new Object[]{
                    symbolArray,
                    modelTypeArrayArray,
                    modelVersionArrayArray,
                    plJpyArray,
                    plDetailArray,
                    netAmountDetailArray,
                    netAmountCcyArray,
                    reportDateTimeArray,
                    createDateTimeArray};
        }
    }
}
