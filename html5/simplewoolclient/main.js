// valid parameters:
// config - display config
// code - source code of dialogue
// editable - if defined, make display config editable

// get params and config ------------------------------------------------
var params = window.location.search.substring(1).split("&")
.reduce(function(res, i) {
	if (i.split("=")[0]) {
		res[i.split("=")[0]] = i.split("=")[1];
	}
	return res;
}, {});

var config = {
	"avatars": {
		"John": 16,
		"Lisa": 36,
	},
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


directServerLoadDialogue("dialogue",decodeURIComponent(params.code));


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

if (params.editable) {
	document.body.innerHTML +=
		"<div class='editbox'>Avatar: "
		+"<div class='incrementbutton' onclick='incCurrentAvatar(1);'>+</div>"
		+"<div class='incrementbutton' onclick='incCurrentAvatar(-1);'>-</div>"
		+"<br>Background: "
		+"<div class='incrementbutton' onclick='incBackground(1);'>+</div>"
		+"<div class='incrementbutton' onclick='incBackground(-1);'>-</div>"
		+"</div>";
}


// helper functions ---------------------------------------------------

function handleBasicReply(id) {
	handleDirectServerCall("GET", null,null,
		"progress_dialogue/?replyId="+id,
			updateNodeUI);
}

function handleTextReply(id) {
	var value = document.getElementById(id+"_content").value;
	handleDirectServerCall("GET", null,null,
		"progress_dialogue/?replyId="+id
		+"&textInput="+encodeURIComponent(value),
			updateNodeUI);
}

function handleNumericReply(id,min,max) {
	// TODO
	handleTextReply(id);
}

function updateNodeUI(node) {
	console.log(node);
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
			"<button class='reply-auto-forward' onclick='handleRestart()'>"
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
					+reply.replyId+"\")'>"+reply.statement+"</button>"
		} else if (reply.replyType=="AUTOFORWARD") {
			replyelem.className = "reply-box-auto-forward";
			replyelem.innerHTML +=
				"<button class='reply-auto-forward' onclick='handleBasicReply(\""
					+reply.replyId+"\")'>"+__("Continue")+"</button>"
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
					+reply.replyId+"\")' value='"+__("Send")+"'></input>";
			if (reply.afterStatement) {
				replyelem.innerHTML += '<p class="after_statement">' 
					+ reply.afterStatement + '</p>';
			}

		}
	}
}

// start dialogue --------------------------------------------------

handleDirectServerCall("GET", null,null,
	"start_dialogue/?dialogueId=dialogue",
		updateNodeUI);

