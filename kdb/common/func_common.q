//
// Functions Commons
//

// ---------------------------------------------------------
//    Load
// ---------------------------------------------------------

QHOME: raze system "echo $QHOME";
commondir: "l ",QHOME,"/common/";
libdir: "l ",QHOME,"/lib/";


// Load all
/ system loaddir,"func_common.q";


// Library
system "l ",QHOME,"/qml.q";
system libdir,"qml_libs.q";


// Load each
system commondir,"func_common_day.q";
system commondir,"func_common_stat.q";
system commondir,"func_common_table.q";

system commondir,"func_optimize.q";
system commondir,"func_randamwalk.q";
system commondir,"func_trading.q";

// with dependency
system commondir,"func_technical.q";



// ---------------------------------------------------------
//    Commmon
// ---------------------------------------------------------

//
// prev/next n times
//
nprev: {[n;x] do[n;x:prev x];x};
nnext: {[n;x] do[n;x:next x];x};

//
// Join previous n items to i, used in select
// Be careful NOT include itself.
//
joinPrev: {[n;x]
    r: (),/: x: prev x;
    // below is not good work when x is list of list
    // r: enlist each x: prev x;
    do[n-1;
        r: (x:prev x),'r
    ];
    r
  };
joinNext: {[n;x]
    r: (),/: x: next x;
    do[n-1;
        r: r,'(x:next x)
    ];
    r
  };
  
//
// Join previous n items to i, used in select
// Include itself
//
joinPrev0: {[n;x]
    r: (),/: x;
    do[n-1;
        r: (x:prev x),'r
    ];
    r
  };
joinNext0: {[n;x]
    r: (),/: x;
    do[n-1;
        r: r,'(x:next x)
    ];
    r
  };


//
// auto-correlation
// x must be price change ratio array
//
autocorrelation: {[n;x]
    acf: ([lag:`int$()] acf:`float$());
    i:0;
    do[n;
        acf[i]: x cor nnext[i;x];
        i+:1
    ];
    : 0!acf;
  };

//
// 標準出力します
//
out: {-1(string .z.Z)," ",x};


