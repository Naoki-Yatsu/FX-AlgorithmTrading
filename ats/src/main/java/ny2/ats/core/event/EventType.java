package ny2.ats.core.event;

import java.util.EnumSet;

public enum EventType {

    // オンライン処理関連イベント

    MARKET_UPDATE,

    MARKET_ORDER,

    ORDER_UPDATE,

    POSITION_UPDATE,

    INDICATOR_UPDATE,

    // Information関連イベント
    /** モデル情報保管用イベント */
    MODEL_INFORMATION,

    /** PL情報保管用イベント */
    PL_INFORMATION,

    /** 最適執行結果イベント */
    EXECUTION_INFORMATION,

    /** Timerの役割を行う特殊なイベント */
    TIMER_INFORMATION,

    /** System管理用のイベント */
    SYSTEM_INFORMATION;

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** 全イベント */
    public static final EnumSet<EventType> EVNET_TYPE_ALL = EnumSet.allOf(EventType.class);

    /** 一般コンポーネント向けの全イベント（Market向きのイベント、DB登録専用Informationは除外） */
    public static final EnumSet<EventType> EVNET_TYPE_CLIENT_ALL = EnumSet.of(
            MARKET_UPDATE, ORDER_UPDATE, POSITION_UPDATE,
            INDICATOR_UPDATE, TIMER_INFORMATION, SYSTEM_INFORMATION);

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

}
