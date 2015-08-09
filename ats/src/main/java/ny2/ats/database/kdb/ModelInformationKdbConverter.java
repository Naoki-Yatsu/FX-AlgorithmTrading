package ny2.ats.database.kdb;

import java.util.List;

import ny2.ats.core.data.ModelInformation;

import com.exxeleron.qjava.QDateTime;

public class ModelInformationKdbConverter implements IKdbConverter<ModelInformation> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    private static final String TABLE_NAME = "ModelInformation";

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
    public Object[] convert(List<ModelInformation> dataList) {
        int rowCount = dataList.size();
        KdbModelInformation kdbData = new KdbModelInformation(rowCount);
        for (int rowIndex=0; rowIndex < rowCount; rowIndex++) {
            kdbData.addData(rowIndex, dataList.get(rowIndex));
        }
        return kdbData.toKdbDataObject();
    }

    @Override
    public Object[] convert(ModelInformation data) {
        KdbModelInformation kdbData = new KdbModelInformation(1);
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
    //    modelType     | s
    //    modelVersion  | s
    //    information1  |
    //    information2  |
    //    information3  |
    //    information4  |
    //    information5  |
    //    reportDateTime| z
    //    createDateTime| z
    //
    private class KdbModelInformation {
        // columns
        String[] symbolArray;
        String[] modelTypeArrayArray;
        String[] modelVersionArrayArray;

        Object[] information1Array;
        Object[] information2Array;
        Object[] information3Array;
        Object[] information4Array;
        Object[] information5Array;

        QDateTime[] reportDateTimeArray;
        QDateTime[] createDateTimeArray;

        public KdbModelInformation(int rowCount) {
            symbolArray = new String[rowCount];
            modelTypeArrayArray = new String[rowCount];
            modelVersionArrayArray = new String[rowCount];

            information1Array = new Object[rowCount];
            information2Array = new Object[rowCount];
            information3Array = new Object[rowCount];
            information4Array = new Object[rowCount];
            information5Array = new Object[rowCount];

            reportDateTimeArray = new QDateTime[rowCount];
            createDateTimeArray = new QDateTime[rowCount];
        }

        public void addData(int rowIndex, ModelInformation data) {
            symbolArray[rowIndex] = KdbUtility.kdbValue(data.getModelInfromationType());
            modelTypeArrayArray[rowIndex] = KdbUtility.kdbValue(data.getModelType());
            modelVersionArrayArray[rowIndex] = KdbUtility.kdbValue(data.getModelVersion());

            information1Array[rowIndex] = KdbUtility.kdbValueCharList(data.getInformation1());
            information2Array[rowIndex] = KdbUtility.kdbValueCharList(data.getInformation2());
            information3Array[rowIndex] = KdbUtility.kdbValueCharList(data.getInformation3());
            information4Array[rowIndex] = KdbUtility.kdbValueCharList(data.getInformation4());
            information5Array[rowIndex] = KdbUtility.kdbValueCharList(data.getInformation5());

            reportDateTimeArray[rowIndex] = KdbUtility.kdbValue(data.getReportDateTime());
            createDateTimeArray[rowIndex] = KdbUtility.kdbValue(data.getCreateDateTime());
        }

        public Object[] toKdbDataObject() {
            return new Object[]{
                    symbolArray,
                    modelTypeArrayArray,
                    modelVersionArrayArray,
                    information1Array,
                    information2Array,
                    information3Array,
                    information4Array,
                    information5Array,
                    reportDateTimeArray,
                    createDateTimeArray};
        }
    }
}
