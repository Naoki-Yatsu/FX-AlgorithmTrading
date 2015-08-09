package ny2.ats.model.tool;

import java.util.Optional;
import java.util.function.BiFunction;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.NonNull;
import ny2.ats.core.common.Period;
import ny2.ats.core.common.Side;
import ny2.ats.indicator.CalcPeriod;
import ny2.ats.indicator.Indicator;
import ny2.ats.indicator.IndicatorType;

/**
 * 取引の条件のクラスです。<br>
 * <br>
 * [TradeConditionIndicator]<br>
 * Indicatorを取引条件に使用します
 * - [[TradeConditionIndicatorIndicator]]
 * Indicatorとの比較に他のIndicatorを使用します
 * - [[TradeConditionIndicatorNumber]]
 * Indicatorとの比較に固定値を使用します
 * - [[TradeConditionIndicatorNumber]]
 * Indicatorとの比較にTickデータを使用します
 * <br>
 * [TradeConditionPips]<br>
 * 固定Pipsで決済します。決済条件のみに使用します<br>
 * <br>
 * [TradeConditionOriginal]<br>
 * Modelごとの専用ロジックで取引を行います。<br>
 * <br>
 * ※その他<br>
 * tradeSide : 取引方向(複数の条件がある場合は全てが一致したときのみ有効、複数の場合は一部がnullでもよい)、決済時は使用しない<br>
 * status : 条件が満たされてるときはtrue、満たされていないときはfalseに設定する
 */
public abstract class TradeCondition {

    // //////////////////////////////////////
    // Field / Method
    // //////////////////////////////////////

    /** 条件で使用するPeriod(Indicatorの場合はBase側で使用) */
    protected final  Period period;

    /** Trade Side - 複数の条件がある場合は全てが一致したときのみ有効、複数の場合は一部がemptyでもよい、決済時は不要 */
    protected  Optional<Side> tradeSide;

    /** Condition Status. When condition is right, it turns true. */
    protected boolean status = false;

    public TradeCondition(Period period, Side tradeSide) {
        this.period = period;
        setTradeSide(tradeSide);
    }

    /**
     * 取引条件の種別を返します
     */
    public abstract TradeConditionType getTradeConditionType();

    /**
     * Base側の判定に使うPeriodを返します
     */
    public Period getPeriod() {
        return period;
    }
    /**
     * Term側の判定に使うPeriodを返します。Term側が存在しない場合はBaseと同じPeriodを返します
     */
    public Period getTermPeriod() {
        return period;
    }

    public Optional<Side> getTradeSide() {
        return tradeSide;
    }
    public void setTradeSide(Side tradeSide) {
        this.tradeSide = Optional.ofNullable(tradeSide);
    }
    public boolean isStatus() {
        return status;
    }
    public void setStatus(boolean status) {
        this.status = status;
    }

    /**
     * 文字列の短縮表現を返します<br>
     * 例) [MIN_5,EMA,P20>=MIN_5,EMA,P100 | BUY | false]
     */
    public abstract String toStringShort();

    protected StringBuilder getToStringBase(String className) {
        return new StringBuilder(100)
                .append(StringUtils.isBlank(className) ? "" : className + " " )
                .append('[').append(period);
    }

    /**
     * toString用にSide, Statusの値を追加します
     * @param sb
     * @return
     */
    protected StringBuilder addSideStatusToString(StringBuilder sb) {
        sb.append(" | ").append(tradeSide.orElse(null))
            .append(" | ").append(status)
            .append(']');
        return sb;
    }

    // //////////////////////////////////////
    // Extended Class
    // //////////////////////////////////////

    /**
     * 固定pipsで決済する条件です<br>
     * 決済のみに使用できます。データはtickを使用します
     */
    @Getter
    public static class TradeConditionPips extends TradeCondition {
        public static final TradeConditionType CONDITION_TYPE = TradeConditionType.PIPS;
        protected Double value;

