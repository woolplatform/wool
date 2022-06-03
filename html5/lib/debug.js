// handles debug messages and asserts.
// When an assert fails, output is printed and the failure is recorded in
// assertFails. Test functions can check for failed assertions using
// get/clearAssertFails().

var dbg = {};

dbg.level = "debug"; // debug/notice/warning/error

// array of objects {msg,trace}
dbg.assertFails = [];

dbg.MAXASSERTFAILS = 20;

dbg.getStackTrace = function() {
	var tracestr = new Error().stack;
	var trace = tracestr.split(/\r?\n/);
	return trace;
}

// level is "debug"/"notice"/"warning"/"error"
dbg.print = function(level,msg) {
	var print=false;
	if (level=="debug" && dbg.level=="debug") {
		print = true;
	} else if (level=="notice" 
	&& (dbg.level=="debug" || dbg.level == "notice")) {
		print = true;
	} else if (level=="warning" && dbg.level!="error") {
		print = true;
	} else if (level=="error") {
		print = true;
	}
	if (print) {
		console.log(level+" "+dbg.getStackTrace()[4].trim()+":");
		console.log(msg);
	}
}

dbg.debug = function(msg) {
	dbg.print("debug",msg);
}

dbg.notice = function(msg) {
	dbg.print("notice",msg);
}

dbg.warn = function(msg) {
	dbg.print("warning",msg);
}

dbg.error = function(msg) {
	dbg.print("error",msg);
}

dbg.setLevel = function(level) {
	dbg.level = level;
}

// produces flag and error when false
dbg.assert = function(result,msg) {
	if (!result) {
		dbg.print("warning",msg);
		if (dbg.assertFails.length < dbg.MAXASSERTFAILS) {
			dbg.assertFails.push({
				msg: msg,
				trace: dbg.getStackTrace()[4].trim(),
			});
		}
	}
}

// false when there are no fails, otherwise array of fails
dbg.getAssertFails = function() {
	if (dbg.assertFails.length == 0) return false;
	return dbg.assertFails;
}

dbg.clearAssertFails = function() {
	dbg.assertFails = [];
}


if (typeof exports !== 'undefined') {
	// node.js require()
	exports.dbg = dbg;
}

