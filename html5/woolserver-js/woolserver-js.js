/* JS version of dialogue manager.

-------------------------------------------------------------------
DOCS: yarrdn format (YarnParser.java)

NOTE: encoding is UTF8. Generally, empty lines are skipped.

A dialogue spec consists of nodes (sections) separated by lines that contain
"===".

A section is separated into header and body by a line with the separator "---".


The header contains a set of key-value pairs, one on each line, of the format:

    [Key] ':' [Value]

Whitespace in the key and value are trimmed.

Valid keys are: title (required), tags, colorID, position.


The body contains statement lines (specifying what the system says) and one or
more reply options (specifying what a user can reply), and directives (in
particular IF and SET). These can be in any order.


Each line in the body has one of the following formats:

    <text>
    <agentname> ":" <text>

    '<<if' <variable> 'is' <value> '>>'
	'<<endif>>'
	'<<set' <variable> 'to' <value> '>>'
	'<<multimedia' 'type=image' 'name='<name> '>>'
	'<<multimedia' 'type=video' 'name='<name> '>>'
	'<<multimedia' 'type=timer' 'duration='<duration> '>>'

    '[[' <replyText> '| <dialogueNodeID> ']]'
    '[[' <dialogueNodeID> ']]'
	'[EXIT_DIALOGUE]' // not sure what this does

Lines can contain C-style line comments '//' ... .

dialogueNodeID can also refer to a node in another dialogue like so:

   <dialogueID> '.' <dialogueNodeId>

ReplyText can contain an input directive that indicates an input other than
multiple choice, like so:

  <text> '<<' <inputType>'Input' '->' <variableName>
           [ 'min='<min> ] [ 'max='<max> ] '>>' <text>

Note: no whitespace between <inputType>'Input' and 'min='<min> and
'max='<max>.

The body can contain conditional statements.  These are parsed before
Before the body is parsed, conditional statements


-------------------------------------------------------------------
DOCS: API with client

dialogue-handler:

_getAvailableDialoguesURL = _baseURL + "get-available-dialogues/" + _accountId + "/" + _userId;
-> returns array of {dialogueId,dialogueName}



_startDialogueURL = _baseURL +  "start-dialogue/" + _accountId + "/" + _userId;
-> param: {dialogueId}
-> returns: fields of class nl.rrd.yarrdn.agent.protocol/AgentMessage
	private String id;
	private String speaker;
	private String statement;
	private MultimediaMessage multimedia;
	private List<ReplyMessage> replies;
	private List<String> speakersInDialogue;
	
speakersInDialogue: array of agent names

speaker: name of speaking agent

statement: string (concat of all text statements + '\n')

replies: array of { replyType, replyId, statement, endsDialogue[bool],
	beforeStatement, afterStatement, min, max } (class ReplyMessage)

replyType one of: AUTOFORWARD, BASIC, TEXTINPUT, NUMERICINPUT

multimedia: 
{ multimediaType [one of IMAGE,VIDEO,TIMER], resourceName (image),
  timerDuration (timer) }


_progessDialogueURL = _baseURL +  "progress-dialogue/" + _accountId + "/" + _userId;
_endDialogueURL = _baseURL +  "end-dialogue/" + _accountId + "/" + _userId;
_getDialogueForAgentURL = _baseURL +  "get-dialogue-for-agent/" + _accountId + "/" + _userId;


-------------------------------------------------------------------
index-handler:

_getSetupValuesURL = _baseURL + "get-setup-value/" + _accountId;

_getAvailableDialoguesURL (see dialogue-handler)

_resetUserProfileURL = _baseURL +  "reset-user-profile/" + _accountId + "/" + _userId;

_getUISettingsURL = _baseURL + "get-ui-settings/" + _accountId + "/" + _userId;
-> returns {language}  ex: {language: "en_EN"}

_setUISettingsURL = _baseURL + "set-ui-settings/" + _accountId + "/" + _userId;
-> param: language  ex: {language: "en_EN"}
-> return value not used


-------------------------------------------------------------------
login-handler.js:

_loginURL = baseURL + _userName + "&password=" + _password;  
-> returns raw token


-------------------------------------------------------------------
signup-handler.js: (not used in frail)

_signupURL = _baseURL +  "auth/signup?user=" + _userName + "&password=" + _password;

*/


