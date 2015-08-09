package ny2.ats.database.kdb;

import java.util.List;

import com.exxeleron.qjava.QDateTime;

import ny2.ats.core.data.OptimizedExecution;

public class OptimizedExecutionKdbConverter implements IKdbConverter<OptimizedExecution> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    private static final String TABLE_NAME = "OptimizedExecution";

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
    public Object[] convert(List<OptimizedExecution> dataList) {
        int rowCount = dataList.size();
        KdbOptimizedExecution kdbData = new KdbOptimizedExecution(rowCount);
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            kdbData.addData(rowIndex, dataList.get(rowIndex));
        }
        return kdbData.toKdbDataObject();
    }

    @Override
    public Object[] convert(OptimizedExecution data) {
        KdbOptimizedExecution kdbData = new KdbOptimizedExecution(1);
        kdbData.addData(0, data);
        return kdbData.toKdbDataObject();
    }

    // //////////////////////////////////////
    // Inner Class
    // //////////////////////////////////////

    /**
     * c              | t f a
     * ---------------| -----
     * time           | n
     * sym            | s
     * method         | s
     * marketType     | s
     * orderId        | j
     * side           | s
     * orderPrice     | f
     * operatePrice   | f
     * executePrice   | f
     * orderQuoteId   |
     * operateQuoteId |
     * signalOrder    | f
     * signalOperate  | f
     * orderDateTime  | z
     * operateDateTime| z
     * executeDateTime| z
     * createDateTime | z
     *
     */
    private class KdbOptimizedExecution {
        // columns
        String[] symbolArray;
        String[] methodArray;
        String[] marketTypeArray;
        // Array of char[]
        long[] orderIdArrayArray;

        String[] sideArray;
        double[] orderPriceArray;
        double[] operatePriceArray;
        double[] executePriceArray;

        Object[] orderQuoteIdArray;
        Object[] operateQuoteIdArray;

        double[] signalOrderArray;
        double[] signalOperateArray;

        QDateTime[] orderDateTimeArray;
        QDateTime[] operateDateTimeArray;
        QDateTime[] executeDateTimeArray;
        QDateTime[] createDateTimeArray;

        public KdbOptimizedExecution(int rowCount) {
            symbolArray = new String[rowCount];
            methodArray = new String[rowCount];
            marketTypeArray = new String[rowCount];
            orderIdArrayArray = new long[rowCount];

            sideArray = new String[rowCount];
            orderPriceArray = new double[rowCount];
            operatePriceArray = new double[rowCount];
            executePriceArray = new double[rowCount];

            orderQuoteIdArray = new Object[rowCount];
            operateQuoteIdArray = new Object[rowCount];

            signalOrderArray = new double[rowCount];
            signalOperateArray = new double[rowCount];

            orderDateTimeArray = new QDateTime[rowCount];
            operateDateTimeArray = new QDateTime[rowCount];
            executeDateTimeArray = new QDateTime[rowCount];
            createDateTimeArray = new QDateTime[rowCount];
        }

        public void addData(int rowIndex, OptimizedExecution data) {
            symbolArray[rowIndex] = KdbUtility.kdbValue(data.getSymbol());
            methodArray[rowIndex] = KdbUtility.kdbValue(data.getOptimizerMethod());
            marketTypeArray[rowIndex] = KdbUtility.kdbValue(data.getMarketType());
            orderIdArrayArray[rowIndex] = KdbUtility.kdbValue(data.getOrderId());

            sideArray[rowIndex] = KdbUtility.kdbValue(data.getSide());
            orderPriceArray[rowIndex] = data.getOrderPrice();
            operatePriceArray[rowIndex] = data.getOperatePrice();
            executePriceArray[rowIndex] = data.getExecutePrice();

            orderQuoteIdArray[rowIndex] = KdbUtility.kdbValueCharList(data.getOrderQuoteId());
            operateQuoteIdArray[rowIndex] = KdbUtility.kdbValueCharList(data.getOperateQuoteId());

            signalOrderArray[rowIndex] = data.getSignalOrder();
            signalOperateArray[rowIndex] = data.getSignalOperate();

            orderDateTimeArray[rowIndex] = KdbUtility.kdbValue(data.getOrderDateTime());
            operateDateTimeArray[rowIndex] = KdbUtility.kdbValue(data.getOperateDateTime());
            executeDateTimeArray[rowIndex] = KdbUtility.kdbValue(data.getExecuteDateTime());
            createDateTimeArray[rowIndex] = KdbUtility.kdbValue(data.getCreateDateTime());
        }

        public Object[] toKdbDataObject() {
            return new Object[]{
                    symbolArray,
                    methodArray,
                    marketTypeArray,
                    orderIdArrayArray,

                    sideArray,
                    orderPriceArray,
                    operatePriceArray,
                    executePriceArray,

                    orderQuoteIdArray,
                    operateQuoteIdArray,

                    signalOrderArray,
                    signalOperateArray,

                    orderDateTimeArray,
                    operateDateTimeArray,
                    executeDateTimeArray,
                    createDateTimeArray};
        }
    }
}
