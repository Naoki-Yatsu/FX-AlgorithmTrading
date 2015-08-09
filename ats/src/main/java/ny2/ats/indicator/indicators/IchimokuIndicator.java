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
import ny2.ats.indicator.indicators.IchimokuIndicator.IchimokuPeriod;

/**
 * 一目均衡表のクラスです。
 * TODO 先行スパン、遅行スパンの値が現在値に入っているのが問題
 */
public class IchimokuIndicator extends SimpleIndicator<IchimokuPeriod> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // valueMapは基準線を設定

    /** 転換線 データ列 */
    protected final SortedMap<IchimokuPeriod, List<Double>> tenkanMap = new TreeMap<>();

    /** 基準線 データ列 */
    protected final SortedMap<IchimokuPeriod, List<Double>> kijunMap = valueMap;

    /** 先行スパン1 データ列 */
    protected final SortedMap<IchimokuPeriod, List<Double>> senkou1Map = new TreeMap<>();

    /** 先行スパン2 データ列 */
    protected final SortedMap<IchimokuPeriod, List<Double>> senkou2Map = new TreeMap<>();

    /** 遅行スパン データ列 */
    protected final SortedMap<IchimokuPeriod, List<Double>> chikouMap = new TreeMap<>();

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public IchimokuIndicator(IndicatorType type, Symbol symbol, Period period) {
        super(IchimokuIndicator.class, type, symbol, period);
        initializeMapForIchimoku();
    }

    @Override
    protected void initializeMap() {
        // valueMap初期化
        for (IchimokuPeriod calcPeriod : IchimokuPeriod.values()) {
            valueMap.put(calcPeriod, new ArrayList<>());
        }
    }

    /**
     * initializeMapはsuperから呼び出されるため、shortMapなどのインスタンス作成前のため、後で呼び出す
     */
    private void initializeMapForIchimoku() {
        // valueMap初期化
        for (IchimokuPeriod calcPeriod : IchimokuPeriod.values()) {
            tenkanMap.put(calcPeriod, new ArrayList<>());
            // kijunMap.put(calcPeriod, new ArrayList<>());
            senkou1Map.put(calcPeriod, new ArrayList<>());
            senkou2Map.put(calcPeriod, new ArrayList<>());
            chikouMap.put(calcPeriod, new ArrayList<>());
        }
    }

    // //////////////////////////////////////
    // Method (@Override)
    // //////////////////////////////////////

    @Override
    public boolean isInitialized(IchimokuPeriod calcPeriod) {
        Double lastValue = getLastSenkou2(calcPeriod);
        if (lastValue == null || Double.isNaN(lastValue)) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public String getDataString() {
        StringBuilder sb = new StringBuilder();
        for (IchimokuPeriod calcPeriod : kijunMap.keySet()) {
            // 最後の値を取得
            Double tenkan = CollectionUtility.getLast(tenkanMap.get(calcPeriod));
            Double kijun = CollectionUtility.getLast(kijunMap.get(calcPeriod));
            Double senkou1 = CollectionUtility.getLast(senkou1Map.get(calcPeriod));
            Double senkou2 = CollectionUtility.getLast(senkou2Map.get(calcPeriod));
            Double chikou = CollectionUtility.getLast(chikouMap.get(calcPeriod));
            sb.append(calcPeriod.getName()).append(NAME_DELIMITER)
                    .append(tenkan.toString()).append(VALUE_DELIMITER)
                    .append(kijun.toString()).append(VALUE_DELIMITER)
                    .append(senkou1.toString()).append(VALUE_DELIMITER)
                    .append(senkou2.toString()).append(VALUE_DELIMITER)
                    .append(chikou.toString())
                    .append(DATA_DELIMITER);
        }
        // Delete the last delimiter
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    /**
     * データ順序<br>
     * value1: 転換線
     * value2: 基準線
     * value3: 先行スパン1
     * value4: 先行スパン2
     * value5: 遅行スパン
     */
    @Override
    public Map<CalcPeriod, List<Double>> getLastValueMap() {
        Map<CalcPeriod, List<Double>> lastValueMap = new HashMap<>();
        for (IchimokuPeriod calcPeriod : IchimokuPeriod.values()) {
            List<Double> lastValueList = new ArrayList<>();
            lastValueList.add(CollectionUtility.getLast(tenkanMap.get(calcPeriod)));
            lastValueList.add(CollectionUtility.getLast(kijunMap.get(calcPeriod)));
            lastValueList.add(CollectionUtility.getLast(senkou1Map.get(calcPeriod)));
            lastValueList.add(CollectionUtility.getLast(senkou2Map.get(calcPeriod)));
            lastValueList.add(CollectionUtility.getLast(chikouMap.get(calcPeriod)));
            lastValueMap.put(calcPeriod, lastValueList);
        }
        return lastValueMap;
    }

    @Override
    public int getCountCalcPeriod() {
        return IchimokuPeriod.values().length;
    }

    /**
     * データ追加にこのメソッドは使用しない
     */
    @Deprecated
    @Override
    public void addValueData(IchimokuPeriod calcPeriod, Double value) {
        throw new ATSRuntimeException("Don't call this method");
    }

    /**
     * Ichimokuにデータを追加します。
     * @param calcPeriod
     * @param macdValue
     * @param shortValue
     * @param longValue
     * @param triggerValue
     */
    public void addValueData(IchimokuPeriod calcPeriod, Double tenkanValue, Double kijunValue, Double senkou1Value, Double senkou2Value, Double chikouValue) {

        // 先行/遅行の時刻
        // LocalDateTime senkouTime = dateTime.plus((calcPeriod.getSenkouPriod() - 1) * getPeriod().getTimeInverval(), getPeriod().getChronoUnit());
        // LocalDateTime chikouTime = dateTime.minus((calcPeriod.getKijunPeriod() - 1) * getPeriod().getTimeInverval(), getPeriod().getChronoUnit());

        // 転換線
        tenkanMap.get(calcPeriod).add(tenkanValue);
        // 基準線
        kijunMap.get(calcPeriod).add(kijunValue);
        // 先行1
        senkou1Map.get(calcPeriod).add(senkou1Value);
        // 先行2
        senkou2Map.get(calcPeriod).add(senkou2Value);
        // 遅行
        chikouMap.get(calcPeriod).add(chikouValue);
    }

    @Override
    protected void reduceDatAdditionalItems(int holdDays, int remainFromIndex) {
        // 追加は tenkanMap, senkou1Map, senkou2Map, chikouMap
        for (List<Double> list : tenkanMap.values()) {
            CollectionUtility.removeHeadIndex(list, remainFromIndex);
        }
        for (List<Double> list : senkou1Map.values()) {
            CollectionUtility.removeHeadIndex(list, remainFromIndex);
        }
        for (List<Double> list : senkou2Map.values()) {
            CollectionUtility.removeHeadIndex(list, remainFromIndex);
        }
        for (List<Double> list : chikouMap.values()) {
            CollectionUtility.removeHeadIndex(list, remainFromIndex);
        }
    };

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    // 各種最新値取得
    public Double getLastTenkan(IchimokuPeriod calcPeriod) {
        return CollectionUtility.getLast(tenkanMap.get(calcPeriod));
    }
    public Double getLastKijun(IchimokuPeriod calcPeriod) {
        return CollectionUtility.getLast(kijunMap.get(calcPeriod));
    }
    public Double getLastSenkou1(IchimokuPeriod calcPeriod) {
        return CollectionUtility.getLast(senkou1Map.get(calcPeriod));
    }
    public Double getLastSenkou2(IchimokuPeriod calcPeriod) {
        return CollectionUtility.getLast(senkou2Map.get(calcPeriod));
    }
    public Double getLastChikou(IchimokuPeriod calcPeriod) {
        return CollectionUtility.getLast(chikouMap.get(calcPeriod));
    }

    // 各種リスト取得
    public List<Double> getTenkanList(IchimokuPeriod calcPeriod) {
        return tenkanMap.get(calcPeriod);
    }
    public List<Double> getKijunList(IchimokuPeriod calcPeriod) {
        return kijunMap.get(calcPeriod);
    }
    public List<Double> getSenkou1List(IchimokuPeriod calcPeriod) {
        return senkou1Map.get(calcPeriod);
    }
    public List<Double> getSenkou2List(IchimokuPeriod calcPeriod) {
        return senkou2Map.get(calcPeriod);
    }
    public List<Double> getChikouList(IchimokuPeriod calcPeriod) {
        return chikouMap.get(calcPeriod);
    }

    /**
     * 先行スパン1の現在時刻の値を取得する
     * @param calcPeriod
     * @return
     */
    public Double getCurrentSenkou1(IchimokuPeriod calcPeriod) {
        List<Double> senkouList = senkou1Map.get(calcPeriod);
        return senkouList.get(senkouList.size() - calcPeriod.getKijunPeriod());
    }

    /**
     * 先行スパン2の現在時刻の値を取得する
     * @param calcPeriod
     * @return
     */
    public Double getCurrentSenkou2(IchimokuPeriod calcPeriod) {
        List<Double> senkouList = senkou2Map.get(calcPeriod);
        return senkouList.get(senkouList.size() - calcPeriod.getKijunPeriod());
    }


    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

    public SortedMap<IchimokuPeriod, List<Double>> getTenkanMap() {
        return tenkanMap;
    }
    public SortedMap<IchimokuPeriod, List<Double>> getKijunMap() {
        return kijunMap;
    }
    public SortedMap<IchimokuPeriod, List<Double>> getSenkou1Map() {
        return senkou1Map;
    }
    public SortedMap<IchimokuPeriod, List<Double>> getSenkou2Map() {
        return senkou2Map;
    }
    public SortedMap<IchimokuPeriod, List<Double>> getChikouMap() {
        return chikouMap;
    }

    // //////////////////////////////////////
    // Inner Class
    // //////////////////////////////////////

    public enum IchimokuPeriod implements CalcPeriod {
        P_06_24_48(6, 24, 48),
        P_09_26_52(9, 26, 52);
        private int tenkanPeriod;
        private int kijunPeriod;
        private int senkouPriod;

        private IchimokuPeriod(int tenkanPeriod, int kijunPeriod, int senkouPriod) {
            this.tenkanPeriod = tenkanPeriod;
            this.kijunPeriod = kijunPeriod;
            this.senkouPriod = senkouPriod;
        }
        public int getTenkanPeriod() {
            return tenkanPeriod;
        }
        public int getKijunPeriod() {
            return kijunPeriod;
        }
        public int getSenkouPriod() {
            return senkouPriod;
        }

        @Override
        public String getName() {
            return name();
        }
    }
}
