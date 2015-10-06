//
// Functions Commonly
//

/ \d .cmn

// ---------------------------------------------------------
//    Functions
// ---------------------------------------------------------


//
// select with (I)nterval
//
.cmn.selectInterval: {[data; interval] select from data where 0 = i mod interval};



//
// シンプルなpivotを作成します。
// @param t : table (table or symbol)
// @param k : key for pivot (symbol or symbollist)
// @param p : partition for pivot (symbol)
// @param v : value for pivot (symbol)
//
.cmn.pivot:{[t;k;p;v]
    // cast to symbol for partition
    tab: 0!(parse "{update `$string ",(string p)," from x}")[t];
    // partition keys
    P: raze {"`",string x} each asc distinct tab p;
    // string
    k: (),k;
    k: "," sv (string k),'":",/:(string k);
    k: string k;
    p: string p;
    v: string v;
    // make pivot
    // {[t] exec P#(p!v) by k:k from t}
    q: (raze/)"{[t] exec ((),",P,")#(",p,"!",v,") by ",k," from t}";
    pvt: (parse q) tab;
    : pvt
  };



//
// pivotを作成します。
//
// ※バグがあり動きません。。。
//
// @param t : table
// @param k : key for pivot
// @param p : partition for pivot
// @param v : value for pivot
//
/.cmn.piv:{[t;k;p;v]
/    // enlist
/    k:(),k;
/    p:(),p;
/    v:(),v;
/    // このfとgは汎用的ではないので、動かない
/    f:{[v;P]`$raze each string raze P[;0],'/:v,/:\:P[;1]};
/    g:{[k;P;c]k,(raze/)flip flip each 5 cut'10 cut raze reverse 10 cut asc c};
/
/    G:group flip k!(t:.Q.v t)k;
/    F:group flip p!t p;
/    r: 0!key[G]!flip(C:f[v]P:flip value flip key F)!raze
/    {[i;j;k;x;y]
/        // null list for x (X 0N=null of X)
/        a:count[x]#x 0N;
/        // replace indeices y from x
/        a[y]:x y;
/        // false list for x
/        b:count[x]#0b;
/        b[y]:1b;
/        c:a i;
/        c[k]:first'[a[j]@'where'[b j]];
/        c
/    } [I[;0]; I J; J:where 1<>count'[I:value G] ]/:\: [t v; value F];
/    count[k]!g[k;P;C] xcols r
/  };

