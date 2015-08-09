package ny2.ats.model.tool;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * 複数の取引条件の最終判断方法のenumです。
 * TradeCondition のstatusをチェックして全体の判断を行います
 * ※対象の要素がゼロの場合は正しく動作しません
 */
public enum TradeConditionDecision {

    /** 全ての条件が満たされた場合=AND */
    ALL(TradeConditionDecisionPredicate.predicateAll),

    /** いずれかの条件が満たされた場合=OR */
    ANY(TradeConditionDecisionPredicate.predicateAny),

    /** 2つ以上の条件が満たされた場合 */
    TWO(TradeConditionDecisionPredicate.predicateTwo),

    /** 3つ以上の条件が満たされた場合 */
    THREE(TradeConditionDecisionPredicate.predicateThree),

    /** 4つ以上の条件が満たされた場合 */
    FOUR(TradeConditionDecisionPredicate.predicateFour),

    /** 半分以上の条件が満たされた場合 */
    HALF(TradeConditionDecisionPredicate.predicateHalf),

    /** 半分を超える条件が満たされた場合 */
    HALF_MORE(TradeConditionDecisionPredicate.predicateHalfMore),

    /** 1つを除いて条件が満たされた場合 */
    EXCEPT_ONE(TradeConditionDecisionPredicate.predicateExceptOne);


    // //////////////////////////////////////
    // Field / Constructor
    // //////////////////////////////////////

    private Predicate<Collection<TradeCondition>> predicate;

    private TradeConditionDecision(Predicate<Collection<TradeCondition>> predicate) {
        this.predicate = predicate;
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    /**
     * 条件を満たしているか判断します
     * @param conditions
     * @return
     */
    public boolean test(Collection<TradeCondition> conditions) {
        return predicate.test(conditions);
    }


    // //////////////////////////////////////
    // Predicate
    // //////////////////////////////////////

    private static class TradeConditionDecisionPredicate {

        private static final Predicate<Collection<TradeCondition>> predicateAll = conditions -> conditions.stream().allMatch(c -> c.isStatus());

        private static final Predicate<Collection<TradeCondition>> predicateAny = conditions -> conditions.stream().anyMatch(c -> c.isStatus());

        private static final Predicate<Collection<TradeCondition>> predicateTwo = conditions -> conditions.stream().filter(c -> c.isStatus()).count() >= 2 || conditions.stream().allMatch(c -> c.isStatus());

        private static final Predicate<Collection<TradeCondition>> predicateThree = conditions -> conditions.stream().filter(c -> c.isStatus()).count() >= 3 || conditions.stream().allMatch(c -> c.isStatus());

        private static final Predicate<Collection<TradeCondition>> predicateFour = conditions -> conditions.stream().filter(c -> c.isStatus()).count() >= 4 || conditions.stream().allMatch(c -> c.isStatus());

        private static final Predicate<Collection<TradeCondition>> predicateHalf = conditions -> {
            int size = conditions.size();
            int half = size / 2;
            if (size <= 1) {
                return conditions.stream().allMatch(c -> c.isStatus());
            } else {
                return conditions.stream().filter(c -> c.isStatus()).count() >= half;
            }
        };

        private static final Predicate<Collection<TradeCondition>> predicateHalfMore = conditions -> {
            int size = conditions.size();
            int half = size % 2 == 0 ? 1 + size / 2 : size / 2 ;
            return conditions.stream().filter(c -> c.isStatus()).count() >= half;
        };

        private static final Predicate<Collection<TradeCondition>> predicateExceptOne = conditions -> {
            int size = conditions.size();
            if (size <= 1) {
                // If size == 1, allMatch
                return conditions.stream().allMatch(c -> c.isStatus());
            } else {
                return conditions.stream().filter(c -> c.isStatus()).count() >= size - 1;
            }
        };
    }

}

