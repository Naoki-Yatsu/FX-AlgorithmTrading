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
import ny2.ats.indicator.indicators.PriceRangeIndicator.PriceRangePeriod;

/**
 * 価格レンジ(サポートライン)のIndicatorクラスです。
 */
public class PriceRangeIndicator extends SimpleIndicator<PriceRangePeriod> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** 上値サポート データ列 */
    protected final SortedMap<PriceRangePeriod, List<Double>> upperSupportMap = new TreeMap<>();
    /** 下値サポート データ列 */
    protected final SortedMap<PriceRangePeriod, List<Double>> lowerSupportMap = valueMap;

    /** 上値サポート・有効性 データ列 */
    protected final SortedMap<PriceRangePeriod, List<Boolean>> upperSupportValidMap = new TreeMap<>();
    /** 下値サポート・有効性 データ列 */
    protected final SortedMap<PriceRangePeriod, List<Boolean>> lowerSupportValidMap = new TreeMap<>();

    /** 上値サポート・トライ回数 データ列 */
    protected final SortedMap<PriceRangePeriod, List<Integer>> upperSupportTrialMap = new TreeMap<>();
    /** 下値サポート・トライ回数 データ列 */
    protected final SortedMap<PriceRangePeriod, List<Integer>> lowerSupportTrialMap = new TreeMap<>();


    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public PriceRangeIndicator(IndicatorType type, Symbol symbol, Period period) {
        super(PriceRangeIndicator.class, type, symbol, period);

        // 各Map初期化
        for (PriceRangePeriod calcPeriod : PriceRangePeriod.values()) {
            upperSupportMap.put(calcPeriod, new ArrayList<>());
            lowerSupportMap.put(calcPeriod, new ArrayList<>());
            upperSupportValidMap.put(calcPeriod, new ArrayList<>());
            lowerSupportValidMap.put(calcPeriod, new ArrayList<>());
            upperSupportTrialMap.put(calcPeriod, new ArrayList<>());
            lowerSupportTrialMap.put(calcPeriod, new ArrayList<>());
        }
    }

    @Override
    protected void initializeMap() {
        // do in constructor
    }

    // //////////////////////////////////////
    // Method (@Override)
    // //////////////////////////////////////

    @Override
    public boolean isInitialized(PriceRangePeriod calcPeriod) {
        // とりあえずデータ件数がそろっていればよい
        if (getDataSize() >= calcPeriod.getPeriodCount()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getDataString() {
        StringBuilder sb = new StringBuilder();
        for (PriceRangePeriod calcPeriod : valueMap.keySet()) {
            // 最後の値を取得
            Double upper = CollectionUtility.getLast(upperSupportMap.get(calcPeriod));
            Double lower = CollectionUtility.getLast(lowerSupportMap.get(calcPeriod));
            Boolean upperValid = CollectionUtility.getLast(upperSupportValidMap.get(calcPeriod));
            Boolean lowerValid = CollectionUtility.getLast(lowerSupportValidMap.get(calcPeriod));
            Integer upperTrial = CollectionUtility.getLast(upperSupportTrialMap.get(calcPeriod));
            Integer lowerTrial = CollectionUtility.getLast(lowerSupportTrialMap.get(calcPeriod));

            sb.append(calcPeriod.getName()).append(NAME_DELIMITER)
                    .append(upper.toString()).append(VALUE_DELIMITER)
                    .append(lower.toString()).append(VALUE_DELIMITER)
                    .append(upperTrial.toString()).append(VALUE_DELIMITER)
                    .append(lowerTrial.toString())
                    .append(DATA_DELIMITER);
        }
        // Delete the last delimiter
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    /**
     * value1: MA
     * value2: sigma
     * value3: plus
     * value4: minus
     */
    @Override
    public Map<CalcPeriod, List<Double>> getLastValueMap() {
        Map<CalcPeriod, List<Double>> lastValueMap = new HashMap<>();
        for (PriceRangePeriod calcPeriod : PriceRangePeriod.values()) {
            List<Double> lastValueList = new ArrayList<>();
            lastValueList.add(CollectionUtility.getLast(upperSupportMap.get(calcPeriod)));
            lastValueList.add(CollectionUtility.getLast(lowerSupportMap.get(calcPeriod)));
            lastValueList.add(CollectionUtility.getLast(upperSupportValidMap.get(calcPeriod)) ? 1.0 : 0.0);
            lastValueList.add(CollectionUtility.getLast(lowerSupportValidMap.get(calcPeriod)) ? 1.0 : 0.0);
            lastValueList.add(CollectionUtility.getLast(upperSupportTrialMap.get(calcPeriod)).doubleValue());
            lastValueList.add(CollectionUtility.getLast(lowerSupportTrialMap.get(calcPeriod)).doubleValue());
            lastValueMap.put(calcPeriod, lastValueList);
        }
        return lastValueMap;
    }

    @Override
    public int getCountCalcPeriod() {
        return PriceRangePeriod.values().length;
    }

    /**
     * データ追加にこのメソッドは使用しない
     */
    @Deprecated
    @Override
    public void addValueData(PriceRangePeriod calcPeriod, Double value) {
        throw new ATSRuntimeException("Don't call this method");
    }

    /**
     * データを追加します
     * @param calcPeriod
     * @param upperValue
     * @param lowerValue
     * @param upperValid
     * @param lowerValid
     * @param upperTrial
     * @param lowerTrial
     */
    public void addValueData(PriceRangePeriod calcPeriod, Double upperValue, Double lowerValue, Boolean upperValid, Boolean lowerValid, Integer upperTrial, Integer lowerTrial) {
        // support
        upperSupportMap.get(calcPeriod).add(upperValue);
        lowerSupportMap.get(calcPeriod).add(lowerValue);

        // validation
        upperSupportValidMap.get(calcPeriod).add(upperValid);
        lowerSupportValidMap.get(calcPeriod).add(lowerValid);

        // trial count
        upperSupportTrialMap.get(calcPeriod).add(upperTrial);
        lowerSupportTrialMap.get(calcPeriod).add(lowerTrial);
    }

    @Override
    protected void reduceDatAdditionalItems(int holdDays, int remainFromIndex) {
        for (List<Double> list : upperSupportMap.values()) {
            CollectionUtility.removeHeadIndex(list, remainFromIndex);
        }
        for (List<Boolean> list : upperSupportValidMap.values()) {
            CollectionUtility.removeHeadIndex(list, remainFromIndex);
        }
        for (List<Boolean> list : lowerSupportValidMap.values()) {
            CollectionUtility.removeHeadIndex(list, remainFromIndex);
        }
        for (List<Integer> list : upperSupportTrialMap.values()) {
            CollectionUtility.removeHeadIndex(list, remainFromIndex);
        }
        for (List<Integer> list : lowerSupportTrialMap.values()) {
            CollectionUtility.removeHeadIndex(list, remainFromIndex);
        }
    };

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    // 各種最新値取得
    public Double getLastUpperSupport(PriceRangePeriod calcPeriod) {
        return CollectionUtility.getLast(upperSupportMap.get(calcPeriod));
    }
    public Double getLastLowerSupport(PriceRangePeriod calcPeriod) {
        return CollectionUtility.getLast(lowerSupportMap.get(calcPeriod));
    }
    public Boolean getLastUpperSupportValid(PriceRangePeriod calcPeriod) {
        return CollectionUtility.getLast(upperSupportValidMap.get(calcPeriod));
    }
    public Boolean getLastLowerSupportValid(PriceRangePeriod calcPeriod) {
        return CollectionUtility.getLast(lowerSupportValidMap.get(calcPeriod));
    }
    public Integer getLastUpperSupportTrial(PriceRangePeriod calcPeriod) {
        return CollectionUtility.getLast(upperSupportTrialMap.get(calcPeriod));
    }
    public Integer getLastLowerSupportTrial(PriceRangePeriod calcPeriod) {
        return CollectionUtility.getLast(lowerSupportTrialMap.get(calcPeriod));
    }

    // 各種リスト取得
    public List<Double> getUpperSupportList(PriceRangePeriod calcPeriod) {
        return upperSupportMap.get(calcPeriod);
    }
    public List<Double> getLowerSupportList(PriceRangePeriod calcPeriod) {
        return lowerSupportMap.get(calcPeriod);
    }
    public List<Boolean> getUpperSupportValidList(PriceRangePeriod calcPeriod) {
        return upperSupportValidMap.get(calcPeriod);
    }
    public List<Boolean> getLowerSupportValidList(PriceRangePeriod calcPeriod) {
        return lowerSupportValidMap.get(calcPeriod);
    }
    public List<Integer> getUpperSupportTrialList(PriceRangePeriod calcPeriod) {
        return upperSupportTrialMap.get(calcPeriod);
    }
    public List<Integer> getLowerSupportTrialList(PriceRangePeriod calcPeriod) {
        return lowerSupportTrialMap.get(calcPeriod);
    }


    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

    public SortedMap<PriceRangePeriod, List<Double>> getUpperSupportMap() {
        return upperSupportMap;
    }
    public SortedMap<PriceRangePeriod, List<Double>> getLowerSupportMap() {
        return lowerSupportMap;
    }
    public SortedMap<PriceRangePeriod, List<Boolean>> getUpperSupportValidMap() {
        return upperSupportValidMap;
    }
    public SortedMap<PriceRangePeriod, List<Boolean>> getLowerSupportValidMap() {
        return lowerSupportValidMap;
    }
    public SortedMap<PriceRangePeriod, List<Integer>> getUpperSupportTrialMap() {
        return upperSupportTrialMap;
    }
    public SortedMap<PriceRangePeriod, List<Integer>> getLowerSupportTrialMap() {
        return lowerSupportTrialMap;
    }

    // //////////////////////////////////////
    // Inner Class
    // //////////////////////////////////////

    /**
     * Support Line の計算期間を表します
     * 主にMIN_5での計算を想定しています。
     */
    public enum PriceRangePeriod implements CalcPeriod {
        // 120分(24)+α
        P025(25, 0.001),
        // 6時間(72)+α
        P075(75, 0.0015),
        // 12時間(144)+α
        P150(150, 0.002),
        // 24時間(288)+α
        P300(300, 0.002),
        // 2日(576)+α
        P600(600, 0.003);

        /** 計算期間 */
        private int periodCount;

        /** 極値/Spikeを判別する価格変化 */
        private double extremumRatio;

        private PriceRangePeriod(int periodCount, double extremumRatio) {
            this.periodCount = periodCount;
            this.extremumRatio = extremumRatio;
        }
        public int getPeriodCount() {
            return periodCount;
        }
        public double getExtremumRatio() {
            return extremumRatio;
        }
        @Override
        public String getName() {
            return name();
        }
    }
}
