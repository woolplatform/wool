
var directServer = {};

function initDirectServer() {
	directServer.rootDir = null; // (node.js) null = unknown
	directServer.currentLanguage = "en"; // (node.js)
	directServer.defaultLanguage = "en"; // (node.js)
	directServer.jumpedToNewDialogue = false;
	directServer.availableDialogues = [];
	directServer.dialogues = {};
	// function to call if dialogues are loaded
	directServer.nrDialoguesLoaded = 0;
	directServer.allDataLoaded = false;
	// runtime errors
	directServer.errors = [];
	directServer.ui_settings = { language: "nl_NL" };
	// state
	directServer.currentdialogueId = null;
	directServer.currentdialogue = null;
	directServer.currentnode = null;
	directServer.currentnodectx = null;
    directServer.pendingActions = [];
}

initDirectServer();

// type is fatal, error, warning, notice
directServer.logError = function (obj) {
	directServer.errors.push(obj);
}

directServer.setRootDir = function(rootDir) {
	directServer.rootDir = rootDir;
}

directServer.setLanguage = function(defaultLang,currentLang) {
	if (!defaultLang) defaultLang = "en";
	if (!currentLang) currentLang = "en";
	directServer.defaultLanguage = defaultLang;
	directServer.currentLanguage = currentLang;
}

directServer.substituteVars = function(ctx,text) {
    var keys = [];
	for (var key in ctx.vars) {
		if (!ctx.vars.hasOwnProperty(key)) continue;
        keys.push(key);
    }
    // sort keys, longest first to prevent substitution of a variable that is a prefix of another
    keys.sort(function(a,b) {
        return b.length - a.length;
    })
    for (var keyidx in keys) {
        var key = keys[keyidx];
		var val = ctx.vars[key];
		text = text.split("$"+key).join(val);
	}
	return text;
}

directServer.getState = function() {
	return JSON.stringify({
        pendingActions: directServer.pendingActions,
		currentdialogueId: directServer.currentdialogueId,
		currentnodeid: directServer.currentnode ? directServer.currentnode.param.title : null,
		currentnodectxvars: directServer.currentnodectx ? directServer.currentnodectx.vars : null,
	});
}

directServer.clearPendingActions = function() {
    directServer.pendingActions = [];
}

directServer.setState = function(json) {
    console.log("CTX:"+json);
	var state = JSON.parse(json);
	directServer.currentdialogueId = state.currentdialogueId;
	directServer.currentnodectx = new WoolNodeContext(state.currentnodectxvars);
    console.log(directServer.currentnodectx);
    if (state.currentdialogueId) {
        // if current dialogue is defined, currentnodeid is assumed to be defined also
        directServer.currentdialogue = directServer.dialogues[directServer.currentdialogueId];
        var idx = directServer.findNodeIdx(state.currentnodeid);
        directServer.currentnode = directServer.currentdialogue.nodes[idx];
        directServer.currentnode.func(directServer.currentnodectx);
    }
}

// returns id of new dialogue, or null if no jump
directServer.checkDialogueJump = function() {
    var jumped = directServer.jumpedToNewDialogue;
    directServer.jumpedToNewDialogue = false;
    if (jumped) {
        return directServer.currentdialogueId;
    } else {
        return null;
    }
}

directServer.findNodeIdx = function(id) {
	for (var i=0; i<directServer.currentdialogue.nodes.length; i++) {
		var n = directServer.currentdialogue.nodes[i];
		if (n.param.title==id) return i;
	}
	return null;
}


