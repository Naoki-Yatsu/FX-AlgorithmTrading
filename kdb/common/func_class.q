//
// Functions for Class
//

// ---------------------------------------------------------
//    Functions
// ---------------------------------------------------------


//
// クラスのベースとなるdictionaryを作成します
//
.class.base: {d:()!();d[`]:(::);d};


//
// クラスの定義で、フィールドに値をセットします
// クラスの定義中で使います
//
.class.setField: {[this; ftype; filed; fvalue]
    if [not (type fvalue) in ftype[filed];
        : "Type Error : Filed=",(string filed),", Expected=",(string ftype[filed]),"h, Actual=",(string type filed),"h"];
    this[filed]: fvalue;
    : this;
  };






// ---------------------------------------------------------
//    Sample
// ---------------------------------------------------------


// create instance
/ instance: testClass[111i; "BBB"]

// call method
/ instance.testFunc2[instance]

// access filed
/ instance[`f1]


//
// class with filed type restriction
//
testClass: {[f1; f2]
    class: ()!();
    class[`]: (::);
    ftype: class;

    // class name
    class[`name]: `TEST;

    // filed type
    ftype[`f1]: -6h;
    ftype[`f2]: -10h,10h;
    class[`ftype]: ftype;

    // set field
    class: .class.setField[class; ftype; `f1; f1];
    if [10h=type class; :class];

    class: .class.setField[class; ftype; `f2; f2];
    if [10h=type class; :class];

    // constructor
    class[`new]: {[this;f1]
        this[`f1]: f1;
        :this
      };

    // method
    class[`testFunc]: {x,"111"};

    class[`testFunc2]: {[this] this[`f1]};

    class[`setF1]: {[this; f1] this[`f1]:f1; this};

    // return
    : class
  };



//
// class with filed type restriction
//
testClass: {[f1; f2]
    class: .class.base[];
    ftype: .class.base[];

    // class name
    class[`name]: `TEST;

    // filed type
    ftype[`f1]: -6h;
    ftype[`f2]: -10h,10h;
    class[`ftype]: ftype;

    // set field
    class: .class.setField[class; ftype; `f1; f1];
    if [10h=type class; :class];

    class: .class.setField[class; ftype; `f2; f2];
    if [10h=type class; :class];

    // constructor
    class[`new]: {[this;f1]
        this[`f1]: f1;
        :this
      };

    //
    // method
    //
    class[`testFunc]: {x,"111"};

    class[`testFunc2]: {[this] this[`f1]};

    class[`setF1]: {[this; f1] this[`f1]:f1; this};

    // return
    : class;
  };