        public TradeConditionPips(@NonNull Double value) {
            super(Period.TICK, null);
            this.value = value;
        }

        @Override
        public TradeConditionType getTradeConditionType() {
            return CONDITION_TYPE;
        }
        @Override
        public String toString() {
            StringBuilder sb = getToStringBase(getClass().getSimpleName()).append(',').append(getValue()).append(" pip");
            return addSideStatusToString(sb).toString();
        }
        @Override
        public String toStringShort() {
            StringBuilder sb = getToStringBase("").append(',').append(getValue()).append(" pip");
            return addSideStatusToString(sb).toString();
        }
    }

    /**
     * 専用の条件で取引を行います。条件はModel内に記述します<br>
     * 使用するPeriod(複数ある場合は最も短いもの)を指定します
     */
    @Getter
    public static class TradeConditionOriginal extends TradeCondition {
        public static final TradeConditionType CONDITION_TYPE = TradeConditionType.ORIGINAL;

        public TradeConditionOriginal(Side tradeSide, @NonNull Period period) {
            super(period, tradeSide);
        }

        @Override
        public TradeConditionType getTradeConditionType() {
            return CONDITION_TYPE;
        }
        @Override
        public String toString() {
            StringBuilder sb = getToStringBase(getClass().getSimpleName()).append(",ORIGINAL");
            return addSideStatusToString(sb).toString();
        }
        @Override
        public String toStringShort() {
            StringBuilder sb = getToStringBase("").append(",ORIGINAL");
            return addSideStatusToString(sb).toString();
        }
    }

    /**
     * Indicatorを条件に取引を行います。<br>
     * 比較対象のIndicator or Numberをサブクラスで定義します。<br>
     * Indicator Data はfunctionを使用して取得します。functionを指定しない場合は最新データが使用されます。
     */
    @Getter
    public static abstract class TradeConditionIndicator extends TradeCondition {
        public static final TradeConditionType CONDITION_TYPE = TradeConditionType.INDICATOR;
        /** Base indicator */
        protected final IndicatorType indicatorType;
        /** Base calcPeriod */
        protected final CalcPeriod calcPeriod;
        /** Base indicator data taking function、デフォルトを使用する場合はconstructorでnull指定してください */
        protected final BiFunction<Indicator<?>, CalcPeriod, Double> function;

        /** Base と Term の比較演算子 */
        protected final TradeConditionOperator operator;

        public TradeConditionIndicator(Side tradeSide,
                @NonNull IndicatorType indicatorType, @NonNull Period period, @NonNull CalcPeriod calcPeriod, BiFunction<Indicator<?>, CalcPeriod, Double> function,
                @NonNull TradeConditionOperator operator) {
            super(period, tradeSide);
            this.indicatorType = indicatorType;
            this.calcPeriod = calcPeriod;
            if (function != null) {
                this.function = function;
            } else {
                this.function = TradeConditionFunction.LAST;
            }
            this.operator = operator;
        }

        @Override
        public TradeConditionType getTradeConditionType() {
            return CONDITION_TYPE;
        }
        /**
         * Termの取引条件の種別を返します
         */
        public abstract TradeConditionType getTermTradeConditionType();

        @Override
        protected StringBuilder getToStringBase(String className) {
            return super.getToStringBase(className).append(',')
                    .append(indicatorType).append(',')
                    .append(calcPeriod)
                    .append(function.equals(TradeConditionFunction.LAST) ? "" : ",(func)")
                    .append(operator.getExpression());
        }
    }

    /**
     * Indicatorとの比較対象にIndicatorを用いるクラスです。
     */
    @Getter
    public static class TradeConditionIndicatorIndicator extends TradeConditionIndicator {
        public static final TradeConditionType TERM_CONDITION_TYPE = TradeConditionType.INDICATOR;
        /** Term period */
        protected final Period termPeriod;
        /** Term indicator */
        protected final IndicatorType termIndicatorType;
        /** Term calcPeriod */
        protected final CalcPeriod termCalcPeriod;
        /** Term側のIndicatorの値に関数を適用させるときは定義します。使わない場合はconstructorでnull or identityを指定してください(ex. d -> d + 1) */
        protected final BiFunction<Indicator<?>, CalcPeriod, Double> termFunction;

