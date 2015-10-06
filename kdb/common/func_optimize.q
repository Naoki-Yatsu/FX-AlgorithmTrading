//
// Functions for Optimization
//

/ \d .cmn

// ---------------------------------------------------------
//    Optimization
// ---------------------------------------------------------

//
// Optimize
//

// @param targetFunction : 最適化計算対象の関数、paramsを引数に入れて使用される targetFunction[params]
// @param evalFunction : targetFunctionの結果を評価するための関数 、evalFunction[targetFunction[params]]
// @param infoFunction : targetFunctionの結果から追加情報を引き出す関数、infoFunction[targetFunction[params]]
// @param paramsRanges : paramsのRangeを表す、行数はparamsと同じ、列数は2、値を変化指さない場合は、fromtoに同じ値を設定
// @param paramsStep   : paramsの増加幅を表す、行数はparamsと同じ、正数で設定
//
.cmn.optimiazaionParams: {[targetFunction; evalFunction; infoFunction; paramsRanges; paramsStep]
    // create all param list
    cnt: count paramsRanges;
    params: paramsRanges[;0];
    allParams: enlist each params;
    i:0;
    while [i<cnt;
        step: paramsStep[i];
        rangeEnd: paramsRanges[i;1];
        j:0;
        while [params[i]<=rangeEnd;
            allParams[i],: params[i];
            params[i]+:step;
            j+:1;
        ];
        i+:1;
    ];
    // cut duplicated 1st item
    allParams: {1_x} each allParams;
    // cross join
    paramCrossSet: (cross/) allParams;

    // execute
    resultDict:
    {[params]
        resultDict: ()!();
        resultDict[`]: ::;
        tmpRes: targetFunction[params];
        /resultDict[`param],: enlist params;
        /resultDict[`value],: enlist evalFunction tmpRes;
        /resultDict[`info],:  enlist infoFunction tmpRes;
        resultDict[`param]: params;
        resultDict[`val]: evalFunction tmpRes;
        resultDict[`info]:  infoFunction tmpRes;
        : resultDict _`;
    } each paramCrossSet;

    // return as table
    : resultDict;
  };


/ paramsRanges: ((2000,5000);(10000 15000);(0.4 0.7);(200;600));
/ paramsStep:  1000, 5000, 0.1, 200;
/ targetFunction: simulateMACDPos [cdates; csym; pipsCoef; pipsFee; ; 0; 0b];
/ evalFunction: {avg x `PL};
/ infoFunction: {0!.cmn.histgramRatio `int$ x `PL};
/
/ tab: optimiazaionParams[targetFunction; evalFunction; infoFunction; paramsRanges; paramsStep]




// ---------------------------------------------------------
/ \d .
