
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

function saveConfig() {
	config.avatar = avatarRes.serialize();
	config.background = backgroundRes.serialize();
	config.statementFormat = statementFormat;
	config.hasBackButton = hasBackButton;
	dbg.debug(config);
	localStorage.setItem("simplewoolclient_config",JSON.stringify(config));
}

function initEmptyConfig() {
	config = {
		// avatar and background ID are:
		// - number for preset backgrounds 
		// - URL for custom bg
		"avatar": null,
		"background": null,
		"statementFormat": "html",
		"hasBackButton": true,
		// speaker name -> dialogue name -> image filename
		"avatarmapping": null,
	};
	//	"avatars": {
	//		"current": 0, // current index in all
	//		"all": [], // all known avatar IDs and URLs
	//		"mapping": { }, // name -> avatar ID (number) or URL
	//	}
	//	"background": 12, // current background
	//	"backgrounds:" {}, // list of urls 
	//	"backgroundmapping": {}, // colorID -> background ID
	//};
}

function resetConfig() {
	localStorage.removeItem("simplewoolclient_config");
	initEmptyConfig();
}

// load config --------------------------------------------------------

if (urlParams.resetconfig) {
	resetConfig();
	alert("Config reset!");
}

var config;
initEmptyConfig();


if (urlParams.config) {
	config = JSON.parse(myDecodeURIComponent(urlParams.config));
} else {
	var c = localStorage.getItem("simplewoolclient_config");
	if (c) config = JSON.parse(c);
}

var avatarRes = new ResourceUI(config.avatar,makeRange(99),0);
var backgroundRes = new ResourceUI(config.background,makeRange(29),12);

var statementFormat = config.statementFormat ? config.statementFormat : "html";

var hasBackButton = 
	config.hasBackButton !== null && typeof config.hasBackButton != "undefined" 
		? config.hasBackButton 
		: true;
if (urlParams.nobackbutton) hasBackButton = false;

var isNarrator=false;

saveConfig();

// init ---------------------------------------------------------------


// Obtain source code and lang defs. Possible sources:
// - resources: if dialoguepath is defined
// - URL parameter "code" (langDefs cannot be obtained from url yet)
// - windowparams.sourceCode and windowparams.langDefs

var defaultLanguageExplicitlyDefined = false;
var defaultLanguage = urlParams.defaultlanguage;
if (!defaultLanguage) {
	defaultLanguage = "nl";
} else {
	defaultLanguageExplicitlyDefined = true;
}

var dialogueInitVars = {};

if (urlParams.vars) {
	var varsjson = JSON.parse(urlParams.vars);
	if (varsjson) {
		for (var varsjsonname in varsjson) {
			dialogueInitVars[varsjsonname] = varsjson[varsjsonname];
		}
	}
}

var sourceCode = null;

var langDefs = null;

// The ID represents the absolute path. Basedir is used by 
// directServerLoadNodeDialogue when loading a relative path.
var dialogueID = "/"+defaultLanguage+"/dialogue";
if (urlParams.dialoguepath) {
	dialogueID = urlParams.dialoguepath;
}



//sourceCode=localStorage.getItem(LOCALSTORAGEPREFIX+"buffer");

if (urlParams.code) sourceCode = myDecodeURIComponent(urlParams.code);

langDefs = localStorage.getItem(LOCALSTORAGEPREFIX+"langDefs");

try {
	var windowparams = JSON.parse(window.name);
	if (!sourceCode) sourceCode = windowparams.sourceCode;
	if (!langDefs) langDefs = windowparams.langDefs;
} catch (e) {
	dbg.error(e);
}

_i18n.enableNormalization(true);

// NOTE: "nl" is always used for the current language
_i18n.clearDictionary("nl");

