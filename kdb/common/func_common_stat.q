//
// Functions Commonly - statistics
//

/ \d .cmn

// ---------------------------------------------------------
//    Functions Util
// ---------------------------------------------------------


//
// 指定桁数になるように四捨五入で丸めます
//
// @param n (int)   : 桁数(1:小数1桁, -1:1桁目)
// @param x (float) : 対象
//
.cmn.round: {[n;x] (0.1 xexp n)*`long$x*(10 xexp n)};

//
// パーセント変換して丸めます
// @param n (int)        : パーセント変化後の小数点以下桁数
// @param x (float list) : 対象
//
.cmn.percentage: {[n;x] .cmn.round[n;x*100]};


// ---------------------------------------------------------
//    Functions
// ---------------------------------------------------------

//
// Function : リストから該当するindexをそれより前(後)のnull以外の値で補完して返します
// Params   : x (list) -
// ※fillsを使うべき
// 
.cmn.prevValid: {[x]
    i:0;
    do[-1+count x; 
        if [null x[i+:1]; x[i]: x[i-1]];
    ];
    :x 
  };
.cmn.nextValid: {[x]
    do[i: count x; 
        if [null x[i-:1]; x[i]: x[i+1]]
    ];
    :x 
  };

//
// For List of List
// xがlist of listになっている場合に使用する
// 要素に`float$()などが入っている場合に対応
//
.cmn.prevValidList: {[x]
    i:0;
    do[-1+count x;
        i+:1;
        if [(1b~null x[i])|(0=count x[i]); x[i]: x[i-1]];
    ];
    :x 
  };
.cmn.nextValidList: {[x]
    do[i: count x;
        i-:1;
        if [(1b~null x[i])|(0=count x[i]); x[i]: x[i+1]]
    ];
    :x 
  };


// [※遅い] 削除可
// Function : リストから該当するindexをそれより前のnull以外の値で補完して返します
// Params   : x (list) -
//          : i (int)  -
//
.cmn.prevValidEach: {[x;i]
        //x: 1 0n 2 0n 3; i:2; end:5;
        prevValid: 0n;
        while [i>=0;
            if [ not null x[i]; prevValid:x[i]; i:0];
            i-:1];
        prevValid
     };
// usage) the point is loop with each i
// select time, prevValid[price;] each i from


//
// パーセンタイルを計算します。
//
.cmn.percentile: {[array;rate]
        array floor rate * count array
    };
// percentile[valuelist; 0.75]


//
// 線形按分してパーセンタイル値を返します。
//
.cmn.percentileL: {[array;rate]
	    n: count array;
	    a: (n-1)*rate;
	    i: floor a;
	    d: a-i;
	    (array[i]*(1-d)) + (array[i+1]*d)
    };


//
// Function : パーセント指定の線形按分パーセンタイル値を返します。
// Params   : array (list)
//          : p (float) - パーセント値(0-100)
//
.cmn.percentileLP: {[array;p]
	    n: count array;
	    a: 0.01*(n-1)*p;
	    i: floor a;
	    d: a-i;
	    (array[i]*(1-d)) + (array[i+1]*d)
    };
// usage)
//     test: (til 101)
//     percentileLP[test; 50]



//
// 各種統計値を計算して、Dict形式で返します。
// nullは除外します
//
.cmn.statsInfo: {[valuelist; isAsc]
    $[isAsc;
        valuelist: asc valuelist where not null valuelist;
        valuelist: desc valuelist where not null valuelist
    ];
    tmp: ()!();
    tmp[`]: ::;
    tmp[`number]  : count valuelist;
    tmp[`average] : avg valuelist;
    tmp[`median]  : med valuelist;
    tmp[`stdev]   : dev valuelist;
    tmp[`um_stdev]: sdev valuelist;
    tmp[`maximum] : max valuelist;
    tmp[`minimum] : min valuelist;
    tmp[`p01] : .cmn.percentile[valuelist;0.01];
    tmp[`p05] : .cmn.percentile[valuelist;0.05];
    tmp[`p10] : .cmn.percentile[valuelist;0.10];
    tmp[`p25] : .cmn.percentile[valuelist;0.25];
    tmp[`p50] : .cmn.percentile[valuelist;0.50];
    tmp[`p75] : .cmn.percentile[valuelist;0.75];
    tmp[`p90] : .cmn.percentile[valuelist;0.90];
    tmp[`p95] : .cmn.percentile[valuelist;0.95];
    tmp[`p99] : .cmn.percentile[valuelist;0.99];
    tmp[`0.5s]: (reciprocal tmp[`number]) * count where valuelist within (tmp[`average]-0.5*tmp[`um_stdev]),(tmp[`average]+0.5*tmp[`um_stdev]); 
    tmp[`1s]  : (reciprocal tmp[`number]) * count where valuelist within (tmp[`average]-    tmp[`um_stdev]),(tmp[`average]+    tmp[`um_stdev]);
    tmp[`1.5s]: (reciprocal tmp[`number]) * count where valuelist within (tmp[`average]-1.5*tmp[`um_stdev]),(tmp[`average]+1.5*tmp[`um_stdev]); 
    tmp[`2s]  : (reciprocal tmp[`number]) * count where valuelist within (tmp[`average]-  2*tmp[`um_stdev]),(tmp[`average]+  2*tmp[`um_stdev]);
    tmp[`3s]  : (reciprocal tmp[`number]) * count where valuelist within (tmp[`average]-  3*tmp[`um_stdev]),(tmp[`average]+  3*tmp[`um_stdev]);
    : tmp _`
  };
//asc  - statsInfo[valuelist;1b]
//desc - statsInfo[valuelist;0b]


//
// histgramを作成します。
// step-sizeは3-sigmaを基準に自動設定します。
//
.cmn.histgram: {[valuelist]
    // step is based on 3-sigma
    step: .cmn.stepSize[3 * sdev valuelist];
    tab: flip (enlist `val)!enlist valuelist where not null valuelist;
    tab: select cnt: count i by step xbar val from tab;
    tab: update density: cnt % sum cnt from tab;
    tab: update density: (count tab) msum density from tab;
    : tab
  };
// .cmn.histgram[valuelist]

//
// histgramを作成します。
// step-sizeは3-sigmaを基準に自動設定します。
//
.cmn.histgramRatio: {[valuelist]
    // step is based on 3-sigma
    step: .cmn.stepSize[3 * sdev valuelist];
    tab: flip (enlist `val)!enlist valuelist where not null valuelist;
    tab: select cnt: count i by step xbar val from tab;
    tab: update ratio: cnt % sum cnt, density: cnt % sum cnt from tab;
    tab: update density: (count tab) msum density from tab;
    : tab
  };



//
// 桁数を求めます
// 123 -> 3, 0.1 -> -1, 0.02 -> -2
//
.cmn.digitNumber: {[num]
    num: abs num;
    digit: 1;
    // over 1
    if[num>1;
        while[num>10;
            num %: 10;
            digit+:1
        ];
    ];
    //under 1 
    if[num<1;
        while[num<10;
            num *: 10;
            digit-:1
        ];
    ];
    : digit
  };


//
// histgram用に値を分割するためのStep sizeを算出します。
// 1~2 : 片側20~40
// 2~5 : 片側20~50
// 5~  : 片側25~50 
//
.cmn.stepSize: {[num]
    // 桁数 
    digit: .cmn.digitNumber[num];
    // 刻み数字 
    topNum: 0;
    base: 0;
    $[digit>0;
        topNum: num % 10 xexp digit-1;
        topNum: num % 10 xexp digit
    ];
    if [topNum < 2; base:0.5];
    if [topNum within 2 5; base:1];
    if [topNum > 5; base:2];
    // return
    $[digit>0;
        : base * 10 xexp digit-2;
        : base * 10 xexp digit-1
    ];
  };


//
// ゼロパディングします。
//
.cmn.zeroPadding:{[stringlist; length]
    $[(c:count stringlist)>=length;
        : stringlist;
        : ((length-c)#"0"),stringlist
    ];
  };



//
// 重複ありのrankづけを行います
// rankより10倍の時間がかかります
//
// @param nums 
// @param isAverage (boolean) : 1b: avaraging rank
//
.cmn.rankOverlap: {[nums; isAverage]
    orgRank: rank nums;
    d: deltas asc nums;
    // return if not duplicated, first d is ignore
    if [not any (1-count d)#d=0; :orgRank+1];

    // if any rank is duplicated
    ranking: (),1.0;
    i:1;
    do [(count nums)-1;
        $[d[i]<>0;
            ranking,: (`float$i+1);
            ranking,: ranking[i-1]];
        i+:1
    ];
    // return, if not averaging
    if [not isAverage; :ranking orgRank];

    // averaging ranks
    t: select cnt:count i by ranking from ([]ranking:ranking);
    // copy single items
    ranking: exec ranking from t where cnt=1;
    t: select ranking, cnt from t where cnt >1;
    t: update ranking: ranking + 0.5*(cnt-1) from t;
    // copy duplicate items
    ranking,: raze exec (cnt#'ranking) from t;
    ranking: asc ranking;
    // return
    : ranking orgRank
  };

//
// スピアマンの順位相関係数
//
// @param x (list) : rank1
// @param y (list) : rank2
// @param n (int)
//
.cmn.spearmansRho: {[x;y;n]1 - 6*(sum xexp[x-y;2])%(xexp[n;3]-n)};


//
// auto correlation (Q-Tips)
//
.cmn.ac: {x%first x:x{(y#x)$neg[y]#x}/:c-til c:count x-:avg x};



