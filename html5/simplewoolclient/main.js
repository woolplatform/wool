// valid parameters:
// config - display config
// code - source code of dialogue
// editable - if defined, make display config editable

var LOCALSTORAGEPREFIX="wool_js_";

var NARRATOR = "Narrator";

// get urlParams and config ------------------------------------------------
var urlParams = Utils.getUrlParameters();


//localStorage.removeItem("simplewoolclient_config");

// XXX duplicated in wooleditor index.html
var colorInfo = [
	{ rgb: '#EEEEEE', name: "white",},
	{ rgb: '#6EA5E0', name: "blue",},
	{ rgb: '#9EDE74', name: "green",},
	{ rgb: '#FFE374', name: "yellow",},
	{ rgb: '#F7A666', name: "orange",},
	{ rgb: '#C47862', name: "brown",},
	{ rgb: '#97E1E9', name: "cyan",},
	{ rgb: '#FF7080', name: "red",},
	{ rgb: '#D070FF', name: "purple",},
	{ rgb: '#AAAAAA', name: "grey",},
];

function makeRange(max) {
	var ret = [];
	for (var i=0; i<=max; i++) ret.push(i);
	return ret;
}

// reset storage
//localStorage.setItem("simplewoolclient_config",null);

function saveConfig() {
	config.avatar = avatarRes.serialize();
	config.background = backgroundRes.serialize();
	console.log(config);
	localStorage.setItem("simplewoolclient_config",JSON.stringify(config));
}

// load config --------------------------------------------------------

var config = {
	// avatar and background ID are:
	// - number for preset backgrounds 
	// - URL for custom bg
	"avatar": null,
	"background": null,
}
//	"avatars": {
//		"current": 0, // current index in all
//		"all": [], // all known avatar IDs and URLs
//		"mapping": { }, // name -> avatar ID (number) or URL
//	}
//	"background": 12, // current background
//	"backgrounds:" {}, // list of urls 
//	"backgroundmapping": {}, // colorID -> background ID
//};

if (urlParams.config) {
	config = JSON.parse(myDecodeURIComponent(urlParams.config));
} else {
	var c = localStorage.getItem("simplewoolclient_config");
	if (c) config = JSON.parse(c);
}

var avatarRes = new ResourceUI(config.avatar,makeRange(99),0);
var backgroundRes = new ResourceUI(config.background,makeRange(29),12);

var isNarrator=false;

saveConfig();

// init ---------------------------------------------------------------

// 3rd party code should load these defs and set the language
// load language defs directly because we can't load from file without http.
//_i18n.loadJSON({
//	"": { "language": "nl", "plural-forms": "nplurals=2; plural=(n != 1);", },
//	"You:": "Jij:",
//	"Your response:": "Je antwoord:",
//	"Continue": "Ga verder",
//	"Send": "Verstuur",
//});
//_i18n.setLocale("en");


// Obtain source code and lang defs. Possible sources:
// - URL parameter "code" (langDefs cannot be obtained from url yet)
// - windowparams.sourceCode and windowparams.langDefs


var sourceCode = null;

var langDefs = null;

// In the node environment, the ID represents the absolute path. If loaded
// through directServerLoadNodeDialogue, this ensures the initial dialogue is
// taken from the source code rather than the file.
var dialogueID = "dialogue";

if (langDefs) {
	// autodetect json or po
	try {
		JSON.parse(langDefs);
		_i18n.ReadJSONFromString(langDefs,"nl");
	} catch (e) {
		// assuming language is nl
		_i18n.readPODef(langDefs);
	}
	_i18n.setLocale("nl");
}

//sourceCode=localStorage.getItem(LOCALSTORAGEPREFIX+"buffer");

if (urlParams.code) sourceCode = myDecodeURIComponent(urlParams.code);

try {
	var windowparams = JSON.parse(window.name);
	if (!sourceCode) sourceCode = windowparams.sourceCode;
	if (!langDefs) langDefs = windowparams.langDefs;
} catch (e) {
	console.log(e);
}

