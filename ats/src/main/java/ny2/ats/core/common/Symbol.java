package ny2.ats.core.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumSet;
import java.util.Set;

import ny2.ats.core.util.NumberUtility;

public enum Symbol {

    // Major 3
    USDJPY(Ccy.USD, Ccy.JPY, 0.01, 2),
    EURJPY(Ccy.EUR, Ccy.JPY, 0.01, 2),
    EURUSD(Ccy.EUR, Ccy.USD, 0.0001, 4),

    // ABC...
    AUDCAD(Ccy.AUD, Ccy.CAD, 0.0001, 4),
    AUDCHF(Ccy.AUD, Ccy.CHF, 0.0001, 4),
    AUDJPY(Ccy.AUD, Ccy.JPY, 0.01, 2),
    AUDNZD(Ccy.AUD, Ccy.NZD, 0.0001, 4),
    AUDSGD(Ccy.AUD, Ccy.SGD, 0.0001, 4),
    AUDUSD(Ccy.AUD, Ccy.USD, 0.0001, 4),

    CADCHF(Ccy.CAD, Ccy.CHF, 0.0001, 4),
    CADHKD(Ccy.CAD, Ccy.HKD, 0.0001, 4),
    CADJPY(Ccy.CAD, Ccy.JPY, 0.01, 2),

    CHFJPY(Ccy.CHF, Ccy.JPY, 0.01, 2),
    CHFPLN(Ccy.CHF, Ccy.PLN, 0.0001, 4),
    CHFSGD(Ccy.CHF, Ccy.SGD, 0.0001, 4),

    EURAUD(Ccy.EUR, Ccy.AUD, 0.0001, 4),
    EURBRL(Ccy.EUR, Ccy.BRL, 0.0001, 4),
    EURCAD(Ccy.EUR, Ccy.CAD, 0.0001, 4),
    EURCHF(Ccy.EUR, Ccy.CHF, 0.0001, 4),
    EURDKK(Ccy.EUR, Ccy.DKK, 0.0001, 4),
    EURGBP(Ccy.EUR, Ccy.GBP, 0.0001, 4),
    EURHKD(Ccy.EUR, Ccy.HKD, 0.0001, 4),
    EURHUF(Ccy.EUR, Ccy.HUF, 0.01, 2),
    EURMXN(Ccy.EUR, Ccy.MXN, 0.0001, 4),
    EURNOK(Ccy.EUR, Ccy.NOK, 0.0001, 4),
    EURNZD(Ccy.EUR, Ccy.NZD, 0.0001, 4),
    EURPLN(Ccy.EUR, Ccy.PLN, 0.0001, 4),
    EURRUB(Ccy.EUR, Ccy.RUB, 0.0001, 4),
    EURSEK(Ccy.EUR, Ccy.SEK, 0.0001, 4),
    EURSGD(Ccy.EUR, Ccy.SGD, 0.0001, 4),
    EURTRY(Ccy.EUR, Ccy.TRY, 0.0001, 4),
    EURZAR(Ccy.EUR, Ccy.ZAR, 0.0001, 4),

    GBPAUD(Ccy.GBP, Ccy.AUD, 0.0001, 4),
    GBPCAD(Ccy.GBP, Ccy.CAD, 0.0001, 4),
    GBPCHF(Ccy.GBP, Ccy.CHF, 0.0001, 4),
    GBPJPY(Ccy.GBP, Ccy.JPY, 0.01, 2),
    GBPNZD(Ccy.GBP, Ccy.NZD, 0.0001, 4),
    GBPUSD(Ccy.GBP, Ccy.USD, 0.0001, 4),

    HKDJPY(Ccy.HKD, Ccy.JPY, 0.0001, 4),
    HUFJPY(Ccy.HUF, Ccy.JPY, 0.0001, 4),
    MXNJPY(Ccy.MXN, Ccy.JPY, 0.0001, 4),
    NOKJPY(Ccy.NOK, Ccy.JPY, 0.0001, 4),
    NZDCAD(Ccy.NZD, Ccy.CAD, 0.0001, 4),
    NZDCHF(Ccy.NZD, Ccy.CHF, 0.0001, 4),
    NZDJPY(Ccy.NZD, Ccy.JPY, 0.01, 2),
    NZDSGD(Ccy.NZD, Ccy.SGD, 0.0001, 4),
    NZDUSD(Ccy.NZD, Ccy.USD, 0.0001, 4),
    SGDJPY(Ccy.SGD, Ccy.JPY, 0.01, 2),
    SEKJPY(Ccy.SEK, Ccy.JPY, 0.0001, 4),
    TRYJPY(Ccy.TRY, Ccy.JPY, 0.0001, 4),

