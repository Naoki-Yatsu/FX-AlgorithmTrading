package ny2.ats.core.data;

import java.time.Duration;
import java.time.LocalDateTime;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import ny2.ats.core.common.BidAsk;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.exception.ATSRuntimeException;
import ny2.ats.market.connection.MarketType;

/**
 * マーケットデータをあらわすクラスです
 */
@Getter
@EqualsAndHashCode(callSuper=false)
@ToString(callSuper=true)
public class MarketData extends AbstractData {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** Market種別 */
    private final MarketType marketType;

    /** 通貨ペア */
    private final Symbol symbol;

    /** レートの識別ID */
    private final String quoteId;

    /** bid */
    private final double bidPrice;

    /** ask */
    private final double askPrice;

    /** bid amount */
    private final int bidAmount;

    /** ask amount */
    private final int askAmount;

    /** レートの有効性 */
    private final boolean quoteCondition;

    /** Market時刻 */
    private final LocalDateTime marketDateTime;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public MarketData(MarketType marketType, Symbol symbol, String quoteId, double bidPrice, double askPrice, int bidAmount, int askAmount, boolean quoteCondition, LocalDateTime marketDateTime) {
        super();
        this.marketType = marketType;
        this.symbol = symbol;
        this.quoteId = quoteId;
        this.bidPrice = bidPrice;
        this.askPrice = askPrice;
        this.bidAmount = bidAmount;
        this.askAmount = askAmount;
        this.quoteCondition = quoteCondition;
        this.marketDateTime = marketDateTime;
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public String toStringSummary() {
        StringBuilder sb = new StringBuilder(200);
        sb.append("MarketData [")
                .append(marketType).append(TO_STRING_DELIMITER)
                .append(symbol).append(TO_STRING_DELIMITER)
                .append(quoteId).append(TO_STRING_DELIMITER)
                .append(bidPrice).append(ITEM_DELIMITER).append(askPrice).append(TO_STRING_DELIMITER)
                .append(marketDateTime)
                .append("]");
        return sb.toString();
    }

    /**
     * MarketTimeとレート受信時刻との差をミリ秒単位で返します。
     *
     * @return long ミリ秒レイテンシー
     */
    public long calcLatency() {
        Duration duration = Duration.between(marketDateTime, getCreateDateTime());
        return duration.toMillis();
    }

    /**
     * Bid/AskのMidを返します。
     * @return
     */
    public double getMidPrice() {
        return (bidPrice + askPrice) / 2;
    }

    /**
     * Bid または Ask のレートを返します。
     */
    public double getPrice(BidAsk bidAsk) {
        switch (bidAsk) {
            case BID:
                return bidPrice;
            case ASK:
                return askPrice;
            case MID:
                return getMidPrice();
            default:
                throw new ATSRuntimeException("Unexcepted BidAsk = "+ bidAsk);
        }
    }

    /**
     * スプレッドを返します。
     * @return
     */
    public double getSpread() {
        return askPrice - bidPrice;
    }

    /**
     * スプレッドをpips単位で返します。
     * @return
     */
    public double getSpreadPips() {
        return symbol.convertRealToPips(askPrice - bidPrice);
    }

    // //////////////////////////////////////
    // Builder
    // //////////////////////////////////////

    /**
     * Builder for creating MarketData.
     */
    @Getter
    @ToString
    public static class MarketDataBuilder implements DataBuilder<MarketData> {
        private MarketType marketType;
        private Symbol symbol;
        private String quoteId;
        private double bidPrice;
        private double askPrice;
        private int bidAmount;
        private int askAmount;
        private boolean quoteCondition;
        private LocalDateTime marketDateTime;

        public static MarketDataBuilder getBuilder() {
            return new MarketDataBuilder();
        }

        public static MarketDataBuilder getBuilder(MarketData source) {
            return new MarketDataBuilder()
                    .setMarketType(source.getMarketType())
                    .setSymbol(source.getSymbol())
                    .setQuoteId(source.getQuoteId())
                    .setBidPrice(source.getBidPrice())
                    .setAskPrice(source.getAskPrice())
                    .setBidAmount(source.getBidAmount())
                    .setAskAmount(source.getAskAmount())
                    .setQuoteCondition(source.isQuoteCondition())
                    .setMarketDateTime(source.getMarketDateTime());
        }

        @Override
        public MarketData createInstance() {
            return new MarketData(
                    marketType,
                    symbol,
                    quoteId,
                    bidPrice,
                    askPrice,
                    bidAmount,
                    askAmount,
                    quoteCondition,
                    marketDateTime);
        }

        public MarketDataBuilder setMarketType(MarketType marketType) {
            this.marketType = marketType;
            return this;
        }
        public MarketDataBuilder setSymbol(Symbol symbol) {
            this.symbol = symbol;
            return this;
        }
        public MarketDataBuilder setQuoteId(String quoteId) {
            this.quoteId = quoteId;
            return this;
        }
        public MarketDataBuilder setBidPrice(double bidPrice) {
            this.bidPrice = bidPrice;
            return this;
        }
        public MarketDataBuilder setAskPrice(double askPrice) {
            this.askPrice = askPrice;
            return this;
        }
        public MarketDataBuilder setBidAmount(int bidAmount) {
            this.bidAmount = bidAmount;
            return this;
        }
        public MarketDataBuilder setAskAmount(int askAmount) {
            this.askAmount = askAmount;
            return this;
        }
        public MarketDataBuilder setQuoteCondition(boolean quoteCondition) {
            this.quoteCondition = quoteCondition;
            return this;
        }
        public MarketDataBuilder setMarketDateTime(LocalDateTime marketDateTime) {
            this.marketDateTime = marketDateTime;
            return this;
        }
    }
}
