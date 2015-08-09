package ny2.ats.core.data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.ToString;
import ny2.ats.core.common.Period;
import ny2.ats.core.common.Symbol;
import ny2.ats.indicator.CalcPeriod;
import ny2.ats.indicator.Indicator;
import ny2.ats.indicator.IndicatorType;
import ny2.ats.indicator.indicators.SimpleIndicator;

/**
 * Indicatorの最新断面を保持するクラスです
 */
@Getter
@ToString(callSuper=true)
public class IndicatorInformation extends AbstractData {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** 対象のインディケーター */
    private final IndicatorType type;

    /** 対象の通貨ペア */
    private final Symbol symbol;

    /** 対象の期間 */
    private final Period period;

    /** データの基準時刻 */
    private final LocalDateTime dateTime;

    /** 文字列表示のデータ */
    private final String dataString;

    /** CalcPeriodごとの1期間データ */
    private final Map<CalcPeriod, List<Double>> dataValueMap;

    /** 該当Indicatorへの参照 */
    private final Indicator<?> indicator;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public IndicatorInformation(Indicator<?> indicator) {
        super();
        this.indicator = indicator;
        this.type = indicator.getIndicatorType();
        this.symbol = indicator.getSymbol();
        this.period = indicator.getPeriod();
        this.dateTime = indicator.getLastDateTime();
        this.dataString = indicator.getDataString();
        this.dataValueMap = indicator.getLastValueMap();
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public String toStringSummary() {
        StringBuilder sb = new StringBuilder(200);
        sb.append("IndicatorInformation [")
                .append(type.name()).append(TO_STRING_DELIMITER)
                .append(symbol.name()).append(TO_STRING_DELIMITER)
                .append(period.name()).append(TO_STRING_DELIMITER)
                .append(dateTime)
                .append("]");
        return sb.toString();
    }

    /**
     * 対象のIndicatorがSimpleIndicatorかチェックします。
     * @return
     */
    public boolean isSimpleIndicator() {
        return indicator instanceof SimpleIndicator<?>;
    }

    /**
     * SimpleIndicatorの場合、値のMapを返します。
     * @return
     */
    public Map<CalcPeriod, List<Double>> getSimpleIndicatorValueMap() {
        return dataValueMap;
    }

    /**
     * SimpleIndicatorの場合、CalcPeriodの種類の返します。
     * @return
     */
    public int getCountCalcPeriod() {
        if (isSimpleIndicator()) {
            return ((SimpleIndicator<?>)indicator).getCountCalcPeriod();
        }
        return 0;
    }

}