// if URL parameters "woolRoot" and "filepath" are supplied, we assume node.js
// is available, and we can load new dialogues.  Note that the current
// dialogue is not loaded from file because it may not have been saved.
// sourceCode is used instead of file contents every time we jump back to the
// original dialogue.  If sourceCode is not available, then load it from file
// anyway.
if (urlParams.woolRoot && urlParams.filepath) {
	dialogueID = urlParams.filepath;
	if (!detectNodeJS()) {
		alert("Fatal: File path specified but node.js not available");
	} else {
		directServer.setRootDir(urlParams.woolRoot);
		if (!sourceCode) {
			sourceCode = directServerLoadNodeDialogue(dialogueID,
				urlParams.woolRoot+"/"+urlParams.filepath);
		} else {
			directServerLoadDialogue(dialogueID,sourceCode);
		}
	}
} else {
	directServerLoadDialogue(dialogueID,sourceCode);
}


var errorsFound=false;
var errors = {};
for (var i=0; i<directServer.dialogues[dialogueID].nodes.length; i++) {
	var node = directServer.dialogues[dialogueID].nodes[i];
	if (node.errors.length) {
		errorsFound=true;
		errors[node.param.title] = [];
		for (var j=0; j<node.errors.length; j++) {
			var err = node.errors[j];
			errors[node.param.title].push(
				(err.line!==null ? "Line "+(err.line+1)+":" : "")
				+err.msg+" ("+err.level+")");
		}
	}
}
if (errorsFound) {
	showingInDebug="errors";
	var dbox = document.getElementById("debugarea");
	dbox.parentNode.style.display="block";
	dbox.innerHTML = "Errors were found while parsing.\n"
		+"A list of nodes with errors found in each node follows.\n\n"
		+JSON.stringify(errors,null,2);
}

// edit functions ---------------------------------------------------------

// also updates controls
function updateAvatar() {
	document.getElementById("avatarcontrols").style.display =
		isNarrator ? "none" : "block";
	var elem = document.getElementById("agent_object");
	if (isNarrator) {
		elem.innerHTML="";
	} else if (avatarRes.currentIsNumber()) {
		GenAvataaar(elem,avatarRes.getCurrent());
	} else {
		elem.innerHTML="<img class='avatarimage' src='"+avatarRes.getCurrent()+"'></img>";
	}
}
function updateBackground() {
	if (backgroundRes.currentIsNumber()) {
		document.body.className = "pattern"+backgroundRes.getCurrent();
		document.getElementById("background").style.display="none";
	} else {
		document.getElementById("background").src=backgroundRes.getCurrent();
		document.getElementById("background").style.display="block";
	}
}

function incCurrentAvatar(amount) {
	if (isNarrator) return;
	avatarRes.inc(amount);
	document.getElementById("resourceId").innerHTML = 
		"Avatar: "+avatarRes.getCurrentHumanReadable();
	saveConfig();
	updateAvatar();
}

function incBackground(amount) {
	backgroundRes.inc(amount);
	document.getElementById("resourceId").innerHTML = 
		"Background: "+backgroundRes.getCurrentHumanReadable();
	saveConfig();
	updateBackground();
}


function addAvatarURL() {
	if (isNarrator) return;
	var url = prompt("Avatar URL:","");
	if (url) {
		avatarRes.add(url);
		saveConfig();
		updateAvatar();
	}
}

function addBackgroundURL() {
	var url = prompt("Background URL:","");
	if (url) {
		backgroundRes.add(url);
		saveConfig();
		updateBackground();
	}
}

function deleteAvatar() {
	if (avatarRes.currentIsNumber()) return;
	if (!confirm("Delete resource "+avatarRes.getCurrentHumanReadable()+"?"))
		return;
	avatarRes.removeCurrent();
	document.getElementById("resourceId").innerHTML = "";
	updateAvatar();
}

function deleteBackground() {
	if (backgroundRes.currentIsNumber()) return;
	if(!confirm("Delete resource "+backgroundRes.getCurrentHumanReadable()+"?"))
		return;
	backgroundRes.removeCurrent();
	document.getElementById("resourceId").innerHTML = "";
	updateBackground();
}

var showingInDebug=null;

// modified encoder for shorter urls
function myEncodeURIComponent(string) {
	return encodeURIComponent(kissc.compress(string));
}

function myDecodeURIComponent(string) {
	var ret = kissc.decompress(string); 
	if (ret === false) return string;
	return ret;
}

