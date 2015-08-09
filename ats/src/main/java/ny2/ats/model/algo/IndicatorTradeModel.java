package ny2.ats.model.algo;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.LoggerFactory;

import com.udojava.jmx.wrapper.JMXBean;
import com.udojava.jmx.wrapper.JMXBeanAttribute;
import com.udojava.jmx.wrapper.JMXBeanOperation;

import ny2.ats.core.common.Period;
import ny2.ats.core.common.Side;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.data.IndicatorInformation;
import ny2.ats.core.data.MarketData;
import ny2.ats.core.data.Order;
import ny2.ats.core.data.TimerInformation;
import ny2.ats.core.exception.ModelException;
import ny2.ats.core.exception.ModelInitializeException;
import ny2.ats.core.util.NumberUtility;
import ny2.ats.core.util.PriceUtility;
import ny2.ats.indicator.Indicator;
import ny2.ats.indicator.IndicatorType;
import ny2.ats.indicator.impl.OHLC;
import ny2.ats.model.IModelManager;
import ny2.ats.model.ModelType;
import ny2.ats.model.ModelVersion;
import ny2.ats.model.tool.SpreadChecker;
import ny2.ats.model.tool.TradeCondition;
import ny2.ats.model.tool.TradeCondition.TradeConditionIndicator;
import ny2.ats.model.tool.TradeCondition.TradeConditionIndicatorIndicator;
import ny2.ats.model.tool.TradeCondition.TradeConditionIndicatorNumber;
import ny2.ats.model.tool.TradeCondition.TradeConditionIndicatorTick;
import ny2.ats.model.tool.TradeCondition.TradeConditionOriginal;
import ny2.ats.model.tool.TradeCondition.TradeConditionPips;
import ny2.ats.model.tool.TradeConditionDecision;

/**
 * Indicatorを使用した汎用取引モデルです<br>
 * 通常はこのモデルを拡張したモデルを作成して使用します<br>
 * <br>
 * 取引条件判断にTradeCondition関連のクラスを使用します。<br>
 * Open/Closeにそれぞれ複数の条件を登録し、それらの条件が満たされた場合に取引を行います。<br>
 * 複数条件を登録した場合は、TradeConditionDecision を設定することにより判断方法を変更できます。
 *
 */
@JMXBean
public class IndicatorTradeModel extends AbstractSingleOrderLockableModel {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // Logger -> super class

    /** Model Type */
    public static final ModelType MODEL_TYPE = ModelType.INDICATOR_TRADE;
    /** Model Version */
    protected final IndicatorTradeVersion modelVersion;

    //
    // モデル個別情報
    //
    /** 対象Symbol */
    protected final Symbol symbol;
    /** 注文金額 */
    private int orderAmount = 10_000;
    /** Stop loss 実行のpips(スプレッド抜き) */
    private double stopPips = 100;

    //
    // 取引条件
    //
    /** Open条件 */
    private final List<TradeCondition> openConditionList = new ArrayList<>();
    /** Close条件 */
    private final List<TradeCondition> closeConditionList = new ArrayList<>();

    /** Open条件の判断(デフォルトALL) */
    private TradeConditionDecision openDicision = TradeConditionDecision.ALL;
    /** Close条件の判断(デフォルトALL) */
    private TradeConditionDecision closeDicision = TradeConditionDecision.ALL;

    /** Openで使用するPeriod */
    private final Set<Period> openPeriodSet = EnumSet.noneOf(Period.class);
    /** Close使用するPeriod */
    private final Set<Period> closePeriodSet = EnumSet.noneOf(Period.class);

    /** Orderのretryに使用するcounter */
    private AtomicInteger orderRetryCount = new AtomicInteger(0);

    //
    // 各種フラグ
    //
    /** 週末にポジションを閉じるかどうか */
    private boolean closePositionOnWeekend = true;

    /** CloseオーダーがFIlledした際にOpenを再度チェックするかどうか  */
    private boolean recheckOpenWithCloseFilled = false;

