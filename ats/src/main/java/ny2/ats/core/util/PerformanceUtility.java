package ny2.ats.core.util;

public class PerformanceUtility {

    /**
     * Calculate performance of target method
     *
     * @param testName
     * @param count
     * @param method Method References like class::method
     */
    public static void calcPerformance(String testName, int count, PerformanceMethoed method) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            method.execute();
        }
        long end = System.currentTimeMillis();

        long cost = end - start;
        System.out.println(testName + " : It took " + cost + " ms.");
    }

    @FunctionalInterface
    public interface PerformanceMethoed {
        public void execute();
    }

//    public static void main(String[] args) {
//        calcPerformance("TEST", 10, PerformanceUtility::print);
//    }
//
//    public static void print() {
//        System.out.println(LocalTime.now());
//        SystemUtility.waitSleep(1000);
//    }

}
