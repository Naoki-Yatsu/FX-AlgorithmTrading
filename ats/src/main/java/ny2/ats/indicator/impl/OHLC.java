package ny2.ats.indicator.impl;

import java.time.LocalDateTime;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import lombok.Getter;
import ny2.ats.core.common.BidAsk;
import ny2.ats.core.common.Period;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.exception.ATSRuntimeException;

/**
 * マーケットデータの4本値をあらわすクラスです。
 */
@Getter
public class OHLC {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** OHLCにどのデータを使うか設定 。デフォルトはMID */
    private static BidAsk ohlcBidAsk = BidAsk.MID;

    /** symbol of OHLC */
    private final Symbol symbol;

    /** period */
    private final Period period;

    /** time of OHLC */
    private LocalDateTime baseDateTime;

    /** open */
    private double openBid = Double.NaN;
    private double openAsk = Double.NaN;

    /** high */
    private double highBid = Double.NaN;
    private double highAsk = Double.NaN;

    /** low */
    private double lowBid = Double.NaN;
    private double lowAsk = Double.NaN;

    /** close */
    private double closeBid = Double.NaN;
    private double closeAsk = Double.NaN;

    /** 初期化されているかどうか。データが1件でも更新されればtrueになる。 */
    private boolean intialized = false;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public OHLC(Symbol symbol, Period signalPeriod) {
        this.symbol = symbol;
        this.period = signalPeriod;
    }

    public OHLC(Symbol symbol, Period signalPeriod, LocalDateTime baseDateTime) {
        this.symbol = symbol;
        this.period = signalPeriod;
        this.baseDateTime = baseDateTime;
    }

    public OHLC(Symbol symbol, Period signalPeriod, LocalDateTime baseDateTime, double open, double close, double high, double low) {
        this.symbol = symbol;
        this.period = signalPeriod;
        this.baseDateTime = baseDateTime;

        this.openBid = open;
        this.highBid = high;
        this.lowBid = low;
        this.closeBid = close;

        this.openAsk = open;
        this.highAsk = high;
        this.lowAsk = low;
        this.closeAsk = close;
    }

    /**
     * Openを設定し、すべてOpenで初期化します。
     */
    public void initOpen(double openBid, double openAsk) {
        this.openBid = openBid;
        this.highBid = openBid;
        this.lowBid = openBid;
        this.closeBid = openBid;

        this.openAsk = openAsk;
        this.highAsk = openAsk;
        this.lowAsk = openAsk;
        this.closeAsk = openAsk;
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    /**
     * レートを更新します
     */
    public void update(double newBid, double newAsk) {

        // 最初のみ初期化されていないので、念のためチェック
        if (!intialized) {
            initOpen(newBid, newAsk);
            intialized = true;
        }

        // high/lowを更新
        if (newBid > highBid) {
            highBid = newBid;
        } else if (newBid < lowBid) {
            lowBid = newBid;
        }
        if (newAsk > highAsk) {
            highAsk = newAsk;
        } else if (newAsk < lowAsk) {
            lowAsk = newAsk;
        }

        // closeは常に更新
        closeBid = newBid;
        closeAsk = newAsk;
    }

    /**
     * 他の期間のSignalDataからレートを更新します
     */
    public void update(OHLC data) {
        // Open -> High -> Low の順で更新。Openはほぼ無意味
        update(data.getOpenBid(), data.getOpenAsk());
        update(data.getHighBid(), data.getHighAsk());
        update(data.getLowBid(), data.getLowAsk());

        // 最後にClose
        update(data.getCloseBid(), data.getCloseAsk());
    }

    public void setBaseDateTime(LocalDateTime baseDateTime) {
        this.baseDateTime = baseDateTime;
    }

    /**
     * データが1件でも更新され、値が設定されているかチェックします。
     */
    public boolean isInitialized() {
        return intialized;
    }

    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
    }

    /**
     * 全てのデータをSubpipで丸めます。OHLCの更新が環境した際に実行します。
     */
     public void roundAll() {
         openBid = symbol.roundSubPips(openBid);
         highBid = symbol.roundSubPips(highBid);
         lowBid = symbol.roundSubPips(lowBid);
         closeBid = symbol.roundSubPips(closeBid);

         openAsk = symbol.roundSubPips(openAsk);
         highAsk = symbol.roundSubPips(highAsk);
         lowAsk = symbol.roundSubPips(lowAsk);
         closeAsk = symbol.roundSubPips(closeAsk);
     }

    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

     /**
      * OHLCで使用する Bid/Ask/Midを変更します
      * @param bidAsk
      */
    protected static void changeOHLCBidAsk(BidAsk bidAsk) {
        if (bidAsk != null) {
            ohlcBidAsk = bidAsk;
        } else {
            throw new ATSRuntimeException("BidAsk is null.");
        }
    }

    public static BidAsk getOHLCBidAsk() {
        return ohlcBidAsk;
    }

    /**
     * 使用するレート種別を考慮してOpenを返します。
     * @return
     */
    public double getOpen() {
        switch (ohlcBidAsk) {
            case BID:
                return openBid;
            case ASK:
                return openAsk;
            case MID:
                return (openBid + openAsk) / 2;
            default:
                return Double.NaN;
        }
    }

    /**
     * 使用するレート種別を考慮してHighを返します。
     * @return
     */
    public double getHigh() {
        switch (ohlcBidAsk) {
            case BID:
                return highBid;
            case ASK:
                return highAsk;
            case MID:
                return (highBid + highAsk) / 2;
            default:
                return Double.NaN;
        }
    }

    /**
     * 使用するレート種別を考慮してLowを返します。
     * @return
     */
    public double getLow() {
        switch (ohlcBidAsk) {
            case BID:
                return lowBid;
            case ASK:
                return lowAsk;
            case MID:
                return (lowBid + lowAsk) / 2;
            default:
                return Double.NaN;
        }
    }

    /**
     * 使用するレート種別を考慮してCloseを返します。
     * @return
     */
    public double getClose() {
        switch (ohlcBidAsk) {
            case BID:
                return closeBid;
            case ASK:
                return closeAsk;
            case MID:
                return (closeBid + closeAsk) / 2;
            default:
                return Double.NaN;
        }
    }
}
