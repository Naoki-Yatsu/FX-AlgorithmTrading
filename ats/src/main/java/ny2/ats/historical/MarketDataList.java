package ny2.ats.historical;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.exception.ATSRuntimeException;

@Getter
public class MarketDataList {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** 通貨ペア */
    private final Symbol symbol;

    /** Market時刻 */
    private final List<LocalDateTime> dateTimeList;

    /** 価格 */
    private final DoubleList bidList;
    private final DoubleList askList;
    private final DoubleList midList;

    /** 使用する価格 */
    private PriceDataType priceDataType = PriceDataType.MID_PRICE;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public MarketDataList(Symbol symbol) {
        this.symbol = symbol;
        this.dateTimeList = new ArrayList<>();
        this.bidList = new DoubleArrayList();
        this.askList = new DoubleArrayList();
        this.midList = new DoubleArrayList();
    }

    public MarketDataList(Symbol symbol, int size) {
        this.symbol = symbol;
        this.dateTimeList = new ArrayList<>(size);
        this.bidList = new DoubleArrayList(size);
        this.askList = new DoubleArrayList(size);
        this.midList = new DoubleArrayList(size);
    }

    public MarketDataList(Symbol symbol, List<LocalDateTime> dateTimeList, DoubleList bidList, DoubleList askList, DoubleList midList) {
        super();
        this.symbol = symbol;
        this.dateTimeList = dateTimeList;
        this.bidList = bidList;
        this.askList = askList;
        this.midList = midList;
    }

    /**
     * データを追加します。
     *
     * @param dateTime
     * @param bid
     * @param ask
     * @param mid
     */
    public void addData(LocalDateTime dateTime, double bid, double ask, double mid) {
        dateTimeList.add(dateTime);
        bidList.add(bid);
        askList.add(ask);
        midList.add(mid);
    }

    /**
     * データを追加します。
     *
     * @param dateTime
     * @param bid
     * @param ask
     */
    public void addData(LocalDateTime dateTime, double bid, double ask) {
        dateTimeList.add(dateTime);
        bidList.add(bid);
        askList.add(ask);
        midList.add((bid + ask) / 2);
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    public void changeDataType(PriceDataType priceDataType) {
        this.priceDataType = priceDataType;
    }

    public int getDataSize() {
        return dateTimeList.size();
    }

    public DoubleList getPriceList() {
        switch (priceDataType) {
            case MID_PRICE:
                return midList;
            case BID_PRICE:
                return bidList;
            case ASK_PRICE:
                return askList;
            default:
                throw new ATSRuntimeException("PriceDataType is not set.");
        }
    }

    public LocalDateTime getDateTime(int index) {
        return dateTimeList.get(index);
    }

    public double getPrice(int index) {
        switch (priceDataType) {
            case MID_PRICE:
                return midList.get(index);
            case BID_PRICE:
                return bidList.get(index);
            case ASK_PRICE:
                return askList.get(index);
            default:
                throw new ATSRuntimeException("PriceDataType is not set.");
        }
    }


    // //////////////////////////////////////
    // Inner Class
    // //////////////////////////////////////

    /**
     * 過去データとして使用する価格をあらわすenumです。
     */
    public enum PriceDataType {
        BID_PRICE,
        ASK_PRICE,
        MID_PRICE;
    }

}