        /**
         * TradeConditionIndicatorIndicatorを作成します
         * @param tradeSide
         * @param indicatorType
         * @param period
         * @param calcPeriod
         * @param function
         * @param operator
         * @param termIndicatorType
         * @param termPeriod
         * @param termCalcPeriod
         * @param termFunction
         */
        public TradeConditionIndicatorIndicator(Side tradeSide,
                IndicatorType indicatorType, Period period, CalcPeriod calcPeriod, BiFunction<Indicator<?>, CalcPeriod, Double> function, TradeConditionOperator operator,
                @NonNull IndicatorType termIndicatorType, @NonNull Period termPeriod, @NonNull CalcPeriod termCalcPeriod, BiFunction<Indicator<?>, CalcPeriod, Double> termFunction) {
            super(tradeSide, indicatorType, period, calcPeriod, function, operator);
            this.termPeriod = termPeriod;
            this.termIndicatorType = termIndicatorType;
            this.termCalcPeriod = termCalcPeriod;
            if (termFunction != null) {
                this.termFunction = termFunction;
            } else {
                this.termFunction = TradeConditionFunction.LAST;
            }
        }
        /**
         * TradeConditionIndicatorIndicatorを作成します、functionはLASTを使用します
         * @param tradeSide
         * @param indicatorType
         * @param period
         * @param calcPeriod
         * @param operator
         * @param termIndicatorType
         * @param termPeriod
         * @param termCalcPeriod
         */
        public TradeConditionIndicatorIndicator(Side tradeSide,
                IndicatorType indicatorType, Period period, CalcPeriod calcPeriod, TradeConditionOperator operator,
                @NonNull IndicatorType termIndicatorType, @NonNull Period termPeriod, @NonNull CalcPeriod termCalcPeriod) {
            this(tradeSide, indicatorType, period, calcPeriod, null, operator, termIndicatorType, termPeriod, termCalcPeriod, null);
        }

        @Override
        public TradeConditionType getTermTradeConditionType() {
            return TERM_CONDITION_TYPE;
        }
        @Override
        public Period getTermPeriod() {
            return termPeriod;
        }
        @Override
        public String toString() {
            StringBuilder sb = getToStringBase(getClass().getSimpleName())
                    .append(termPeriod).append(',')
                    .append(termIndicatorType).append(',')
                    .append(termCalcPeriod)
                    .append(termFunction.equals(TradeConditionFunction.LAST) ? "" : ",(func)");
            return addSideStatusToString(sb).toString();
        }
        @Override
        public String toStringShort() {
            StringBuilder sb = getToStringBase("")
                    .append(termPeriod).append(',')
                    .append(termIndicatorType).append(',')
                    .append(termCalcPeriod)
                    .append(termFunction.equals(TradeConditionFunction.LAST) ? "" : ",(func)");
            return addSideStatusToString(sb).toString();
        }
    }

    /**
     * Indicatorとの比較対象に数値を用いるクラスです。
     */
    @Getter
    public static class TradeConditionIndicatorNumber extends TradeConditionIndicator {
        public static final TradeConditionType TERM_CONDITION_TYPE = TradeConditionType.NUMBER;
        private final Double value;
        /** used only TradeConditionOperator.BETWEEN/WITHOUT */
        private final Double value2;

