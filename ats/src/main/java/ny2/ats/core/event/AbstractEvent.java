package ny2.ats.core.event;

import java.util.UUID;

import ny2.ats.core.data.AbstractData;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public abstract class AbstractEvent<T extends AbstractData> implements IEvent<T> {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** イベント作成インスタンスのUUID */
    private UUID creatorUUID;

    /** イベント作成クラス */
    private Class<?> creatorClass;

    // /** 作成時刻 */
    // private LocalDateTime createDateTime;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public AbstractEvent(UUID creatorUUID, Class<?> creatorClass) {
        this.creatorUUID = creatorUUID;
        this.creatorClass = creatorClass;
        // this.createDateTime = LocalDateTime.now();
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public String toString() {
        // 自分と中身のEventを表示する
        return (new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).toString()) + " "
                + (new ReflectionToStringBuilder(getContent(), ToStringStyle.SHORT_PREFIX_STYLE).toString());
    }

    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

    @Override
    public UUID getCreatorUUID() {
        return creatorUUID;
    }

    @Override
    public Class<?> getCreatorClass() {
        return creatorClass;
    }

    // public LocalDateTime getCreateDateTime() {
    //      return createDateTime;
    // }
    //
    // public void setCreateDateTime(LocalDateTime createDateTime) {
    //      this.createDateTime = createDateTime;
    // }
}
