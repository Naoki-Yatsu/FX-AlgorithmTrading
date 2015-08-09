package ny2.ats.core.util;

import java.util.List;

public class CollectionUtility {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    /**
     * Listの最後のNこの要素を取得します。
     * @param list
     * @param count
     * @return
     */
    public static <T> List<T> lastSubListView(List<T> list, int count) {
        int toIndex = list.size();
        int fromIndex = toIndex - count;
        // countがリストより長い場合、不正な値が設定された場合はそのまま返す。
        if (count > toIndex || fromIndex > toIndex) {
            return list;
        }
        return list.subList(fromIndex, toIndex);
    }

    /**
     * Listの最後の要素を取得します。
     * @param list
     * @return
     */
    public static <T> T getLast(List<T> list) {
        int size = list.size();
        if (size >= 1) {
            return list.get(size - 1);
        } else {
            return null;
        }
    }

    /**
     * Listの最後の1つ手前の要素を取得します。
     * @param list
     * @return
     */
    public static <T> T getLastBefore(List<T> list) {
        int size = list.size();
        if (size >= 2) {
            return list.get(size - 2);
        } else {
            return null;
        }
    }

    /**
     * Listの指定範囲の要素を削除します
     * @param list
     * @param deleteFrom 削除開始(inclusive)
     * @param deleteTo 削除終了(exclusive)
     */
    public static void removeSelectedIndices(List<?> list, int deleteFrom, int deleteTo) {
        list.subList(deleteFrom, deleteTo).clear();
    }

    /**
     * Listの先頭の要素を削除します(個数で指定します)
     *
     * @param list
     * @param deleteCount
     */
    public static <T> void removeHeadItems(List<T> list, int deleteCount) {
        list.subList(0, deleteCount).clear();
    }

    /**
     * Listの先頭の要素を削除します(Indexで指定します)
     *
     * @param list
     * @param remainFromIndex index以降が残る
     */
    public static <T> void removeHeadIndex(List<T> list, int remainFromIndex) {
        list.subList(0, remainFromIndex).clear();
    }

    /**
     * Listの最後の要素を削除します
     *
     * @param list
     * @param deleteCount 削除するitem数
     */
    public static <T> void removeTailItems(List<T> list, int deleteCount) {
        list.subList(list.size() - deleteCount, list.size()).clear();
    }

    /**
     * Listの最後の要素を削除します
     *
     * @param list
     * @param deleteFromIndex index以降を削除
     */
    public static <T> void removeTailIndex(List<T> list, int deleteFromIndex) {
        list.subList(deleteFromIndex, list.size()).clear();
    }

    // //////////////////////////////////////
    // Method - List <-> Array
    // //////////////////////////////////////

    /**
     * Listをprimitiveの配列に変換します<br>
     * ref) org.apache.commons.lang.ArrayUtils#toPrimitive
     * @param list DoubleのList
     * @return
     */
    public static double[] toPrimitiveDouble(List<Double> list) {
        double[] array = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    /**
     * Listをprimitiveの配列に変換します<br>
     * ref) org.apache.commons.lang.ArrayUtils#toPrimitive
     * @param list IntegerのList
     * @return
     */
    public static int[] toPrimitiveInt(List<Integer> list) {
        int[] array = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    /**
     * Listをprimitiveの配列に変換します<br>
     * ref) org.apache.commons.lang.ArrayUtils#toPrimitive
     * @param list LongのList
     * @return
     */
    public static long[] toPrimitiveLong(List<Long> list) {
        long[] array = new long[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

}
