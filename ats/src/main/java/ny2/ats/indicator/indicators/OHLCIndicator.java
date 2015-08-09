package ny2.ats.indicator.indicators;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ny2.ats.core.common.Period;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.exception.ATSRuntimeException;
import ny2.ats.core.util.CollectionUtility;
import ny2.ats.indicator.CalcPeriod;
import ny2.ats.indicator.IndicatorType;
import ny2.ats.indicator.impl.OHLC;
import ny2.ats.indicator.indicators.OHLCIndicator.OHLCType;

/**
 * Indicator用のOHLCのクラスです
 */
public class OHLCIndicator extends SimpleIndicator<OHLCType> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** データ列 */
    // valueMap にはOHLCをそれぞれ入れる

    /** OHLC列 */
    private final List<OHLC> ohlcList = new ArrayList<>();

    /** openデータ列 */
    private final List<Double> openList = new ArrayList<>();

    /** highデータ列 */
    private final List<Double> highList = new ArrayList<>();

    /** lowデータ列 */
    private final List<Double> lowList = new ArrayList<>();

    /** closeデータ列 */
    private final List<Double> closeList = new ArrayList<>();

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public OHLCIndicator(IndicatorType type, Symbol symbol, Period period) {
        super(OHLCIndicator.class, type, symbol, period);
        initializeMapForOHLC();
    }

    @Override
    protected void initializeMap() {
        // valueMap初期化 は各Listインスタンス作成後に行う
    }

    /**
     * initializeMapはsuperから呼び出されるため、別途設定
     */
    private void initializeMapForOHLC() {
        // valueMap初期化
        valueMap.put(OHLCType.OPEN, openList);
        valueMap.put(OHLCType.HIGH, highList);
        valueMap.put(OHLCType.LOW, lowList);
        valueMap.put(OHLCType.CLOSE, closeList);
    }


    // //////////////////////////////////////
    // Method (@Override)
    // //////////////////////////////////////

    @Override
    public String getDataString() {
        StringBuilder sb = new StringBuilder();

        // 最後の値を取得、使用しているBid/Ask種別で文字列表現にする
        OHLC ohlc = CollectionUtility.getLast(ohlcList);
        sb.append(OHLCType.OPEN.name()).append(NAME_DELIMITER).append(symbol.roundSubSubPips(ohlc.getOpen())).append(DATA_DELIMITER);
        sb.append(OHLCType.HIGH.name()).append(NAME_DELIMITER).append(symbol.roundSubSubPips(ohlc.getHigh())).append(DATA_DELIMITER);
        sb.append(OHLCType.LOW.name()).append(NAME_DELIMITER).append(symbol.roundSubSubPips(ohlc.getLow())).append(DATA_DELIMITER);
        sb.append(OHLCType.CLOSE.name()).append(NAME_DELIMITER).append(symbol.roundSubSubPips(ohlc.getClose()));
        return sb.toString();
    }

    @Override
    public Map<CalcPeriod, List<Double>> getLastValueMap() {
        Map<CalcPeriod, List<Double>> lastValueMap = new HashMap<>();
        List<Double> lastValueList = new ArrayList<>();
        lastValueList.add(CollectionUtility.getLast(openList));
        lastValueList.add(CollectionUtility.getLast(highList));
        lastValueList.add(CollectionUtility.getLast(lowList));
        lastValueList.add(CollectionUtility.getLast(closeList));
        lastValueMap.put(OHLCType.ALL, lastValueList);
        return lastValueMap;
    }

    @Override
    public int getCountCalcPeriod() {
        // ALLのみ
        return 1;
    }

    /**
     * データ追加にこのメソッドは使用しない
     */
    @Deprecated
    @Override
    public void addValueData(OHLCType calcPeriod, Double value) {
        throw new ATSRuntimeException("Don't call this method");
    }

    /**
     * OHLCのデータ追加はこちらのメソッドを使用する
     * @param dateTime
     * @param ohlc
     */
    public void addValueData(OHLC ohlc) {
        ohlcList.add(ohlc);
        // OHLCをそれぞれ追加
        openList.add(ohlc.getOpen());
        highList.add(ohlc.getHigh());
        lowList.add(ohlc.getLow());
        closeList.add(ohlc.getClose());
    }

    @Override
    protected void reduceDatAdditionalItems(int holdDays, int remainFromIndex) {
        // ohlcList のみ追加リスト
        CollectionUtility.removeHeadIndex(ohlcList, remainFromIndex);
    };

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    /**
     * 最新のOHLCを返します
     */
    public OHLC getLastOHLC() {
        return CollectionUtility.getLast(ohlcList);
    }

    /**
     * 最新の終値を返します
     */
    public Double getLastClose() {
        return CollectionUtility.getLast(closeList);
    }

    /**
     * 最新N期間のOHLCのリストを返します。
     * @param count
     * @return
     */
    public List<OHLC> getOHLCLastSubList(int count) {
        return CollectionUtility.lastSubListView(ohlcList, count);
    }

    /**
     * 特定の日時以降のOHLCのリストを返します。
     * @param fromDateTime
     * @return
     */
    public List<OHLC> getOHLCLastSubList(LocalDateTime fromDateTime) {
        int fromIndex = dateTimeList.indexOf(fromDateTime);
        if (fromIndex < 0) {
            return null;
        }
        return ohlcList.subList(fromIndex, ohlcList.size());
    }

    /**
     * 指定した種別の最新N期間のOHLCのリストを返します。
     * @param count
     * @return
     */
    public List<Double> getValueLastSubList(OHLCType ohlcType, int count) {
        switch (ohlcType) {
            case OPEN:
                return CollectionUtility.lastSubListView(openList, count);
            case HIGH:
                return CollectionUtility.lastSubListView(highList, count);
            case LOW:
                return CollectionUtility.lastSubListView(lowList, count);
            case CLOSE:
                return CollectionUtility.lastSubListView(closeList, count);
            default:
                return null;
        }
    }

    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

    public List<OHLC> getOHLCList() {
        return ohlcList;
    }

    public List<Double> getOpenList() {
        return openList;
    }

    public List<Double> getHighList() {
        return highList;
    }

    public List<Double> getLowList() {
        return lowList;
    }

    public List<Double> getCloseList() {
        return closeList;
    }

    // //////////////////////////////////////
    // Inner Class
    // //////////////////////////////////////

    public enum OHLCType implements CalcPeriod {
        ALL,
        // each item
        OPEN,
        HIGH,
        LOW,
        CLOSE;
        @Override
        public String getName() {
            return name();
        }
    }
}