        /**
         * TradeConditionIndicatorNumberを作成します。
         * @param tradeSide
         * @param indicatorType
         * @param period
         * @param calcPeriod
         * @param function
         * @param operator
         * @param value
         * @param value2
         */
        public TradeConditionIndicatorNumber(Side tradeSide,
                IndicatorType indicatorType, Period period, CalcPeriod calcPeriod, BiFunction<Indicator<?>, CalcPeriod, Double> function, TradeConditionOperator operator,
                @NonNull Double value, Double value2) {
            super(tradeSide, indicatorType, period, calcPeriod, function, operator);
            this.value = value;
            this.value2 = value2;
        }
        /**
         * TradeConditionIndicatorNumberを作成します。functionはLASTを使用します
         * @param tradeSide
         * @param indicatorType
         * @param period
         * @param calcPeriod
         * @param operator
         * @param value
         * @param value2
         */
        public TradeConditionIndicatorNumber(Side tradeSide,
                IndicatorType indicatorType, Period period, CalcPeriod calcPeriod, TradeConditionOperator operator,
                @NonNull Double value, Double value2) {
            this(tradeSide, indicatorType, period, calcPeriod, null, operator, value, value2);
        }

        @Override
        public TradeConditionType getTermTradeConditionType() {
            return TERM_CONDITION_TYPE;
        }
        @Override
        public String toString() {
            StringBuilder sb = getToStringBase(getClass().getSimpleName())
                    .append(value)
                    .append(operator.hasTwoPredicate() ? " and " + value2 : "");
            return addSideStatusToString(sb).toString();
        }
        @Override
        public String toStringShort() {
            StringBuilder sb = getToStringBase("")
                    .append(value)
                    .append(operator.hasTwoPredicate() ? " and " + value2 : "");
            return addSideStatusToString(sb).toString();
        }
    }

    /**
     * Indicatorとの比較対象にTickデータを用いるクラスです。
     * MarketDataのBid/Ask判定に使用するため、sideを必ず指定してください
     * TickデータのBid/AskにIndicator計算と同じBid/Askを使用する場合は、useOHLCBidAskをtrueに設定してください
     */
    @Getter
    public static class TradeConditionIndicatorTick extends TradeConditionIndicator {
        public static final TradeConditionType TERM_CONDITION_TYPE = TradeConditionType.TICK;
        protected final Period termPeriod;
        /** 比較用TickにOHLCと同じBidAskのデータを使用する場合はtrue */
        protected final boolean useOHLCBidAsk;

        /**
         * TradeConditionIndicatorTickを作成します
         * @param tradeSide
         * @param indicatorType
         * @param period
         * @param calcPeriod
         * @param function
         * @param operator
         */
        public TradeConditionIndicatorTick(Side tradeSide,
                IndicatorType indicatorType, Period period, CalcPeriod calcPeriod, BiFunction<Indicator<?>, CalcPeriod, Double> function, TradeConditionOperator operator, boolean useOHLCBidAsk) {
            super(tradeSide, indicatorType, period, calcPeriod, function, operator);
            this.termPeriod = Period.TICK;
            this.useOHLCBidAsk = useOHLCBidAsk;
        }
        /**
         * TradeConditionIndicatorTickを作成します。functionはLASTを使用します
         * @param tradeSide
         * @param indicatorType
         * @param period
         * @param calcPeriod
         * @param operator
         */
        public TradeConditionIndicatorTick(@NonNull Side tradeSide,
                IndicatorType indicatorType, Period period, CalcPeriod calcPeriod, TradeConditionOperator operator, boolean useOHLCBidAsk) {
            this(tradeSide, indicatorType, period, calcPeriod, null, operator, useOHLCBidAsk);
        }

        @Override
        public TradeConditionType getTermTradeConditionType() {
            return TERM_CONDITION_TYPE;
        }
        @Override
        public Period getTermPeriod() {
            return termPeriod;
        }
        @Override
        public String toString() {
            StringBuilder sb = getToStringBase(getClass().getSimpleName())
                    .append(termPeriod);
            return addSideStatusToString(sb).toString();
        }
        @Override
        public String toStringShort() {
            StringBuilder sb = getToStringBase("")
                    .append(termPeriod);
            return addSideStatusToString(sb).toString();
        }
    }
}
