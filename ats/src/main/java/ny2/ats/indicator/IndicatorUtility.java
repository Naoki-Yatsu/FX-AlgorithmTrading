package ny2.ats.indicator;

/**
 * Indicator用のUtilityクラスです
 */
@Deprecated
public class IndicatorUtility {

//    public static Double getLastValue(Indicator<?> indicator, IndicatorType indicatorType, CalcPeriod calcPeriod) {
//
//        switch (indicatorType) {
//            case OHLC:
//                return ((OHLCIndicator) indicator).getLastValue((OHLCType)calcPeriod);
//
//            case MA:
//            case EMA:
//                return ((MovingAverageIndicator) indicator).getLastValue((MAPeriod)calcPeriod);
//
//            case RSI:
//                return ((RSIIndicator) indicator).getLastValue((RSIPeriod)calcPeriod);
//
//            case RCI:
//                return ((RankCorrelationIndexIndicator) indicator).getLastValue((RankCIPeriod)calcPeriod);
//
//            case MACD:
//                return ((MACDIndicator) indicator).getLastValue((MACDPeriod)calcPeriod);
//
//            case BOLLINGER:
//            case BOLLINGER_EMA:
//                return ((BollingerBandIndicator) indicator).getLastValue((BollingerPeriod)calcPeriod);
//
//            case STOCHASTICS:
//                return ((StochasticsIndicator) indicator).getLastValue((StochasticsPeriod)calcPeriod);
//
//            case ICHIMOKU:
//                return ((IchimokuIndicator) indicator).getLastValue((IchimokuPeriod)calcPeriod);
//
//            case LINEAR_REG:
//                return ((LinearRegressionIndicator) indicator).getLastValue((LRPeriod)calcPeriod);
//
//            default:
//                throw new ATSRuntimeException(indicatorType.name() + " - switch conditions is NOT defined.");
//        }
//    }

}
