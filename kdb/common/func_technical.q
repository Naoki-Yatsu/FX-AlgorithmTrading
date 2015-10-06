//
// Functions for Trend Analysis
//

/ Namespace : Technical Analysis
/ \d .tech

// ---------------------------------------------------------
//    Constant
// ---------------------------------------------------------

.tech.TREND_TYPES: `UP`UP_R`RANGE`DOWN_R`DOWN;

//
// トレンド重み付け情報
//
.tech.TREND_INFO: ([trend:`$()] UP:`int$(); UP_R:`int$(); RANGE:`int$(); DOWN_R:`int$(); DOWN:`int$());
.tech.TREND_INFO[`UP]:    2   1   0   0   0;
.tech.TREND_INFO[`UP_R]:  1   2   1   0   0;
.tech.TREND_INFO[`RANGE]: 0   1   2   1   0;
.tech.TREND_INFO[`DOWN_R]:0   0   1   2   1;
.tech.TREND_INFO[`DOWN]:  0   0   0   1   2;


// float version
/.tech.TREND_INFO: ([trend:`$()] UP:`float$(); UP_R:`float$(); RANGE:`float$(); DOWN_R:`float$(); DOWN:`float$());

// Total=1
/.tech.TREND_INFO[`UP]:    0.67 0.33 0    0    0;
/.tech.TREND_INFO[`UP_R]:  0.25 0.5  0.25 0    0;
/.tech.TREND_INFO[`RANGE]: 0    0.25 0.5  0.25 0;
/.tech.TREND_INFO[`DOWN_R]:0    0    0.25 0.5  0.25;
/.tech.TREND_INFO[`DOWN]:  0    0    0    0.33 0.67;

/.tech.TREND_INFO[`UP]:    1   .5  0   0   0;
/.tech.TREND_INFO[`UP_R]:  .5  1   .5  0   0;
/.tech.TREND_INFO[`RANGE]: 0   .5  1   .5  0;
/.tech.TREND_INFO[`DOWN_R]:0   0   .5  1   .5;
/.tech.TREND_INFO[`DOWN]:  0   0   0   .5  1;

/.tech.TREND_INFO[`UP]:    1   .3  0   0   0;
/.tech.TREND_INFO[`UP_R]:  .3  1   .3  0   0;
/.tech.TREND_INFO[`RANGE]: 0   .3  1   .3  0;
/.tech.TREND_INFO[`DOWN_R]:0   0   .3  1   .3;
/.tech.TREND_INFO[`DOWN]:  0   0   0   .3  1;



// ---------------------------------------------------------
//    Technical Indicator
// ---------------------------------------------------------

//
// OHLC + GMMA
//
.tech.ohlcGMMA: {[dates; psym; byMin]
    t: .trd.FXOHLCMin[dates; psym; byMin];
    t: t,'.tech.GMMA[(exec close from t)];
    : t;
  };
/ .tech.ohlcGMMA[dates; psym; 1]


//
// GMMA Indicator
//
// @param pricelist (float list) : price list
//
.tech.GMMA: {[pricelist]
    periods: 3 5 8 10 12 15 30 35 40 45 50 60;
    t: flip raze
    {[pricelist; period]
        ma: ewma[period; pricelist];
        : (enlist `$"E",string period)!enlist ma;
    }[pricelist;] each periods;
    : t;
  };


//
// ADX + OHLC
//
.tech.ohlcADX: {[dates; psym; byMin]
    // Data
    t: .trd.FXOHLCMin[dates; psym; byMin];
    // DM
    t: update plusDM: high - prev high, minusDM: (prev low) - low from t;
    t: update plusDM: 0.0 from t where plusDM < 0;
    t: update minusDM: 0.0 from t where minusDM < 0;
    t: update plusDM: 0.0 from t where minusDM > plusDM;
    t: update minusDM: 0.0 from t where plusDM > minusDM;
    // TR
    t: update TR: max each ((high - low),'(high - prev close),'((prev close) - low)) from t;
    // DI
    t: update plusDI:  100 * msum[period; plusDM] % msum[period; TR] from t;
    t: update minusDI: 100 * msum[period;minusDM] % msum[period; TR] from t;
    // DX
    t: update DX: 0^ abs 100 * (plusDI - minusDI)%(plusDI + minusDI) from t;
    t: update ADX: ewma[period; DX] from t;
    // Return
    : select timestamp, open, high, low, close, .cmn.round[1;plusDI], .cmn.round[1;minusDI], .cmn.round[1;ADX] from t
  };


