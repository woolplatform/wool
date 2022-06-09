// Handles persistence through a simple AJAX api.
//
// Based on storing one JSONable data structure per game. Key is typically
// a one-time token that is passed to the game at startup.
//
// Stores in both localStorage and to a remote server through an AJAX
// call.  Each item has a time stamp. When retrieving, the item with the
// highest timestamp is taken as the most recent.
//
// Remote API:
// GET [url]&token=TOKEN -> body contains json
// POST [url]&token=TOKEN <- body contains json
//
//

var Utils = {};


Utils._ajax = function(method,url,body,success,failure) {
	// code for IE7+, Firefox, Chrome, Opera, Safari
	var xhr = new XMLHttpRequest();
	xhr.onreadystatechange = function() {
		if (xhr.readyState == XMLHttpRequest.DONE ) {
			if (xhr.status == 200) {
				success(xhr.responseText);
			} else {
				failure(""+xhr.status);
			}
		}
	}
	xhr.overrideMimeType('text/plain');
	xhr.open(method, url, true);
	xhr.send(body);
}

Utils.getUrlParameters = function() {
	return window.location.search.substring(1).split("&")
	.reduce(function(res, i) {
		if (i.split("=")[0]) {
			res[i.split("=")[0]] = decodeURIComponent(i.split("=")[1]);
		}
		return res;
	}, {});
}

// helper function
// from: http://stackoverflow.com/questions/19491336/get-url-parameter-jquery
Utils.getUrlParameter = function (sParam) {
    var sPageURL = decodeURIComponent(window.location.search.substring(1)),
        sURLVariables = sPageURL.split('&'),
        sParameterName,
        i;

    for (i = 0; i < sURLVariables.length; i++) {
        sParameterName = sURLVariables[i].split('=');

        if (sParameterName[0] === sParam) {
            return sParameterName[1] === undefined ? true : sParameterName[1];
        }
    }
	return false;
};

// helper function
// http://stackoverflow.com/questions/105034/create-guid-uuid-in-javascript
Utils.generateUUID = function() {
    var d = new Date().getTime();
    if (typeof performance !== 'undefined' && typeof performance.now === 'function'){
        d += performance.now(); //use high-precision timer if available
    }
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        var r = (d + Math.random() * 16) % 16 | 0;
        d = Math.floor(d / 16);
        return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
    });
}

Utils.parseJWT = function(token) {
    var base64Url = token.split('.')[1];
    var base64 = decodeURIComponent(atob(base64Url).split('').map(function(c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));

    return JSON.parse(base64);
};

// copy all fields in a require() return value to global scope
//Utils.globalizeExportFields = function(exportName) {
//	for (var key in eval(exportName)) {
//		(1,eval)("var "+key+" = "+exportName+"."+key+";");
//	}
//}

// import module with require, copy all fields in the return value to
// global scope
Utils.requireGlobal = function(path) {
	global["mod_export"] = require(path);
	for (var key in mod_export) {
		(1,eval)("global['"+key+"'] = global['mod_export']."+key+";");
	}
}

if (typeof exports !== 'undefined') {
	// node.js require()
	exports.Utils = Utils;
}

