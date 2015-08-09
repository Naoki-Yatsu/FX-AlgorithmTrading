package ny2.ats.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemUtility {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    private static final Logger logger = LoggerFactory.getLogger(SystemUtility.class);

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    /**
     * 指定時間waitします
     *
     * @param millis
     */
    public static void waitSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            logger.error("", e);
        }
    }

    /**
     * 指定時間waitします
     *
     * @param millis
     * @param nanos
     */
    public static void waitSleep(long millis, int nanos) {
        try {
            Thread.sleep(millis, nanos);
        } catch (InterruptedException e) {
            logger.error("", e);
        }
    }
}