//
// OHLC + RCI indicator
//
// @param dates
// @param psym
// @param byMin (int)        : date minutes ex. 5
// @param periods (int list) : rci periods ex. 9 26 52
//
.tech.ohlcRCI: {[dates; psym; byMin; periods]
    periods: (),periods;
    t: .trd.FXOHLCMin[dates; psym; byMin];
    t: t,'.tech.RCI[(exec close from t); periods];
    : t;
  };
/ .tech.ohlcRCI[2014.10.01 2014.10.31; `USDJPY; 5; 9 26 52]


//
// RCI Indicator
//
// @param pricelist (float list) : price list
// @param periods (int list) : rci periods ex. 9 26 52
//
.tech.RCI: {[pricelist; periods]
    periods: (),periods;
    t: flip raze
    {[pricelist; period]
        baserank: 1+til period;
        rci: joinPrev0[period; pricelist];
        rci: `float$.cmn.rankOverlap[;1b] each rci;
        rci: .cmn.spearmansRho[baserank; ; period] each rci;
        rci: `int$0.5*100+100*rci;
        : (enlist `$"rci",string period)!enlist rci;
    }[pricelist;] each periods;
    : t;
  };



//
// OHLC + Bollinger
//
.tech.ohlcBollinger: {[dates; psym; byMin; period; bblines]
    t: .trd.FXOHLCMin[dates; psym; byMin];
    t: t,'.tech.bollinger[(exec close from t); period; bblines];
    : t;
  };

//
// Bollinger Band Indicator
//
// @param pricelist (float list) : price list
// @param period (int)           : bollinger period
// @param bblines (float list)   : bollinger sigma lines (1~3)
//
.tech.bollinger: {[pricelist; period; bblines]
    bblines: (),bblines;
    ma: mavg[period; pricelist];
    sigma: mdev[period; pricelist];
    // bollinger lines
    t: flip raze
    {[ma; sigma; bbline]
        bbU: ma + sigma * bbline;
        bbD: ma - sigma * bbline;
        : ((`$"bbU",string bbline),(`$"bbD",string bbline))!(bbU;bbD);
    }[ma; sigma; ] each bblines;
    colnames: asc cols t;
    macol: (`$"ma",string period);
    stdcol: (`$"std",string period);
    t: t,'flip (macol,stdcol)!(ma;sigma);
    t: (macol,colnames) xcols t;
    : t;
  };






// ---------------------------------------------------------
//    Functions Trend
// ---------------------------------------------------------

