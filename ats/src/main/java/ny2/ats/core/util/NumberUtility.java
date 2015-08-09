package ny2.ats.core.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * 数値関連のUtilityクラスです
 */
public class NumberUtility {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    /**
     * doubleがしきい値の範囲で等しいかどうか判断します。特に、計算誤差を含めた評価に使用します。
     *
     * @param d1
     * @param d2
     * @param threshold
     * @return
     */
    public static boolean almostEquals(double d1, double d2, double threshold) {
        if (d1 >= d2 - threshold && d1 <= d2 + threshold) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 第1引数がしきい値の範囲を超えて、第2引数より大きいかどうか判断します。
     *
     * @param d1
     * @param d2
     * @param threshold
     * @return
     */
    public static boolean almostGreater(double d1, double d2, double threshold) {
        if (d1 > d2 + threshold) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 第1引数がしきい値を考慮して、第2引数より大きいか等しいか判断します。
     *
     * @param d1
     * @param d2
     * @param threshold
     * @return
     */
    public static boolean almostGreaterEqual(double d1, double d2, double threshold) {
        if (d1 >= d2 - threshold) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 第1引数がしきい値の範囲を超えて第2引数より小さいかどうか判断します。
     *
     * @param d1
     * @param d2
     * @param threshold
     * @return
     */
    public static boolean almostLess(double d1, double d2, double threshold) {
        if (d1 < d2 - threshold) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 第1引数がしきい値を考慮して、第2引数より小さいか等しいか判断します。
     *
     * @param d1
     * @param d2
     * @param threshold
     * @return
     */
    public static boolean almostLessEqual(double d1, double d2, double threshold) {
        if (d1 <= d2 + threshold) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 小数部をRoundingしたBigDecimalを返します。
     *
     * @param d1
     * @param decimalPrecision
     * @return
     */
    public static BigDecimal valueOfBigDecimalRounding(double d1, int decimalPrecision) {
        BigDecimal bd = new BigDecimal(d1);
        MathContext mc = new MathContext(decimalPrecision, RoundingMode.HALF_UP);
        return bd.round(mc);
    }

    /**
     * BigDecimal の値が等しいか判断します
     *
     * @param bd1
     * @param bd2
     * @return
     */
    public static boolean equalsBigDecimal(BigDecimal bd1, BigDecimal bd2) {
        if (bd1.compareTo(bd2) == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Base単位での最後の値を返します。<br>
     * number=8,base=5 ⇒ 5<br>
     * number=14,base=5 ⇒ 10<br>
     *
     * @param number
     * @param base
     * @return
     */
    public static int previousNumberByBase(int number, int base) {
        int remainder = number % base;
        // 余りがあれば割り切れるように変更
        if (remainder > 0) {
            return number - remainder;
        }
        return number;
    }

    /**
     * Base単位での次の値を返します。<br>
     * number=8, base=5 ⇒ 10<br>
     * number=14,base=5 ⇒ 15<br>
     *
     * @param number
     * @param base
     * @return
     */
    public static int nextNumberByBase(int number, int base) {
        int previous = previousNumberByBase(number, base);
        return previous + base;
    }

    /**
     * 第2引数が、第1引数の一定の範囲内にあるかチェックします。
     *
     * @param d1
     * @param d2
     * @param ratioRange 小数のレンジ(例: 5%範囲であれば、0.05)
     * @return
     */
    public static boolean within(double d1, double d2, double ratioRange) {
        return almostEquals(d1, d2, d1 * ratioRange);
    }

    /**
     * 第2引数が、第1引数の一定の範囲内にあるかチェックします。
     *
     * @param int1
     * @param int2
     * @param ratioRange 小数のレンジ(例: 5%範囲であれば、0.05)
     * @return
     */
    public static boolean within(int int1, int int2, double ratioRange) {
        double upper = (double)int1 * (1 + ratioRange);
        double lower = (double)int1 * (1 - ratioRange);
        if (int2 >= lower && int2 <= upper) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 第2引数が、第1引数の一定の範囲内にあるかチェックします。
     *
     * @param int1
     * @param int2
     * @param range 幅(実際の数値)
     * @return
     */
    public static boolean within(int int1, int int2, int range) {
        if (int2 >= int1 - range && int2 <= int1 + range) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 第2引数が、第1引数の一定の範囲内にあるかチェックします。境界は含まない
     *
     * @param int1
     * @param int2
     * @param range 幅(実際の数値)
     * @return
     */
    public static boolean withinNotInclude(int int1, int int2, int range) {
        if (int2 > int1 - range && int2 < int1 + range) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 小数部がn桁になるように丸めます。
     * @param d
     * @param fractionalPortion
     * @return
     */
    public static double roundFixedFraction(double d, int fractionalPortion) {
        if (Double.isNaN(d) || Double. isInfinite(d)) {
            return d;
        }
        BigDecimal bigDecimal = new BigDecimal(d);
        return bigDecimal.setScale(fractionalPortion, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 小数部がn桁になるように丸めます。
     * @param d
     * @param fractionalPortion
     * @return
     */
    public static double roundFixedFraction(Double d, int fractionalPortion) {
        if (d.isNaN() || d.isInfinite()) {
            return d;
        }
        BigDecimal bigDecimal = new BigDecimal(d);
        return bigDecimal.setScale(fractionalPortion, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 有効数字がn桁になるように丸めます。
     * @param d
     * @param precision
     * @return
     */
    public static double roundFixedPrecision(double d, int precision) {
        if (Double.isNaN(d) || Double. isInfinite(d)) {
            return d;
        }
        return new BigDecimal(d, new MathContext(precision, RoundingMode.HALF_UP)).doubleValue();
    }

    /**
     * 有効数字がn桁になるように丸めます。
     * @param d
     * @param precision
     * @return
     */
    public static double roundFixedPrecision(Double d, int precision) {
        if (d.isNaN() || d.isInfinite()) {
            return d;
        }
        return new BigDecimal(d, new MathContext(precision, RoundingMode.HALF_UP)).doubleValue();
    }

    /**
     * 指定の単位になるように丸めます。<br>
     *    {@literal (11, 5) -> 10}<br>
     *    {@literal (-11, 5)-> -10}<br>
     * ※ゼロに近づくのでマイナスの場合は注意
     *
     * @param i
     * @param unit
     * @return
     */
    public static int roundWithUnit(int i, int unit) {
        // unit単位に変換。割ってあまりを引くことで算出
        return i - i % unit;
    }

    /**
     * 指定の単位になるように丸めます(上方向丸め)
     * @param i
     * @param unit
     * @return
     */
    public static int roundWithUnitUP(int i, int unit) {
        // 単位-余り を加える
        int add = (i % unit == 0) ? 0 : unit - i % unit;
        return i + add;
    }


    // //////////////////////////////////////
    // Method - String
    // //////////////////////////////////////

    /**
     * doubleの文字列表現を返します。
     * {@literal 1.0E-5 -> 0.00001} のように表示したいときに使用します
     * @param d
     * @return
     */
    public static String toStringDouble(double d) {
        return BigDecimal.valueOf(d).toPlainString();
    }

    /**
     * 文字と数字の混ざった文字列から、数字のみを取り出してLongを作成します。
     * 主に、外部で採番されたIDから数字のIDを作成するために使用します。
     *
     * @param stringWithNumber
     * @return
     */
    public static Long extractNumberString(Long headerNumber, String stringWithNumber) {
        StringBuilder sb = new StringBuilder(30);
        if (headerNumber != null) {
            sb.append(headerNumber.longValue());
        }

        for (int i = 0; i < stringWithNumber.length(); i++) {
            if (CharUtils.isAsciiNumeric(stringWithNumber.charAt(i))) {
                sb.append(stringWithNumber.charAt(i));
            }
        }

        if (sb.length() == 0) {
            return NumberUtils.LONG_ZERO;
        } else if(sb.length() >= 19) {
            // 19桁以上の場合は先頭の18文字を使用する
            return Long.valueOf(sb.substring(0, 18));
        } else {
            return Long.valueOf(sb.toString());
        }
    }

}