// is passed to eval'ed code as C
// vars: associative array
function WoolNodeContext(vars) {
	this.vars = vars;
	// no speaker defined: speaker = "UNKNOWN";
	this.speakers = [];
	this.lines = [];
	this.media = null;
	this.type = "default";
	this.afreply = null;
	this.inputreply = null;
	this.choices = [];

	this.addLine = function(line,speaker) {
		this.lines.push(line);
		if (speaker) this.speakers.push(speaker);
	}
	this.addMultimedia = function(type,param) {
		this.media = {type: type, param: param};
	}
	this.setNodeType = function(type) {
		this.type = type;
	}
	this.addAutoForwardReply = function(optid) {
		if (this.afreply) directServer.logError("Multiple af replies");
		this.afreply = optid;
	}
	this.addInputReply = function(optid,beforeText,inputtype,inputvar,
	afterText,action,min,max) {
		// TODO support multiple input replies
		if (this.inputreply) directServer.logError("Multiple input replies");
		this.inputreply = {
			optid: optid,
			beforeText: beforeText,
			inputtype: inputtype,
			inputvar: inputvar,
			afterText: afterText,
			action: action,
			min: min,
			max: max,
		};
	}
	this.addReplyChoice = function(optid,text,action) {
		this.choices.push({
			optid: optid,
			text: text,
			action: action,
		});
	}
}


