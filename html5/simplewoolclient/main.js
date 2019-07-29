// valid parameters:
// config - display config
// code - source code of dialogue
// editable - if defined, make display config editable

var LOCALSTORAGEPREFIX="wool_js_";

// get params and config ------------------------------------------------
var params = Utils.getUrlParameters();


//localStorage.removeItem("simplewoolclient_config");

var config = {
	"avatars": { },
	"background": 12,
};

if (params.config) {
	config = JSON.parse(params.config);
} else {
	var c = localStorage.getItem("simplewoolclient_config");
	if (c) config = JSON.parse(c);
}

function saveConfig() {
	localStorage.setItem("simplewoolclient_config",JSON.stringify(config));
}


saveConfig();

// init ---------------------------------------------------------------

// load language defs directly because we can't load from file without http.
_i18n.loadJSON({
	"": { "language": "nl", "plural-forms": "nplurals=2; plural=(n != 1);", },
	"You:": "Jij:",
	"Your response:": "Je antwoord:",
	"Continue": "Ga verder",
	"Send": "Verstuur",
});
_i18n.setLocale("en");


if (config.background!==null)
	document.body.className = "pattern"+config.background;

var sourceCode=localStorage.getItem(LOCALSTORAGEPREFIX+"buffer");

if (params.code) sourceCode = params.code;

directServerLoadDialogue("dialogue",sourceCode);

var errorsFound=false;
var errors = {};
for (var i=0; i<directServer.dialogues["dialogue"].nodes.length; i++) {
	var node = directServer.dialogues["dialogue"].nodes[i];
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

var currentSpeaker=null;

function incCurrentAvatar(amount) {
	config.avatars[currentSpeaker] += amount;
	saveConfig();
	GenAvataaar(document.getElementById("agent_object"),
		config.avatars[currentSpeaker]);
}

function incBackground(amount) {
	config.background += amount;
	if (config.background < 0) config.background = 29;
	if (config.background > 29) config.background = 0;
	saveConfig();
	document.body.className = "pattern"+config.background;
}

var showingInDebug=null;

function showUrl() {
	showingInDebug="URL";
	var dbox = document.getElementById("debugarea");
	dbox.parentNode.style.display="block";
	var params = Utils.getUrlParameters();

	dbox.innerHTML = window.location.protocol + "//" +
		window.location.host + window.location.pathname
		+ "?config=" + encodeURIComponent(JSON.stringify(config))
		+ "&code=" + encodeURIComponent(sourceCode);
}

function showVariables() {
	showingInDebug="variables";
	var dbox = document.getElementById("debugarea");
	dbox.parentNode.style.display="block";
	dbox.innerHTML = JSON.stringify(directServer.currentnodectx.vars,null,2);
}

if (params.editable) {
	var edithtml = "";
	if (params.editurl) {
		edithtml =
			"<br><div class='commandbutton'>"
			+"<a href='"+params.editurl+"'>Back to editor</a>"
			+"</div>"
			+"<div class='commandbutton'>"
			+"<a id='editnodeurl' href='"+params.editurl+"'>Edit Node</a>"
			+"</div>";
	}
	document.body.innerHTML +=
		"<div class='editbox'>Avatar: "
		+"<div class='incrementbutton' onclick='incCurrentAvatar(1);'>+</div>"
		+"<div class='incrementbutton' onclick='incCurrentAvatar(-1);'>-</div>"
		+"<br>Background: "
		+"<div class='incrementbutton' onclick='incBackground(1);'>+</div>"
		+"<div class='incrementbutton' onclick='incBackground(-1);'>-</div>"
		+"<br><div class='commandbutton' onclick='showUrl();'>Get URL</div>"
		+"<div class='commandbutton' onclick='showVariables();'>Variables</div>"
		+edithtml
		+"</div>";
}


// helper functions ---------------------------------------------------

// index=null indicates autoforward reply (no index)
function handleBasicReply(id,index) {
	handleDirectServerCall("GET", null,null,
		"progress_dialogue/?replyId="+id
		+(index!==null ? "&replyIndex="+index : ""),
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
		"start_dialogue/?dialogueId=dialogue",
			updateNodeUI);
}


function updateNodeUI(node) {
	console.log(node);
	var editnodelink = document.getElementById("editnodeurl");
	if (editnodelink) editnodelink.href = 
		params.editurl+"?editnode="+encodeURIComponent(node.id);
	if (showingInDebug=="variables") showVariables();
	if (node.speaker && node.speaker!="UNKNOWN") {
		if (typeof config.avatars[node.speaker] == 'undefined') {
			config.avatars[node.speaker] = Math.floor(Math.random()*1000);
			saveConfig();
		}
		currentSpeaker = node.speaker;
		GenAvataaar(document.getElementById("agent_object"),
			config.avatars[currentSpeaker]);
	} else {
		document.getElementById("agent_object").innerHTML="";
	}
	var replyelem = document.getElementById("user-reply");
	if (node.id=="End") {
		document.getElementById("agent-name").innerHTML = "";
		document.getElementById("agent-statement").innerHTML = "End Dialogue";
		replyelem.className = "reply-box-auto-forward";
		replyelem.innerHTML =
			"<button class='reply-auto-forward' onclick='startDialogue()'>"
				+__("Restart")+"</button>"
		return;
	}
	document.getElementById("agent-name").innerHTML = node.speaker + ":";
	document.getElementById("agent-statement").innerHTML = node.statement;
	replyelem.innerHTML = 
		"<p id='user-name'>"+__("You:")+"</p>"
		+"<p id='user-instruction'>"+__("Your response:")+"</p>";
	for (var i=0; i<node.replies.length; i++) {
		var reply = node.replies[i];
		if (reply.replyType=="BASIC") {
			replyelem.className = "reply-box";
			replyelem.innerHTML +=
				"<button class='reply' onclick='handleBasicReply(\""
					+reply.replyId+"\",\""+i+"\")'>"
					+reply.statement+"</button>"
		} else if (reply.replyType=="AUTOFORWARD") {
			replyelem.className = "reply-box-auto-forward";
			replyelem.innerHTML +=
				"<button class='reply-auto-forward' onclick='handleBasicReply(\""
					+reply.replyId+"\",null)'>"+__("Continue")+"</button>"
		} else if (reply.replyType=="TEXTINPUT"
		||         reply.replyType=="NUMERICINPUT") {
			var replyclass = "reply";
			var submitclass = "submit";
			if (reply.afterStatement) {
				replyclass += " reply_with_after_statement";
				submitclass += " submit_with_after_statement";
			}
			var func = reply.replyType=="TEXTINPUT"
				? "handleTextReply" : "handleNumericReply";
			replyelem.className = "reply-box";
			replyelem.innerHTML += '<p class="before_statement">' 
				+ reply.beforeStatement + '</p>';
			replyelem.innerHTML +=
				"<input type='text' placeholder='Type here' value='' type='text' name='test' class='"+replyclass+"'"
				+" id='"+reply.replyId+"_content'"
				+"></input>"
				+"<input class='"+submitclass+"'"
				+" onclick='"+func+"(\""
					+reply.replyId+"\",\""+i+"\")' value='"+__("Send")+"'></input>";
			if (reply.afterStatement) {
				replyelem.innerHTML += '<p class="after_statement">' 
					+ reply.afterStatement + '</p>';
			}

		}
	}
}

// start dialogue --------------------------------------------------

startDialogue();


