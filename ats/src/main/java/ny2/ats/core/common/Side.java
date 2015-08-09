package ny2.ats.core.common;

public enum Side {

    BUY(BidAsk.ASK, BidAsk.BID, '1'),

    SELL(BidAsk.BID, BidAsk.ASK, '2');

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** その方向の売買をするためのプライス種別 */
    private BidAsk openBidAsk;

    /** その方向のポジションを保持している場合に、決済に使うレートの方向 */
    private BidAsk closeBidAsk;

    /** FIXメッセージ用のSide */
    private char sideForFIX;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    private Side(BidAsk openBidAsk, BidAsk closeBidAsk, char sideForFIX) {
        this.openBidAsk = openBidAsk;
        this.closeBidAsk = closeBidAsk;
        this.sideForFIX = sideForFIX;
    }

    /**
     * FIXのSideから作成します。
     * @param sideForFIX side char of FIX
     * @return
     */
    public static Side valueOfFixSide(char sideForFIX) {
        switch (sideForFIX) {
            case '1':
                return BUY;
            case '2':
                return SELL;
            default:
                return null;
        }
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    public static Side getReverseSide(Side side) {
        if (side == BUY) {
            return SELL;
        } else {
            return BUY;
        }
    }

    public Side getReverseSide() {
        if (this == BUY) {
            return SELL;
        } else {
            return BUY;
        }
    }

    public BidAsk getOpenBidAsk() {
        return openBidAsk;
    }
    public BidAsk getCloseBidAsk() {
        return closeBidAsk;
    }
    public char getSideForFIX() {
        return sideForFIX;
    }
}
