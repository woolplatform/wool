
var nodefs = require('fs');


// we need to do this to load browser style js files into node. We should
// rewrite library files to be useable for both node and browser.

var mod = require("../lib/utils.js");
var Utils = mod.Utils;

var mod = require("../lib/debug.js");
var dbg = mod.dbg;


//var filedata = nodefs.readFileSync("lib/debug.js",'utf8');
//eval(filedata);

//var filedata = nodefs.readFileSync("lib/utils.js",'utf8');
//eval(filedata);

var gettext = require("../lib/i18n/gettext.js");
i18n = gettext.i18n;

//var i18n_includes = require("../lib/i18n/i18n.js");
//Utils.globalizeExportFields("i18n_includes");
Utils.requireGlobal("../lib/i18n/i18n.js");

//for (var key in i18n_includes) {
//	eval("var "+key+" = i18n_includes."+key);
//}

//filedata = nodefs.readFileSync("lib/i18n/i18n.js",'utf8');
//eval(filedata);

Utils.requireGlobal("../lib/filesystem.js");
//filedata = nodefs.readFileSync("lib/filesystem.js",'utf8');
//eval(filedata);

Utils.requireGlobal("../woolserver-js/woolserver-js.js");
//filedata = nodefs.readFileSync("woolserver-js/woolserver-js.js",'utf8');
//eval(filedata);

Utils.requireGlobal("../woolserver-js/woolserver-js-api.js");
//filedata = nodefs.readFileSync("woolserver-js/woolserver-js-api.js",'utf8');
//eval(filedata);


dbg.setLevel("notice");
dbg.clearAssertFails();

function progressDialogue(node,recurse,errorsFound) {
	dbg.notice("TESTING dialogue: "+directServer.currentdialogueId
		+",node: "+directServer.currentnode.param.title);
	// inspect the result
	// links should be array of {line:<num>,node:<string>}
	for (var n=0; n<directServer.currentnode.links.length; n++) {
		var link = directServer.currentnode.links[n];
		dbg.assert(Number.isInteger(link.line),"Noninteger link.line");
		dbg.assert(typeof link.node == "string", "Non-string link.node");
		dbg.assert(link.node, "Empty link.node");
	}
	dbg.assert(directServer.currentnode.param.title, "Empty node title");
	// take another dialogue step?
	recurse--;
	if (recurse < 0) return;
	// follow first reply
	if (node.replies.length > 0) {
		var i = 0;
		// for testing we currently assume this is a synchronous call
		handleDirectServerCall("GET", null,null,
			"progress_dialogue/?replyId="+encodeURIComponent(node.replies[i].replyId)+"&replyIndex="+i+"&textInput=textinput",
			function(node) { progressDialogue(node,recurse,errorsFound); }
		);
	}
}


test("Example dialogues", done => {

	fs = getPlatformFileSystem();

	var woolTestRoot = "../test-dialogues/";

	var errorsFound=false;

	fs.readdirtree(woolTestRoot, function(err,res) {
		if (err) {
			console.log("Error reading dialogues.");
			done(err);
			return;
		} else {
			for (var i=0; i<res.length; i++) {
				if (res[i][0] != "FILE") continue;
				var path = res[i][2];
				if (!path.endsWith(".wool")) continue;
				// reset vars
				initDirectServer();
				directServer.setRootDir(woolTestRoot);
				// load dialogue
				var source = directServerLoadNodeDialogue(path,woolTestRoot+path);
				// inspect result
				var allnodes = directServer.dialogues[path].nodes;
				for (var n=0; n<allnodes.length; n++) {
					var node = allnodes[n];
					if (node.errors.length > 0) {
						done(node.errors);
						return;
					}
				}
				// try dialogue functions
				// for testing we currently assume this is a synchronous call
				handleDirectServerCall("GET", null,null,
					"start_dialogue/?keepVars=true&dialogueId="
					+encodeURIComponent(path),
					function(node) { progressDialogue(node,2,errorsFound); }
				);
			}
		}
		if (errorsFound) {
			done("Errors found");
		} else {
			done(dbg.getAssertFails());
		}
	});
})



