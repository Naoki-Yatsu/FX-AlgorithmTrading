//
// Functions Disk Utils
//


// ---------------------------------------------------------
//    Save Splayed
// ---------------------------------------------------------

// maintain a dictionary of the db partitions which have been written to by the loader
partitions:()!();

// set an attribute on a specified column, return success status
setattribute:{[partition; attrcol; attribute] .[{@[x;y;z];1b};(partition;attrcol;attribute);0b]};

//
// save function
//
savaDataSplayed: {[data; dbdir; stablename; timestampCol]

    // enumerate the table - best to do this once
    out "Enumerating";
    data: .Q.en[dbdir; data];
    
    // write out data to each date partition
    {[data; dbdir; stablename; timestampCol; pdate]
        // sub-select the data to write
        / towrite: select from data where date=`date$marketDateTime;
        towrite: select from data where pdate=`date$timestamp;
        / towrite: ?[data; enlist (=; ($;enlist `date; timestampCol); pdate); 0b;()]
        
        // generate the write path
        writepath:.Q.par[dbdir; pdate; `$(stablename,"/")];
        out "Writing ",(string count towrite)," rows to ",string writepath;
        
        // splay the table - use an error trap
        .[upsert; (writepath;towrite); {out"ERROR - failed to save table: ",x}];
        
        // make sure the written path is in the partition dictionary
        partitions[writepath]: pdate;
    
    }[data; dbdir; stablename; timestampCol; ] each asc distinct `date$(0!data)[timestampCol]
  };

//
// set the partition attribute (sort the table if required)
//
sortandsetp:{[partition; sortcols]

    out "Sorting and setting `p# attribute in partition ",string partition;

    // attempt to apply an attribute.
    // the attribute should be set on the first of the sort cols
    parted: setattribute[partition; first sortcols; `p#];

    // if it fails, resort the table and set the attribute
    if[not parted;
        out"Sorting table";
        sorted:.[{x xasc y;1b};(sortcols;partition);{out"ERROR - failed to sort table: ",x; 0b}];
        // check if the table has been sorted
        if[sorted;
            // try to set the attribute again after the sort
            parted:setattribute[partition;first sortcols;`p#]]];

    // print the status when done
    $[parted; out"`p# attribute set successfully"; out"ERROR - failed to set attribute"];
  };

executeSaveSplayed: {[data; dbdir; stablename; timestampCol; sortcols]
    // Create splayed table
    savaDataSplayed[data; dbdir; stablename; timestampCol];

    // Sort
    sortandsetp[;sortcols] each key partitions;
  };
/ sample 
/ executeSaveSplayed[HistoricalPL; `:historicalpl; "HistoricalPL"; `sym`sourcetime]






//
//-- EXECUTE -------------
//

timestampCol: `timestamp

aaa[timecol]
aaa:
select from FX1min where date within 2013.01.01 2013.01.31

select from aaa where aaa[timecol] < 2013.01.03D01:00:00.000000000 

parse "select from aaa where (`datetime$timestamp) < 2013.01.03T01:00:00"

?[`aaa;enlist (<;`timestamp;2013.01.03D01:00:00.000000000);0b;()]

?[`aaa; enlist (<;($;enlist `datetime;`timestamp);2013.01.03T01:00:00.000); 0b;()]


parse "exec distinct `date$timestamp from aaa"


(?;`aaa;();();enlist (?:;($;enlist `date;`timestamp)))

?[aaa;();();enlist (?:;($;enlist `date;`timestamp))]

asc distinct `date$(0!aaa)[`timestamp]


selectSample: {[data; dbdir; tablename; timestampCol]
    ?[data; enlist (=; ($;enlist `date; timestampCol); 2013.01.04); 0b;()]
  };

selectSample[aaa; `AAA; `aaa; `timestamp ]





    select from data where date=`date$marketDateTime;    

// ---------------------------------------------------------
//    Commmon
// ---------------------------------------------------------



