package ny2.ats.database.kdb;

import java.util.List;

import ny2.ats.core.data.Order;

import com.exxeleron.qjava.QDateTime;

public class OrderKdbConverter implements IKdbConverter<Order> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    private static final String TABLE_NAME = "MarketOrder";

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
    public Object[] convert(List<Order> dataList) {
        int rowCount = dataList.size();
        KdbOrder kdbData = new KdbOrder(rowCount);
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            kdbData.addData(rowIndex, dataList.get(rowIndex));
        }
        return kdbData.toKdbDataObject();
    }

    @Override
    public Object[] convert(Order data) {
        KdbOrder kdbData = new KdbOrder(1);
        kdbData.addData(0, data);
        return kdbData.toKdbDataObject();
    }

    // //////////////////////////////////////
    // Inner Class
    // //////////////////////////////////////

    /**
     *
     * c                | t f a
     * -----------------| -----
     * time             | n
     * sym              | s   g
     * marketType       | s
     * orderId          | j
     * orderAction      | s
     * orderStatus      | s
     * modelType        | s
     * modelVersion     | s
     * side             | s
     * orderType        | s
     * orderPrice       | f
     * orderAmount      | i
     * quoteId          |
     * marketPositionId |
     * executePrice     | f
     * executeAmount    | i
     * originalOrderId  | j
     * originalMarketId |
     * orderDateTime    | z
     * marketDateTime   | z
     * createDateTime   | z
     *
     */
    private class KdbOrder {
        // columns
        String[] symbolArray;
        String[] marketTypeArray;
        // Array of char[]
        long[] orderIdArrayArray;

        String[] orderActionArrayArray;
        String[] orderStatuseArrayArray;
        String[] modelTypeArrayArray;
        String[] modelVersionArrayArray;

        String[] sideArray;
        String[] orderTypeArray;
        double[] orderPriceArray;
        int[] orderAmountArray;
        Object[] quoteIdArray;

        Object[] marketPositionIdArrayArray;
        double[] executePriceArray;
        int[] executeAmountArray;
        long[] originalOrderIdArray;
        Object[] originalMarketIdArray;

        QDateTime[] orderDateTimeArray;
        QDateTime[] marketDateTimeArray;
        QDateTime[] createDateTimeArray;

        public KdbOrder(int rowCount) {
            symbolArray = new String[rowCount];
            marketTypeArray = new String[rowCount];
            orderIdArrayArray = new long[rowCount];

            orderActionArrayArray = new String[rowCount];
            orderStatuseArrayArray = new String[rowCount];
            modelTypeArrayArray = new String[rowCount];
            modelVersionArrayArray = new String[rowCount];

            sideArray = new String[rowCount];
            orderTypeArray = new String[rowCount];
            orderPriceArray = new double[rowCount];
            orderAmountArray = new int[rowCount];
            quoteIdArray = new Object[rowCount];

            marketPositionIdArrayArray = new Object[rowCount];
            executePriceArray = new double[rowCount];
            executeAmountArray = new int[rowCount];
            originalOrderIdArray = new long[rowCount];
            originalMarketIdArray = new Object[rowCount];

            orderDateTimeArray = new QDateTime[rowCount];
            marketDateTimeArray = new QDateTime[rowCount];
            createDateTimeArray = new QDateTime[rowCount];
        }

        public void addData(int rowIndex, Order data) {
            symbolArray[rowIndex] = KdbUtility.kdbValue(data.getSymbol());
            marketTypeArray[rowIndex] = KdbUtility.kdbValue(data.getMarketType());
            orderIdArrayArray[rowIndex] = KdbUtility.kdbValue(data.getOrderId());

            orderActionArrayArray[rowIndex] = KdbUtility.kdbValue(data.getOrderAction());
            orderStatuseArrayArray[rowIndex] = KdbUtility.kdbValue(data.getOrderStatus());
            modelTypeArrayArray[rowIndex] = KdbUtility.kdbValue(data.getModelType());
            modelVersionArrayArray[rowIndex] = KdbUtility.kdbValue(data.getModelVersion());

            sideArray[rowIndex] = KdbUtility.kdbValue(data.getSide());
            orderTypeArray[rowIndex] = KdbUtility.kdbValue(data.getOrderType());
            orderPriceArray[rowIndex] = data.getOrderPrice();
            orderAmountArray[rowIndex] = data.getOrderAmount();
            quoteIdArray[rowIndex] = KdbUtility.kdbValueCharList(data.getQuoteId());

            marketPositionIdArrayArray[rowIndex] = KdbUtility.kdbValueCharList(data.getMarketPositionId());
            executePriceArray[rowIndex] = data.getExecutePrice();
            executeAmountArray[rowIndex] = data.getExecuteAmount();
            originalOrderIdArray[rowIndex] = KdbUtility.kdbValue(data.getOriginalOrderId());
            originalMarketIdArray[rowIndex] = KdbUtility.kdbValueCharList(data.getOriginalMarketId());

            orderDateTimeArray[rowIndex] = KdbUtility.kdbValue(data.getOrderDateTime());
            marketDateTimeArray[rowIndex] = KdbUtility.kdbValue(data.getMarketDateTime());
            createDateTimeArray[rowIndex] = KdbUtility.kdbValue(data.getCreateDateTime());
        }

        public Object[] toKdbDataObject() {
            return new Object[]{
                    symbolArray,
                    marketTypeArray,
                    orderIdArrayArray,
                    orderActionArrayArray,
                    orderStatuseArrayArray,
                    modelTypeArrayArray,
                    modelVersionArrayArray,

                    sideArray,
                    orderTypeArray,
                    orderPriceArray,
                    orderAmountArray,
                    quoteIdArray,

                    marketPositionIdArrayArray,
                    executePriceArray,
                    executeAmountArray,
                    originalOrderIdArray,
                    originalMarketIdArray,

                    orderDateTimeArray,
                    marketDateTimeArray,
                    createDateTimeArray};
        }
    }
}