    /** Spread Checker を使用するかどうか(Open/Close:Middleを使用) */
    private boolean useSpreadChecker = true;

    /** Stop Loss判断にSpreadを含めるかどうか */
    private boolean stopLossIncludeSpread = false;


    // //////////////////////////////////////
    // Constructor / Setup
    // //////////////////////////////////////

    public IndicatorTradeModel(IModelManager modelManager, IndicatorTradeVersion version, Symbol symbol) {
        super(modelManager, LoggerFactory.getLogger("IndicatorTradeModel"));
        this.modelVersion = version;
        this.symbol = symbol;

        // Model初期化

        // MarketData受信登録
        modelManager.registerModelForMarketUpdate(this, symbol);
        // Indicator登録
        modelManager.registerIndicator(this, symbol);
        // Timer登録
        modelManager.registerTimer(this, Period.MIN_1);
        modelManager.registerTimer(this, Period.MIN_5);
        modelManager.registerTimer(this, Period.HOUR_1);

        // log information
        logger.info("{} started.", getModelInformation());
    }

    /**
     * インスタンスを作成します
     *
     * @param modelManager
     * @param modelVersionStr
     * @param symbolStr
     * @return
     * @throws ModelInitializeException
     */
    public static IndicatorTradeModel getInstance(IModelManager modelManager, String modelVersionStr, Symbol symbol) throws ModelInitializeException {
        try {
            IndicatorTradeVersion version = IndicatorTradeVersion.valueOf(modelVersionStr);
            return new IndicatorTradeModel(modelManager, version, symbol);
        } catch (Exception e) {
            throw new ModelInitializeException(e);
        }
    }

    /**
     * Open/Close条件を設定します<br>
     * ※Listを入れ替えるので注意してください
     * @param openConditionList
     * @param closeConditionList
     */
    public void setupConditions(List<TradeCondition> openConditionList, List<TradeCondition> closeConditionList) {
        // Open
        for (TradeCondition tradeCondition : openConditionList) {
            addOpenCondition(tradeCondition);
        }
        // Close
        for (TradeCondition tradeCondition : closeConditionList) {
            addCloseCondition(tradeCondition);
        }
        logger.info("{} Setup trade condition.\n{}\n{}", getDisplayName(), showTradeCondition(true), showTradeCondition(false));
    }
    /**
     * Open条件を追加します
     * @param openCondition
     */
    public void addOpenCondition(TradeCondition openCondition) {
        openConditionList.add(openCondition);
        openPeriodSet.add(openCondition.getPeriod());
        openPeriodSet.add(openCondition.getTermPeriod());
    }
    /**
     * Close条件を追加します
     * @param openCondition
     */
    public void addCloseCondition(TradeCondition openCondition) {
        closeConditionList.add(openCondition);
        closePeriodSet.add(openCondition.getPeriod());
        closePeriodSet.add(openCondition.getTermPeriod());
    }

    /**
     * Open条件の判断方法を設定します
     */
    public void setOpenDicision(TradeConditionDecision openDicision) {
        this.openDicision = openDicision;
    }
    /**
     * Close条件の判断方法を設定します
     */
    public void setCloseDicision(TradeConditionDecision closeDicision) {
        this.closeDicision = closeDicision;
    }

    //
    // 各種フラグ設定
    //
    /**
     * 週末にポジションを閉じるかどうかを設定します
     */
    public void setClosePositionOnWeekend(boolean closePositionOnWeekend) {
        this.closePositionOnWeekend = closePositionOnWeekend;
    }
    /**
     * CloseオーダーがFIlledした際にOpenを再度チェックの判断方法を設定します
     */
    public void setRecheckOpenWithCloseFilled(boolean recheckOpenWithCloseFilled) {
        this.recheckOpenWithCloseFilled = recheckOpenWithCloseFilled;
    }
    /**
     * Spread Checkerの使用有無を設定します
     */
    public void setUseSpreadChecker(boolean useSpreadChecker) {
        this.useSpreadChecker = useSpreadChecker;
    }
    /**
     * Stop Loss 判断にSpreadを含めるかどうかを設定します
     */
    public void setStopLossIncludeSpread(boolean stopLossIncludeSpread) {
        this.stopLossIncludeSpread = stopLossIncludeSpread;
    }

