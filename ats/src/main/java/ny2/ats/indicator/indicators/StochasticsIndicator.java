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
import ny2.ats.indicator.indicators.StochasticsIndicator.StochasticsPeriod;

/**
 * Stochasticsのクラスです。
 * SimpleIndicatoのvalueとして、Stochasticsの%Kの値を持ちます。
 * 計算に使用する長短EMA、およびシグナル(Trigger)はクラス固有の値として保持します。
 */
public class StochasticsIndicator extends SimpleIndicator<StochasticsPeriod> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // valueMapは%Dの値を設定

    /** %K データ列 */
    protected final SortedMap<StochasticsPeriod, List<Double>> kMap = new TreeMap<>();

    /** %D データ列 */
    protected final SortedMap<StochasticsPeriod, List<Double>> dMap = valueMap;

    /** Slow %D データ列 */
    protected final SortedMap<StochasticsPeriod, List<Double>> slowDMap = new TreeMap<>();

    // 途中計算用
    /** 終値 - X最安値 */
    protected final SortedMap<StochasticsPeriod, List<Double>> closeMinMap = new TreeMap<>();

    /** X最高値 - X最安値 */
    protected final SortedMap<StochasticsPeriod, List<Double>> maxMinMap = new TreeMap<>();

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public StochasticsIndicator(IndicatorType type, Symbol symbol, Period period) {
        super(StochasticsIndicator.class, type, symbol, period);
        initializeMapForStochastics();
    }

    @Override
    protected void initializeMap() {
        // valueMap初期化
        for (StochasticsPeriod stochasticsPeriod : StochasticsPeriod.values()) {
            valueMap.put(stochasticsPeriod, new ArrayList<>());
        }
    }

    /**
     * initializeMapはsuperから呼び出されるため、別途設定
     */
    private void initializeMapForStochastics() {
        // valueMap初期化
        for (StochasticsPeriod stochasticsPeriod : StochasticsPeriod.values()) {
            kMap.put(stochasticsPeriod, new ArrayList<>());
//            dMap.put(stochasticsPeriod, new ArrayList<>());
            slowDMap.put(stochasticsPeriod, new ArrayList<>());
            closeMinMap.put(stochasticsPeriod, new ArrayList<>());
            maxMinMap.put(stochasticsPeriod, new ArrayList<>());
        }
    }

    // //////////////////////////////////////
    // Method (@Override)
    // //////////////////////////////////////

    @Override
    public boolean isInitialized(StochasticsPeriod calcPeriod) {
        Double lastValue = getLastSlowD(calcPeriod);
        if (lastValue == null || Double.isNaN(lastValue)) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public String getDataString() {
        StringBuilder sb = new StringBuilder();
        for (StochasticsPeriod stochasticsPeriod : valueMap.keySet()) {
            // 最後の値を取得
            Double k = CollectionUtility.getLast(kMap.get(stochasticsPeriod));
            Double d = CollectionUtility.getLast(dMap.get(stochasticsPeriod));
            Double slowD = CollectionUtility.getLast(slowDMap.get(stochasticsPeriod));

            sb.append(stochasticsPeriod.getName()).append(NAME_DELIMITER)
                    .append(k.toString()).append(VALUE_DELIMITER)
                    .append(d.toString()).append(VALUE_DELIMITER)
                    .append(slowD.toString()).append(DATA_DELIMITER);
        }
        // Delete the last delimiter
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    /**
     * value1: Stochastics
     * value2: %K
     * value3: %D
     * value4: Slow %D
     */
    @Override
    public Map<CalcPeriod, List<Double>> getLastValueMap() {
        Map<CalcPeriod, List<Double>> lastValueMap = new HashMap<>();
        for (StochasticsPeriod calcPeriod : StochasticsPeriod.values()) {
            List<Double> lastValueList = new ArrayList<>();
            lastValueList.add(CollectionUtility.getLast(kMap.get(calcPeriod)));
            lastValueList.add(CollectionUtility.getLast(dMap.get(calcPeriod)));
            lastValueList.add(CollectionUtility.getLast(slowDMap.get(calcPeriod)));
            lastValueMap.put(calcPeriod, lastValueList);
        }
        return lastValueMap;
    }

    @Override
    public int getCountCalcPeriod() {
        return StochasticsPeriod.values().length;
    }

    /**
     * データ追加にこのメソッドは使用しない
     */
    @Deprecated
    @Override
    public void addValueData(StochasticsPeriod calcPeriod, Double value) {
        throw new ATSRuntimeException("Don't call this method");
    }

    /**
     * Stochasticsにデータを追加します。
     * @param stochasticsPeriod
     * @param dateTime
     * @param kValue
     * @param dValue
     * @param slowDValue
     */
    public synchronized void addValueData(StochasticsPeriod stochasticsPeriod, Double kValue, Double dValue, Double slowDValue, Double closeMin, Double maxMin) {
        // k
        kMap.get(stochasticsPeriod).add(kValue);
        // d
        dMap.get(stochasticsPeriod).add(dValue);
        // slow d
        slowDMap.get(stochasticsPeriod).add(slowDValue);
        // close - min
        closeMinMap.get(stochasticsPeriod).add(closeMin);
        // max - min
        maxMinMap.get(stochasticsPeriod).add(maxMin);
    }

    @Override
    protected void reduceDatAdditionalItems(int holdDays, int remainFromIndex) {
        // 追加は kMap, dMap, closeMinMap
        for (List<Double> list : kMap.values()) {
            CollectionUtility.removeHeadIndex(list, remainFromIndex);
        }
        for (List<Double> list : slowDMap.values()) {
            CollectionUtility.removeHeadIndex(list, remainFromIndex);
        }
        for (List<Double> list : closeMinMap.values()) {
            CollectionUtility.removeHeadIndex(list, remainFromIndex);
        }
        for (List<Double> list : maxMinMap.values()) {
            CollectionUtility.removeHeadIndex(list, remainFromIndex);
        }
    };

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    // 各種最新値取得
    public Double getLastK(StochasticsPeriod stochasticsPeriod) {
        return CollectionUtility.getLast(kMap.get(stochasticsPeriod));
    }
    public Double getLastD(StochasticsPeriod stochasticsPeriod) {
        return CollectionUtility.getLast(dMap.get(stochasticsPeriod));
    }
    public Double getLastSlowD(StochasticsPeriod stochasticsPeriod) {
        return CollectionUtility.getLast(slowDMap.get(stochasticsPeriod));
    }

    // 各種リスト取得
    public List<Double> getKtList(StochasticsPeriod stochasticsPeriod) {
        return kMap.get(stochasticsPeriod);
    }
    public List<Double> getDList(StochasticsPeriod stochasticsPeriod) {
        return dMap.get(stochasticsPeriod);
    }
    public List<Double> getSlowDList(StochasticsPeriod stochasticsPeriod) {
        return slowDMap.get(stochasticsPeriod);
    }
    public List<Double> getCloseMinList(StochasticsPeriod stochasticsPeriod) {
        return closeMinMap.get(stochasticsPeriod);
    }
    public List<Double> getMaxMinList(StochasticsPeriod stochasticsPeriod) {
        return maxMinMap.get(stochasticsPeriod);
    }

    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

    public SortedMap<StochasticsPeriod, List<Double>> getKMap() {
        return kMap;
    }
    public SortedMap<StochasticsPeriod, List<Double>> getDMap() {
        return dMap;
    }
    public SortedMap<StochasticsPeriod, List<Double>> getSlowDMap() {
        return slowDMap;
    }

    public SortedMap<StochasticsPeriod, List<Double>> getCloseMinMap() {
        return closeMinMap;
    }
    public SortedMap<StochasticsPeriod, List<Double>> getMaxMinMap() {
        return maxMinMap;
    }

    // //////////////////////////////////////
    // Inner Class
    // //////////////////////////////////////

    public enum StochasticsPeriod implements CalcPeriod {
        P_05_3_3(5, 3, 3),
        P_09_3_3(9, 3, 3),
        P_14_3_3(14, 3, 3);
        private int kPeriod;
        private int dPeriod;
        private int slowDPeriod;
        private StochasticsPeriod(int kPeriod, int dPeriod, int slowDPeriod) {
            this.kPeriod = kPeriod;
            this.dPeriod = dPeriod;
            this.slowDPeriod = slowDPeriod;
        }
        @Override
        public String getName() {
            return name();
        }
        public int getKPeriod() {
            return kPeriod;
        }
        public int getDPeriod() {
            return dPeriod;
        }
        public int getSlowDPeriod() {
            return slowDPeriod;
        }
    }
}
