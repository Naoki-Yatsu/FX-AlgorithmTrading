//
// Functions Commonly - day
//

/ \d .cmn

// ---------------------------------------------------------
//    Constant
// ---------------------------------------------------------

.cmn.DAY_OF_WEEK: `Sat`Sun`Mon`Tue`Wed`Thu`Fri;

// ---------------------------------------------------------
//    Functions
// ---------------------------------------------------------

//
// 曜日を計算します
// return : day of week
//
.cmn.dayOfWeek: {[cdate]
    rem: cdate mod 7;
    if[rem=0; :"Sat" ];
    if[rem=1; :"Sun" ];
    if[rem=2; :"Mon" ];
    if[rem=3; :"Tue" ];
    if[rem=4; :"Wed" ];
    if[rem=5; :"Thu" ];
    if[rem=6; :"Fri" ];
  };
     
//
// "(Mon)"
//
.cmn.dayOfWeekPar: {[cdate]
    "(",.cmn.dayOfWeek[cdate],")"
  };
  
//
// "2015.01.16 (Fri)"
//
.cmn.dayOfWeekDayPar: {[cdate]
    (string cdate)," (",.cmn.dayOfWeek[cdate],")"
  };
// dayOfWeekDayPar[.z.d]

//
// 曜日をSymbol型で取得します。
//
.cmn.dayOfWeekSym: {[cdate]
    : `$.cmn.dayOfWeek[cdate];
  };

//
// 曜日を計算します(List対応版)
// @return day of week list (list of sym)
//
.cmn.dayOfWeekList: {[ldate]
    ldate: `date$ldate;
    rem: ldate mod 7;
    dow: (count ldate)#`;
    (dow where rem=0): `Sat;
    (dow where rem=1): `Sun;
    (dow where rem=2): `Mon;
    (dow where rem=3): `Tue;
    (dow where rem=4): `Wed;
    (dow where rem=5): `Thu;
    (dow where rem=6): `Fri;
    : dow
  };

//
// 週末を判断します
//
.cmn.isWeekend: {2 > x mod 7};




//
// 指定日の月の初日を取得します
//
// @param d : 日付
// @param m : 月の前後(0:当月、1:翌月)
//
.cmn.firstDate: {[d;m] `date$(`month$d)+m};

//
// 指定日の月の最終日を取得します
//
// @param d : 日付
// @param m : 月の前後(0:当月、1:翌月)
//
.cmn.lastDate: {[d;m] -1+`date$(`month$d)+m+1};


//
// 月を加えます
// 加算後に同日が存在しない場合は最終日をかえします
//
// @param ldate (date or date list)
// @param month
//
.cmn.addMonth: {[ldate; month]
    ldate: (),ldate;
    days: ldate - `date$`month$ldate;
    afterMonths: month + `month$ldate;
    afterDates: days + `date$afterMonths;
    // if the same date is not exist in target month, update last date of prev month
    overIndex: where afterMonths<>`month$afterDates;    
    afterDates[overIndex]: .cmn.lastDate[afterDates[overIndex];-1];
    $[1=count afterDates;
        : first afterDates;
        : afterDates;
    ];
/    : afterDates;
  };

//
// 年を加えます
// 加算後に同日が存在しない場合は最終日をかえします
//
.cmn.addYear: {[ldate; year] .cmn.addMonth[ldate; 12*year]};




// ---------------------------------------------------------
//    Sample Data
// ---------------------------------------------------------

//
// 1時間ごとのtimestampデータを作成します
//
.cmn.sampleTimestamp: {[cnt] flip (enlist `timestamp)!enlist `timestamp$60*60*1000000000*til cnt};

//
// dateごとのデータを作成します
//
.cmn.sampleDate: {[cnt] flip (enlist `date)!enlist `date$ til cnt};