//
// トレンドを計算します
// トレンド決定にはMACDを使用して行います
// 25%値を基準にしてトレンドを決定します
//
// @param pdates    : 対象期間
// @param psym      : 対象symbol
// @param p1        : 期間1
// @param p2        : 期間2
// @param p3        : 期間3
// @param isDay     : 日足か、日中データか
// @param isReturnAll : 詳細データを返すか
//
.tech.trendMACD: {[pdates; psym; p1; p2; p3; isDay; isReturnAll]
    // data
    t: ();
    $[isDay;
        t: `date xasc 0!select last price by date from FXPips where date within pdates, sym=psym;
        t: `timestamp xasc select timestamp, price from FXPips where date within pdates, sym=psym
    ];
    // MACD
    t: update ema1: ewma[p1;price],  ema2: ewma[p2;price], ema3: ewma[p3;price] from t;
    t: update macd12: (100%price) * ema1 - ema2, macd23: (100%price) * ema2 - ema3 from t;
    t: update signal12: mavg[p1; macd12], signal23: mavg[p1; macd23] from t;
    / t: update slope1: sqrt[p1]*calcSlope[ema1;10], slope2: sqrt[p2]*calcSlope[ema2;10], slope3: sqrt[p3]*calcSlope[ema3;10], slopemacd: calcSlope[macd23;10]  from t;
    / t: update slope3: sqrt[p3]*calcSlope[ema3;10], slopemacd: 1000*calcSlope[macd23;10] from t;
    / t: update slopemacd2: mavg[p2; slopemacd] from t;

    // MACDのしきい値
    thres12: .cmn.statsInfo[abs t[`macd12]; 1b]`p25;
    thres23: .cmn.statsInfo[abs t[`macd23]; 1b]`p25;
    thres12R: (neg thres12),thres12;
    thres23R: (neg thres23),thres23;

    // トレンド
    t: update trendVal:0N, trend:`       from t;
    t: update trendVal: 0, trend:`RANGE  from t where macd23 within thres23R;
    t: update trendVal: 1, trend:`UP_R   from t where macd23 > thres23;
    t: update trendVal:-1, trend:`DOWN_R from t where macd23 < neg thres23;
    t: update trendVal: 2, trend:`UP     from t where macd12 > thres12,      macd23 > thres23;
    t: update trendVal:-2, trend:`DOWN   from t where macd12 < neg thres12,  macd23 < neg thres23;
    /t: update trendVal2: mavg[6;trendVal]  from t;

    // return
    $[isReturnAll;
        :t;
        :select timestamp, price, trendVal, trend from t;
    ];
  };

//
// 日足のトレンドを計算します
// 6日(1週間),　25日(3ヶ月), 75日(3ヶ月) を使用します
//
.tech.trendMACDDaily: .tech.trendMACD[ ; ; 6; 25; 75; 1b; 0b];

//
// 短期のトレンドを計算します
//
.tech.trendMACDShort: .tech.trendMACD[ ; ; 200; 1000; 5000; 0b; 0b];
/ .tech.trendMACDShort[2014.01.01 2014.06.30; `USDJPY]



//
// 日足のトレンドを計算します
// トレンド決定にはMACDを使用して行います
// 25%値を基準にしてトレンドを決定します
//
.tech.calcTrend: {[pdates; psym]
    t: 0!select last price by date from FXPips where date within pdates, sym=psym;
    t: update ema06: ewma[6;price],  ema25: ewma[25;price], ema75: ewma[75;price] from t;
    t: update macd1: (100%price) * ema06 - ema25, macd2: (100%price) * ema25 - ema75 from t;
    /t: update signal1: mavg[6; macd1], signal2: mavg[12; macd2] from t;
    /t: update slope06: sqrt[6]*calcSlope[ema06;10], slope25: sqrt[25]*calcSlope[ema25;10], slope75: sqrt[75]*calcSlope[ema75;10] from t;

    // MACDのしきい値
    thres1: .cmn.statsInfo[abs t[`macd1]; 1b]`p25;
    thres2: .cmn.statsInfo[abs t[`macd2]; 1b]`p25;
    thres1R: (neg thres1),thres1;
    thres2R: (neg thres2),thres2;

    // トレンド
    t: update trendVal:0N, trend:`       from t;
    t: update trendVal: 0, trend:`RANGE  from t where macd2 within thres2R;
    t: update trendVal: 1, trend:`UP_R   from t where macd2 > thres2;
    t: update trendVal:-1, trend:`DOWN_R from t where macd2 < neg thres2;
    t: update trendVal: 2, trend:`UP     from t where macd1 > thres1,      macd2 > thres2;
    t: update trendVal:-2, trend:`DOWN   from t where macd1 < neg thres1,  macd2 < neg thres2;
    /t: update trendVal2: mavg[6;trendVal]  from t;

    // return
    :t
  };
/ tab: .tech.calcTrend[2009.01.01 2015.4.30; `USDJPY]
/ select count i by trend from tab










//
// 単回帰分析を行い傾きを算出します
// xはindexを用います
//
.tech.calcSlope: {[ylist; period]
    ylist: joinPrev[period-1;ylist],'ylist;
    X: flip (period#1;til period);
    // solve
    reg: .qml.mlsq[X;] each ylist;
    : last each reg
  };

//
// 単回帰分析を行い傾きを算出します
// X軸の幅の指定が可能です
//
.tech.calcSlopeX: {[xlist; ylist; period]
    ylist: joinPrev[period-1;ylist],'ylist;
    xlist: joinPrev[period-1;xlist],'xlist;
    xlist: {flip ((count x)#1;x)} each xlist;
    // solve
    reg: .qml.mlsq'[xlist;ylist];
    : last each reg
  };

// 予備 Old版
/.tech.calcSlopeOld: {[xlist;ylist;period]
/    indices: (neg period)#til index+1;
/    Y: ylist@indices;
/    X: xlist@indices;
/    X: flip ((count X)#1;X);
/    // solve
/    reg: last .qml.mlsq[X;Y];
/    : last reg
/  };



// ---------------------------------------------------------
//    Functions Market Profile
// ---------------------------------------------------------