function WoolNode(dialogue,lines) {
	console.log("Created node! Lines:"+lines.length);
	var self=this;
	// compile time errors
	// level - notice, warning, error, fatal
	// line = line in body, or null if N/A
	function logError(level,line,msg) {
		self.errors.push({level:level,line:line,msg:msg});
		console.log("Logged error: "+level+" "+line+" "+msg);
	}

	this.head = [];
	this.body = [];
	this.param = []; // key-value pairs from head
	this.errors = []; // array of {level, line, msg}
	var inBody=false;
	for (var i=0; i<lines.length; i++) {
		var line = lines[i];
		if (line == "---") {
			if (inBody) logError("warning", i,
				"Encountered '---' twice in node, ignoring");
			inBody = true;
			continue;
		}
		if (inBody) {
			this.body.push(line);
		} else {
			this.head.push(line);
		}
	}
	function rewriteExpression(expr) {
		return expr.replace(/[$]([a-zA-Z0-9_]+)/g, function(match,p1) {
			return "C.vars."+p1;
		});
	}
	// parse space separated list of key="value" pairs
	function parseKeyValList(expr) {
		var ret = {};
		var regex = /^\s*([a-zA-Z0-9_]+)\s*=\s*"([^"]+)"/;
		while (true) {
			var matches = regex.exec(expr);
			if (!matches) break; // XXX report stray characters
			ret[matches[1]] = matches[2];
			expr = expr.replace(regex,"");
		}
		return ret;
	}
	if (this.body.length==0) logError("error",null,"Node has no body");
	// get key-value pairs from head
	for (var i=0; i<this.head.length; i++) {
		if (this.head[i] == "") continue;
		matches = /^(\w+)\s*:\s*(.*)$/.exec(this.head[i]);
		if (matches) {
			this.param[matches[1]] = matches[2];
		} else {
			logError("error",null,"Encountered unparseable line in head: "+this.head[i]);
		}
	}
	if (this.param.speaker) dialogue.speakers[speaker] = this.param.speaker;
	console.log(this.param);
	// parse body. Format for each line:
	// <text>
	// <agentname> ":" <text>
	// Variables can occur in the text, format is '$'<variablename>
	//
	// '<<if' <variable> 'is' <value> '>>'
	// '<<endif>>'
	// '<<set' <variable> 'to' <value> '>>'
	// '<<multimedia' 'type=image' 'name='<name> '>>'
	// '<<multimedia' 'type=video' 'name='<name> '>>'
	// '<<multimedia' 'type=timer' 'duration='<duration> '>>'
	// 
	// '[[' <replyText> '| <dialogueNodeID> ']]'
	// '[[' <dialogueNodeID> ']]'   (autoforward)
	for (var i=0; i<this.body.length; i++) {
		var line = this.body[i];
		var linecommentchar = line.indexOf('//');
		if (linecommentchar >= 0) {
			line = line.substring(0,linecommentchar);
		}
		line = line.trim();
		this.body[i] = line;
		if (line == "") continue;
		var matches = /^<<(else)?if\s+(.+)\s*>>$/.exec(line);
		if (matches) {
			this.body[i] = (matches[1] ? "} else if (" : "if (")
				+ rewriteExpression(matches[2])
				+ ") {";
			continue;
		}
		var matches = /^<<else\s*>>$/.exec(line);
		if (matches) {
			this.body[i] = "} else {";
			continue;
		}
		var matches = /^<<endif\s*>>$/.exec(line);
		if (matches) {
			this.body[i] = "}";
			continue;
		}
		var matches = /^<<set\s+[$](\w+)\s*[=]\s*(.+)\s*>>$/.exec(line);
		if (matches) {
			this.body[i] = "C.vars."+matches[1]+" = "+matches[2]+";";
			continue;
		}
		var matches = /^<<multimedia\s+type=image\s+name=([^\]]+)>>$/.exec(line);
		if (matches) {
			this.body[i] = "C.addMultimedia('image','"+matches[1]+"');";
			continue;
		}
		var matches = /^<<multimedia\s+type=video\s+name=(\w+)\s*>>$/.exec(line);
		if (matches) {
			this.body[i] = "C.addMultimedia('video','"+matches[1]+"');";
			continue;
		}
		var matches = /^<<multimedia\s+type=timer\s+duration=([0-9:.]+)\s*>>$/.exec(line);
		if (matches) {
			this.body[i] = "C.addMultimedia('timer','"+matches[1]+"');";
			continue;
		}
		// Oude spec. Is nu aparte node "End"
		var matches = /^\s*\[EXIT_DIALOGUE\]\s*$/.exec(line);
		if (matches) {
			this.body[i] = "C.setNodeType('exit');";
			continue;
		}
		// autoforward reply, no '|'
		var matches = /^\[\[\s*([^|\]]+)\s*\]\]$/.exec(line);
		if (matches) {
			var optid = matches[1];
			this.body[i] = "C.addAutoForwardReply('"+optid+"');";
			continue;
		}
		// normal reply
		var matches = /^\[\[\s*([^|\]]+)\s*\|\s*([a-zA-Z0-9_.-]+)\s*(|.*)?\]\]$/.exec(line);
		if (matches) {
			// XXX textinput also accepts min, max
			var desc = matches[1];
			var optid = matches[2];
			var actionsstr = matches[3];
			var action=null;
			if (actionsstr) {
				actionsstr = actionsstr.substring(1); // chop leading '|'
				// chop leading and trailing brackets
				matches = /^\s*<<(.*)>>\s*$/.exec(actionsstr);
				if (!matches) {
					logError("error",i,"Cannot parse action "+actionstr);
				} else {
					actionsstr = matches[1];
					// XXX brackets in quotes not parsed properly
					var actions = actionsstr.split(/>>\s*<</);
					var actfunc = "";
					for (var j=0; j<actions.length; j++) {
						var actionstr = "<<"+actions[j]+">>";
						var matches=/^<<set\s+[$](\w+)\s*[=]\s*(.+)\s*>>$/.exec(actionstr);
						if (matches) {
							actfunc += "C.vars."+matches[1]+" = "+matches[2]+";";
						} else {
							logError("error",i,"Cannot parse action "+actionstr);
						}
					}
					if (actfunc) {
						action = "function(C) { "+actfunc+" }";
					} else {
						action = null;
					}
				}
			}
			var matches = /^(.*)<<input\s+(.+)\s*>>(.*)$/.exec(desc);
			if (matches) {
				var beforeText = matches[1];
				var inputparams = matches[2];
				var afterText = matches[3];
				inputparams = parseKeyValList(inputparams);
				this.body[i] = "C.addInputReply('"
					+optid+"',"
					+JSON.stringify(beforeText)+",'"
					+inputparams.type+"','"
					+inputparams.value.substring(1)+"',"
					+JSON.stringify(afterText)+","
					+action
					+(inputparams.min ? ",'"+inputparams.min+"'":"")
					+(inputparams.max ? ",'"+inputparams.max+"'":"")
					+");";
				continue;
			} else {
				this.body[i] = "C.addReplyChoice("
					+JSON.stringify(optid)+","
					+JSON.stringify(desc)+","
					+action+");";
				continue;
			}
		}
		// plain line
		var matches = /^([a-zA-Z0-9_]+):\s*(.*)$/.exec(line);
		if (matches) {
			var speaker = matches[1];
			dialogue.speakers[speaker] = speaker;
			// with speaker
			this.body[i] = "C.addLine("+
				JSON.stringify(matches[2])+","
				+JSON.stringify(speaker)+");";
			continue;
		} else {
			// without
			this.body[i] = "C.addLine("+JSON.stringify(line)+");";
			continue;
		}
	}
	console.log("Parsing function:");
	console.log(this.body.join("\n"));
	// turn code into function
	try {
		this.func = new Function("C", this.body.join("\n") );
	} catch (e) {
		logError("fatal",null,
			"Script error: "+e);
		console.log(e);
	}
	dialogue.nodeMap[this.param.title] = this;
}

