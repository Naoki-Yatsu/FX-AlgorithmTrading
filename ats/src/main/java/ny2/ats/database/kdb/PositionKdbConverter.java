package ny2.ats.database.kdb;

import java.util.List;

import ny2.ats.core.data.Position;

import com.exxeleron.qjava.QDateTime;

public class PositionKdbConverter implements IKdbConverter<Position> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    private static final String TABLE_NAME = "MarketPosition";

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
    public Object[] convert(List<Position> dataList) {
        int rowCount = dataList.size();
        KdbPosition kdbData = new KdbPosition(rowCount);
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            kdbData.addData(rowIndex, dataList.get(rowIndex));
        }
        return kdbData.toKdbDataObject();
    }

    @Override
    public Object[] convert(Position data) {
        KdbPosition kdbData = new KdbPosition(1);
        kdbData.addData(0, data);
        return kdbData.toKdbDataObject();
    }

    // //////////////////////////////////////
    // Inner Class
    // //////////////////////////////////////

    //
    //    c                 | t f a
    //    ------------------| -----
    //    time              | n
    //    sym               | s   g
    //    netOpenAmount     | i
    //    averagePrice      | f
    //    calcPrice         | f
    //    totalPlJpy        | i
    //    realizedPlJpy     | i
    //    realizedPlCcy2    | f
    //    realizedPlPips    | f
    //    lastExecuteOrderId| j
    //    executeDateTime   | z
    //    createDateTime    | z
    //
    private class KdbPosition {
        // columns
        String[] symbolArray;

        int[] netOpenAmountArray;
        double[] averagePriceArray;
        double[] calcPriceArray;
        int[] totalPlJpyArray;

        int[] realizedPlJpyArray;
        double[] realizedPlCcy2Array;
        double[] realizedPlPipsArray;
        long[] lastExecuteOrderIdArray;

        QDateTime[] executeDateTimeArray;
        QDateTime[] createDateTimeArray;

        public KdbPosition(int rowCount) {
            symbolArray = new String[rowCount];

            netOpenAmountArray = new int[rowCount];
            averagePriceArray = new double[rowCount];
            calcPriceArray = new double[rowCount];
            totalPlJpyArray = new int[rowCount];

            realizedPlJpyArray = new int[rowCount];
            realizedPlCcy2Array = new double[rowCount];
            realizedPlPipsArray = new double[rowCount];
            lastExecuteOrderIdArray = new long[rowCount];

            executeDateTimeArray = new QDateTime[rowCount];
            createDateTimeArray = new QDateTime[rowCount];
        }

        public void addData(int rowIndex, Position data) {
            symbolArray[rowIndex] = KdbUtility.kdbValue(data.getSymbol());

            netOpenAmountArray[rowIndex] = data.getNetOpenAmount();
            averagePriceArray[rowIndex] = data.getAveragePrice();
            calcPriceArray[rowIndex] = data.getCalcPrice();
            totalPlJpyArray[rowIndex] = data.getTotalPlJpy();

            realizedPlJpyArray[rowIndex] = data.getRealizedPlJpy();
            realizedPlCcy2Array[rowIndex] = data.getRealizedPlCcy2();
            realizedPlPipsArray[rowIndex] = data.getRealizedPlPips();
            lastExecuteOrderIdArray[rowIndex] = KdbUtility.kdbValue(data.getLastExecuteOrderId());

            executeDateTimeArray[rowIndex] = KdbUtility.kdbValue(data.getExecuteDateTime());
            createDateTimeArray[rowIndex] = KdbUtility.kdbValue(data.getCreateDateTime());
        }

        public Object[] toKdbDataObject() {
            return new Object[]{
                    symbolArray,

                    netOpenAmountArray,
                    averagePriceArray,
                    calcPriceArray,
                    totalPlJpyArray,

                    realizedPlJpyArray,
                    realizedPlCcy2Array,
                    realizedPlPipsArray,
                    lastExecuteOrderIdArray,

                    executeDateTimeArray,
                    createDateTimeArray};
        }
    }
}
