// wrapper around gettext (https://github.com/guillaumepotier/gettext.js/)

// i18n additional class defs ------------------------------------------------

// create "instance"
var _i18n = window.i18n({
	ctxt_delimiter: "|",
});


// Usage:
// call ReadPODefFromFile or ReadPODef or _i18n.loadJSON to load defs
// Then use _i18n.setLocale("locale"); to set locale after reading defs.


// locale - store under this locale, use setLocale to use.
// callback (optional) - function to call when read finished or failed
//                       Signature: callback(error),
//                       error is null or error string
// charmapping (optional) - function(string) -> string that maps chars

// string follows gettext (PO) format
// charmapping - optional function(String) for mapping special chars in strings
_i18n.readPODef = function(string,charmapping) {
	var ret = {
		"": {}
	};
	function WritePODef(def) {
		var joinsep = "";
		if (def.msgid.length==0 || def.msgstr0.length==0) {
			return;
			//throw "Definition is missing msgid or msgstr";
		}
		var msgid = def.msgid.join(joinsep);
		if (def.msgctxt.length > 0) {
			var msgctxt = def.msgctxt.join(joinsep);
			if (msgctxt!="") {
				msgid =  msgctxt + "|" + msgid;
			}
		}
		var msgstrs = [];
		// plural form of source language is not represented in definition
		//if (def.msgid_plural.length > 0) {
		//	msgstrs.push(def.msgid_plural.join(joinsep));
		//}
		for (var i=0; i<6; i++) {
			if (def["msgstr"+i].length > 0) {
				msgstrs.push(def["msgstr"+i].join(joinsep));
			}
		}
		// - convert '\' n' to "\n"
		// - map chars
		msgid = msgid.replace(/[\\]n/g, "\n");
		for (var i=0; i<msgstrs.length; i++) {
			msgstrs[i] = msgstrs[i].replace(/[\\]n/g, "\n");
			if (charmapping) msgstrs[i] = charmapping(msgstrs[i]);
		}
		if (msgstrs.length == 1) {
			ret[msgid] = msgstrs[0];
		} else {
			ret[msgid] = msgstrs;
		}
	}
	function GetEmptyPODef() {
		return {
			"msgctxt": [],// array of lines
			"msgid": [],// array of lines
			"msgid_plural": [],// array of lines
			"msgstr0" : [], // array of lines
			"msgstr1" : [], // array of lines
			"msgstr2" : [], // array of lines
			"msgstr3" : [], // array of lines
			"msgstr4" : [], // array of lines
			"msgstr5" : [], // array of lines
		};
	}
	var lines = string.replace(/\r\n/g, "\r").replace(/\n/g, "\r").split(/\r/);
	var msgelem = GetEmptyPODef();
	var curelem = null; // key into msgelem
	for (var i=0; i<lines.length; i++) {
		var line = lines[i].trim();
		// skip empty lines
		if (line == "") continue;
		if (line.lastIndexOf("#", 0) === 0) {
			// 1st char is "#" -> comment
			continue;
		} else if (line.lastIndexOf('"', 0) === 0) {
			// can be "<string>" or "<key>: <value>\n"
			//var matches = /^"([a-zA-Z0-9_-]+):\s*([^"]+)[\]n"$/.exec(line);
			//if (matches) {
			//	ret[matches[1]] = matches[2];
			//} else {
			//	console.log("Cannot parse key-value definition: '"+line+"'");
			//}
			// expect closing quote to be the last character. 
			// This handles escaped quotes.
			var matches = /^"(.*)"$/.exec(line);
			if (curelem===null) {
				console.log("Unexpected string line: '"+line+"'");
			} else if (matches) {
				var str = matches[1];
				str = str.replace(/\\"/g,'"');
				msgelem[curelem].push(str);
			} else {
				console.log("Cannot parse string line: '"+line+"'");
			}
		} else if (line.lastIndexOf("msgstr[", 0) === 0) {
			// msgstr[<number>] "<string>"
			var matches = /^msgstr\[([0-9]+)\]\s+"(.*)"$/.exec(line);
			if (matches) {
				curelem = "msgstr"+matches[1];
				var str = matches[2];
				str = str.replace(/\\"/g,'"');
				msgelem[curelem].push(str);
			} else {
				console.log("Cannot parse msgstr[] line: '"+line+"'");
			}
		} else if (line.lastIndexOf("msgctxt", 0) === 0
		||         line.lastIndexOf("msgid", 0) === 0
		||         line.lastIndexOf("msgstr", 0) === 0
		||         line.lastIndexOf("msgid_plural", 0) === 0) {
			// msgid "<string>", msgstr "<string>",  etc
			var matches = /^([a-z_]+)\s+"(.*)"$/.exec(line);
			if (matches) {
				curelem = matches[1];
				// first, check if start of new message
				//        -> store and clear old message
				if ( (curelem == "msgid" || curelem == "msgctxt")
				&& msgelem["msgid"].length > 0) {
					WritePODef(msgelem);
					msgelem = GetEmptyPODef();
				}
				if (curelem == "msgstr") curelem = "msgstr0";
				var str = matches[2];
				str = str.replace(/\\"/g,'"');
				msgelem[curelem].push(str);
			} else {
				console.log("Cannot parse msgid/msgid_plural/msgstr line: '"
					+line+"'");
			}
		} else {
			console.log("Unknown line type: '"+line+"'");
		}
	}
	// write last definition
	WritePODef(msgelem);
	// make exception for "" key. Here, lines are split into elements
	var options = ret[""].split('\n');
	ret[""] = {};
	for (var i=0; i<options.length; i++) {
		var opt = options[i];
		if (opt=="") continue;
		var matches = /^([a-zA-Z0-9_-]+)[:]\s*(.+)$/.exec(opt);
		if (matches) {
			// gettext.js wants lower case
			ret[""][matches[1].toLowerCase()] = matches[2];
		} else {
			console.log("Cannot parse key-value pair: '"+opt+"'");
		}
	}
	console.log(ret);
	this.loadJSON(ret);
}

_i18n.ReadPODefFromFile =
function(filename,locale,callback,charmapping) {
	Utils._ajax("GET",filename,"",
		function(data) {
			var podef = ReadPODef(data,charmapping);
			console.log("PO file read.");
			if (callback) callback(null);
		}, function(data) {
			console.log("Error reading PO file: "+data);
			if (callback) callback(data);
		}
	);
}

_i18n._getGettextContext = function(string) {
	var res = string.split("|");
	if (res.length == 1) return { ctxt: null, str: string };
	return {
		ctxt: res.shift(),
		str: res.join(""),
	};
}

// shortcuts ------------------------------------------------------------

function __(string) {
	var ctxtstr = _i18n._getGettextContext(string);
	//if (/jutters/.exec(ctxtstr.str)) console.log(ctxtstr);
	//$res = explode("|",$string);
	//return portal_translateOptionalContext($text,$context);
 	//return dcnpgettext(domain, msgctxt, msgid, msgid_plural, n /* , extra */);
 	return _i18n.dcnpgettext(null, ctxtstr.ctxt, ctxtstr.str, null, null);
}

// 1-parameter version
function __1(string,arg1) {
	var ctxtstr = _i18n._getGettextContext(string);
	//$res = explode("|",$string);
	//return sprintf(portal_translateOptionalContext($text,$context),$arg1);
	//return _i18n.strfmt(string,arg1);
 	return _i18n.dcnpgettext(null, ctxtstr.ctxt, ctxtstr.str, null, null, arg1);
}

// 2-parameter version
function __2(string,arg1,arg2) {
	var ctxtstr = _i18n._getGettextContext(string);
	//$res = explode("|",$string);
	//return sprintf(portal_translateOptionalContext($text,$context),$arg1,$arg2);
	//return _i18n.strfmt(string,arg1,arg2);
 	return _i18n.dcnpgettext(null, ctxtstr.ctxt, ctxtstr.str, null, null,
		arg1, arg2);
}


// similar to dngettext
// pass $context=null for no context
function n__(context,strings,stringp,n) {
	//return portal_translateOptionalContext_plural($strings,$stringp,$n,$context);
 	return _i18n.dcnpgettext(null, context, strings, stringp, n);
}

// similar to dngettext, 1 stands for 1 sprintf parameter
function n__1(context,strings,stringp,n,arg1) {
	//return sprintf(portal_translateOptionalContext_plural(
	//	$strings,$stringp,$n,$context),  $arg1);
	//return _i18n.strfmt(strings,arg1);
 	return _i18n.dcnpgettext(null, context, strings, stringp, n, arg1);
}

// similar to dngettext, 2 stands for 2 sprintf parameters
function n__2(context,strings,stringp,n,arg1,arg2) {
	//return _i18n.strfmt(strings,arg1,arg2);
 	return _i18n.dcnpgettext(null, context, strings, stringp, n, arg1, arg2);
}


