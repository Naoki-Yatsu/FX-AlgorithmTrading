package ny2.ats.indicator;

/**
 * インディケーター用の計算期間です。継承したEnumを作成して使用します。
 */
public interface CalcPeriod {

    /**
     * nameを返します。実体クラスでEnum#name()として実装します。
     */
    public String getName();
}