    // //////////////////////////////////////
    // Method @Override
    // //////////////////////////////////////

    @Override
    public ModelType getModelType() {
        return MODEL_TYPE;
    }

    @Override
    public ModelVersion getModelVersion() {
        return modelVersion;
    }

    @Override
    public String getModelParams() {
        StringJoiner sjOpen = new StringJoiner(",", "Open-" + openDicision.name() + "[", "]");
        for (TradeCondition tradeCondition : openConditionList) {
            sjOpen.add(tradeCondition.toStringShort());
        }
        StringJoiner sjClose = new StringJoiner(",", "Close-" + closeDicision.name() + "[", "]");
        for (TradeCondition tradeCondition : closeConditionList) {
            sjClose.add(tradeCondition.toStringShort());
        }
        return String.join(",", symbol.name(), String.valueOf(orderAmount), String.valueOf(stopPips), sjOpen.toString(), sjClose.toString());
    }

    @Override
    public void receiveIndicatorUpdate(IndicatorInformation indicatorInformation) {
        // モデル停止中はは何もしない
        if (!isRunning) {
            return;
        }
        // 該当PeriodのOHLCを受信したタイミングで実行する
        if (indicatorInformation.getType() != IndicatorType.OHLC) {
            return;
        }
        // Open
        if (getPositionStatus() == ModelPositionStatus.NONE) {
            if (openPeriodSet.contains(indicatorInformation.getPeriod())) {
                checkTradeCondition(true, indicatorInformation.getPeriod());
            }
        }
        // Close
        if (getPositionStatus() == ModelPositionStatus.OPEN) {
            if (closePeriodSet.contains(indicatorInformation.getPeriod())) {
                checkTradeCondition(false, indicatorInformation.getPeriod());
            }
        }
    }

