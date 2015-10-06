//
// Functions for Trading
//

// ---------------------------------------------------------
//    Functions
// ---------------------------------------------------------

//
// ema[period; price]
//
// https://groups.google.com/forum/#!searchin/personal-kdbplus/exponential$20moving/personal-kdbplus/G7FgxzzKfnc/Fn_u-jHxUpAJ
ema: {((x-1)#0n),i,{z+x*y}\[i:avg x#y;1-a;(x _y)*a:2%1+x]};

//
// ema, first one as intial value
//
ewma:{{y+x*z-y}[x:2%1+x]\[y]};

// ---------------------------------------------------------
//    Static Infomation
// ---------------------------------------------------------

//
// Ccy Information Table
//
CCYINFO: ([sym:`$()] ccy1:`$(); ccy2:`$(); pipsCoef:`int$(); pipsScale:`int$(); pip:`float$(); subpipsCoef:`int$(); subpipsScale:`int$(); subpip:`float$(); spread:`float$(); spreadpip:`float$() );
CCYINFO[`AUDNZD]: `AUD`NZD, 10000, 4, 0.0001, 100000, 5, 0.00001, 2.5, 0.00025;
CCYINFO[`AUDUSD]: `AUD`USD, 10000, 4, 0.0001, 100000, 5, 0.00001, 1.0, 0.0001;
CCYINFO[`EURCHF]: `EUR`CHF, 10000, 4, 0.0001, 100000, 5, 0.00001, 2.0, 0.0002;
CCYINFO[`EURGBP]: `EUR`GBP, 10000, 4, 0.0001, 100000, 5, 0.00001, 1.5, 0.00015;
CCYINFO[`EURUSD]: `EUR`USD, 10000, 4, 0.0001, 100000, 5, 0.00001, 0.6, 0.00006;
CCYINFO[`GBPUSD]: `GBP`USD, 10000, 4, 0.0001, 100000, 5, 0.00001, 1.0, 0.0001;
CCYINFO[`NZDUSD]: `NZD`USD, 10000, 4, 0.0001, 100000, 5, 0.00001, 1.5, 0.00015;
CCYINFO[`USDCAD]: `USD`CAD, 10000, 4, 0.0001, 100000, 5, 0.00001, 1.0, 0.0001;
CCYINFO[`USDCHF]: `USD`CHF, 10000, 4, 0.0001, 100000, 5, 0.00001, 2.5, 0.00025;
CCYINFO[`AUDJPY]: `AUD`JPY, 100, 2, 0.01, 1000, 3, 0.001, 1.0, 0.01;
CCYINFO[`CADJPY]: `CAD`JPY, 100, 2, 0.01, 1000, 3, 0.001, 1.5, 0.015;
CCYINFO[`CHFJPY]: `CHF`JPY, 100, 2, 0.01, 1000, 3, 0.001, 2.0, 0.02;
CCYINFO[`EURJPY]: `EUR`JPY, 100, 2, 0.01, 1000, 3, 0.001, 1.0, 0.01;
CCYINFO[`GBPJPY]: `GBP`JPY, 100, 2, 0.01, 1000, 3, 0.001, 2.0, 0.02;
CCYINFO[`NZDJPY]: `NZD`JPY, 100, 2, 0.01, 1000, 3, 0.001, 1.5, 0.015;
CCYINFO[`USDJPY]: `USD`JPY, 100, 2, 0.01, 1000, 3, 0.001, 0.6, 0.006;


//
// Summer Information Table
//
SUMMER_INFO: ([city:`$(); year:`int$()] fromDate:`date$(); toDate:`date$());
SUMMER_INFO,: flip ((cols SUMMER_INFO)!
    (`LDN`LDN`LDN`LDN`LDN`LDN`LDN`LDN`LDN`LDN`LDN`LDN`LDN`LDN`LDN`LDN`LDN`LDN`LDN`LDN`LDN;
    2000,2001,2002,2003,2004,2005,2006,2007,2008,2009,2010,2011,2012,2013,2014,2015,2016,2017,2018,2019,2020;   
    2000.03.26,2001.03.25,2002.03.31,2003.03.30,2004.03.28,2005.03.27,2006.03.26,2007.03.25,2008.03.30,2009.03.29,2010.03.28,2011.03.27,2012.03.25,2013.03.31,2014.03.30,2015.03.29,2016.03.27,2017.03.26,2018.03.25,2019.03.31,2020.03.29;
    2000.10.29,2001.10.28,2002.10.27,2003.10.26,2004.10.31,2005.10.30,2006.10.29,2007.10.28,2008.10.26,2009.10.25,2010.10.31,2011.10.30,2012.10.28,2013.10.27,2014.10.26,2015.10.25,2016.10.30,2017.10.29,2018.10.28,2019.10.27,2020.10.25));
SUMMER_INFO,: flip (cols SUMMER_INFO)!
    (`NYC`NYC`NYC`NYC`NYC`NYC`NYC`NYC`NYC`NYC`NYC`NYC`NYC`NYC`NYC`NYC`NYC`NYC`NYC`NYC`NYC;
    2000,2001,2002,2003,2004,2005,2006,2007,2008,2009,2010,2011,2012,2013,2014,2015,2016,2017,2018,2019,2020;   
    2000.04.02,2001.04.01,2002.04.07,2003.04.06,2004.04.04,2005.04.03,2006.04.02,2007.03.11,2008.03.09,2009.03.08,2010.03.14,2011.03.13,2012.03.11,2013.03.10,2014.03.09,2015.03.08,2016.03.13,2017.03.12,2018.03.11,2019.03.10,2020.03.08;
    2000.10.29,2001.10.28,2002.10.27,2003.10.26,2004.10.31,2005.10.30,2006.10.29,2007.11.04,2008.11.02,2009.11.01,2010.11.07,2011.11.06,2012.11.04,2013.11.03,2014.11.02,2015.11.01,2016.11.06,2017.11.05,2018.11.04,2019.11.03,2020.11.01);


//
// City Information Table
//
CITY_INFO: ([city:`$(); season:`$()] name:`$(); open:`time$(); close:`time$(); offset:`time$(); offsetTKY:`time$());
CITY_INFO[`TKY`ALL]   : `Tokyo,  08:00:00.000, 17:00:00.000, 09:00:00.000, 00:00:00.000;
CITY_INFO[`LDN`WINTER]: `London, 17:00:00.000, 26:00:00.000, 00:00:00.000,-09:00:00.000;
CITY_INFO[`LDN`SUMMER]: `London, 16:00:00.000, 25:00:00.000, 00:00:00.000,-08:00:00.000;
CITY_INFO[`NYC`WINTER]: `NewYork,22:00:00.000, 07:00:00.000,-04:00:00.000,-14:00:00.000;
CITY_INFO[`NYC`SUMMER]: `NewYork,21:00:00.000, 06:00:00.000,-03:00:00.000,-13:00:00.000;

//
// Trade Time
//
TRADE_TIME: ([city:`$(); season:`$()] startday:`$(); starttime:`time$(); endday:`$(); endtime:`time$() );
TRADE_TIME[`TKY`SUMMER]: `Mon, 07:30:00.000, `Sat, 05:30:00.000;
TRADE_TIME[`TKY`WINTER]: `Mon, 07:30:00.000, `Sat, 06:30:00.000;
TRADE_TIME[`GMT`SUMMER]: `Sun, 22:30:00.000, `Fri, 20:30:00.000;
TRADE_TIME[`GMT`WINTER]: `Sun, 22:30:00.000, `Fri, 21:30:00.000;
TRADE_TIME[`NYC`SUMMER]: `Sun, 18:30:00.000, `Fri, 16:30:00.000;
TRADE_TIME[`NYC`WINTER]: `Sun, 17:30:00.000, `Fri, 16:30:00.000;



// ---------------------------------------------------------
//    Trading Functions
// ---------------------------------------------------------
/ \d .trd



// ---------------------------------------------------------
//    Date Time Utils
// ---------------------------------------------------------

//
// 夏時間かどうか判定します
//
.trd.isSummer: {[pcity; cdate]
    r: 0 < exec count i from SUMMER_INFO where city=pcity, cdate >= fromDate, cdate < toDate;
    :r
  };
/ .trd.isSummer[`LDN; .z.d]

//
// 夏時間かどうか判定します
//
// @param pcity (symbol)
// @param dates (date list)
//
.trd.isSummerList: {[pcity; dates]
    r: flip (enlist `date)!enlist cdate;
    r: update year:`year$date, city:pcity from r;
    r: r lj select from SUMMER_INFO where city=pcity;
    r: update summer: 1b from r where date >= fromDate, date < toDate;
    exec summer from r
  };

//
// いずれかの都市が夏時間か判定します
//
.trd.isSummerAny: {[cdate]
    r: 0 < exec count i from SUMMER_INFO where cdate >= fromDate, cdate < toDate;
    :r
  };
/ .trd.isSummerAny[ .z.d]



//
// 取引可能時間を判定します
//
// @param timestamplist (timestamp/datetime or list)
// 
.trd.isTradeTimeTKY: {[timestamplist]
    timestamplist: (),timestamplist;
    // def
    starttime: TRADE_TIME[`TKY`SUMMER][`starttime];
    endtimeSummer: TRADE_TIME[`TKY`SUMMER][`endtime];
    endtimeWinter: TRADE_TIME[`TKY`WINTER][`endtime];
    // 
    day: (`date$timestamplist) mod 7;
    time: `time$timestamplist;
    summer: .trd.isSummer[`NYC;] each `date$timestamplist;
    res: (count timestamplist)#1b;

    // Sunday
    (res where day=1): 0b;
    // Monday
    (res where (day=2) and (time<starttime)): 0b;
    // Saturday
    (res where (day=0) and (time>endtimeSummer) and summer): 0b;
    (res where (day=0) and (time>endtimeWinter) and not summer): 0b;
    
    // return
    $[1=count res; :first res; :res]
  };




// ---------------------------------------------------------
//    Pips Utils
// ---------------------------------------------------------

//
// price listをpip単位で丸めます
//
.trd.roundpip: {[csym; priceList]
    pipsCoef: CCYINFO[csym][`pipsCoef];
    pip: CCYINFO[csym][`pip];
    priceList: pip*`int$pipsCoef*priceList;
    : priceList;
  };
/ .trd.roundpip[`USDJPY; 111.112,123.455]

//
// price listをsubpip単位で丸めます
//
.trd.roundsubpip: {[csym; priceList]
    subpipsCoef: CCYINFO[csym][`subpipsCoef];
    subpip: CCYINFO[csym][`subpip];
    priceList: subpip*`int$subpipsCoef*priceList;
    : priceList;
  };
/ .trd.roundsubpip[`USDJPY; 111.1121,123.4565011]


// ---------------------------------------------------------
//    Data Source
// ---------------------------------------------------------

//
// OHLCを作成します
//
.trd.ohlcDaily: {[tablename; pricecolname; cdates; csym]
    t: `$string tablename;
    csym: `$string csym;
    c: ((within;`date;cdates);(=;`sym;enlist csym));
    b: (enlist `date)!(enlist `date);
    col: `$string pricecolname; 
    a: `open`high`low`close!((first;col);(max;col);(min;col);(last;col));
    : ?[t; c; b; a]
    / ?[t; enlist (=;`sym;enlist `USDJPY); (enlist `date)!(enlist `date); `open`hith`low`close!((first;`price);(max;`price);(min;`price);(last;`price))]  
  };
/ .trd.ohlcDaily[`FXPips; `price; 2010.01.01 2010.12.31; `USDJPY]

//
// OHLCデータを取得します。
//
.trd.FXOHLC: {[cdates; csym; bySec]
    t: select timestamp, price: 0.5*bid+ask from FX where date within cdates, sym=csym;
    t: `timestamp xasc 0!select open: first price, high: max price, low: min price, close: last price by (`timespan$`second$bySec) xbar timestamp from t;
    : t
  };
/ .trd.FXOHLC[2014.10.01 2014.10.31; `USDJPY; 60]

//
// OHLCデータを取得します(分単位)
//
.trd.FXOHLCMin: {[dates; csym; byMin]
    t: select timestamp, open, high, low, close from FX1min where date within dates, sym=csym;
    t: `timestamp xasc select first open, max high, min low, last close by (`timespan$`minute$byMin) xbar timestamp from t;
    : t
  };


//
// 終値データを取得します。
//
.trd.FXClosePrice: {[cdates; csym; bySec]
    :`timestamp xasc 0!select price: last 0.5*bid+ask by (`timespan$`second$bySec) xbar timestamp from FX where date within cdates, sym=csym;
  };
/ .trd.FXClosePrice[2014.10.01 2014.10.31; `USDJPY; 60]

//
// 終値データを取得します（分単位）
//
.trd.FXClosePriceMin: {[cdates; csym; byMin]
    :`timestamp xasc 0!select price: last close by (`timespan$`minute$byMin) xbar timestamp from FX1min where date within cdates, sym=csym;
  };


//
// 価格列データを取得します。
//
.trd.FXPipsPrice: {[cdates; csym; isHalf]
    $[isHalf;
        :`timestamp xasc select timestamp, price from FXPipsH where date within cdates, sym=csym;
        :`timestamp xasc select timestamp, price from FXPips  where date within cdates, sym=csym];        
  };
/ .trd.FXPipsPrice[2014.10.01 2014.10.31; `USDJPY; 0b]

//
// 価格列データのOHLCを返します
//
.trd.FXPipsOHLC: {[cdates; csym; byCount; isHalf]
    t: .trd.FXPipsPrice[cdates; csym; isHalf];
    t: `timestamp xasc 0!select first timestamp, open: first price, high: max price, low: min price, close: last price by index:byCount xbar i from t;
    : delete index from t
  };


// ---------------------------------------------------------
//    Analysis
// ---------------------------------------------------------

//
// simulate function monthly
// メモリ節約のため、指定月次で区切ってシミュレーションを行います
//
// @param fSimulate[cdates] : シミュレーション対象関数, param: cdates, return: table
// @param cdates (2 dates)  : シミュレーション全体期間
// @param periodMonth (int) : 一度に計算する月数
//
.trd.simulateMonthly: {[fSimulate; cdates; periodMonth; usePeach]
    startDate: first cdates;
    endDate: last cdates;
    monthEnd: -1+`date$(periodMonth+`month$startDate);
    tempDates: ();
    while [monthEnd < endDate;
        tempDates,: enlist startDate, monthEnd;
        startDate: `date$1+`month$monthEnd;
        monthEnd: -1+`date$(periodMonth+`month$startDate);
    ];
    tempDates,: enlist startDate, endDate;
    // simulate
    $[usePeach;
        : raze fSimulate each tempDates;
        : raze fSimulate peach tempDates;
    ];
  };
/ .trd.simulateMonthly[fSimulate; cdates; 3]


//
// 指定された期間を学習期間・テスト期間に分けて
// 日付をずらしながらでシミュレーションを行います。
//    .trd.simulateAllMonth 月単位
//    .trd.simulateAllDay   日単位
//
// @param cdates         : 全体期間
// @param trainingPeriod : 学習期間(月 or 日)
// @param testPeriod     : テスト期間(月 or 日)
// @param fsimulate[trainingDates;testDates] : シミュレーション対象関数(return: table)
//
.trd.simulateAllMonth: {[cdates; trainingPeriod; testPeriod; fsimulate]
    out "[simulateAllMonth] Start.";
    // tranining & test dates
    trainingDates: ();
    testDates: ();
    // create dates
    nextTrainingDates: (cdates[0], .cmn.lastDate[cdates[0]; trainingPeriod-1]);
    nextTestDates: ((nextTrainingDates[1]+1), .cmn.lastDate[nextTrainingDates[1]; testPeriod]);
    // loop to last date
    while [nextTestDates[0] < cdates[1];
        trainingDates,: enlist nextTrainingDates;
        if [nextTestDates[1] > cdates[1]; nextTestDates[1]: cdates[1]];
        testDates,: enlist nextTestDates;
        // add month
        nextTrainingDates: (.cmn.firstDate[nextTrainingDates[0]; testPeriod], .cmn.lastDate[nextTrainingDates[1]; testPeriod]);
        nextTestDates: ((nextTrainingDates[1]+1), .cmn.lastDate[nextTrainingDates[1]; testPeriod]);
    ];
    
    // execute
    result: raze fsimulate'[trainingDates;testDates];
    // return
    out "[simulateAllMonth] End.";
    : result;
  };
.trd.simulateAllDay: {[cdates; trainingPeriod; testPeriod; fsimulate]
    out "[simulateAllDay] Start.";
    // tranining & test dates
    trainingDates: ();
    testDates: ();
    // create dates
    nextTrainingDates: (cdates[0], cdates[0]+trainingPeriod-1);
    nextTestDates: ((nextTrainingDates[1]+1), nextTrainingDates[1]+testPeriod);
    // loop to last date
    while [nextTestDates[0] < cdates[1];
        if [(testPeriod>=2) | (not .cmn.dayOfWeekSym[nextTestDates[0]]=`Sun);
            trainingDates,: enlist nextTrainingDates;
            if [nextTestDates[1] > cdates[1]; nextTestDates[1]: cdates[1]];
            testDates,: enlist nextTestDates;
        ];
        // add days
        nextTrainingDates+: testPeriod;
        nextTestDates+: testPeriod;
    ];

    // execute
    result: raze fsimulate'[trainingDates;testDates];
    // return
    out "[simulateAllDay] End.";
    : result;
  };


