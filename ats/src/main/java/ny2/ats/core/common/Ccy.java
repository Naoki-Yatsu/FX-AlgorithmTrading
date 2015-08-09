package ny2.ats.core.common;

import java.util.Currency;

/**
 * enum of currency
 */
public enum Ccy {

    // Metal
    XAU(Currency.getInstance("XAU")),
    XAG(Currency.getInstance("XAG")),
    // England
    EUR(Currency.getInstance("EUR")),
    GBP(Currency.getInstance("GBP")),
    AUD(Currency.getInstance("AUD")),
    NZD(Currency.getInstance("NZD")),
    // USD
    USD(Currency.getInstance("USD")),
    // Others
    BRL(Currency.getInstance("BRL")),
    CAD(Currency.getInstance("CAD")),
    CNH(Currency.getInstance("CNY")),   // use CNY for CNH
    CNY(Currency.getInstance("CNY")),
    CZK(Currency.getInstance("CZK")),
    DKK(Currency.getInstance("DKK")),
    HKD(Currency.getInstance("HKD")),
    HUF(Currency.getInstance("HUF")),
    MXN(Currency.getInstance("MXN")),
    NOK(Currency.getInstance("NOK")),
    PLN(Currency.getInstance("PLN")),
    RON(Currency.getInstance("RON")),
    RUB(Currency.getInstance("RUB")),
    SEK(Currency.getInstance("SEK")),
    SGD(Currency.getInstance("SGD")),
    TRY(Currency.getInstance("TRY")),
    ZAR(Currency.getInstance("ZAR")),
    // CHF,JPY
    CHF(Currency.getInstance("CHF")),
    JPY(Currency.getInstance("JPY"));

    private Currency currency;

    private Ccy(Currency currency) {
        this.currency = currency;
    }

    public Currency getCurrency() {
        return currency;
    }

}
