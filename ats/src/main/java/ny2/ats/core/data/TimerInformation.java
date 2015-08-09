package ny2.ats.core.data;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.ToString;
import ny2.ats.core.common.Period;

/**
 * Timer情報を保持する特殊なクラスです
 */
@Getter
@ToString(callSuper=true)
public class TimerInformation extends AbstractData {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** Timerの期間を表します */
    private final Period period;

    /** 通知基準日時 */
    private final LocalDateTime currentDateTime;

    /** 次の通知日時 */
    private final LocalDateTime nextDateTime;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public TimerInformation(Period period, LocalDateTime currentDateTime, LocalDateTime nextDateTime) {
        super();
        this.period = period;
        this.currentDateTime = currentDateTime;
        this.nextDateTime = nextDateTime;
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public String toStringSummary() {
        StringBuilder sb = new StringBuilder(200);
        sb.append("TimerInformation [")
                .append(period.name()).append(TO_STRING_DELIMITER)
                .append(currentDateTime).append(TO_STRING_DELIMITER)
                .append(nextDateTime)
                .append("]");
        return sb.toString();
    }
}