// ---------------------------------------------------------
//    Analysis
// ---------------------------------------------------------

//
// Summarize simulation result
// @param resultTable (table) : resultTable must have
//   timestamp | p - opentime
//   timestamp2| p - closetime (Option)
//   BS        | s - BUY/SELL (Option)
//   PL        | f
//
.trd.performanceAnalysis: {[resultTable]
    // define table
    summary: ([item:`$()] total:`float$(); profit:`float$(); loss:`float$());
    // calc values
    days: first value exec dayCount: 1 + (`date$max timestamp)-(`date$min timestamp) from resultTable;
    rsltPL: exec PL from resultTable;
    winsPL: exec PL from resultTable where PL > 0;
    losePL: exec PL from resultTable where PL < 0;
    // summary
    summary[`Days]:      `float$days, 0n, 0n;
    summary[`Trade]:     `float$(count rsltPL), (count winsPL), (count losePL);
    summary[`Trade_Day]: `float$((count rsltPL)%days), ((count winsPL)%days), ((count losePL)%days);
    summary[`Percentage]:`float$0n, (100*(count winsPL)%(count rsltPL)), (100*(count losePL)%(count rsltPL));
    summary[`Total]:     `float$(sum rsltPL), (sum winsPL), (sum losePL);
    summary[`Average]:   `float$(avg rsltPL), (avg winsPL), (avg losePL);
    summary[`Median]:    `float$(med rsltPL), (med winsPL), (med losePL);
    summary[`Maximum]:   `float$0n, (max winsPL), (min losePL);
    // Long/Short info
    if [any `BS=cols resultTable;
        buysPL: exec PL from resultTable where BS = `BUY;
        sellPL: exec PL from resultTable where BS = `SELL;
        summary[`Long]:     `float$(count buysPL), (count buysPL where buysPL>0), (count buysPL where buysPL<0);
        summary[`Long_Avg]: `float$(avg buysPL),   (avg buysPL where buysPL>0),   (avg buysPL where buysPL<0);
        summary[`Short]:    `float$(count sellPL), (count sellPL where sellPL>0), (count sellPL where sellPL<0);
        summary[`Short_Avg]:`float$(avg sellPL),   (avg sellPL where sellPL>0),   (avg sellPL where sellPL<0);
    ];
    // Trade Time info
    if [any `timestamp2=cols resultTable;
        timetotal: exec avg `minute$timestamp2 - timestamp from resultTable;
        timeprofit:exec avg `minute$timestamp2 - timestamp from resultTable where PL > 0;
        timeloss:  exec avg `minute$timestamp2 - timestamp from resultTable where PL < 0;
        summary[(`$"Time(min.)")]: timetotal, `float$timeprofit, timeloss;
    ];
    : summary;
  };
// performanceAnalysis[result]


//
// Summarize simulation result (dictionary version)
//
.trd.performanceAnalysisDict: {[resultTable]
    // calc values
    Day_Count: first value exec dayCount: (`date$max timestamp)-(`date$min timestamp) from resultTable;
    Gross_Profit: exec sum PL from resultTable where PL > 0;
    Gross_Loss:   exec sum PL from resultTable where PL < 0;
    exec Day_Count,
        Trade: count PL,
        Trade_Day: 0.1*`int$10*(count PL) % Day_Count,
        Net_Profit: sum PL,
        Average: 0.01*`int$100*avg PL,
        Median: med PL,
        Max_Profit: max PL,
        Max_Loss: min PL
    from resultTable
  };
// performanceAnalysis2[result]


//
// chart用のPLを算出します
// レバレッジ10倍で、100pipsで10%の増減です。
//
.trd.performanceChart: {[resultTable]
    cnt: count resultTable;
    select timestamp, PLsum: cnt msum PL from resultTable
  };
// performanceChart[result]


//
// chart用のPLを算出します2
// 合計PL, 日ごとのPL, 取引回数を返します。
//
.trd.performanceChart2: {[resultTable]
    cnt: count resultTable;
    select last price, last PLsum, sum PL, trade: count i by `date$timestamp from
    select timestamp, price, PL, PLsum: cnt msum PL from resultTable
  };
// performanceChart2[result]


//
// 価格列から指定pips変化の価格列を作成します。
// 結果の長さは元と同じで、指定の価格変化が無ければnullが入ります
//
// @param priceList - 価格列
// @param csym      - 対象のsymbol
// @param pipMode   - pips変化幅 (1.0, 0.5, 2.0, etc)
// 
.trd.searchPipsChange: {[priceList; csym; pipMode]
    // create floor and ceiling
    floorList:   (pipMode*CCYINFO[csym][`pip]) xbar priceList;
    ceilingList: pipMode * CCYINFO[csym][`pip] * ceiling (1%pipMode) * CCYINFO[csym][`pipsCoef] * priceList;
    resultList:  `float$();

    // for down check ceiling, for up check floor
    currentPrice: first floorList;
    i:0;
    do [count priceList;
        // check for up
        $[floorList[i]>currentPrice; 
            resultList,: currentPrice: floorList[i];
            // check for down
            $[ceilingList[i]<currentPrice;
                resultList,: currentPrice: ceilingList[i];
                // no change
                resultList,:0n
            ]
        ];
        i+:1;
    ];
    // return
    : resultList;
  };


//
// 価格列から指定 basis point 変化の価格列を作成します
// 結果の長さは元と同じで、指定の価格変化が無ければnullが入ります
// 
// @param priceList - 価格列
// @param csym      - 対象のsymbol
// @param pipMode   - pips変化幅 (1.0, 0.5, 2.0, etc)
// 
.trd.searchBPChange: {[priceList; psym; basisPoint]
    // create basis point list
    basis: 0.0001*basisPoint;
    bpcnt: `int$5*1%basis;
    basePrice: 10000*CCYINFO[psym][`pip];
    bplist: basePrice * (1+basis) xexp (til 2*bpcnt)-bpcnt;
    bplist: .cmn.round[; CCYINFO[psym][`subpipsScale]] each bplist;
    // trim bp list
    minbp: min where bplist >= min priceList;
    maxbp: max where bplist <= max priceList;
    bplist: bplist (neg maxbp - minbp - 3)#til maxbp+2;

    // calc bp change
    t: flip (enlist `price)!enlist priceList;
    t: update floorprice:   {[bplist;price] max bplist where bplist<=price}[bplist;] each price from t;
    t: update ceilingprice: {[bplist;price] min bplist where bplist>=price}[bplist;] each price from t;
    // setup bp price
    t: update bpprice: 0n from t;
    // for up, use floor
    t: update bpprice: floorprice from t where floorprice > prev floorprice;
    // for down use ceiling
    t: update bpprice: ceilingprice from t where ceilingprice <= prev floorprice;
    t: update bpprice: 0n from t where bpprice = prev bpprice;
    // trim same bpprice
    t: update .cmn.prevValid[bpprice] from t;
    t: update bpprice:0n from t where bpprice = prev bpprice;

    // return
    : exec bpprice from t;
  };
/ tab: select timestamp, price: 0.5*bid+ask from FX where date within 2013.01.01 2013.01.31, sym=psym
/ tab: update .trd.searchBPChange[price; psym; 1] from tab



