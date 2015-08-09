package ny2.ats.model.tool;

import java.util.function.BiPredicate;

/**
 * 取引条件計算の演算子のクラスです
 */
public enum TradeConditionOperator {

    EQUAL((d1, d2) -> Double.compare(d1, d2) == 0, null,  "="),

    LESS((d1, d2) -> d1 < d2, null, "<"),
    LESS_EQUAL((d1, d2) -> d1 <= d2, null, "<="),

    GRATER((d1, d2) -> d1 > d2, null, ">"),
    GRATER_EQUAL((d1, d2) -> d1 >= d2, null, ">="),

    BETWEEN((d1, d2) -> d1 >= d2,  (d1, d2) -> d1 <= d2, " between "),
    WITHOUT((d1, d2) -> d1 < d2,  (d1, d2) -> d1 > d2, " without ");


    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    private BiPredicate<Double, Double> biPredicate;
    private BiPredicate<Double, Double> biPredicate2;
    private String expression;

    private TradeConditionOperator(BiPredicate<Double, Double> biPredicate, BiPredicate<Double, Double> biPredicate2, String expression) {
        this.biPredicate = biPredicate;
        this.expression = expression;
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    /**
     * 2種類のBiPredicateを持つかどうかを返します<br>
     * BETWEEN, WITHOUT のみtrueを返します
     * @return
     */
    public boolean hasTwoPredicate() {
        if (biPredicate2 != null) {
            return true;
        } else {
            return false;
        }
    }

    public BiPredicate<Double, Double> getBiPredicate() {
        return biPredicate;
    }

    public BiPredicate<Double, Double> getBiPredicate2() {
        return biPredicate2;
    }

    public String getExpression() {
        return expression;
    }

}
