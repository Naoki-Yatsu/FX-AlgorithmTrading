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
import ny2.ats.indicator.indicators.LinearRegressionIndicator.LRPeriod;

/**
 * 線形回帰分析をあわらすクラスです。 <br>
 * SimpleIndicatoのvalueとして、回帰分析の傾きを持ちます。 <br>
 * クラス個別の値として、計算基準時刻での切片の値を持ちます。 <br>
 * ※切片情報は現在時刻で保持します。
 */
public class LinearRegressionIndicator extends SimpleIndicator<LRPeriod> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // valueMapは傾きの値を設定

    /** 回帰分析の傾きデータ列 */
    protected final SortedMap<LRPeriod, List<Double>> slopeMap = valueMap;

    /** 回帰分析の現在時刻切片データ列 */
    protected final SortedMap<LRPeriod, List<Double>> currentInterceptMap = new TreeMap<>();

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public LinearRegressionIndicator(IndicatorType type, Symbol symbol, Period period) {
        super(LinearRegressionIndicator.class, type, symbol, period);
        initializeMapForLR();
    }

    @Override
    protected void initializeMap() {
        // valueMap初期化
        for (LRPeriod lrPeriod : LRPeriod.values()) {
            valueMap.put(lrPeriod, new ArrayList<>());
        }
    }

    /**
     * initializeMapはsuperから呼び出されるため、shortMapなどのインスタンス作成前のため、後で呼び出す
     */
    private void initializeMapForLR() {
        // valueMap初期化
        for (LRPeriod lrPeriod : LRPeriod.values()) {
            currentInterceptMap.put(lrPeriod, new ArrayList<>());
        }
    }

    // //////////////////////////////////////
    // Method (@Override)
    // //////////////////////////////////////

    @Override
    public String getDataString() {
        StringBuilder sb = new StringBuilder();
        for (LRPeriod lrPeriod : valueMap.keySet()) {
            // 最後の値を取得
            Double gradient = CollectionUtility.getLast(slopeMap.get(lrPeriod));
            Double intercept = CollectionUtility.getLast(currentInterceptMap.get(lrPeriod));

            sb.append(lrPeriod.getName()).append(NAME_DELIMITER)
                    .append(gradient).append(VALUE_DELIMITER)
                    .append(intercept).append(DATA_DELIMITER);
        }
        // Delete the last delimiter
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    /**
     * value1: slope
     * value2: intercept
     */
    @Override
    public Map<CalcPeriod, List<Double>> getLastValueMap() {
        Map<CalcPeriod, List<Double>> lastValueMap = new HashMap<>();
        for (LRPeriod calcPeriod : LRPeriod.values()) {
            List<Double> lastValueList = new ArrayList<>();
            lastValueList.add(CollectionUtility.getLast(slopeMap.get(calcPeriod)));
            lastValueList.add(CollectionUtility.getLast(currentInterceptMap.get(calcPeriod)));
            lastValueMap.put(calcPeriod, lastValueList);
        }
        return lastValueMap;
    }

    @Override
    public int getCountCalcPeriod() {
        return LRPeriod.values().length;
    }

    /**
     * データ追加にこのメソッドは使用しない
     */
    @Deprecated
    @Override
    public void addValueData(LRPeriod calcPeriod, Double value) {
        throw new ATSRuntimeException("Don't call this method");
    }

    /**
     * 回帰分析データを追加します。
     *
     * @param lrPeriod
     * @param slope
     * @param intercept
     */
    public void addValueData(LRPeriod lrPeriod, Double slope, Double intercept) {
        // 傾き
        slopeMap.get(lrPeriod).add(slope);
        // 現在時刻切片
        currentInterceptMap.get(lrPeriod).add(intercept);
    }

    @Override
    protected void reduceDatAdditionalItems(int holdDays, int remainFromIndex) {
        // 追加は currentInterceptMap
        for (List<Double> list : currentInterceptMap.values()) {
            CollectionUtility.removeHeadIndex(list, remainFromIndex);
        }
    };

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    // 各種最新値取得
    public Double getLastGradient(LRPeriod lrPeriod) {
        return CollectionUtility.getLast(slopeMap.get(lrPeriod));
    }

    public Double getLastIntercept(LRPeriod lrPeriod) {
        return CollectionUtility.getLast(currentInterceptMap.get(lrPeriod));
    }

    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

    public SortedMap<LRPeriod, List<Double>> getSlopeMap() {
        return slopeMap;
    }

    public SortedMap<LRPeriod, List<Double>> getInterceptMap() {
        return currentInterceptMap;
    }

    // //////////////////////////////////////
    // Inner Class
    // //////////////////////////////////////

    public enum LRPeriod implements CalcPeriod {
        P03(3),
        P05(5),
        P10(10),
        P20(20),
        P50(50);
        private int periodCount;

        private LRPeriod(int periodCount) {
            this.periodCount = periodCount;
        }

        public int getPeriodCount() {
            return periodCount;
        }

        @Override
        public String getName() {
            return name();
        }
    }
}
