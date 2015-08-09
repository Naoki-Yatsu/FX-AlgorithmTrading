package ny2.ats.database.kdb;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ny2.ats.core.data.IndicatorInformation;
import ny2.ats.indicator.CalcPeriod;

import com.exxeleron.qjava.QDateTime;

/**
 * Indicator用のkdb converter<br>
 * ※Indicatorは1つのデータを複数行に分割するため特殊処理を行う。
 */
public class IndicatorDetailKdbConverter implements IKdbConverter<IndicatorInformation> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    private static final String TABLE_NAME = "IndicatorDetail";

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public Object[] convert(List<IndicatorInformation> dataList) {
        // IndicatorInformationの場合はIndicatorによって場合分けを行う。
        int rowCount = calcRowCount(dataList);
        KdbIndicatorDetail kdbData = new KdbIndicatorDetail(rowCount);

        // add data
        int rowIndex = 0;
        for (IndicatorInformation indicatorInformation : dataList) {
            Map<CalcPeriod, List<Double>> valueMap = indicatorInformation.getSimpleIndicatorValueMap();
            for (Entry<CalcPeriod, List<Double>> entry : valueMap.entrySet()) {
                kdbData.addData(rowIndex, indicatorInformation, entry.getKey(), entry.getValue());
                rowIndex++;
            }
        }
        return kdbData.toKdbDataObject();
    }

    @Override
    public Object[] convert(IndicatorInformation data) {
        // IndicatorDetailの場合はIndicatorによって場合分けを行う。
        return convert(Arrays.asList(data));
    }

    /**
     * Indicator用の特殊処理のため、専用のロジックで行数を計算します。
     * @param dataList
     * @return
     */
    private int calcRowCount(List<IndicatorInformation> dataList) {
        // 各Indicatorの、CalcPeriod分行数を追加する。
        int rowCount = 0;
        for (IndicatorInformation indicatorInformation : dataList) {
            if (indicatorInformation.isSimpleIndicator()) {
                rowCount += indicatorInformation.getCountCalcPeriod();
            } else {
                rowCount++;
            }
        }
        return rowCount;
    }

    // //////////////////////////////////////
    // Inner Class
    // //////////////////////////////////////

    /**
     * IndicatorDetail
     *  c             | t f a
     *  --------------| -----
     *  time          | n
     *  sym           | s
     *  indicatorType | s
     *  period        | s
     *  calcPeriod    | s
     *  values        | F
     *  reportDateTime| z
     *  createDateTime| z
     */
    private class KdbIndicatorDetail {
        // columns
        String[] symbolArray;
        String[] indicatorTypeArray;
        String[] periodArray;
        String[] calcPeriodArray;
        Object[] valuesArray;
        QDateTime[] reportDateTimeArray;
        QDateTime[] createDateTimeArray;

        public KdbIndicatorDetail(int rowCount) {

            symbolArray = new String[rowCount];
            indicatorTypeArray = new String[rowCount];
            periodArray = new String[rowCount];
            calcPeriodArray = new String[rowCount];
            valuesArray = new Object[rowCount];
            reportDateTimeArray = new QDateTime[rowCount];
            createDateTimeArray = new QDateTime[rowCount];
        }

        public void addData(int rowIndex, IndicatorInformation data, CalcPeriod calcPeriod, List<Double> valueList) {
            symbolArray[rowIndex] = KdbUtility.kdbValue(data.getSymbol());
            indicatorTypeArray[rowIndex] = KdbUtility.kdbValue(data.getType());
            periodArray[rowIndex] = KdbUtility.kdbValue(data.getPeriod());
            calcPeriodArray[rowIndex] = KdbUtility.kdbValue(calcPeriod.getName());
            valuesArray[rowIndex] = (Double[]) valueList.toArray(new Double[0]);
            reportDateTimeArray[rowIndex] = KdbUtility.kdbValue(data.getDateTime());
            createDateTimeArray[rowIndex] = KdbUtility.kdbValue(data.getCreateDateTime());
        }

        public Object[] toKdbDataObject() {
            return new Object[] {
                    symbolArray,
                    indicatorTypeArray,
                    periodArray,
                    calcPeriodArray,
                    valuesArray,
                    reportDateTimeArray,
                    createDateTimeArray };
        }
    }

}