/* convert current node and node ctx into node spec for the client, following:
String id; // node id
List<String> speakersInDialogue; // array of agent names
String speaker; // name of speaking agent
String statement; //string (concat of all text statements + '\n')
MultimediaMessage multimedia;
	{ multimediaType [one of IMAGE,VIDEO,TIMER], resourceName (image),
	  timerDuration (timer) }
List<ReplyMessage> replies:
	//array of { replyType, replyId, statement, endsDialogue[bool],
	//beforeStatement, afterStatement, min, max } (class ReplyMessage)
	//replyType one of: AUTOFORWARD, BASIC, TEXTINPUT, NUMERICINPUT
*/
directServer.getNode = function() {
	var dia = directServer.currentdialogue;
	var node = directServer.currentnode;
	var ctx = directServer.currentnodectx;
	//console.log(dia);
	//console.log(node);
	//console.log(ctx);
	var speaker = "UNKNOWN";
	if (ctx.speakers.length > 0) {
		speaker = ctx.speakers[0];
	}
	if (node.param["speaker"]) speaker = node.param["speaker"];
	var statement = "";
	for (var i=0; i<ctx.text.length; i++) {
		var texti = ctx.text[i].trim();
		if (__) {
			var origtexti = texti;
			texti = __(speaker+"|"+origtexti);
			// not found? try without context
			if (texti == origtexti) {
				texti = __(origtexti);
			}
		}
		statement += texti + "\n";
	}
	var ret = {
		id: node.param.title,
		colorID: node.param.colorID,
		// TODO
		speakersInDialogue: ctx.speakers,
		speaker: speaker,
		statement: directServer.substituteVars(ctx,statement),
		multimedia: null,
		replies: []
	};
	if (ctx.afreply) {
		ret.replies.push({
			replyType: "AUTOFORWARD",
			replyId: ctx.afreply,
			endsDialogue: false,
		});
		
	}
	if (ctx.media) {
		var type = ctx.media.type.toUpperCase();
		ret.multimedia = {
			multimediaType : type,
		};
		if (type=="IMAGE") {
			ret.multimedia.resourceName = ctx.media.param;
		}
		if (type=="TIMER") {
			ret.multimedia.timerDuration = ctx.media.param;
		}
	}
	for (var i=0; i<ctx.choices.length; i++) {
		var choice = ctx.choices[i];
		// substitute variables in choice.text
		var text = directServer.substituteVars(ctx,choice.text);
		ret.replies.push({
			replyType: "BASIC",
			replyId: choice.optid,
			statement: text,
			endsDialogue: false,
			beforeStatement: "",
			afterStatement: "",
			action: choice.action,
		});
	}
	if (ctx.inputreply) {
		var ir = ctx.inputreply;
		if (ir.inputtype == "numeric") {
			ret.replies.push({
				replyType: "NUMERICINPUT",
				replyId: ir.optid,
				endsDialogue: false,
				beforeStatement: directServer.substituteVars(ctx,ir.beforeText),
				afterStatement: directServer.substituteVars(ctx,ir.afterText),
				action: ir.action,
				min: ir.min,
				max: ir.max,
			});
		} else { // Text
			ret.replies.push({
				replyType: "TEXTINPUT",
				replyId: ir.optid,
				endsDialogue: false,
				beforeStatement: directServer.substituteVars(ctx,ir.beforeText),
				afterStatement: directServer.substituteVars(ctx,ir.afterText),
			});
		}
	}
	return ret;
}




function handleDirectServerCall(type,dataType,headers,callbackURL,
functionOnSuccess) {
	var urldir = callbackURL.split(/[\/]/);
	//console.log("Call received:");
	var paramraw = urldir[urldir.length-1].split(/[&?]/);
	urldir[urldir.length-1] = paramraw.shift();
	var param = {};
	for (var i=0; i<paramraw.length; i++) {
		var keyval = paramraw[i].split("=");
		param[decodeURIComponent(keyval[0])] = decodeURIComponent(keyval[1]);
	}
	// 1st dir is function, 1nd and subsequent dirs are params
	for (var i=1; i<urldir.length; i++) {
		param["_"+i] = decodeURIComponent(urldir[i]);
	}
	func = "_directServer_" + urldir[0].split("-").join("_");
	var fn = window[func];
	if(typeof fn === 'function') {
		console.log("Calling DirectServer function:");
		console.log(func);
		console.log(param);
		var ret = fn(param);
		if (functionOnSuccess) functionOnSuccess(ret);
	} else {
		console.log("ERROR: Undefined DirectServer function:");
		console.log(func);
		console.log(param);
	}
	return;
}

function _directServer_auth(par) {
	if (par._1 == "login") {
		console.log("Login: "+par.email+" "+par.password);
		return "dummytoken";
	} else {
		console.log("DirectServer auth: unknown function "+par._1);
	}
}

