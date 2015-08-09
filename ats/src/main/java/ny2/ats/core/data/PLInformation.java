package ny2.ats.core.data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.TreeMap;

import lombok.Getter;
import lombok.ToString;
import ny2.ats.core.common.Ccy;
import ny2.ats.core.common.Symbol;
import ny2.ats.model.ModelType;
import ny2.ats.model.ModelVersion;

/**
 * PL情報を保持する特殊なクラスです
 */
@Getter
@ToString(callSuper=true)
public class PLInformation extends AbstractData {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** 種別 */
    private final PLInformationType plInformationType;

    /** モデル種別 */
    private final ModelType modelType;

    /** モデルバージョン種別 */
    private final ModelVersion modelVersion;

    /** JPY換算のPL */
    private final int plJpy;

    /** 通貨ペアごとのPL。CCY2金額 ( USDJPY=1234/EURJPY=-789/... ) */
    private final String plDetail;

    /** 通貨ペアごとのNetAmount。CCY1金額 ( USDJPY=1000/EURJPY=5000/... ) */
    private final String netAmountDetail;

    /** 通貨ごとのNetAmount ( USD=1000/JPY=105000/... ) */
    private final String netAmountCcy;

    /** 情報基準日時 */
    private final LocalDateTime reportDateTime;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public PLInformation(PLInformationType plInformationType, ModelType modelType, ModelVersion modelVersion,
            int plJpy, Map<Symbol, Integer> plMap, Map<Symbol, Integer> amountMap, LocalDateTime reportDateTime, Map<Symbol, MarketData> marketDataMap) {
        super();
        this.plInformationType = plInformationType;
        this.modelType = modelType;
        this.modelVersion = modelVersion;
        this.plJpy = plJpy;
        this.plDetail = calculatePlDetail(plMap);
        this.netAmountDetail = calculateNetAmountDetail(amountMap);
        this.netAmountCcy = calculateNetAmountCcy(amountMap, marketDataMap);
        this.reportDateTime = reportDateTime;
    }

    @Override
    public PLInformation clone() {
        try {
            return (PLInformation)super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            throw new InternalError(e.toString());
        }
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public String toStringSummary() {
        StringBuilder sb = new StringBuilder(200);
        sb.append("PLInformation [")
                .append(plInformationType).append(TO_STRING_DELIMITER)
                .append(modelType.name()).append(ITEM_DELIMITER).append(modelVersion.getName()).append(TO_STRING_DELIMITER)
                .append(plJpy).append(TO_STRING_DELIMITER)
                .append(reportDateTime)
                .append("]");
        return sb.toString();
    }

    private String calculatePlDetail(Map<Symbol, Integer> plMap) {
        StringJoiner sj = new StringJoiner("/");
        for (Entry<Symbol, Integer> entry : plMap.entrySet()) {
            sj.add(entry.getKey().name() + "=" + entry.getValue().toString());
        }
        return sj.toString();
    }

    private String calculateNetAmountDetail(Map<Symbol, Integer> amountMap) {
        StringJoiner sj = new StringJoiner("/");
        for (Entry<Symbol, Integer> entry : amountMap.entrySet()) {
            sj.add(entry.getKey().name() + "=" + entry.getValue().toString());
        }
        return sj.toString();
    }

    private String calculateNetAmountCcy(Map<Symbol, Integer> amountMap, Map<Symbol, MarketData> marketDataMap) {
        // create ccy amount
        Map<Ccy, Integer> ccyMap = new TreeMap<>();
        for (Entry<Symbol, Integer> entry : amountMap.entrySet()) {
            Symbol symbol = entry.getKey();
            Integer amount = entry.getValue();
            MarketData marketData = marketDataMap.get(symbol);
            if (marketData == null) {
                continue;
            }

            Ccy ccy1 = symbol.getCcy1();
            Ccy ccy2 = symbol.getCcy2();
            Integer amount1 = ccyMap.containsKey(ccy1) ? ccyMap.get(ccy1) : 0;
            Integer amount2 = ccyMap.containsKey(ccy2) ? ccyMap.get(ccy2) : 0;

            // add amount (amountが符号付きなのでここでは考慮しない)
            amount1 += amount;
            amount2 -= (int) (amount * marketData.getBidPrice());

            ccyMap.put(ccy1, amount1);
            ccyMap.put(ccy2, amount2);
        }


        StringJoiner sj = new StringJoiner("/");
        for (Entry<Ccy, Integer> entry : ccyMap.entrySet()) {
            sj.add(entry.getKey().name() + "=" + entry.getValue().toString());
        }
        return sj.toString();
    }

    // //////////////////////////////////////
    // Inner Class
    // //////////////////////////////////////

    public enum PLInformationType {
        ALL,
        MODEL
    }
}
