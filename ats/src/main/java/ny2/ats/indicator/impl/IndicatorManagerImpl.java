package ny2.ats.indicator.impl;

import java.util.UUID;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import ny2.ats.core.common.BidAsk;
import ny2.ats.core.common.Period;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.data.IndicatorInformation;
import ny2.ats.core.data.MarketData;
import ny2.ats.core.event.EventType;
import ny2.ats.core.event.IndicatorUpdateEvent;
import ny2.ats.core.event.MarketUpdateEvent;
import ny2.ats.core.event.TimerInformationEvent;
import ny2.ats.core.router.IEventRouter;
import ny2.ats.core.util.ExceptionUtility;
import ny2.ats.indicator.IIndicatorDataHolder;
import ny2.ats.indicator.IIndicatorManager;
import ny2.ats.indicator.Indicator;
import ny2.ats.indicator.IndicatorType;
import ny2.ats.market.connection.MarketType;

@Service
@ManagedResource(objectName="IndicatorService:name=IndicatorManager")
public class IndicatorManagerImpl implements IIndicatorManager {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final UUID uuid = UUID.randomUUID();

    @Autowired
    private IEventRouter eventRouter;

    @Autowired
    private IIndicatorDataHolder indicatorDataHolder;

    /** インディケーター作成対象Market */
    @Value("${indicator.manager.markettype}")
    private MarketType indicatorMarketType;

    /** インディケーター作成対象Market */
    @Value("${indicator.manager.ohlcbidask}")
    private BidAsk ohlcBidAsk;

    /** Indicatorを更新するかどうか */
    private boolean updateIndicator = true;


    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public IndicatorManagerImpl() {
        logger.info("Create instance.");
    }

    @PostConstruct
    private void init() {
        logger.info("PostConstruct instance.");

        // listener登録
        eventRouter.registerListener(EventType.MARKET_UPDATE, this);
        eventRouter.registerListener(EventType.TIMER_INFORMATION, this);

        // info
        logger.info("Indicator MarketType = {}", indicatorMarketType);
        logger.info("Indicator OHLC BidAsk = {}", ohlcBidAsk);
        OHLC.changeOHLCBidAsk(ohlcBidAsk);
    }

    // //////////////////////////////////////
    // Method @Override
    // //////////////////////////////////////

    @Override
    public void onEvent(MarketUpdateEvent event) {
        MarketData marketData = event.getContent();
        // 対象Marketのデータのみ使用する
        if (marketData.getMarketType() != indicatorMarketType) {
            return;
        }
        indicatorDataHolder.updateMarketData(marketData);
    }

    @Override
    public void onEvent(TimerInformationEvent event) {
        indicatorDataHolder.changePeriod(event.getContent());
    }

    @Override
    public void sendIndicatorUpdate(Indicator<?> indicator) {
        IndicatorInformation indicatorInformation = new IndicatorInformation(indicator);
        IndicatorUpdateEvent event = new IndicatorUpdateEvent(uuid, getClass(), indicatorInformation);
        eventRouter.addEvent(event);
    }

    // //////////////////////////////////////
    // JMX
    // //////////////////////////////////////

    @ManagedAttribute(description="Indicator Data Market")
    public String getIndicatorMarketType() {
        return indicatorMarketType.name();
    }

    @ManagedAttribute(description="OHLC Bid/Ask Type")
    public String getOHLCBidAsk() {
        return OHLC.getOHLCBidAsk().name();
    }

    @ManagedAttribute(description="Update Indicator Flag")
    public boolean isUpdateIndicator() {
        return updateIndicator;
    }

    @ManagedOperation
    public String showIndicatorTarget() {
        StringBuilder sb = new StringBuilder("[Indicator Target]\n");

        sb.append("Symbol : ");
        for (Symbol symbol : indicatorDataHolder.getIndicatorSymbols()) {
            sb.append(symbol.name()).append(",");
        }
        sb.deleteCharAt(sb.length() - 1).append("\n");

        sb.append("Indicator : ");
        for (IndicatorType type : indicatorDataHolder.getIndicatorTypes()) {
            sb.append(type.name()).append(",");
        }
        sb.deleteCharAt(sb.length() - 1).append("\n");

        sb.append("Period(Tick) : ");
        for (Period period : indicatorDataHolder.getIndicatorPeriodsTick()) {
            sb.append(period.name()).append(",");
        }
        sb.deleteCharAt(sb.length() - 1).append("\n");

        sb.append("Period(Time) : ");
        for (Period period : indicatorDataHolder.getIndicatorPeriodsTime()) {
            sb.append(period.name()).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    /**
     * Indicatorを表示します
     */
    @ManagedOperation
    @ManagedOperationParameters({
        @ManagedOperationParameter(name = "indicatorTypeStr", description = "IndicatorType.name()"),
        @ManagedOperationParameter(name = "symbolStr", description = "Symbol.name()"),
        @ManagedOperationParameter(name = "periodStr", description = "Period.name()")})
    public String showIndicator(String indicatorTypeStr, String symbolStr, String periodStr) {
        try {
            IndicatorType indicatorType = IndicatorType.valueOf(indicatorTypeStr);
            Symbol symbol = Symbol.valueOf(symbolStr);
            Period period = Period.valueOf(periodStr);
            String indicatorStr = indicatorDataHolder.getIndicator(indicatorType, symbol, period).getDataString();
            return String.format("%s-%s-%s %s", indicatorType.name(), symbol.name(), period.name(), indicatorStr);
        } catch (Exception e) {
            return String.format("Failed to Execute : %s, %s, %s \n\n%s", indicatorTypeStr, symbolStr, periodStr, ExceptionUtility.getStackTraceString(e));
        }
    }

}