    @Override
    public synchronized void onTimer(TimerInformation timerInformation) {
        // レート受信前は何もしない
        if (modelIndicatorDataHolder.getMarketData(symbol) == null) {
            return;
        }
        // モデル停止中はは何もしない
        if (!isRunning) {
            return;
        }

        if (timerInformation.getPeriod() == Period.MIN_1) {
            // Position情報更新
            updatePLAndSendPLInformation(timerInformation.getCurrentDateTime());
        }
        if (timerInformation.getPeriod() == Period.HOUR_1) {
        }
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    protected void checkForNewOrderInternal(MarketData marketData) {
        if (openPeriodSet.contains(Period.TICK)) {
            checkTradeConditionInternal(true, Period.TICK);
        }
    }

    @Override
    protected void checkForCloseOrderInternal(MarketData marketData) {
        if (closePeriodSet.contains(Period.TICK)) {
            checkTradeConditionInternal(false, Period.TICK);
        }

        // Check stop loss
        double profit = Double.NaN;
        if (stopLossIncludeSpread) {
            profit = PriceUtility.calculateUnrealizedProfit(openOrder, marketData);
        } else {
            // if not include spread, plus spread into profit
            profit = PriceUtility.calculateUnrealizedProfit(openOrder, marketData) + getLastMarketData(symbol).getSpreadPips();
        }
        if (profit <= -stopPips) {
            // spread check
            if (useSpreadChecker) {
                if (!SpreadChecker.checkSpreadWide(marketData)) {
                    logger.warn("{} Spread check error for Stop Loss. Market Spread = {} >= {}", getDisplayName(), NumberUtility.toStringDouble(marketData.getSpreadPips()),
                            NumberUtility.toStringDouble(SpreadChecker.getSpreadNarrowPips(symbol)));
                    return;
                }
            }
            logger.warn("{} Stop Loss. profit/spread = {}/{}", getDisplayName(), NumberUtility.toStringDouble(profit), marketData.getSpreadPips());
            closeOrder = createCloseMarketOrder(marketData, openOrder);
            logger.info("{} Send close order. {}", getDisplayName(), closeOrder.toStringSummary());
            modelManager.sendOrderToMarket(closeOrder);
        }
    }

    /**
     * 取引実行条件の確認を行います(ロック用wrapper)
     *
     * @param isOpen isOpen isOpen true:open側, false:close側
     * @param updatePeriod 更新対象Period
     */
    private void checkTradeCondition(boolean isOpen, Period updatePeriod) {
        try {
            // 既にロックされている場合は待つ
            if (orderLock.tryLock(5, TimeUnit.SECONDS)) {
                try {
                    checkTradeConditionInternal(isOpen, updatePeriod);
                } finally {
                    orderLock.unlock();
                }
            } else {
                logger.warn("{} Skipped check for new order as already locked.", getDisplayName());
                return;
            }
        } catch (InterruptedException e) {
            logger.error("", e);
        }
    }

    /**
     * 取引実行条件の確認を行います（実処理）
     * ※外部でロック制御しているのでsynchronizeは行いません
     *
     * @param isOpen isOpen isOpen true:open側, false:close側
     * @param updatePeriod 更新対象Period
     */
    private void checkTradeConditionInternal(boolean isOpen, Period updatePeriod) {
        // re-check position status again
        if (isOpen) {
            if (getPositionStatus() != ModelPositionStatus.NONE) {
                logger.warn("{} Re-check position status failed for OPEN in checkTradeCondition", getDisplayName());
                return;
            }
        } else {
            if (getPositionStatus() != ModelPositionStatus.OPEN) {
                logger.warn("{} Re-check position status failed for CLOSE in checkTradeCondition", getDisplayName());
                return;
            }
        }

        // target list
        List<TradeCondition> conditionList = isOpen ? openConditionList : closeConditionList;
        MarketData marketData = getLastMarketData(symbol);

        for (TradeCondition tradeCondition : conditionList) {
            // Periodが対象外ならskip
            if (updatePeriod != tradeCondition.getPeriod() && updatePeriod != tradeCondition.getTermPeriod()) {
                continue;
            }

            if (tradeCondition instanceof TradeConditionIndicator) {
                TradeConditionIndicator indicatorCondition = (TradeConditionIndicator) tradeCondition;
                // base側
                Indicator<?> baseIndicator = modelIndicatorDataHolder.getIndicator(indicatorCondition.getIndicatorType(), symbol, indicatorCondition.getPeriod());
                Double baseValue = Double.NaN;
                if (baseIndicator.isInitializedWithCast(indicatorCondition.getCalcPeriod())) {
                    baseValue = indicatorCondition.getFunction().apply(baseIndicator, indicatorCondition.getCalcPeriod());
                }

                // term側
                Double termValue = Double.NaN;
                Double termValue2 = Double.NaN;
                if (indicatorCondition instanceof TradeConditionIndicatorIndicator) {
                    // Term側にIndicatorを使用
                    TradeConditionIndicatorIndicator indicatorIndicatorCondition = (TradeConditionIndicatorIndicator) indicatorCondition;
                    Indicator<?> termIndicator = modelIndicatorDataHolder.getIndicator(indicatorIndicatorCondition.getTermIndicatorType(), symbol, indicatorIndicatorCondition.getTermPeriod());
                    if (termIndicator.isInitializedWithCast(indicatorIndicatorCondition.getTermCalcPeriod())) {
                        termValue = indicatorIndicatorCondition.getTermFunction().apply(termIndicator, indicatorIndicatorCondition.getTermCalcPeriod());
                    }
                } else if (indicatorCondition instanceof TradeConditionIndicatorNumber) {
                    // Term側に固定Numberを使用
                    TradeConditionIndicatorNumber indicatorNumberCondition = (TradeConditionIndicatorNumber) indicatorCondition;
                    termValue = indicatorNumberCondition.getValue();
                    termValue2 = indicatorNumberCondition.getValue2();
                } else if (indicatorCondition instanceof TradeConditionIndicatorTick) {
                    // Term側にTickを使用
                    // UseOHLCBidAskの場合はOHLCから該当Bid/Ask, それ以外でSideが指定されている場合は該当bid/ask、指定されていない場合はmidを使用
                    if (((TradeConditionIndicatorTick) indicatorCondition).isUseOHLCBidAsk()) {
                        termValue = marketData.getPrice(OHLC.getOHLCBidAsk());
                    } else if (indicatorCondition.getTradeSide().isPresent()) {
                        termValue = marketData.getPrice(indicatorCondition.getTradeSide().get().getOpenBidAsk());
                    } else {
                        termValue = marketData.getMidPrice();
                    }
                } else {
                    throw new ModelInitializeException("Not defined TradeConditionIndicator.");
                }

                // 判定
                boolean fill = indicatorCondition.getOperator().getBiPredicate().test(baseValue, termValue);
                boolean fill2 = true;
                if (indicatorCondition.getOperator().hasTwoPredicate()) {
                    fill2 = indicatorCondition.getOperator().getBiPredicate2().test(baseValue, termValue2);
                }
                // 条件の結果でstatusを更新する
                tradeCondition.setStatus(fill && fill2);

            } else if (tradeCondition instanceof TradeConditionPips) {
                TradeConditionPips pipsCondition = (TradeConditionPips) tradeCondition;
                if (isOpen) {
                    // Open側にPIPSが来ることはありえない
                    throw new ModelException(getClass(), "Open condication must NOT include PIPS confition");
                }
                // Closeの場合のみ
                double limitPips = pipsCondition.getValue();
                if (PriceUtility.calculateUnrealizedProfit(openOrder, marketData) >= limitPips) {
                    tradeCondition.setStatus(true);
                } else {
                    tradeCondition.setStatus(false);
                }

            } else if (tradeCondition instanceof TradeConditionOriginal) {
                // Originalの場合は個別判断
                checkConditionOriginal((TradeConditionOriginal) tradeCondition, isOpen);
            } else {
                throw new ModelInitializeException("Not defined TradeCondition.");
            }
        }

        // 取引の条件判断
        boolean doOrder = openDicision.test(conditionList);
        Side tradeSide = null;
        // Openのみ - Sideの方向/方向チェック
        if (doOrder && isOpen) {
            Object[] sides = conditionList.stream()
                    // status==trueのみチェック
                    .filter(c -> c.isStatus())
                    .filter(c -> c.getTradeSide().isPresent())
                    .map(c -> c.getTradeSide().get())
                    .distinct()
                    .toArray();

            // Sideが1種類であればOK
            if (sides.length == 1) {
                tradeSide = (Side) sides[0];
            } else {
                // Sideが定まらない場合は取引しない
                doOrder = false;
                logger.warn("{} Trade sides are NOT match or null.\n{}", getDisplayName(), showTradeCondition(isOpen));
            }
        }

        // spread check
        if (doOrder && useSpreadChecker) {
            if (!SpreadChecker.checkSpreadMiddle(marketData)) {
                logger.warn("{} Spread check error. Market Spread = {} >= {}", getDisplayName(),
                        NumberUtility.toStringDouble(marketData.getSpreadPips()), NumberUtility.toStringDouble(SpreadChecker.getSpreadNarrowPips(symbol)));
                doOrder = false;
            }
        }

        if (doOrder) {
            if (isOpen) {
                sendOpenOrder(marketData, symbol, tradeSide, orderAmount);
            } else {
                sendCloseOrder(marketData, openOrder);
            }
            // orderを出したらstatusをfalseに戻す
            conditionList.forEach(condition -> condition.setStatus(false));
        }
    }

    @Override
    protected void onWeekend() {
        super.onWeekend();
        // 週末にPositionを閉じる場合
        if (closePositionOnWeekend && getPositionStatus() == ModelPositionStatus.OPEN) {
            logger.warn("{} Close position for weekend.", getDisplayName());
            closeAllPosition();
        }
    }

    /**
     * BaseTradeConditionType.ORIGINAL の条件判断を行います。必要な場合はオーバーライドしてください
     * 条件を満たした場合は status=true、満たさない場合は status=false にしてください
     *
     * @param isOpen true:open側, false:close側
     */
    protected void checkConditionOriginal(TradeConditionOriginal tradeCondition, boolean isOpen) {
        // Do nothing
        // if OK, condition.setStatus(true). If NOT, condition.setStatus(false).
    }

    /**
     * Open Orderが執行できなかった場合
     */
    @Override
    protected void withOpenOrderCanceled(Order canceledOrder) {
        if (orderRetryCount.getAndIncrement() < 5) {
            sendOpenOrder(getLastMarketData(symbol), symbol, canceledOrder.getSide(), orderAmount);
        }
    }

    /**
     * Close Orderが執行できなかった場合
     */
    @Override
    protected void withCloseOrderCanceled(Order canceledOrder) {
        if (orderRetryCount.getAndIncrement() < 5) {
            sendCloseOrder(getLastMarketData(symbol), openOrder);
        }
    }

    // Fillした場合はcounterを0に戻す
    protected void withOpenOrderFilled(Order filleddOrder) {
        orderRetryCount.set(0);
    }
    protected void withCloseOrderFilled(Order filleddOrder) {
        orderRetryCount.set(0);
        // Openを再チェック
        if (recheckOpenWithCloseFilled) {
            // 最も短いPeriodの更新として実行
            checkTradeCondition(true, Period.getShortestPeriod(openPeriodSet));
        }
    }

    /**
     * TradeConditionの文字列表現を返します
     *
     * @param isOpen
     * @return
     */
    public String showTradeCondition(boolean isOpen) {
        List<TradeCondition> conditionList = null;
        StringBuilder sb = new StringBuilder(100);
        if (isOpen) {
            conditionList = openConditionList;
            sb.append("[Open Condition]\n");
        } else {
            conditionList = closeConditionList;
            sb.append("[Close Condition]\n");
        }

        for (TradeCondition tradeCondition : conditionList) {
            sb.append(tradeCondition.toString()).append("\n");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    /**
     * TradeConditionの文字列表現を返します(Open&Close)
     */
    @JMXBeanOperation
    public String showTradeConditionAll() {
        return showTradeCondition(true) + "\n" + showTradeCondition(false);
    }

    // //////////////////////////////////////
    // Method - JMX
    // //////////////////////////////////////
    @JMXBeanAttribute
    public int getOrderAmount() {
        return orderAmount;
    }
    @JMXBeanAttribute
    public void setOrderAmount(int orderAmount) {
        this.orderAmount = orderAmount;
    }
    @JMXBeanAttribute
    public double getStopPips() {
        return stopPips;
    }
    @JMXBeanAttribute
    public void setStopPips(double stopPips) {
        this.stopPips = stopPips;
    }

    @JMXBeanAttribute
    public TradeConditionDecision getOpenDicision() {
        return openDicision;
    }
    @JMXBeanAttribute
    public TradeConditionDecision getCloseDicision() {
        return closeDicision;
    }
    @JMXBeanAttribute
    public boolean isClosePositionOnWeekend() {
        return closePositionOnWeekend;
    }
    @JMXBeanAttribute
    public boolean isRecheckOpenWithCloseFilled() {
        return recheckOpenWithCloseFilled;
    }
    @JMXBeanAttribute
    public boolean isUseSpreadChecker() {
        return useSpreadChecker;
    }
    @JMXBeanAttribute
    public boolean isStopLossIncludeSpread() {
        return stopLossIncludeSpread;
    }

    // //////////////////////////////////////
    // Inner Class
    // //////////////////////////////////////

    /**
     * モデルのバージョン
     */
    public static class IndicatorTradeVersion implements ModelVersion {
        protected String name;
        public IndicatorTradeVersion(String name) {
            this.name = name;
        }
        @Override
        public String getName() {
            return name;
        }
        /**
         * enum の代わりとなる factory method です。中身はnewと同じでです
         * @param name
         * @return
         */
        public static IndicatorTradeVersion valueOf(String name) {
            return new IndicatorTradeVersion(name);
        }
    }

}