function showUrl() {
	showingInDebug="URL";
	var dbox = document.getElementById("debugarea");
	dbox.parentNode.style.display="block";

	dbox.innerHTML = window.location.protocol + "//" +
		window.location.host + window.location.pathname
		+ "?config=" + myEncodeURIComponent(JSON.stringify(config))
		+ "&code=" + myEncodeURIComponent(sourceCode);
}

function showVariables() {
	showingInDebug="variables";
	var dbox = document.getElementById("debugarea");
	dbox.parentNode.style.display="block";
	dbox.innerHTML = JSON.stringify(directServer.currentnodectx.vars,null,2);
}

if (urlParams.editable) {
	var edithtml = "";
	if (urlParams.editurl) {
		edithtml =
			"<br><div class='commandbutton'>"
			+"<a href='"+urlParams.editurl+"'>Back to editor</a>"
			+"</div>"
			+"<div class='commandbutton'>"
			+"<a id='editnodeurl' href='"+urlParams.editurl+"'>Edit Node</a>"
			+"</div>";
	}
	document.body.innerHTML +=
		"<div class='editbox'>"
		+"<div id='avatarcontrols'>Avatar: "
		+"<div class='incrementbutton' onclick='incCurrentAvatar(1);'>+</div>"
		+"<div class='incrementbutton' onclick='incCurrentAvatar(-1);'>-</div>"
		+"<div class='incrementbutton' onclick='deleteAvatar();'>&#x1F5D1;</div>"
		+"<div class='commandbutton' onclick='addAvatarURL();'>URL</div>"
		+"</div>"
		+"<div id='backgroundcontrols'>Background: "
		+"<div class='incrementbutton' onclick='incBackground(1);'>+</div>"
		+"<div class='incrementbutton' onclick='incBackground(-1);'>-</div>"
		+"<div class='incrementbutton' onclick='deleteBackground();'>&#x1F5D1;</div>"
		+"<div class='commandbutton' onclick='addBackgroundURL();'>URL</div>"
		+"</div>"
		+"<div class='commandbutton' onclick='showUrl();'>Get URL</div>"
		+"<div class='commandbutton' onclick='showVariables();'>Variables</div>"
		+edithtml
		+"</div>\n"
		+"<div class='currentresourcebox' id='resourceId'></div>\n"
		+"<div class='currentdialoguebox' id='dialogueId'></div>\n";
}


// helper functions ---------------------------------------------------

// index=null indicates autoforward reply (no index)
function handleBasicReply(id,index) {
	handleDirectServerCall("GET", null,null,
		"progress_dialogue/?replyId="+encodeURIComponent(id)
		+(index!==null ? "&replyIndex="+encodeURIComponent(index) : ""),
		updateNodeUI);
}

function handleTextReply(id,index) {
	var value = document.getElementById(id+"_content").value;
	handleDirectServerCall("GET", null,null,
		"progress_dialogue/?replyId="+id+"&replyIndex="+index
		+"&textInput="+encodeURIComponent(value),
			updateNodeUI);
}

function handleNumericReply(id,index,min,max) {
	// TODO
	handleTextReply(id,index);
}

function startDialogue() {
	handleDirectServerCall("GET", null,null,
		"start_dialogue/?dialogueId="+encodeURIComponent(dialogueID),
			updateNodeUI);
}


