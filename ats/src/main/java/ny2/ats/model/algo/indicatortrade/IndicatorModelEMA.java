package ny2.ats.model.algo.indicatortrade;

import com.udojava.jmx.wrapper.JMXBean;

import ny2.ats.core.common.Period;
import ny2.ats.core.common.Side;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.exception.ModelInitializeException;
import ny2.ats.indicator.IndicatorType;
import ny2.ats.indicator.indicators.MovingAverageIndicator.MAPeriod;
import ny2.ats.model.IModelManager;
import ny2.ats.model.algo.IndicatorTradeModel;
import ny2.ats.model.tool.TradeCondition;
import ny2.ats.model.tool.TradeCondition.TradeConditionIndicatorIndicator;
import ny2.ats.model.tool.TradeCondition.TradeConditionPips;
import ny2.ats.model.tool.TradeConditionFunction;
import ny2.ats.model.tool.TradeConditionOperator;

/**
 * EMAのゴールデンクロスで取引を行うモデルです
 */
@JMXBean
public class IndicatorModelEMA extends IndicatorTradeModel {

    private static final String VERSION_BUY = "EMA_BUY";
    private static final String VERSION_SELL = "EMA_SELL";

    public IndicatorModelEMA(IModelManager modelManager, String versionName, Symbol symbol) {
        super(modelManager, IndicatorTradeVersion.valueOf(versionName), symbol);
        switch (versionName) {
            case VERSION_BUY:
                setupBuy();
                break;
            case VERSION_SELL:
                setupSell();
                break;
            default:
                throw new ModelInitializeException("TradeModelEMA version name must be " + VERSION_BUY + "or" +  VERSION_SELL + ". versionName = " + versionName);
        }
    }

    // //////////////////////////////////////
    // Model Setup
    // //////////////////////////////////////

    public void setupBuy() {
        TradeCondition condition;

        // Open条件1 - EMA,P20>P100
        condition = new TradeConditionIndicatorIndicator(
                Side.BUY,
                IndicatorType.EMA,
                Period.MIN_5,
                MAPeriod.P010,
                null,
                TradeConditionOperator.GRATER,
                IndicatorType.EMA,
                Period.MIN_5,
                MAPeriod.P100,
                null);
        addOpenCondition(condition);

        // Open条件2 - EMA,1期間前,P20<=P100
        condition = new TradeConditionIndicatorIndicator(
                Side.BUY,
                IndicatorType.EMA,
                Period.MIN_5,
                MAPeriod.P010,
                TradeConditionFunction.LAST_BEFORE,
                TradeConditionOperator.LESS_EQUAL,
                IndicatorType.EMA,
                Period.MIN_5,
                MAPeriod.P100,
                null);
        addOpenCondition(condition);


        // Close条件
        condition = new TradeConditionPips(10.0);
        addCloseCondition(condition);

        // Stop Loss
        setStopPips(10);
    }

    public void setupSell() {
        TradeCondition condition;

        // Open条件1 - EMA,P20<P100
        condition = new TradeConditionIndicatorIndicator(
                Side.SELL,
                IndicatorType.EMA,
                Period.MIN_5,
                MAPeriod.P010,
                null,
                TradeConditionOperator.LESS,
                IndicatorType.EMA,
                Period.MIN_5,
                MAPeriod.P100,
                null);
        addOpenCondition(condition);

        condition = new TradeConditionIndicatorIndicator(
                Side.SELL,
                IndicatorType.EMA,
                Period.MIN_5,
                MAPeriod.P010,
                TradeConditionFunction.LAST_BEFORE,
                TradeConditionOperator.GRATER_EQUAL,
                IndicatorType.EMA,
                Period.MIN_5,
                MAPeriod.P100,
                null);
        addOpenCondition(condition);

        // Close条件
        condition = new TradeConditionPips(10.0);
        addCloseCondition(condition);

        // Stop Loss
        setStopPips(10);
    }
}
