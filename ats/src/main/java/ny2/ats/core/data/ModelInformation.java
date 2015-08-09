package ny2.ats.core.data;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.ToString;
import ny2.ats.model.ModelType;
import ny2.ats.model.ModelVersion;

/**
 * モデル情報を保持する特殊なクラスです
 */
@Getter
@ToString(callSuper = true)
public class ModelInformation extends AbstractData {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** モデル種別 */
    private final ModelType modelType;

    /** モデル種別 */
    private final ModelVersion modelVersion;

    /** 情報種別 */
    private final ModelInfromationType modelInfromationType;

    /** モデル固有情報。使い方はモデルによります。 */
    private final String information1;
    private final String information2;
    private final String information3;
    private final String information4;
    private final String information5;

    /** 情報基準日時(作成時刻ではなく、作成の基準となる時刻) */
    private final LocalDateTime reportDateTime;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public ModelInformation(ModelType modelType, ModelVersion modelVersion, ModelInfromationType modelInfromationType,
                String information1, String information2, String information3, String information4, String information5, LocalDateTime reportDateTime) {
        super();
        this.modelType = modelType;
        this.modelVersion = modelVersion;
        this.modelInfromationType = modelInfromationType;
        this.information1 = information1;
        this.information2 = information2;
        this.information3 = information3;
        this.information4 = information4;
        this.information5 = information5;
        this.reportDateTime = reportDateTime;
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public String toStringSummary() {
        StringBuilder sb = new StringBuilder(200);
        sb.append("ModelInformation [")
                .append(modelType.name()).append(ITEM_DELIMITER).append(modelVersion.getName()).append(TO_STRING_DELIMITER)
                .append(modelInfromationType.name()).append(TO_STRING_DELIMITER)
                .append(reportDateTime)
                .append("]");
        return sb.toString();
    }

    public enum ModelInfromationType {
        INFORMATION,
        STATUS_UPDATE,
        DEAL_SYGNAL,
        WARNING_ERROR;
    }

}
