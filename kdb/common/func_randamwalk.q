//
// Functions for Randam Walk
//

/ \d .rw

// ---------------------------------------------------------
//    Randam Walk
// ---------------------------------------------------------

// default values (USDJPY 1 day)
.rw.number: 100000;
.rw.start:  100.0;
.rw.ticksize: 0.0005;

//
// create RW list
//
.rw.createRandamWalk: {[number; start; ticksize]
    rw: rand each (number-1)#0b;
    rw: 0,ticksize*-1+2*`int$rw;
    : start + number msum rw
  };
// .rw.createRandamWalk[10000; 100; 0.001]


//
// create RW table
//
.rw.createRandamWalkTable: {[number; start; ticksize]
    index: til number;
    rw: .rw.createRandamWalk[number; start; ticksize];
    : flip `index`rw!(index;rw)
  };
// .rw.createRandamWalkTable[20000; 100; 0.001]





// ---------------------------------------------------------
/ \d .
