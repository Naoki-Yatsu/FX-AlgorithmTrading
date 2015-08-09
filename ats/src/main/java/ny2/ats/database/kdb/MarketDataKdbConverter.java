package ny2.ats.database.kdb;

import java.util.List;

import ny2.ats.core.data.MarketData;

import com.exxeleron.qjava.QDateTime;

public class MarketDataKdbConverter implements IKdbConverter<MarketData> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    public static final String TABLE_NAME = "MarketData";

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
    public Object[] convert(List<MarketData> dataList) {
        int rowCount = dataList.size();
        KdbMarketData kdbData = new KdbMarketData(rowCount);
        for (int rowIndex=0; rowIndex < rowCount; rowIndex++) {
            kdbData.addData(rowIndex, dataList.get(rowIndex));
        }
        return kdbData.toKdbDataObject();
    }

    @Override
    public Object[] convert(MarketData data) {
        KdbMarketData kdbData = new KdbMarketData(1);
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
    //    sym           | s   g
    //    marketType    | s
    //    quoteId       | C
    //    bidPrice      | f
    //    askPrice      | f
    //    bidAmount     | i
    //    askAmount     | i
    //    quoteCondition| b
    //    marketDateTime| z
    //    createDateTime| z
    //
    private class KdbMarketData {
        // columns
        String[] symbolArray;
        String[] marketTypeArray;
        // Array of char[]
        Object[] quoteIdArray;
        double[] bidPriceArray;
        double[] askPriceArray;
        int[] bidAmountArray;
        int[] askAmountArray;
        boolean[] quoteConditionArray;
        QDateTime[] marketDateTimeArray;
        QDateTime[] createDateTimeArray;

        public KdbMarketData(int rowCount) {
            symbolArray = new String[rowCount];
            marketTypeArray = new String[rowCount];
            quoteIdArray = new Object[rowCount];
            bidPriceArray = new double[rowCount];
            askPriceArray = new double[rowCount];
            bidAmountArray = new int[rowCount];
            askAmountArray = new int[rowCount];
            quoteConditionArray = new boolean[rowCount];
            marketDateTimeArray = new QDateTime[rowCount];
            createDateTimeArray = new QDateTime[rowCount];
        }

        public void addData(int rowIndex, MarketData data) {
            symbolArray[rowIndex] = KdbUtility.kdbValue(data.getSymbol());
            marketTypeArray[rowIndex] = KdbUtility.kdbValue(data.getMarketType());
            quoteIdArray[rowIndex] = KdbUtility.kdbValueCharList(data.getQuoteId());
            bidPriceArray[rowIndex] = data.getBidPrice();
            askPriceArray[rowIndex] = data.getAskPrice();
            bidAmountArray[rowIndex] = data.getBidAmount();
            askAmountArray[rowIndex] = data.getAskAmount();
            quoteConditionArray[rowIndex] = data.isQuoteCondition();
            marketDateTimeArray[rowIndex] = KdbUtility.kdbValue(data.getMarketDateTime());
            createDateTimeArray[rowIndex] = KdbUtility.kdbValue(data.getCreateDateTime());
        }

        public Object[] toKdbDataObject() {
            return new Object[]{
                    symbolArray,
                    marketTypeArray,
                    quoteIdArray,
                    bidPriceArray,
                    askPriceArray,
                    bidAmountArray,
                    askAmountArray,
                    quoteConditionArray,
                    marketDateTimeArray,
                    createDateTimeArray};
        }
    }
}