    USDBRL(Ccy.USD, Ccy.BRL, 0.0001, 4),
    USDCAD(Ccy.USD, Ccy.CAD, 0.0001, 4),
    USDCHF(Ccy.USD, Ccy.CHF, 0.0001, 4),
    USDCNH(Ccy.USD, Ccy.CNH, 0.0001, 4),
    USDCZK(Ccy.USD, Ccy.CZK, 0.01, 2),
    USDDKK(Ccy.USD, Ccy.DKK, 0.0001, 4),
    USDHKD(Ccy.USD, Ccy.HKD, 0.0001, 4),
    USDHUF(Ccy.USD, Ccy.HUF, 0.01, 2),
    USDMXN(Ccy.USD, Ccy.MXN, 0.0001, 4),
    USDNOK(Ccy.USD, Ccy.NOK, 0.0001, 4),
    USDPLN(Ccy.USD, Ccy.PLN, 0.0001, 4),
    USDRON(Ccy.USD, Ccy.RON, 0.0001, 4),
    USDRUB(Ccy.USD, Ccy.RUB, 0.0001, 4),
    USDSEK(Ccy.USD, Ccy.SEK, 0.0001, 4),
    USDSGD(Ccy.USD, Ccy.SGD, 0.0001, 4),
    USDTRY(Ccy.USD, Ccy.TRY, 0.0001, 4),
    USDZAR(Ccy.USD, Ccy.ZAR, 0.0001, 4),

    XAGUSD(Ccy.XAG, Ccy.USD, 0.01, 2),
    XAUUSD(Ccy.XAU, Ccy.USD, 0.01, 2),
    ZARJPY(Ccy.ZAR, Ccy.JPY, 0.0001, 4),

    // CFD
    US30("US30", 1, 0),
    JPN225("JPN225", 1, 0),
    USOil("USOIL", 0.01, 2);


    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    /** Major通貨ペア */
    public static final Set<Symbol> G3 = EnumSet.of(Symbol.USDJPY, Symbol.EURUSD, Symbol.EURJPY);

    /** G7通貨ペア */
    public static final Set<Symbol> G7 =
            EnumSet.of(Symbol.USDJPY, Symbol.EURJPY, Symbol.EURUSD,
                    Symbol.GBPUSD, Symbol.AUDUSD, Symbol.USDCAD, Symbol.USDCHF,
                    Symbol.EURGBP, Symbol.EURAUD, Symbol.EURCAD, Symbol.EURCHF,
                    Symbol.GBPJPY, Symbol.AUDJPY, Symbol.CADJPY, Symbol.CHFJPY,
                    Symbol.GBPCHF);

    private static final String CCYPAIR_SEPARATOR = "/";

    private Ccy ccy1;
    private Ccy ccy2;
    private double pipValue;
    private int pipScale;

    /** ProductType - FOREX or CFD */
    private ProductType productType;

    /** Name for CFD*/
    private String strName;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    /**
     * 通貨ペアのコンストラクタ
     */
    private Symbol(Ccy ccy1, Ccy ccy2, double pipValue, int pipScale) {
        this.ccy1 = ccy1;
        this.ccy2 = ccy2;
        this.pipValue = pipValue;
        this.pipScale = pipScale;
        this.productType = ProductType.FOREX;

        this.strName = ccy1.name() + ccy2.name();
    }

    /**
     * CFD用のコンストラクタ
     */
    private Symbol(String strName, double pipValue, int pipScale) {
        // use USD for NOT FOREX
        this.ccy1 = Ccy.USD;
        this.ccy2 = Ccy.USD;
        this.pipValue = pipValue;
        this.pipScale = pipScale;
        this.productType = ProductType.CFD_INDEX;

        this.strName = strName;
    }

