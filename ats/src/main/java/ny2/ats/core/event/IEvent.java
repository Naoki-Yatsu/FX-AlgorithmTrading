package ny2.ats.core.event;

import java.util.UUID;

import ny2.ats.core.data.IData;

public interface IEvent<T extends IData> {

    /**
     * イベントの種類を返す
     *
     * @return EventType
     */
    public EventType getEventType();

    /**
     * Return UUID of creator. UUID will be used to identify creator of the event.
     *
     * @return UUID インスタンスのUUID
     */
    public UUID getCreatorUUID();

    /**
     * Return Class of creator. This will be used to identify creator of the event.
     *
     * @return UUID インスタンスのUUID
     */
    public Class<?> getCreatorClass();

    /**
     * Return content data of this event
     *
     * @return AbstractData Data of this event
     */
    public T getContent();

}
