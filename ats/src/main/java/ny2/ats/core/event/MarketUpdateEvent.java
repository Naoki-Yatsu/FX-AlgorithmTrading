package ny2.ats.core.event;

import java.time.LocalDateTime;
import java.util.UUID;

import ny2.ats.core.common.Symbol;
import ny2.ats.core.data.MarketData;
import ny2.ats.market.connection.MarketType;

/**
 * MarketDataの更新を表すイベントです。
 */
public class MarketUpdateEvent extends AbstractEvent<MarketData> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** Event種別 */
    public static final EventType eventType = EventType.MARKET_UPDATE;

    /** Marketレートデータ */
    private final MarketData marketData;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public MarketUpdateEvent(UUID creatorUUID, Class<?> creatorClass, MarketData marketData) {
        super(creatorUUID, creatorClass);
        this.marketData = marketData;
    }

    public MarketUpdateEvent(UUID creatorUUID, Class<?> creatorClass,
            MarketType marketType, Symbol symbol, String rateID,
            double bidRate, double askRate, int bidAmount, int askAmount, boolean quoteCondition,
            LocalDateTime marketDateTime) {
        super(creatorUUID, creatorClass);

        this.marketData = new MarketData(marketType, symbol, rateID, bidRate, askRate, bidAmount, askAmount, quoteCondition, marketDateTime);
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

    @Override
    public EventType getEventType() {
        return eventType;
    }

    @Override
    public MarketData getContent() {
        return marketData;
    }

    public MarketData getMarketData() {
        return marketData;
    }

    public MarketType getMarketType() {
        return marketData.getMarketType();
    }
}