function updateNodeUI(node) {
	var dialogueidview = document.getElementById("dialogueId");
	if (dialogueidview) dialogueidview.innerText =
		directServer.currentdialogueId;
	var editnodelink = document.getElementById("editnodeurl");
	if (editnodelink) {
		if (!directServer.jumpedToNewDialogue) {
			editnodelink.href =
				urlParams.editurl+"?editnode="+encodeURIComponent(node.id);
		} else {
			editnodelink.className = "linkdisabled";
		}
	}
	if (showingInDebug=="variables") showVariables();
	if (directServer.errors.length > 0) {
		alert(JSON.stringify(directServer.errors));
		directServer.errors = [];
	}
	isNarrator = node.speaker == NARRATOR;
	if (node.speaker && node.speaker!="UNKNOWN" && !isNarrator ) {
		avatarRes.switchEntity(node.speaker,Math.floor(Math.random()*99));
	}
	updateAvatar();
	backgroundRes.switchEntity(node.colorID, backgroundRes.getCurrent());
	updateBackground();
	saveConfig();
	var replyelem = document.getElementById("user-reply");
	if (node.id=="End") {
		document.getElementById("agent-name").innerHTML = "";
		document.getElementById("agent-statement").innerHTML = 
			__("End of dialogue");
	} else {
		if (isNarrator) {
			document.getElementById("agent-name").style.display="none";
		} else {
			document.getElementById("agent-name").style.display="block";
			document.getElementById("agent-name").innerHTML = node.speaker + ":";
		}
		// XXX we use trim to make the translation phrase generated by the
		// server. Move this to the server eventually.
		document.getElementById("agent-statement").innerHTML = __(node.statement.trim());
	}
	if (node.id=="End" || node.replies.length==0) {
		replyelem.className = "reply-box-auto-forward";
		replyelem.innerHTML =
			"<button class='reply-auto-forward' onclick='startDialogue()'>"
				+__("Restart")+"</button>"
		return;
	}
	if (isNarrator) {
		replyelem.innerHTML = "";
	} else {
		replyelem.innerHTML = 
			"<p id='user-name'>"+__("You:")+"</p>"
			+"<p id='user-instruction'>"+__("Your response:")+"</p>";
	}
	for (var i=0; i<node.replies.length; i++) {
		var reply = node.replies[i];
		if (reply.replyType=="BASIC") {
			replyelem.className = "reply-box";
			replyelem.innerHTML +=
				"<button class='reply' onclick='handleBasicReply(\""
					+reply.replyId+"\",\""+i+"\")'>"
					+__(reply.statement)+"</button>"
		} else if (reply.replyType=="AUTOFORWARD") {
			replyelem.className = "reply-box-auto-forward";
			replyelem.innerHTML +=
				"<button class='reply-auto-forward' onclick='handleBasicReply(\""
					+reply.replyId+"\",null)'>"+__("Continue")+"</button>"
		} else if (reply.replyType=="TEXTINPUT"
		||         reply.replyType=="NUMERICINPUT") {
			// XXX parsing before-after as a whole by splitting and joining
			// should eventually be moved to the server
			var translated=false;
			if (reply.beforeStatement && reply.afterStatement) {
				// If both before and after, translate as a whole
				var statement = __1(reply.beforeStatement
					+ "%1" + reply.afterStatement, "%1");
				var seg = statement.split("%1");
				if (seg.length!=2) {
					console.log("Translation error: mismatched %1 in "
						+"translation '"+statement+"'.");
				} else {
					reply.beforeStatement = seg[0];
					reply.afterStatement = seg[1];
					translated=true;
				}
			}
			if (!translated) {
				// Otherwise, translate only the individual statement
				if (reply.beforeStatement)
					reply.beforeStatement = __(reply.beforeStatement);
				if (reply.afterStatement)
					reply.afterStatement = __(reply.afterStatement);
			}
			var replyclass = "reply";
			var submitclass = "submit";
			if (reply.afterStatement) {
				replyclass += " reply_with_after_statement";
				submitclass += " submit_with_after_statement";
			}
			var func = reply.replyType=="TEXTINPUT"
				? "handleTextReply" : "handleNumericReply";
			replyelem.className = "reply-box";
			replyelem.innerHTML += '<div class="before_statement">' 
				+ __(reply.beforeStatement) + '</div>';
			replyelem.innerHTML +=
				"<div class='responseblock'>"
				+"<input type='text' placeholder='Type here' value='' type='text' name='test' class='"+replyclass+"'"
				+" id='"+reply.replyId+"_content'"
				+"></input>"
				+"<input class='"+submitclass+"'"
				+" onclick='"+func+"(\""
					+reply.replyId+"\",\""+i+"\")' value='"+__("Send")+"'></input>"
				+"</div>\n"; /* responseblock */
			if (reply.afterStatement) {
				replyelem.innerHTML += '<div class="after_statement">' 
					+ __(reply.afterStatement) + '</div>';
			}

		}
	}
	localStorage.setItem("simplewoolclient_dialoguestate",directServer.getState());
}

// start dialogue --------------------------------------------------

var prevstate = localStorage.getItem("simplewoolclient_dialoguestate");
if (urlParams.docontinue) {
	if (prevstate) {
		directServer.setState(prevstate);
		updateNodeUI(directServer.getNode());
	}
} else {
	startDialogue();
}


