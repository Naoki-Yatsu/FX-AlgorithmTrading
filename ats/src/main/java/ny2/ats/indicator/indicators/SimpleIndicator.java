package ny2.ats.indicator.indicators;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import ny2.ats.core.common.Period;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.util.CollectionUtility;
import ny2.ats.indicator.CalcPeriod;
import ny2.ats.indicator.Indicator;
import ny2.ats.indicator.IndicatorType;

/**
 * 各時刻での値が1つであるような単純なIndicatorのための抽象クラスです。
 * 基本的なIndicatorはこのクラスを継承して定義します。
 */
public abstract class SimpleIndicator<T extends CalcPeriod> extends Indicator<T> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** データMap */
    protected final SortedMap<T, List<Double>> valueMap = new TreeMap<>();

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public SimpleIndicator(Class<?> indicatorClass, IndicatorType type, Symbol symbol, Period period) {
        super(indicatorClass, type, symbol, period);
        // valueMap初期化
        initializeMap();
    }

    /**
     * valueMapを初期化します。
     */
    protected abstract void initializeMap();

    // //////////////////////////////////////
    // Method (@Override)
    // //////////////////////////////////////

    @Override
    public Double getLastValue(T calcPeriod) {
        return CollectionUtility.getLast(valueMap.get(calcPeriod));
    }

    @Override
    public List<Double> getValueList(T calcPeriod) {
        return valueMap.get(calcPeriod);
    }

    @Override
    public boolean isInitialized(T calcPeriod) {
        Double lastValue = getLastValue(calcPeriod);
        if (lastValue == null || Double.isNaN(lastValue)) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean isInitializedAll() {
        for (T calcPeriod : valueMap.keySet()) {
            if (!isInitialized(calcPeriod)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getDataString() {
        StringBuilder sb = new StringBuilder();
        for (Entry<T, List<Double>> entry : valueMap.entrySet()) {
            // 最後の値を取得
            Double d = CollectionUtility.getLast(entry.getValue());
            sb.append(entry.getKey().getName()).append(NAME_DELIMITER).append(d.toString()).append(DATA_DELIMITER);
        }
        // Delete the last delimiter
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    @Override
    public Map<CalcPeriod, List<Double>> getLastValueMap() {
        Map<CalcPeriod, List<Double>> lastValueMap = new HashMap<>();
        for (Entry<T, List<Double>> entry : valueMap.entrySet()) {
            List<Double> lastValueList = new ArrayList<>();
            lastValueList.add(CollectionUtility.getLast(entry.getValue()));
            lastValueMap.put(entry.getKey(), lastValueList);
        }
        return lastValueMap;
    }

    @Override
    public void reduceData(int holdDays) {
        LocalDateTime holdBaseDateTime = LocalDateTime.of(getLastDateTime().toLocalDate().minus(holdDays, ChronoUnit.DAYS), LocalTime.MIN);
        int baseIndex = Collections.binarySearch(dateTimeList, holdBaseDateTime);
        int remainFromIndex = baseIndex >= 0 ? baseIndex : ~baseIndex;
        logger.info("{} Reduce data : head/base/last = {} / {} / {}, remain/size = {} / {}", getLogHeadder(), dateTimeList.get(0), holdBaseDateTime, getLastDateTime(), remainFromIndex, dateTimeList.size());

        StringBuilder sb = new StringBuilder(100);
        sb.append("time = ").append(dateTimeList.size()).append("->");

        // remove from each list
        // time
        CollectionUtility.removeHeadIndex(dateTimeList, remainFromIndex);
        sb.append(dateTimeList.size()).append(", ");

        // value
        for (Entry<T, List<Double>> valueEntry : valueMap.entrySet()) {
            sb.append(valueEntry.getKey().getName()).append(" = ").append(valueEntry.getValue().size()).append("->");
            CollectionUtility.removeHeadIndex(valueEntry.getValue(), remainFromIndex);
            sb.append(valueEntry.getValue().size()).append(", ");
        }
        logger.info("{} Reduce result : {}", getLogHeadder(), sb.toString());

        // Indicatorごとの追加item削除
        reduceDatAdditionalItems(holdDays, remainFromIndex);
    }

    /**
     * 各Indicatorで追加したItemから過去データを削除します
     *
     * @param holdDays データ保存期間
     * @param remainFromIndex dateTimeと同じ個数のListを使用している場合はこのindex以降を残す
     */
    protected abstract void reduceDatAdditionalItems(int holdDays, int remainFromIndex);

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    /**
     * CalcPeriodを継承して定義される計算期間の種類の個数を返します。
     * @return
     */
    public abstract int getCountCalcPeriod();

    /**
     * 時刻データを追加します。同一更新時刻で1回だけ実行します
     * @param newDateTime
     */
    public void addTimeData(LocalDateTime newDateTime) {
        // 念のため同一時刻の確認
        LocalDateTime lastTime = getLastDateTime();
        if (!newDateTime.equals(lastTime)) {
            dateTimeList.add(newDateTime);
        }
    }

    /**
     * Valueデータを追加します。Value項目が多い場合は別途専用のメソッドを作成します
     *
     * @param calcPeriod
     * @param value
     */
    public void addValueData(T calcPeriod, Double value) {
        List<Double> valueList = valueMap.get(calcPeriod);
        valueList.add(value);
    }

    /**
     * 指定の計算期間のデータ列を最後のN件分のビューを返します。
     * @param calcPeriod
     * @param count
     * @return
     */
    public List<Double> getSubValueList(T calcPeriod, int count) {
        return CollectionUtility.lastSubListView(valueMap.get(calcPeriod), count);
    }

    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

    public SortedMap<T, List<Double>> getValueMap() {
        return valueMap;
    }

    // //////////////////////////////////////
    // Inner Class
    // //////////////////////////////////////

}
