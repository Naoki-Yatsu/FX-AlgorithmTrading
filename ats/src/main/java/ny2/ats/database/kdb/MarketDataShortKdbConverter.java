package ny2.ats.database.kdb;

import java.util.List;

import ny2.ats.core.data.MarketData;

import com.exxeleron.qjava.QDateTime;

public class MarketDataShortKdbConverter extends MarketDataKdbConverter {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    public static final String TABLE_NAME = "MarketDataShort";

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

    public Object[] convert(List<MarketData> dataList) {
        int rowCount = dataList.size();
        MarketDataShort kdbData = new MarketDataShort(rowCount);
        for (int rowIndex=0; rowIndex < rowCount; rowIndex++) {
            kdbData.addData(rowIndex, dataList.get(rowIndex));
        }
        return kdbData.toKdbDataObject();
    }

    @Override
    public Object[] convert(MarketData data) {
        MarketDataShort kdbData = new MarketDataShort(1);
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
    //    quoteId       | j
    //    bidPrice      | f
    //    askPrice      | f
    //    quoteCondition| b
    //    marketDateTime| z
    //    createDateTime| z
    //
    private class MarketDataShort {
        // columns
        String[] symbolArray;
        String[] marketTypeArray;
        // Array of char[]
        long[] quoteIdArray;
        double[] bidPriceArray;
        double[] askPriceArray;
        boolean[] quoteConditionArray;
        QDateTime[] marketDateTimeArray;
        QDateTime[] createDateTimeArray;

        public MarketDataShort(int rowCount) {
            symbolArray = new String[rowCount];
            marketTypeArray = new String[rowCount];
            quoteIdArray = new long[rowCount];
            bidPriceArray = new double[rowCount];
            askPriceArray = new double[rowCount];
            quoteConditionArray = new boolean[rowCount];
            marketDateTimeArray = new QDateTime[rowCount];
            createDateTimeArray = new QDateTime[rowCount];
        }

        public void addData(int rowIndex, MarketData data) {
            symbolArray[rowIndex] = KdbUtility.kdbValue(data.getSymbol());
            marketTypeArray[rowIndex] = KdbUtility.kdbValue(data.getMarketType());
            quoteIdArray[rowIndex] = Long.valueOf(data.getQuoteId()).longValue();
            bidPriceArray[rowIndex] = data.getBidPrice();
            askPriceArray[rowIndex] = data.getAskPrice();
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
                    quoteConditionArray,
                    marketDateTimeArray,
                    createDateTimeArray};
        }
    }
}
