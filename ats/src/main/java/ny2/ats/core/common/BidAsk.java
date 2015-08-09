package ny2.ats.core.common;

/**
 * Bid/Ask/Mid の種別をあらわすenumです
 */
public enum BidAsk {

    //
    // ※Sideとの初期化順序の都合上、BidAskのSideはStringで定義し、使用するときに変換します
    //

    BID("SELL"),

    ASK("BUY"),

    MID(null);

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    private String makePositionSide;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    private BidAsk(String makePositionSide) {
        this.makePositionSide = makePositionSide;
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    public Side getMakePositionSide() {
        if (makePositionSide != null) {
            return Side.valueOf(makePositionSide);
        } else {
            return null;
        }
    }

}