// As a quick hack, we provide predefined translations for the standard texts,
// in case resources are provided or the default language is explicitly
// defined.
// These will be erased when translations are defined, so you will have to
// define them there.
if ((urlParams.resources || defaultLanguageExplicitlyDefined) && defaultLanguage == "nl") {
	if (!langDefs) {
		langDefs = JSON.stringify({
			"": { "language": "nl", "plural-forms": "nplurals=2; plural=(n != 1);", },
			"You:": "Jij:",
			"Your response:": "Je antwoord:",
			"Continue": "Ga verder",
			"Go back": "Ga terug",
			"Send": "Verstuur",
		});
		directServer.setDefaultTranslation(langDefs);
	}
	//_i18n.setLocale("nl");
}



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

// configure marked --------------------------------------------------------

var renderer = new marked.Renderer();

renderer.link = function(href, title, text) {
    var link = marked.Renderer.prototype.link.apply(this, arguments);
    return link.replace("<a","<a target='_blank'");
};

marked.setOptions({
    renderer: renderer
});


// edit functions ---------------------------------------------------------

// also updates controls if present
function updateAvatar() {
	var controls = document.getElementById("avatarcontrols");
	if (controls) controls.style.display = isNarrator ? "none" : "block";
	var elem = document.getElementById("agent_object");
	var agentid = avatarRes.getCurrentEntity();
	var dialogueid = directServer.stripDialoguePath(directServer.currentdialogueId);
	var nodeid = directServer.currentnode.param.title;
	//dbg.debug("AV "+agentid+"/"+dialogueid+"/"+nodeid);
	if (isNarrator) {
		elem.innerHTML="";
	} else if (config.avatarmapping
	&&         config.avatarmapping[agentid]
	&&         config.avatarmapping[agentid][dialogueid]
	&&         config.avatarmapping[agentid][dialogueid][nodeid]) {
		elem.innerHTML = "<img class='avatarimage' src='images/"
			+ config.avatarmapping[agentid][dialogueid][nodeid]
			+ "'></img>";
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

function setStatementFormat(format,updateUI) {
	statementFormat = format;
	// select button in editbox
	var allbuttons = document.getElementsByClassName("formatcommand");
	for (var i in allbuttons) {
		allbuttons[i].className = "commandbutton formatcommand";
	}
	var button = document.getElementById("format_"+format);
	button.classList.add("selected");
	if (updateUI) {
		// also saves config
		updateNodeUI(directServer.getNode());
	} else {
		saveConfig();
	}
}

// true, false, or undefined = toggle
function setBackButton(backButton,updateUI) {
	if (typeof backButton == "undefined" || backButton === null) {
		backButton = !hasBackButton;
	}
	hasBackButton = backButton;
	var elem = document.getElementById("backbuttoncheckbox");
	elem.checked = hasBackButton ? "checked" : null;
	if (updateUI) {
		// also saves config
		updateNodeUI(directServer.getNode());
	} else {
		saveConfig();
	}
}

var showingInDebug=null;

// modified encoder for shorter urls
function myEncodeURIComponent(string,notzipped) {
	if (!notzipped) {
		string = kissc.compress(string);
	}
	return encodeURIComponent(string);
}

function myDecodeURIComponent(string) {
	var ret = kissc.decompress(string); 
	if (ret === false) return string;
	return ret;
}

function showUrl(zipped) {
	showingInDebug="URL";
	var dbox = document.getElementById("debugarea");
	dbox.parentNode.style.display="block";

	dbox.innerHTML = window.location.protocol + "//" +
		window.location.host + window.location.pathname
		+ "?config=" + myEncodeURIComponent(JSON.stringify(config),!zipped)
		+ "&code=" + myEncodeURIComponent(sourceCode,!zipped);
}

function showVariables() {
	showingInDebug="variables";
	var dbox = document.getElementById("debugarea");
	dbox.parentNode.style.display="block";
	dbox.innerHTML = JSON.stringify(directServer.getVars(),null,2);
}

function resetConfigButton() {
	if (confirm("Reset all avatar and background configuration, and restart dialogue?")) {
		resetConfig();
		window.location.reload();
	}
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
		+"<div id='formatcontrols'>Format: "
		+"<div id='format_markdown' class='commandbutton formatcommand' onclick='setStatementFormat(\"markdown\",true);'>Markdown</div>"
		+"<div id='format_html'  class='commandbutton formatcommand' onclick='setStatementFormat(\"html\",true);'>HTML</div>"
		+"</div>"
		+"<div id='backbuttoncontrols'>Back button: "
		+"<input type='checkbox' id='backbuttoncheckbox' onclick='setBackButton(null,true);'/>\n"
		+"</div>"
		+"<div class='commandbutton' onclick='showUrl(false);'>Get URL</div>"
		+"<div class='commandbutton' onclick='showUrl(true);'>Get URL zipped</div>"
		+"<br/><div class='commandbutton' onclick='showVariables();'>Variables</div>"
		+"<div class='commandbutton' onclick='resetConfigButton();'>Reset config</div>"
		+edithtml
		+"</div>\n"
		+"<div class='currentresourcebox' id='resourceId'></div>\n"
		+"<div class='currentdialoguebox' id='dialogueId'></div>\n";

	setStatementFormat(statementFormat,false); // updates editbox
	setBackButton(hasBackButton,false);
}


// helper functions ---------------------------------------------------


function setInputFilter(textbox, inputFilter) {
	["input", "keydown", "keyup", "mousedown", "mouseup", "select", "contextmenu", "drop"].forEach(function(event) {
		textbox.addEventListener(event, function() {
			if (inputFilter(this.value)) {
				this.oldValue = this.value;
				this.oldSelectionStart = this.selectionStart;
				this.oldSelectionEnd = this.selectionEnd;
			} else if (this.hasOwnProperty("oldValue")) {
				this.value = this.oldValue;
				this.setSelectionRange(this.oldSelectionStart, this.oldSelectionEnd);
			} else {
				this.value = "";
			}
		});
	});
}

function setNumericInputFilter(textbox) {
	setInputFilter(textbox,function(value) {
		return (/^[.,0-9-]*$/.test(value));
	});
}

// index=null indicates autoforward reply (no index)
function handleBasicReply(id,index) {
	handleDirectServerCall("GET", null,null,
		"progress_dialogue/?replyId="+encodeURIComponent(id)
		+(index!==null ? "&replyIndex="+encodeURIComponent(index) : ""),
		updateNodeUI);
}

function handleReply(id,index,value) {
	var value = document.getElementById(id+"_content").value;
	handleDirectServerCall("GET", null,null,
		"progress_dialogue/?replyId="+id+"&replyIndex="+index
		+"&textInput="+encodeURIComponent(value),
			updateNodeUI);
}

function handleTextReply(id,index) {
	var value = document.getElementById(id+"_content").value;
	handleReply(id,index,value);
}

function handleNumericReply(id,index,min,max) {
	var value = document.getElementById(id+"_content").value;
	if (isNaN(value)) {
		alert("Please input numeric value.");
		return;
	}
	if (value < min || value > max) {
		alert("Please enter value between "+min+" and "+max+".");
		return;
	}
	handleReply(id,index,value);
}

function handleBackButton() {
	handleDirectServerCall("GET", null,null,
		"go_back/",
		updateNodeUI);
}


function startDialogue() {
	// Hack: preload initial vars into current nodecontext, so they are
	// present for determining the first action.
	// TODO pass these vars via start_dialogue
	directServer.currentnodectx = new WoolNodeContext(dialogueInitVars);
	handleDirectServerCall("GET", null,null,
		"start_dialogue/?keepVars=true&dialogueId="
			+encodeURIComponent(dialogueID),
		updateNodeUI);
	//for (var name in dialogueInitVars) {
	//	directServer.setVar(name, dialogueInitVars[name]);
	//}
}


function updateNodeUI(node) {
	// Check if we need to redirect after dialogue end first.
	// If so, do not update UI
	if (node.id=="End" || node.replies.length==0) {
		if (urlParams.redirecturl) {
			window.location.href = urlParams.redirecturl
				+ "vars=" + encodeURIComponent(JSON.stringify(
					directServer.getVars()));
			return;
		}
	}
	// show actions
	if (directServer.currentnodectx) {
		var actions = directServer.currentnodectx.pendingActions;
		if (actions) {
			for (var i=0; i<actions.length; i++) {
				var action = actions[i];
				if (action.type == "link") {
					if (action.url) {
						window.open(action.url,"_blank");
					} else {
						alert("Action type 'link' misses required parameter 'url'");
					}
				} else {
					alert("Action called: "+JSON.stringify(actions[i]));
				}
			}
		}
	}
	directServer.clearPendingActions();
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
		document.getElementById("agent-statement").innerHTML =
			statementFormat == "html" 
				? node.statement
				: marked.parse(node.statement);
	}
	// Non-redirect case of dialogue end
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
	var normalReply=false;
	for (var i=0; i<node.replies.length; i++) {
		var reply = node.replies[i];
		if (reply.replyType=="BASIC") {
			replyelem.className = "reply-box";
			replyelem.innerHTML +=
				"<button class='reply' onclick='handleBasicReply(\""
					+reply.replyId+"\",\""+i+"\")'>"
					+reply.statement+"</button>"
			normalReply=true;
		} else if (reply.replyType=="AUTOFORWARD") {
			replyelem.className = "reply-box-auto-forward";
			replyelem.innerHTML +=
				"<button class='reply-auto-forward' onclick='handleBasicReply(\""
					+reply.replyId+"\",null)'>"+__("Continue")+"</button>"
		} else if (reply.replyType=="TEXTINPUT"
		||         reply.replyType=="NUMERICINPUT") {
			// The whole section is now translated by the server, so this is
			// no longer necessary.
			/*var translated=false;
			if (reply.beforeStatement && reply.afterStatement) {
				// If both before and after, translate as a whole
				var statement = __1(reply.beforeStatement
					+ "%1" + reply.afterStatement, "%1");
				var seg = statement.split("%1");
				if (seg.length!=2) {
					dbg.warn("Translation error: mismatched %1 in "
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
			}*/
			var replyclass = "reply";
			var submitclass = "submit";
			if (reply.afterStatement) {
				replyclass += " reply_with_after_statement";
				submitclass += " submit_with_after_statement";
			}
			var func = reply.replyType=="TEXTINPUT"
				? "handleTextReply" : "handleNumericReply";
			var minmax =
				(reply.replyType=="NUMERICINPUT" && reply.min && reply.max)
					? ","+reply.min+","+reply.max 
					: "";
			replyelem.className = "reply-box";
			replyelem.innerHTML += '<div class="before_statement">' 
				+ reply.beforeStatement + '</div>';
			replyelem.innerHTML +=
				"<div class='responseblock'>"
				+"<input type='text' placeholder='Type here' value='' type='text' name='test' class='"+replyclass+"'"
				+" id='"+reply.replyId+"_content'"
				+"></input>"
				+"<input class='"+submitclass+"'"
				+" onclick='"+func+"(\""
					+reply.replyId+"\",\""+i+"\""+minmax+")' value='"+__("Send")+"'></input>"
				+"</div>\n"; /* responseblock */
			if (reply.afterStatement) {
				replyelem.innerHTML += '<div class="after_statement">' 
					+ reply.afterStatement + '</div>';
			}
			// this gets executed too early, so we use the img onload trick
			//document.write(
			//	"<script>"
			//	+"setNumericInputFilter(document.getElementById('"
			//		+ reply.replyId + "_content') );"
			//	+"</script>"
			//);
			if (reply.replyType=="NUMERICINPUT") {
				replyelem.innerHTML += '<img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8+OFDPQAI/QNSrn40LQAAAABJRU5ErkJggg==" onload="'
					+"setNumericInputFilter(document.getElementById('"
					+ reply.replyId + "_content') );"
					+'" />';
			}
			normalReply=true;
		}
	}
	var backelem = document.getElementById("user-back");
	backelem.innerHTML = "";
	if (directServer.canGoBack() && hasBackButton) {
		if (normalReply) {
			backelem = document.getElementById("user-reply");
			backelem.innerHTML += "<div style='clear:both;' />\n";
		}
		backelem.innerHTML +=
			"<button class='reply-auto-forward' onclick='handleBackButton()'>"
				+ __("Go back") + "</button>";
	}
	localStorage.setItem("simplewoolclient_dialoguestate",directServer.getState());
}

// load resources, start dialogue ------------------------------------------


var nrResourcesLoaded=0;
if (urlParams.resources) {
	var res = JSON.parse(urlParams.resources);
	if (res) {
		for (var i=0; i<res.length; i++) {
			var resname = res[i];
			BrowserFileSystem.cacheFile(
				RESOURCEBASEDIR + "/" + resname + getNocachePar(),
				resname,
				function() {
					nrResourcesLoaded++;
					dbg.debug("Loaded resource " + nrResourcesLoaded +
						"/" + res.length);
					if (nrResourcesLoaded == res.length) {
						startOrResumeDialogue();
					}
				},
				function(errormsg) {
					dbg.debug("Error reading resource: '"+errormsg+"'");
				}
			);
		}
	}
} else {
	startOrResumeDialogue();
}

var preloadedImages = [];

function startOrResumeDialogue() {
	// handle avatar mapping file if present
	var avatarmapping=getPlatformFileSystem().readFileSync("/avatarmapping.csv");
	if (avatarmapping) {
		config.avatarmapping = [];
		var lines = avatarmapping.split(/\r?\n/);
		for (var i=0; i<lines.length; i++) {
			if (lines[i] == "") continue;
			var fields = lines[i].split(/,/);
			if (fields.length == 0) continue;
			if (fields.length != 2) {
				dbg.debug("avatarmapping.csv: unexpected line '"+lines[i]+"'");
				continue;
			}
			var comp = fields[0].split(/[.]/);
			if (comp.length != 3) {
				dbg.debug("avatarmapping.csv: unexpected directive '"+fields[0]+"'");
				continue;
			}
			if (!config.avatarmapping[comp[0]]) 
				config.avatarmapping[comp[0]] = {};
			if (!config.avatarmapping[comp[0]][comp[1]]) 
				config.avatarmapping[comp[0]][comp[1]] = {};
			config.avatarmapping[comp[0]][comp[1]][comp[2]] = fields[1];
			var img = new Image();
			img.src = "images/"+fields[1];
			document.getElementById("hidden").appendChild(img);
			preloadedImages.push(img);
			//dbg.debug("AVATAR "+comp[0]+" DIALOGUE "+comp[1]+" NODE "+comp[2]
			//	+" IMAGE "+fields[1]);
		}
		dbg.debug(config.avatarmapping);
	}
	if (urlParams.dialoguepath) {
		sourceCode=getPlatformFileSystem().readFileSync(urlParams.dialoguepath);
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
			directServer.setLanguage(
				localStorage.getItem(LOCALSTORAGEPREFIX+"defaultlanguage"),
				localStorage.getItem(LOCALSTORAGEPREFIX+"language"),
				defaultLanguage
			);
			if (!sourceCode) {
				sourceCode = directServerLoadNodeDialogue(dialogueID,
					urlParams.woolRoot+"/"+urlParams.filepath);
			} else {
				directServerLoadDialogue(dialogueID,sourceCode);
			}
		}
	} else {
		directServer.setLanguage(null,null,defaultLanguage);
		directServerLoadDialogue(dialogueID,sourceCode);
	}


	var errorsFound=false;
	var errors = {};
	for (var i=0; i<directServer.dialogues[dialogueID].nodes.length; i++) {
		var node = directServer.dialogues[dialogueID].nodes[i];
		if (node.errors.length) {
			for (var j=0; j<node.errors.length; j++) {
				var err = node.errors[j];
				if (err.level == "warning" || err.level == "notice") continue;
				if (!errors[node.param.title]) errors[node.param.title] = [];
				errors[node.param.title].push(
					(err.line!==null ? "Line "+(err.line+1)+":" : "")
					+err.msg+" ("+err.level+")");
			}
			if (errors[node.param.title]) {
				errorsFound=true;
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

	var prevstate = localStorage.getItem("simplewoolclient_dialoguestate");
	if (urlParams.docontinue) {
		if (prevstate) {
			directServer.setState(prevstate);
			updateNodeUI(directServer.getNode());
		}
	} else {
		startDialogue();
	}
}


