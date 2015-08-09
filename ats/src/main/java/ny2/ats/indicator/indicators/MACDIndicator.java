package ny2.ats.indicator.indicators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import ny2.ats.core.common.Period;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.exception.ATSRuntimeException;
import ny2.ats.core.util.CollectionUtility;
import ny2.ats.indicator.CalcPeriod;
import ny2.ats.indicator.IndicatorType;
import ny2.ats.indicator.indicators.MACDIndicator.MACDPeriod;

/**
 * MACDのクラスです。
 * SimpleIndicatoのvalueとして、MACDの値を持ちます。
 * 計算に使用する長短EMA、およびシグナル(Trigger)はクラス固有の値として保持します。
 */
public class MACDIndicator extends SimpleIndicator<MACDPeriod> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // valueMapはMACDを設定

    /** MACD データ列 */
    protected final SortedMap<MACDPeriod, List<Double>> macdMap = valueMap;

    /** short EMA データ列 */
    protected final SortedMap<MACDPeriod, List<Double>> shortMap = new TreeMap<>();

    /** long EMA データ列 */
    protected final SortedMap<MACDPeriod, List<Double>> longMap = new TreeMap<>();

    /** trigger EMA データ列 */
    protected final SortedMap<MACDPeriod, List<Double>> triggertMap = new TreeMap<>();

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public MACDIndicator(IndicatorType type, Symbol symbol, Period period) {
        super(MACDIndicator.class, type, symbol, period);
        initializeMapForMACD();
    }

    @Override
    protected void initializeMap() {
        // valueMap初期化
        for (MACDPeriod macdPeriod : MACDPeriod.values()) {
            valueMap.put(macdPeriod, new ArrayList<>());
        }
    }

    /**
     * initializeMapはsuperから呼び出されるため、shortMapなどのインスタンス作成前のため、後で呼び出す
     */
    private void initializeMapForMACD() {
        // valueMap初期化
        for (MACDPeriod macdPeriod : MACDPeriod.values()) {
            // macdMap
            shortMap.put(macdPeriod, new ArrayList<>());
            longMap.put(macdPeriod, new ArrayList<>());
            triggertMap.put(macdPeriod, new ArrayList<>());
        }
    }

    // //////////////////////////////////////
    // Method (@Override)
    // //////////////////////////////////////

    @Override
    public boolean isInitialized(MACDPeriod calcPeriod) {
        Double lastValue = getLastTrigger(calcPeriod);
        if (lastValue == null || Double.isNaN(lastValue)) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public String getDataString() {
        StringBuilder sb = new StringBuilder();
        for (MACDPeriod macdPeriod : macdMap.keySet()) {
            // 最後の値を取得
            Double macd = CollectionUtility.getLast(macdMap.get(macdPeriod));
            Double trigger = CollectionUtility.getLast(triggertMap.get(macdPeriod));

            sb.append(macdPeriod.getName()).append(NAME_DELIMITER)
                    .append(macd.toString()).append(VALUE_DELIMITER)
                    .append(trigger.toString()).append(DATA_DELIMITER);
        }
        // Delete the last delimiter
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    /**
     * value1: MACD
     * value2: short
     * value3: long
     * value4: trigger
     */
    @Override
    public Map<CalcPeriod, List<Double>> getLastValueMap() {
        Map<CalcPeriod, List<Double>> lastValueMap = new HashMap<>();
        for (MACDPeriod calcPeriod : MACDPeriod.values()) {
            List<Double> lastValueList = new ArrayList<>();
            lastValueList.add(CollectionUtility.getLast(macdMap.get(calcPeriod)));
            lastValueList.add(CollectionUtility.getLast(shortMap.get(calcPeriod)));
            lastValueList.add(CollectionUtility.getLast(longMap.get(calcPeriod)));
            lastValueList.add(CollectionUtility.getLast(triggertMap.get(calcPeriod)));
            lastValueMap.put(calcPeriod, lastValueList);
        }
        return lastValueMap;
    }

    @Override
    public int getCountCalcPeriod() {
        return MACDPeriod.values().length;
    }

    /**
     * データ追加にこのメソッドは使用しない
     */
    @Deprecated
    @Override
    public void addValueData(MACDPeriod calcPeriod, Double value) {
        throw new ATSRuntimeException("Don't call this method");
    }

    /**
     * MACDにデータを追加します。
     * @param macdPeriod
     * @param macdValue
     * @param shortValue
     * @param longValue
     * @param triggerValue
     */
    public void addValueData(MACDPeriod macdPeriod, Double macdValue, Double shortValue, Double longValue, Double triggerValue) {
        // MACD
        macdMap.get(macdPeriod).add(macdValue);
        // short
        shortMap.get(macdPeriod).add(shortValue);
        // long
        longMap.get(macdPeriod).add(longValue);
        // trigger
        triggertMap.get(macdPeriod).add(triggerValue);
    }

    @Override
    protected void reduceDatAdditionalItems(int holdDays, int remainFromIndex) {
        // 追加は shortMap, longMap, triggertMap
        for (List<Double> list : shortMap.values()) {
            CollectionUtility.removeHeadIndex(list, remainFromIndex);
        }
        for (List<Double> list : longMap.values()) {
            CollectionUtility.removeHeadIndex(list, remainFromIndex);
        }
        for (List<Double> list : triggertMap.values()) {
            CollectionUtility.removeHeadIndex(list, remainFromIndex);
        }
    };

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    // 各種最新値取得
    public Double getLastMACD(MACDPeriod macdPeriod) {
        return CollectionUtility.getLast(macdMap.get(macdPeriod));
    }
    public Double getLastShort(MACDPeriod macdPeriod) {
        return CollectionUtility.getLast(shortMap.get(macdPeriod));
    }
    public Double getLastLong(MACDPeriod macdPeriod) {
        return CollectionUtility.getLast(longMap.get(macdPeriod));
    }
    public Double getLastTrigger(MACDPeriod macdPeriod) {
        return CollectionUtility.getLast(triggertMap.get(macdPeriod));
    }

    // 各種リスト取得
    public List<Double> getMACDList(MACDPeriod macdPeriod) {
        return macdMap.get(macdPeriod);
    }
    public List<Double> getShortList(MACDPeriod macdPeriod) {
        return shortMap.get(macdPeriod);
    }
    public List<Double> getLongList(MACDPeriod macdPeriod) {
        return longMap.get(macdPeriod);
    }
    public List<Double> getTriggerList(MACDPeriod macdPeriod) {
        return triggertMap.get(macdPeriod);
    }

    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

    public SortedMap<MACDPeriod, List<Double>> getMACDMap() {
        return macdMap;
    }
    public SortedMap<MACDPeriod, List<Double>> getShortMap() {
        return shortMap;
    }
    public SortedMap<MACDPeriod, List<Double>> getLongMap() {
        return longMap;
    }
    public SortedMap<MACDPeriod, List<Double>> getTriggertMap() {
        return triggertMap;
    }

    // //////////////////////////////////////
    // Inner Class
    // //////////////////////////////////////

    public enum MACDPeriod implements CalcPeriod {
        P_09_17_07(9, 17, 7),
        P_12_26_09(12, 26, 9);
        private int shortPeriod;
        private int longPeriod;
        private int triggerPriod;
        private MACDPeriod(int shortPeriod, int longPeriod, int triggerPriod) {
            this.shortPeriod = shortPeriod;
            this.longPeriod = longPeriod;
            this.triggerPriod = triggerPriod;
        }
        public int getShortPeriod() {
            return shortPeriod;
        }
        public int getLongPeriod() {
            return longPeriod;
        }
        public int getTriggerPriod() {
            return triggerPriod;
        }
        @Override
        public String getName() {
            return name();
        }
    }
}
