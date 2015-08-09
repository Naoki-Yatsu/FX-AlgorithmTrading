package ny2.ats.market.order;

public enum OptimizerMethod {

    /** ZMT Optimizer */
    MA_TICK,

    /** Married in OrderMarry */
    MARRY,

    /** Not Married in OrderMarry, rely on Market Wave */
    WAVE;
}
