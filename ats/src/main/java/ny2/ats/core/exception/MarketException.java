package ny2.ats.core.exception;

import ny2.ats.market.connection.MarketType;

/**
 * Market related exception
 */
public class MarketException extends ATSRuntimeException {

    private static final long serialVersionUID = 1L;

    public MarketException(MarketType marketType) {
        super(getMessageHeadder(marketType));
    }

    public MarketException(MarketType marketType, String message) {
        super(getMessageHeadder(marketType) + message);
    }

    public MarketException(MarketType marketType, String message, Throwable cause) {
        super(getMessageHeadder(marketType) + message, cause);
    }

    private static String getMessageHeadder(MarketType marketType) {
        return marketType.name() + " Exception : ";
    }

}