function WoolDialogue(woolsource) {
	this.source = woolsource.split(/\r?\n/);
	this.nodes = [];
	this.speakers = {};
	this.nodeMap = {};
	var nodelines = [];
	for (var i=0; i<this.source.length; i++) {
		var line = this.source[i].trim();
		if (line == "===") {
			if (nodelines.length > 0) {
				this.nodes.push(new WoolNode(this,nodelines));
			}
			nodelines = [];
		} else {
			nodelines.push(line);
		}
	}
	console.log(this.nodes);
};


// load single dialogue client side ---------------------------------
// For client-side only handling (wool editor)

function directServerLoadDialogue(dialogueID,data) {
	//var data = localStorage.getItem(storageKey);
	//if (data) {
		directServer.dialogues[dialogueID] = new WoolDialogue(data);
		//return true;
	//}
	//return false;
}


// load set of dialogues -----------------------------------------------
// For dialogue selection screen (get_available_dialogues)

function directServerLoadFile(i,filename,callback) {
	// XXX assumes file format is [dirtree]/filename.yarn.txt
	var basename = filename.split(/[\/.]/);
	basename = basename[basename.length-3];
	function handleDataLoaded() {
		console.log("Wool-JS: all data loaded.");
		directServer.allDataLoaded = true;
		console.log(directServer);
		if (callback) callback();
	}
	console.log("#"+filename+"#"+basename);
	$.ajax({
		url: "../woolserver-js/"+filename,
		type: 'get',
		success: function(data) {
			directServer.dialogues[basename] = new WoolDialogue(data);
			directServer.availableDialogues[i] = {
				dialogueId: basename,
				dialogueName: basename,
			};
			directServer.nrDialoguesLoaded++;
			if (directServer.nrDialoguesLoaded == directServer.testfiles.length) {
				handleDataLoaded();
			}
		},
		error: function(data) {
			directServer.logError("Cannot load dialogue def: "+filename);
			directServer.nrDialoguesLoaded++;
			if (directServer.nrDialoguesLoaded == directServer.testfiles.length) {
				handleDataLoaded();
			}
		}
	});
}

function directServerLoadDialogues(callback) {
	// only load dialogues if server has directserver protocol
	var protocol = serverLocation.split("://");
	if (protocol[0] != "directserver") return;

	$.ajax({
		url: '../woolserver-js/yarntestfiles-new.txt',
		type: 'get',
		success: function(data) {
			directServer.testfiles = data.split(/\r?\n/);
			directServer.testfiles.pop();
			for (var i=0; i<directServer.testfiles.length; i++) {
				directServerLoadFile(i,directServer.testfiles[i],callback);
				//break;
			}
		}
	});
}