function _directServer_get_available_dialogues(par) {
	var user = par._1;
	console.log("DirectServer getAvailableDialogues "+par._1);
	return directServer.availableDialogues;
}


function _directServer_set_ui_settings(par) {
	var user = par._1;
	directServer.ui_settings = {language: par.language };
	return null;
}

function _directServer_get_ui_settings(par) {
	var user = par._1;
	return directServer.ui_settings;
}


// par: {
//     dialogueId - ID of dialogue
//     startNodeId - ID of start node (optional, default is "Start")
//     keepVars - keep any defined vars (optional, default is false)
// }
function _directServer_start_dialogue(par) {
	directServer.currentdialogueId = par.dialogueId;
	if (!par.startNodeId) par.startNodeId = "Start";
	directServer.currentdialogue = directServer.dialogues[par.dialogueId];
    console.log("Starting dialogue: "+par.dialogueId)
	// start node is node named "Start", otherwise first node
	var node = directServer.currentdialogue.nodes[0];
	var idx = directServer.findNodeIdx(par.startNodeId);
	if (idx!==null) node = directServer.currentdialogue.nodes[idx];
	directServer.currentnode = node;
    console.log("Start node: "+idx)
	// pass kb variables here
	var vars = {};
	if (par.keepVars && directServer.currentnodectx) {
		vars = directServer.currentnodectx.vars;
	}
	directServer.currentnodectx = new WoolNodeContext(vars);
	directServer.currentnode.func(directServer.currentnodectx);
	return directServer.getNode();
}

function _directServer_progress_dialogue(par) {
	// do actions first
	var replyId = par.replyId;
	var newDialogueId = null; // defined when jumping to different dialogue
	var newTranslationPath = null;
	var pathSep = replyId.lastIndexOf(".");
	if (pathSep >= 0) {
        newDialogueId = directServerGetPath(replyId.substring(0,pathSep),
			directServer.defaultLanguage);
        newTranslationPath = directServerGetPath(replyId.substring(0,pathSep),
			directServer.currentLanguage);
  		replyId = replyId.substring(pathSep+1);
	}
	var replydef = typeof par.replyIndex != 'undefined'
		? directServer.currentnodectx.choices[par.replyIndex]
		: null;
	/*var replydef = null;
	for (var i=0; i<directServer.currentnodectx.choices.length; i++) {
		var rd = directServer.currentnodectx.choices[i];
		if (rd.optid == replyId) replydef = rd;
	}*/
	if (replydef) {
		if (replydef.action) replydef.action(directServer.currentnodectx);
	}
	if (par.textInput) {
		var ctx = directServer.currentnodectx;
		if (ctx.inputreply) {
			ctx.vars[ctx.inputreply.inputvar] = par.textInput;
		}
	}
    // log actions that were collected in the context
    if (directServer.currentnodectx.pendingActions) {
        directServer.pendingActions = directServer.pendingActions.concat(
                directServer.currentnodectx.pendingActions)
        //console.log("Added to pendingActions; " + JSON.stringify(directServer.pendingActions))
    }
	// jump to new node
	if (newDialogueId) {
		directServer.jumpedToNewDialogue = true;
		// load translations before loading dialogue
		directServerLoadNodeTranslation(
			directServer.rootDir + newTranslationPath + ".json");
   		directServerLoadNodeDialogue(newDialogueId,
			directServer.rootDir + newDialogueId + ".wool");
     console.log("startDialogue "+newDialogueId)
		return _directServer_start_dialogue({
			dialogueId: newDialogueId,
			startNodeId: replyId,
			keepVars: true,
		});
	} else {
		directServer.currentnode = directServer.currentdialogue.nodeMap[
			replyId.trim().toLowerCase() ];
		directServer.currentnodectx = new WoolNodeContext(
			directServer.currentnodectx.vars
		);
		if (typeof directServer.currentnode.func == 'function' ) {
			try {
				directServer.currentnode.func(directServer.currentnodectx);
			} catch (e) {
				directServer.logError("Node "+directServer.currentnode.param["title"]+": runtime script error: "+e);
			}
		} else {
			directServer.logError("Node "+directServer.currentnode.param["title"]+": script compile error.");
		}
		return directServer.getNode();
	}
}