    /**
     * ccy1, ccy2 からSymbolを作成します。
     * @param ccy1
     * @param ccy2
     * @return
     */
    public static Symbol convertFrom(Ccy ccy1, Ccy ccy2) {
        return Symbol.valueOf(ccy1.name() + ccy2.name());
    }

    /**
     * FIXのSymbolから内部用のSymbolを作成します。
     * @param fixSymbol
     * @return
     */
    public static Symbol valueOfFixSymbol(String fixSymbol) {
        return valueOf(removeCcypairSeparator(fixSymbol));
    }

    /**
     * Stringの配列からSetを作成します
     * @param symbols
     * @return
     */
    public static Set<Symbol> valueOfStringArray(String[] symbols) {
        Set<Symbol> symbolSet = EnumSet.noneOf(Symbol.class);
        for (String str : symbols) {
            if (str.equals("G3")) {
                symbolSet.addAll(G3);
            } else if (str.equals("G7")) {
                symbolSet.addAll(G7);
            } else if (!str.isEmpty()) {
                symbolSet.add(Symbol.valueOf(str));
            }
        }
        return symbolSet;
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public String toString() {
        if (strName == null) {
            name();
        }
        return strName;
    }

    /**
     * Returns currency separator
     * @return currency separator
     */
    public static String getCcypairsSeparator() {
        return CCYPAIR_SEPARATOR;
    }

    public static String removeCcypairSeparator(String str) {
        return str.replaceAll(CCYPAIR_SEPARATOR, "");
    }

    /**
     * 区切り文字付の名前を返します。
     * @return
     */
    public String getNameWithSeparator() {
        if (productType == ProductType.FOREX) {
            return ccy1.name() + CCYPAIR_SEPARATOR + ccy2.name();
        } else {
            return name();
        }
    }

    /**
     * Ccy2/JPY のSymbolを返します。
     * @return
     */
    public Symbol getCcy2JpySymbol() {
        if (isContainJpy()) {
            return this;
        } else {
            return Symbol.convertFrom(getCcy2(), Ccy.JPY);
        }
    }

    /**
     * 対円通貨ペアかどうか判断します
     * @return
     */
    public boolean isContainJpy() {
        return ccy2 == Ccy.JPY;
    }

    /**
     * 対USD通貨ペアかどうか判断します
     * @return
     */
    public boolean isContainUsd() {
        return ccy1 == Ccy.USD || ccy2 == Ccy.USD;
    }

    /**
     * CCY2がUSDかどうか判断します
     * @return
     */
    public boolean isCcy2Usd() {
        return ccy2 == Ccy.USD;
    }

    // //////////////////////////////////////
    // Method (pips)
    // //////////////////////////////////////

    /**
     * Pipsで丸めます
     * @param price
     * @return
     */
    public double roundPips(double price) {
        BigDecimal bigDecimal = new BigDecimal(price);
        return bigDecimal.setScale(pipScale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * SubPipsで丸めます
     * @param price
     * @return
     */
    public double roundSubPips(double price) {
        BigDecimal bigDecimal = new BigDecimal(price);
        return bigDecimal.setScale(pipScale + 1, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * SubSubPipsで丸めます
     * @param price
     * @return
     */
    public double roundSubSubPips(double price) {
        BigDecimal bigDecimal = new BigDecimal(price);
        return bigDecimal.setScale(pipScale + 2, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * SubPips単位でSubpipの値が指定単位になるようにで丸めます
     * baseSubPip: 2 _ 100.002,100.004,...
     * baseSubPip: 5 _ 100.005,100.010,...
     *
     * @param price
     * @return
     */
    public double roundSubPipsBase(double price, int baseSubPip, RoundingMode roundingMode) {
        int intSubpipPrice = roundSubPipsIntValue(price);
        int roundPrice = 0;
        switch (roundingMode) {
            case UP:
            case CEILING:
            case HALF_UP:
                roundPrice = NumberUtility.roundWithUnitUP(intSubpipPrice, baseSubPip);
                break;
            case DOWN:
            case FLOOR:
            case HALF_DOWN:
                roundPrice = NumberUtility.roundWithUnit(intSubpipPrice, baseSubPip);
                break;
            default:
                throw new IllegalArgumentException(roundingMode.name() + " is NOT supported. Argument must be UP or DOWN.");
        }
        return convertSubPipsToReal(roundPrice);
    }

    /**
     * Pipsで丸めたint値を返します。
     * @param price
     * @return
     */
    public int roundPipsIntValue(double price) {
        // 整数に変換
        double intPrice = price * Math.pow(10, pipScale);
        int intRoundPrice = (int)Math.round(intPrice);
        return intRoundPrice;
    }

    /**
     * Pipsで丸めたint値を返します。BidAskを考慮して外側に丸めます。(midは考慮しません)
     * @param price
     * @return
     */
    public int roundPipsIntValueOutside(double price, BidAsk bidAsk) {
        // 端数を考慮して調整したあとで、整数変換
        int intPrice = (int) ( (price + getSubSubPipValue() * 0.1) * Math.pow(10, pipScale));
        if (bidAsk == BidAsk.BID) {
            return intPrice;
        } else {
            return intPrice + 1;
        }
    }

    /**
     * SubPipsで丸めたint値を返します。
     * @param price
     * @return
     */
    public int roundSubPipsIntValue(double price) {
        // 整数に変換
        double intPrice = price * Math.pow(10, pipScale+1);
        int intRoundPrice = (int)Math.round(intPrice);
        return intRoundPrice;
    }

    /**
     * SubPipsで丸めたint値を返します。さらにUnit単位になるようにまるめます。
     * @param price
     * @param unit まとめ単位{@literal (>= 2)}
     * @return
     */
    public int roundSubPipsIntValueWithUnit(double price, int unit) {
        // 整数に変換
        double intPrice = price * Math.pow(10, pipScale+1);
        int intRoundPrice = (int)Math.round(intPrice);
        // unit単位に変換。割ってあまりを引くことで算出
        return intRoundPrice - intRoundPrice % unit;
    }

    /**
     * pipsを実数値に変換します。
     * @param pips
     * @return
     */
    public double convertPipsToReal(double pips) {
        return pips / Math.pow(10, pipScale);
    }

    /**
     * int型のpipsを実数値に変換します。
     * @param pips
     * @return
     */
    public double convertPipsToReal(int pips) {
        return pips / Math.pow(10, pipScale);
    }

    /**
     * 実数値をpipsに変換します。
     * @param realRate
     * @return
     */
    public double convertRealToPips(double realRate) {
        return realRate * Math.pow(10, pipScale);
    }

    /**
     * 実数値をsub pipsに変換します。
     * @param realRate
     * @return
     */
    public double convertRealToSubPips(double realRate) {
        return realRate * Math.pow(10, pipScale + 1);
    }

    /**
     * sub pipsを実数値に変換します。
     * @param subpips
     * @return
     */
    public double convertSubPipsToReal(double subpips) {
        return subpips / Math.pow(10, pipScale + 1);
    }

    /**
     * int型のsub pipsを実数値に変換します。
     * @param subpips
     * @return
     */
    public double convertSubPipsToReal(int subpips) {
        return subpips / Math.pow(10, pipScale + 1);
    }

    /**
     * Sub Pip の値をかえします
     * @return
     */
    public double getSubPipValue() {
        BigDecimal bigDecimal = new BigDecimal(pipValue);
        return bigDecimal.movePointLeft(1).doubleValue();
    }

    /**
     * Sub Sub Pip の値をかえします
     * @return
     */
    public double getSubSubPipValue() {
        BigDecimal bigDecimal = new BigDecimal(pipValue);
        return bigDecimal.movePointLeft(2).doubleValue();
    }

    // //////////////////////////////////////
    // Getters and Setters
    // //////////////////////////////////////

    public String getStrName() {
        return strName;
    }
    public Ccy getCcy1() {
        return ccy1;
    }
    public Ccy getCcy2() {
        return ccy2;
    }
    public double getPipValue() {
        return pipValue;
    }
    public int getPipScale() {
        return pipScale;
    }
    public ProductType getProductType() {
        return productType;
    }

    // //////////////////////////////////////
    // Inner Class
    // //////////////////////////////////////

    public enum ProductType {

        /** Forex */
        FOREX("FOREX"),

        /** CFD指数 */
        CFD_INDEX("CFD Index");

        private String value;

        private ProductType(String value) {
            this.value = value;
        }

        public String toString() {
            return value;
        }
    }
}
