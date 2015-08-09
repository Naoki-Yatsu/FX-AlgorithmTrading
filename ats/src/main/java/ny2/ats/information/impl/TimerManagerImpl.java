package ny2.ats.information.impl;

import java.time.LocalDateTime;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import ny2.ats.core.common.Period;
import ny2.ats.information.ITimerManager;
import ny2.ats.information.TimerChecker;

@Component
@ManagedResource(objectName="InformationService:name=TimerGenerator")
public class TimerManagerImpl implements ITimerManager {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // Logger
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** Timeチェッカー */
    @Autowired
    private TimerChecker timerChecker;

    /** Timer用のExecuter */
    private ScheduledExecutorService executorService;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    @PostConstruct
    private void init() {
        logger.info("PostConstruct instance.");

        // NO listen event

        // Start TimerChecker thread
        logger.info("Start TimerChecker thread.");
        timerChecker.resetTimer(LocalDateTime.now());

        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    timerChecker.checkTime(LocalDateTime.now());
                } catch (Throwable t) {
                    logger.error("Error occured in TimeChecker. ", t);
                }
            }
        }, 1000, 100, TimeUnit.MILLISECONDS);
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public void stopTimerForBacktest() {
        logger.warn("Stop timer checker thread.");
        executorService.shutdown();
    }

    @Override
    public LocalDateTime getNextTimerTime() {
        return timerChecker.getNextTimerTime();
    }

    @Override
    public LocalDateTime getLastTimerTime() {
        return timerChecker.getLastTimerTime();
    }

    /**
     * Timerの各時刻情報を表示します
     */
    @ManagedOperation
    public String showAllTimer() {
        StringBuilder sb = new StringBuilder(200);
        sb.append("[Last Time]\n");
        for (Entry<Period, LocalDateTime> entry : timerChecker.getLastDateTimeMap().entrySet()) {
            sb.append(entry.getKey()).append(" : ").append(entry.getValue()).append('\n');
        }
        sb.append("\n[Next Time]\n");
        for (Entry<Period, LocalDateTime> entry : timerChecker.getNextDateTimeMap().entrySet()) {
            sb.append(entry.getKey()).append(" : ").append(entry.getValue()).append('\n');
        }
        return sb.toString();
    }

}
