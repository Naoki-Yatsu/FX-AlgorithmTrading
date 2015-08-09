package ny2.ats.indicator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ny2.ats.core.common.Period;
import ny2.ats.core.common.Symbol;
import ny2.ats.core.util.CollectionUtility;

/**
 * IndicatorType, Symbol, Period ごとのIndicator情報を保持する抽象クラスです。<br>
 * データ保持の階層構造は以下の通りです。<br>
 *    {@literal IndicatorDataHolderImpl > IndicatorDataMap > IndicatorDataSymbolMap > [#]Indicator}
 * @param <T> CalcPeriod Indicatorごとの計算期間enumの実装クラス
 */
public abstract class Indicator<T extends CalcPeriod> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** Logger */
    protected final Logger logger;

    /** データ文字列出力時の区切り文字 */
    protected static final String NAME_DELIMITER = "=";

    /** データ文字列出力時の区切り文字 */
    protected static final String DATA_DELIMITER = "/";

    /** データ文字列出力時に複数値が存在する際の区切り文字 */
    protected static final String VALUE_DELIMITER = ",";


    /** 対象のインディケーター */
    protected final IndicatorType type;

    /** 対象の通貨ペア */
    protected final Symbol symbol;

    /** 対象の期間 */
    protected final Period period;

    /** データ列時刻(後方が新しい時刻) */
    protected final List<LocalDateTime> dateTimeList = new ArrayList<>();

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public Indicator(Class<?> indicatorClass, IndicatorType type, Symbol symbol, Period period) {
        this.logger = LoggerFactory.getLogger(indicatorClass);
        this.type = type;
        this.symbol = symbol;
        this.period = period;
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    /**
     * 最新の更新時刻を返します。
     * @return
     */
    public LocalDateTime getLastDateTime() {
        return CollectionUtility.getLast(dateTimeList);
    }

    /**
     * 最新のデータを返します。
     * @return
     */
    public abstract Double getLastValue(T calcPeriod);

    /**
     * calcPeriodをcastして、最新のデータを返します
     * @param calcPeriod
     * @return last value
     * @throws ClassCastException If calcPeriod is NOT instance of its of Indicator.
     */
    @SuppressWarnings("unchecked")
    public Double getLastValueWithCast(CalcPeriod calcPeriod) {
        return getLastValue((T)calcPeriod);
    };

    /**
     * 指定の計算期間のデータ列を返します。
     * @param calcPeriod 計算期間(各Indicator実装enum)
     * @return
     */
    public abstract List<Double> getValueList(T calcPeriod);

    /**
     * calcPeriodをcastして、データ列を返します
     * @param calcPeriod
     * @return last value
     * @throws ClassCastException If calcPeriod is NOT instance of its of Indicator.
     */
    @SuppressWarnings("unchecked")
    public List<Double> getValueListWithCast(CalcPeriod calcPeriod) {
        return getValueList((T)calcPeriod);
    }

    /**
     * Indicatorごとの最終データを返します。
     * @return
     */
    public abstract Map<CalcPeriod, List<Double>> getLastValueMap();

    /**
     * 該当の計算期間のデータが初期化されているか判断します
     * @param calcPeriod
     * @return
     */
    public abstract boolean isInitialized(T calcPeriod);


    /**
     * calcPeriodをcastして、該当の計算期間のデータが初期化されているか判断します
     * @param calcPeriod
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean isInitializedWithCast(CalcPeriod calcPeriod) {
        return isInitialized((T)calcPeriod);
    }

    /**
     * すべての計算期間のデータが初期化されているか判断します
     * @return
     */
    public abstract boolean isInitializedAll();

    /**
     * 最新データを"/"区切りの文字列で返します。
     * @return
     */
    public abstract String getDataString();

    //    /**
    //     * 文字列データからデータを復元します。
    //     * @param dataString
    //     */
    //    public abstract void extractDataFromString(String dataString);


    /**
     * Indicatorのデータサイズを返します
     * @return
     */
    public int getDataSize() {
        return dateTimeList.size();
    }

    /**
     * データリストの長さをチェックします。
     * @param lists
     * @return
     */
    public boolean checkListSize(List<?>...lists) {
        int size = dateTimeList.size();
        for (List<?> list : lists) {
            if (size != list.size()) {
                logger.warn("{} Indicator data size is differenct with time data size. Time size = {}, Data size = {}", getLogHeadder(), size, list.size());
                return false;
            }
        }
        return true;
    }


    /**
     * 過去データを削減します
     * @param holdDays 残存期間
     */
    public abstract void reduceData(int holdDays);


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(CollectionUtility.getLast(dateTimeList)).append(" ")
            .append(type.name()).append(" ")
            .append(symbol.name()).append(" ")
            .append(period.name()).append(" : ")
            .append(getDataString());
        return sb.toString();
    }

    protected String getLogHeadder() {
        StringBuilder sb = new StringBuilder(50);
        sb.append('[')
            .append(type).append('-')
            .append(symbol).append('-')
            .append(period).append(']');
        return sb.toString();
    }

    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

    public IndicatorType getIndicatorType() {
        return type;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public Period getPeriod() {
        return period;
    }

    public List<LocalDateTime> getDateTimeList() {
        return dateTimeList;
    }
}
