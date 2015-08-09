package ny2.ats.core.util;

import java.util.Comparator;

public class ComparatorUtility {

    /**
     * Integerを小さい順に並べるComparatorです
     */
    public static class IntegerComparatorAsc implements Comparator<Integer> {
        @Override
        public int compare(Integer o1, Integer o2) {
            if (o1 > o2) {
                return 1;
            } else if (o1 < o2) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    /**
     * Integerを大きい順に並べるComparatorです
     */
    public static class IntegerComparatorDesc implements Comparator<Integer> {
        @Override
        public int compare(Integer o1, Integer o2) {
            if (o1 < o2) {
                return 1;
            } else if (o1 > o2) {
                return -1;
            } else {
                return 0;
            }
        }
    }

}
