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
import ny2.ats.indicator.indicators.BollingerBandIndicator.BollingerPeriod;

/**
 * Bollinger Band のクラスです。
 * 計算にはSMAを使用します
 */
public class BollingerBandIndicator extends SimpleIndicator<BollingerPeriod> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** データ列 */
    // valueMap には SMA を設定

    /** SMA データ列 */
    protected final SortedMap<BollingerPeriod, List<Double>> maMap = valueMap;

    /** sigma データ列 */
    protected final SortedMap<BollingerPeriod, List<Double>> sigmaMap = new TreeMap<>();

    /** +1s データ列 */
    protected final SortedMap<BollingerPeriod, List<Double>> plusMap = new TreeMap<>();

    /** -1s データ列 */
    protected final SortedMap<BollingerPeriod, List<Double>> minusMap = new TreeMap<>();

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public BollingerBandIndicator(IndicatorType type, Symbol symbol, Period period) {
        this(BollingerBandIndicator.class, type, symbol, period);
    }

    // For sub-class
    public BollingerBandIndicator(Class<?> indicatorClass, IndicatorType type, Symbol symbol, Period period) {
        super(indicatorClass, type, symbol, period);
        initializeMapForBollinger();
    }

    @Override
    protected void initializeMap() {
        // valueMap初期化
        for (BollingerPeriod calcPeriod : BollingerPeriod.values()) {
            valueMap.put(calcPeriod, new ArrayList<>());
        }
    }

    /**
     * initializeMapはsuperから呼び出されるため、shortMapなどのインスタンス作成前のため、後で呼び出す
     */
    private void initializeMapForBollinger() {
        // valueMap初期化
        for (BollingerPeriod calcPeriod : BollingerPeriod.values()) {
            sigmaMap.put(calcPeriod, new ArrayList<>());
            plusMap.put(calcPeriod, new ArrayList<>());
            minusMap.put(calcPeriod, new ArrayList<>());
        }
    }

    // //////////////////////////////////////
    // Method (@Override)
    // //////////////////////////////////////

    @Override
    public boolean isInitialized(BollingerPeriod calcPeriod) {
        // sigmaが作成されていればよい
        Double lastValue = getLastSigma(calcPeriod);
        if (lastValue == null || Double.isNaN(lastValue)) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public String getDataString() {
        StringBuilder sb = new StringBuilder();
        for (BollingerPeriod calcPeriod : valueMap.keySet()) {
            // 最後の値を取得
            Double ma = CollectionUtility.getLast(maMap.get(calcPeriod));
            Double sigma = CollectionUtility.getLast(sigmaMap.get(calcPeriod));
            Double plus = CollectionUtility.getLast(plusMap.get(calcPeriod));
            Double minus = CollectionUtility.getLast(minusMap.get(calcPeriod));

            sb.append(calcPeriod.getName()).append(NAME_DELIMITER)
                    .append(ma.toString()).append(VALUE_DELIMITER)
                    .append(sigma.toString()).append(VALUE_DELIMITER)
                    .append(plus.toString()).append(VALUE_DELIMITER)
                    .append(minus.toString())
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
        for (BollingerPeriod calcPeriod : BollingerPeriod.values()) {
            List<Double> lastValueList = new ArrayList<>();
            lastValueList.add(CollectionUtility.getLast(maMap.get(calcPeriod)));
            lastValueList.add(CollectionUtility.getLast(sigmaMap.get(calcPeriod)));
            lastValueList.add(CollectionUtility.getLast(plusMap.get(calcPeriod)));
            lastValueList.add(CollectionUtility.getLast(minusMap.get(calcPeriod)));
            lastValueMap.put(calcPeriod, lastValueList);
        }
        return lastValueMap;
    }

    @Override
    public int getCountCalcPeriod() {
        return BollingerPeriod.values().length;
    }

    /**
     * データ追加にこのメソッドは使用しない
     */
    @Deprecated
    @Override
    public void addValueData(BollingerPeriod calcPeriod, Double value) {
        throw new ATSRuntimeException("Don't call this method");
    }

    /**
     * データを追加します
     * @param calcPeriod
     * @param smaValue
     * @param sigmaValue
     * @param plusValue
     * @param minusValue
     */
    public void addValueData(BollingerPeriod calcPeriod, Double smaValue, Double sigmaValue, Double plusValue, Double minusValue) {
        // MA
        maMap.get(calcPeriod).add(smaValue);
        // sigma
        sigmaMap.get(calcPeriod).add(sigmaValue);
        // plus
        plusMap.get(calcPeriod).add(plusValue);
        // minus
        minusMap.get(calcPeriod).add(minusValue);
    }

    @Override
    protected void reduceDatAdditionalItems(int holdDays, int remainFromIndex) {
        // 追加は sigmaMap, plusMap, minusMap
        for (List<Double> list : sigmaMap.values()) {
            CollectionUtility.removeHeadIndex(list, remainFromIndex);
        }
        for (List<Double> list : plusMap.values()) {
            CollectionUtility.removeHeadIndex(list, remainFromIndex);
        }
        for (List<Double> list : minusMap.values()) {
            CollectionUtility.removeHeadIndex(list, remainFromIndex);
        }
    };

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    // 各種最新値取得
    public Double getLastMA(BollingerPeriod calcPeriod) {
        return CollectionUtility.getLast(maMap.get(calcPeriod));
    }
    public Double getLastSigma(BollingerPeriod calcPeriod) {
        return CollectionUtility.getLast(sigmaMap.get(calcPeriod));
    }
    public Double getLastPlus(BollingerPeriod calcPeriod) {
        return CollectionUtility.getLast(plusMap.get(calcPeriod));
    }
    public Double getLastMinus(BollingerPeriod calcPeriod) {
        return CollectionUtility.getLast(minusMap.get(calcPeriod));
    }

    // 各種リスト取得
    public List<Double> getMAList(BollingerPeriod calcPeriod) {
        return maMap.get(calcPeriod);
    }
    public List<Double> getSigmaList(BollingerPeriod calcPeriod) {
        return sigmaMap.get(calcPeriod);
    }
    public List<Double> getPlusList(BollingerPeriod calcPeriod) {
        return plusMap.get(calcPeriod);
    }
    public List<Double> getMinusList(BollingerPeriod calcPeriod) {
        return minusMap.get(calcPeriod);
    }

    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////


    // //////////////////////////////////////
    // Inner Class
    // //////////////////////////////////////

    public enum BollingerPeriod implements CalcPeriod {
        P20(20),
        P40(40),
        P60(60);
        private int period;
        private BollingerPeriod(int period) {
            this.period = period;
        }
        public int getPeriod() {
            return period;
        }
        @Override
        public String getName() {
            return name();
        }
    }

}
