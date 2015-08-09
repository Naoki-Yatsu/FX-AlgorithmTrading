package ny2.ats.core.data;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Entityデータをあらわす抽象クラス
 */
@Getter @Setter
@ToString
public abstract class AbstractData implements IData {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** toStringSummary用のDelimiter */
    protected static final String TO_STRING_DELIMITER = ", ";
    protected static final String ITEM_DELIMITER = "/";

    /** 作成時刻 */
    protected LocalDateTime createDateTime;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    /**
     * 通常使用するコンストラクタです。
     */
    public AbstractData() {
        // 作成時刻を設定
        this.createDateTime = LocalDateTime.now();
    }

    /**
     * 特別に作成時刻を指定したい場合に使います。
     */
    public AbstractData(LocalDateTime dateTime) {
        this.createDateTime = dateTime;
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    // //////////////////////////////////////
    // Builder
    // //////////////////////////////////////

    public interface DataBuilder<T extends IData> {
        public T createInstance();
    }
}
