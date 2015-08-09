package ny2.ats.database.kdb;

/**
 * byをまとめる時の期間データのまとめ方を表すenumです
 */
public enum KdbByFunctionType {

    // If not in use
    UNUSED(null, null),

    // by ticks (ticks is enable for one symbol only)
    FIRST_TICK("first", null),
    LAST_TICK("last", null),

    // by seconds
    FIRST_SEC("first", null),
    LAST_SEC("last", null);

    // min bid, max ask
    // WORST("min", "max"),



    private String function;
    private String function2;

    private KdbByFunctionType(String function, String function2) {
        this.function = function;
        this.function2 = function2;
    }

    public String getFunction() {
        return function;
    }

    public String getFunction2() {
        return function2;
    }
}
