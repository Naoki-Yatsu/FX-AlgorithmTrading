package ny2.ats.core.data;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.ToString;

/**
 * システム的な情報を保持するデータです
 */
@Getter
@ToString(callSuper=true)
public class SystemInformation extends AbstractData {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** InfromationT種別 */
    private final SystemInfromationType infromationType;

    /** メッセージ */
    private final String message;

    /** 引渡しパラメーター(使い方は SystemInfromationType による) */
    private final List<String> parameters;

    /** 情報基準日時 */
    private final LocalDateTime reportDateTime;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public SystemInformation(SystemInfromationType infromationType, String message, List<String> parameters, LocalDateTime reportDateTime) {
        super();
        this.infromationType = infromationType;
        this.message = message;
        this.parameters = parameters;
        this.reportDateTime = reportDateTime;
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public String toStringSummary() {
        StringBuilder sb = new StringBuilder(200);
        sb.append("SystemInformation [")
                .append(infromationType.name()).append(TO_STRING_DELIMITER)
                .append(message).append(TO_STRING_DELIMITER)
                .append(reportDateTime)
                .append("]");
        return sb.toString();
    }

    // //////////////////////////////////////
    // Inner Class
    // //////////////////////////////////////

    /**
     * SystemInformationの種別です
     */
    public enum SystemInfromationType {
        // 全般
        START,
        STOP,
        // モデル操作
        MODEL_START,
        MODEL_STOP,
        MODEL_DEPLOY,
        // Market系
        MARKET_LOGIN,
        MARKET_LOGOUT,
        // 週初/週末
        WEEK_START,
        WEEK_END;
    }
}
