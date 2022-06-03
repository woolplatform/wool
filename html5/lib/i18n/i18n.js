// wrapper around gettext (https://github.com/guillaumepotier/gettext.js/)

// i18n additional class defs ------------------------------------------------

// create "instance"
var _i18n = i18n({
	ctxt_delimiter: "|",
});


// Usage:
// call ReadPODefFromFile, ReadJSONFromString, ReadPODef, or _i18n.loadJSON
// to load defs.
// Then use _i18n.setLocale("locale"); to set locale after reading defs.


// locale - store under this locale, use setLocale to use.
// callback (optional) - function to call when read finished or failed
//                       Signature: callback(error),
//                       error is null or error string
// charmapping (optional) - function(string) -> string that maps chars


// if set to true, normalize source strings by replacing all whitespace by a
// single space
_i18n.enableNormalization = function(enable) {
	_i18n._normalize = enable;
}

_i18n.normalizeString = function(string) {
	if (!_i18n._normalize) return string;
	var ret = string.replace(/\s+/g," ");
	console.log("#"+ret);
	return ret;
}

_i18n.normalizeStrings = function(json) {
	var ret = {};
	for (var key in json) {
		if (!json.hasOwnProperty(key)) continue;
		ret[_i18n.normalizeString(key)] = json[key];
		//ret[_i18n.normalizeString(key)] = _i18n.normalizeString(json[key]);
	}
	return ret;
}

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
	ret = _i18n.normalizeStrings(ret);
	// make exception for "" key. Here, lines are split into elements
	var options = ret[""].split('\n');
	ret[""] = {
		"plural-forms": "nplurals=2; plural=(n != 1);",
	};
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

_i18n.clearDictionary = function(language) {
	var ret = {}
	ret[""] = {
		"plural-forms": "nplurals=2; plural=(n != 1);",
		"language": language,
	};
	this.loadJSON(ret);
}

_i18n.ReadPODefFromFile =
function(filename,locale,callback,charmapping) {
	Utils._ajax("GET",filename,"",
		function(data) {
			var podef = _i18n.readPODef(data,charmapping);
			console.log("PO language file read.");
			if (callback) callback(null);
		}, function(data) {
			console.log("Error reading PO file: "+data);
			if (callback) callback(data);
		}
	);
}

// "context": { "key" : "value" }  =>  "context|key": "value"
_i18n.flattenKeyValueJSON = function(json0) {
	var json = {};
	for (var key in json0) {
		var value = json0[key];
		if (typeof value == "object") {
			for (var subkey in value) {
				if (!value.hasOwnProperty(subkey)) continue;
				var subvalue = value[subkey];
				if (Array.isArray(subvalue)) {
					console.log("Ignored subcontext: "+subkey);
				} else {
					json[key+"|"+subkey] = subvalue;
				}
			}
		} else {
			json[key] = value;
		}
	}
	return json;
}

// json supported by gettext.js is similar to the "key-value" json in
// poeditor. Context is encoded as a block with the context as key. Language +
// pluralforms are also not included in the defs.
// charmapping not used (yet)
_i18n.ReadJSONFromString =
function(data,locale,pluralForms,charmapping) {
	if (!locale) locale="nl";
	if (!pluralForms) pluralForms="nplurals=2; plural=(n!=1);";
	var json0 = JSON.parse(data);
	// convert key-value json into what i18n expects
	var json = _i18n.flattenKeyValueJSON(json0);
	json = _i18n.normalizeStrings(json);
	json[""] = {
		"language": locale,
		"plural-forms": pluralForms,
	};
	this.loadJSON(json);
	console.log("JSON language file read.");
}

_i18n._getGettextContext = function(string) {
	var res = string.split("|");
	if (res.length == 1) return {
		ctxt: null,
		str: string
	};
	var ret = {
		ctxt: res.shift(),
		str: res.join(""),
	};
	return ret;
}

// language definition (the content of the "" key) is supplied as a separate parameter
_i18n.loadJSONSeparate = function(jsonData,langdef, domain) {
    jsonData[""] = langdef;
    _i18n.loadJSON(jsonData,domain);
}


// version that return unnormalized string when no translation found
_i18n.unnormalizedGettext = function(context, string, arg1, arg2) {
	var normString = _i18n.normalizeString(string);
 	var ret = _i18n.dcnpgettext(null, context, normString, null, null, arg1, arg2);
	// if result is equal, we assume translation is not found -> return
	// unnormalized original
	if (ret == normString) return string;
	return ret;
}

// shortcuts ------------------------------------------------------------

function __(string) {
	var ctxtstr = _i18n._getGettextContext(string);
 	//return dcnpgettext(domain, msgctxt, msgid, msgid_plural, n /* , extra */);
 	//return _i18n.dcnpgettext(null, ctxtstr.ctxt, ctxtstr.str, null, null);
	return _i18n.unnormalizedGettext(ctxtstr.ctxt, ctxtstr.str);
}

// 1-parameter version
function __1(string,arg1) {
	var ctxtstr = _i18n._getGettextContext(string);
	//return _i18n.strfmt(string,arg1);
 	//return _i18n.dcnpgettext(null, ctxtstr.ctxt, ctxtstr.str, null, null, arg1);
	return _i18n.unnormalizedGettext(ctxtstr.ctxt, ctxtstr.str, arg1);
}

// 2-parameter version
function __2(string,arg1,arg2) {
	var ctxtstr = _i18n._getGettextContext(string);
	//return _i18n.strfmt(string,arg1,arg2);
 	//return _i18n.dcnpgettext(null, ctxtstr.ctxt, ctxtstr.str, null, null, arg1, arg2);
	return _i18n.unnormalizedGettext(ctxtstr.ctxt, ctxtstr.str, arg1, arg2);
}


// similar to dngettext
// pass $context=null for no context
function n__(context,strings,stringp,n) {
 	return _i18n.dcnpgettext(null, context, strings, stringp, n);
}

// similar to dngettext, 1 stands for 1 sprintf parameter
function n__1(context,strings,stringp,n,arg1) {
	//return _i18n.strfmt(strings,arg1);
 	return _i18n.dcnpgettext(null, context, strings, stringp, n, arg1);
}

// similar to dngettext, 2 stands for 2 sprintf parameters
function n__2(context,strings,stringp,n,arg1,arg2) {
	//return _i18n.strfmt(strings,arg1,arg2);
 	return _i18n.dcnpgettext(null, context, strings, stringp, n, arg1, arg2);
}

if (typeof exports !== 'undefined') {
	// node.js require()
	exports._i18n = _i18n;
	exports.__ = __;
	exports.__1 = __1;
	exports.__2 = __2;
	exports.n__ = n__;
	exports.n__1 = n__1;
	exports.n__2 = n__2;
}

