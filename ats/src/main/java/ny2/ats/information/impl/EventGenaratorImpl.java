package ny2.ats.information.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import ny2.ats.core.data.SystemInformation.SystemInfromationType;
import ny2.ats.core.event.SystemInformationEvent;
import ny2.ats.core.util.ExceptionUtility;
import ny2.ats.information.IEventGenarator;
import ny2.ats.information.IInformationManager;
import ny2.ats.market.connection.MarketType;

@Component
@ManagedResource(objectName="InformationService:name=EventGenarator")
public class EventGenaratorImpl implements IEventGenarator {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // Logger
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** インスタンス識別用のUUID */
    private final UUID uuid = UUID.randomUUID();

    @Autowired
    private IInformationManager informationManager;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    @PostConstruct
    private void init() {
        logger.info("PostConstruct instance.");
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    @ManagedOperation
    public String sendStartEvent() {
        SystemInformationEvent envet = new SystemInformationEvent(uuid, getClass(), SystemInfromationType.START, "Start Application", null);
        informationManager.sendEvent(envet);
        return "Successfully send event\n" + envet.toString();
    }

    @Override
    @ManagedOperation
    public String sendStopEvent() {
        SystemInformationEvent envet = new SystemInformationEvent(uuid, getClass(), SystemInfromationType.STOP, "Stop Application", null);
        informationManager.sendEvent(envet);
        return "Successfully send event\n" + envet.toString();
    }

    @Override
    @ManagedOperation
    public String sendModelStartEvent() {
        SystemInformationEvent envet = new SystemInformationEvent(uuid, getClass(), SystemInfromationType.MODEL_START, "Start Model", null);
        informationManager.sendEvent(envet);
        return "Successfully send event\n" + envet.toString();
    }

    @Override
    @ManagedOperation
    public String sendModelStopEvent() {
        SystemInformationEvent envet = new SystemInformationEvent(uuid, getClass(), SystemInfromationType.MODEL_STOP, "Stop Model", null);
        informationManager.sendEvent(envet);
        return "Successfully send event\n" + envet.toString();
    }

    @Override
    @ManagedOperation(description="Login to selected market")
    @ManagedOperationParameter(name = "marketTypeName", description = "MarketType.name()")
    public String loginMarket(String marketTypeName) {
        try {
            MarketType marketType = MarketType.valueOf(marketTypeName);
            informationManager.sendEvent(new SystemInformationEvent(uuid, getClass(), SystemInfromationType.MARKET_LOGIN, marketType.name(), null));
        } catch (Exception e) {
            return "Failed send login to " + marketTypeName + ".\n\n" + ExceptionUtility.getStackTraceString(e);
        }
        return "Successfully send login to " + marketTypeName;
    }

    @Override
    @ManagedOperation(description="Logout from selected market")
    @ManagedOperationParameter(name = "marketTypeName", description = "MarketType.name()")
    public String logoutMarket(String marketTypeName) {
        try {
            MarketType marketType = MarketType.valueOf(marketTypeName);
            informationManager.sendEvent(new SystemInformationEvent(uuid, getClass(), SystemInfromationType.MARKET_LOGOUT, marketType.name(), null));
        } catch (Exception e) {
            return "Failed send logout to " + marketTypeName + ".\n\n" + ExceptionUtility.getStackTraceString(e);
        }
        return "Successfully send logout to " + marketTypeName;
    }


    @Override
    @ManagedOperation
    public String sendWeekStartEvent() {
        SystemInformationEvent envet = new SystemInformationEvent(uuid, getClass(), SystemInfromationType.WEEK_START, "Week Start", null);
        informationManager.sendEvent(envet);
        return "Successfully send event\n" + envet.toString();
    }

    @Override
    public void sendWeekStartEvent(LocalDateTime reportDateTime) {
        SystemInformationEvent envet = new SystemInformationEvent(uuid, getClass(), SystemInfromationType.WEEK_START, "Week Start", null, reportDateTime);
        informationManager.sendEvent(envet);
    }

    @Override
    @ManagedOperation
    public String sendWeekEndEvent() {
        SystemInformationEvent envet = new SystemInformationEvent(uuid, getClass(), SystemInfromationType.WEEK_END, "Week End", null);
        informationManager.sendEvent(envet);
        return "Successfully send event\n" + envet.toString();
    }

    @Override
    public void sendWeekEndEvent(LocalDateTime reportDateTime) {
        SystemInformationEvent envet = new SystemInformationEvent(uuid, getClass(), SystemInfromationType.WEEK_END, "Week Start", null, reportDateTime);
        informationManager.sendEvent(envet);
    }

    // //////////////////////////////////////
    // For Historical
    // //////////////////////////////////////

    @Override
    public void sendModelStartEventForHistorical(LocalDateTime reportDateTime) {
        SystemInformationEvent envet = new SystemInformationEvent(uuid, getClass(), SystemInfromationType.MODEL_START, "Start Model", null, reportDateTime);
        informationManager.sendEvent(envet);
    }

    @Override
    public void sendModelStopEventForHistorical(LocalDateTime reportDateTime) {
        SystemInformationEvent envet = new SystemInformationEvent(uuid, getClass(), SystemInfromationType.MODEL_STOP, "Stop Model", null, reportDateTime);
        informationManager.sendEvent(envet);
    }



}
