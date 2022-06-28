/* JS version of Wool parsing and execution

DOCS: Wool format

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

    '<<if' <expression> '>>'
    '<<elseif' <expression> '>>'
	'<<else>>'
	'<<endif>>'
	'<<set' <variable> '=' <value> '>>'
	'<<multimedia' 'type=image' 'name='<name> '>>'
	'<<random' 'weight='<value> '>>'
	'<<or' 'weight='<value> '>>'
	'<<endrandom>>'
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


Escape character semantics.

Escape characters are used to escape special Wool characters.

An escaped character is passed as a literal character to the underlying
representation language (like HTML or Markdown).


-------------------------------------------------------------------
DOCS: API with client [THIS PART IS OUTDATED]

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

if (typeof exports != "undefined") {
	// we are node.js module, include dependencies
	var mod = require("../lib/utils.js");
	if (mod) {
		Utils = mod.Utils;
		Utils.requireGlobal("../lib/debug.js");
		Utils.requireGlobal("../woolserver-js/woolserver-js.js");
	}
}


// is passed to eval'ed code as C
// vars: associative array
// actions (optional): array of actions
function WoolNodeContext(vars,actions) {
	this.vars = vars;
	// no speaker defined: speaker = "UNKNOWN";
	this.speakers = [];
	this.text = [""]; // array of (translateable) strings
	this.media = null;
	this.type = "default";
	this.afreply = null;
	this.inputreply = null;
	this.choices = [];
	if (actions) {
	    this.pendingActions = actions;
	} else {
	    this.pendingActions = [];
	}

	this.randomvalues = []; // [ID -> value]
	this.addLine = function(line,speaker) {
		this.text[this.text.length-1] += line + "\n";
		if (speaker) this.speakers.push(speaker);
	}
	// start new translatable text block
	this.newTextBlock = function() {
		if (this.text[this.text.length-1] != "") this.text.push("");
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
	this.doAction = function(params) {
		dbg.debug("Action called: "+JSON.stringify(params));
        this.pendingActions.push(params)
	}
	this.setRandom = function(id,min,max) {
		this.randomvalues[id] = min + Math.random()*(max-min);
	}
	this.getRandom = function(id) {
		return this.randomvalues[id];
	}
}

// Helper fields:
// links - can be used for editor to show arrows
// texts - can be used for translation.  The node's text lines are trimmed,
//         with \n added to each line.
//         XXX translation of strings with substitute vars is not supported yet!
// agenttexts - subset of texts spoken by agent
// usertexts - subset of texts spoken by agent (note: can have overlap with
//             agenttexts)
function WoolNode(dialogue,lines) {
	//dbg.debug("Created node! Lines:"+lines.length);
	var self=this;
	// compile time errors
	// level - notice, warning, error, fatal
	// line = line in body, or null if N/A
	function logError(level,line,msg) {
		self.errors.push({level:level,line:line,msg:msg});
		dbg.error("Logged error: "+level+" "+line+" "+msg);
	}

	// info for random variables: [ randID -> randweightrunningtotal ]
	this.randvars = [];
	// next free ID, produces unique ID for each random clause in node
	this.nextrandID = 0;
	// stack of IDs for nested random statements. When statement starts,
	// new ID is pushed; when statement ends, top ID is popped.
	// Top of stack is ID of current random statement.
	this.randstack = [];

	this.head = [];
	this.body = [];
	this.param = []; // key-value pairs from head
	this.errors = []; // array of {level, line, msg}
	this.links = []; // array of {linenr,nodename}, used for editor
	this.texts = {}; // array { <text> => true}, for translation
	this.agenttexts = {}; // array { <text> => true}, for translation
	this.usertexts = {}; // array { <text> => true}, for translation
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
			this.head.push(line.trim());
		}
	}
	function removeLiteralStrings(expr) {
		expr = expr.replace(/["](?:[^"\\]|\\.)*["]/g, "");
		expr = expr.replace(/['](?:[^'\\]|\\.)*[']/g, "");
		//expr = expr.replace(/["][^"]*["]/g,"");
		//expr = expr.replace(/['][^']*[']/g,"");
		return expr;
	}
	dbg.assert(removeLiteralStrings('"Literal string"') == "",
		"removeLiteralStrings assertion 1");
	dbg.assert(removeLiteralStrings('" string 1" "string2 "') == " ",
		"removeLiteralStrings assertion 2");
	// check for existence of bare identifiers (e.g. myVar instead of $myVar)
	// If found, add error to this.errors
	function checkExpressionForBareIds(expr,line) {
		var found = null;
		// XXX shallow parsing
		expr = removeLiteralStrings(expr);
		if ((found=expr.match(/^([a-zA-Z_][a-zA-Z0-9_]+)/))
		||  (found=expr.match(/\s+([a-zA-Z_][a-zA-Z0-9_]+)/))
		//||  (found=expr.match(/[^$a-zA-Z0-9_"-]([a-zA-Z0-9_]+)/))
		) {
			if (found[1]!="true"
			&&  found[1]!="false"
			&&  found[1]!="null") {
				logError("error",line,"Variable '"+found[1]+"' missing '$' prefix");
			}
		}
	}
	// check for existence of '=', generate a warning in this.errors.
	function checkExpressionForSingleEquals(expr,line) {
		expr = removeLiteralStrings(expr);
		if (expr.match(/[^=<>!]=[^=]/)) {
			logError("warning",line,"Assignment operator '=' found in <<if>> statement. Did you mean '=='?");
		}
	}
	function rewriteExpression(expr) {
		// XXX $a == $b is not rewritten, inherits js semantics
		// XXX shallow parsing, could match string literal
		// XXX improve number parser, e.g. ".0" not supported
		expr = expr.replace(/([^=!])==\s*true\b/g,  "$1 === true");
		expr = expr.replace(/([^=!])==\s*false\b/g, "$1 === false");
		expr = expr.replace(/([^=!])==\s*0\b/g,     "$1 === 0");
		expr = expr.replace(/([^=!])==\s*0[.]0\b/g, "$1 === 0.0");
		expr = expr.replace( /\btrue\s*==([^=!])/g, "true === $1");
		expr = expr.replace(/\bfalse\s*==([^=!])/g, "false === $1");
		expr = expr.replace(    /\b0\s*==([^=!])/g, "0 === $1");
		expr = expr.replace(/\b0[.]0\s*==([^=!])/g, "0.0 === $1");

		expr = expr.replace(/!=\s*true\b/g,  " !== true");
		expr = expr.replace(/!=\s*false\b/g, " !== false");
		expr = expr.replace(/!=\s*0\b/g,     " !== 0");
		expr = expr.replace(/!=\s*0[.]0\b/g, " !== 0.0");
		expr = expr.replace( /\btrue\s*!=/g, "true !== ");
		expr = expr.replace(/\bfalse\s*!=/g, "false !== ");
		expr = expr.replace(    /\b0\s*!=/g, "0 !== ");
		expr = expr.replace(/\b0[.]0\s*!=/g, "0.0 !== ");

		return expr.replace(/[$]([a-zA-Z0-9_]+)/g, function(match,p1) {
			return "C.vars."+p1;
		});
	}
	// parse space separated list of key="value" pairs
	// Returns null on parse error
	function parseKeyValList(expr) {
		var ret = {};
		var regex = /^\s*([a-zA-Z0-9_]+)\s*=\s*"((?:[^"\\]|\\.)*)"/;
		while (true) {
			var matches = regex.exec(expr);
			if (!matches) break;
			ret[matches[1]] = matches[2];
			expr = expr.replace(regex,"");
		}
		// stray characters imply error
		if (!expr) return ret;
		if (expr.trim() != "") return null;
		return ret;
	}
	// returns action statement when <<set ... >> found, otherwise null
	function checkSetStatement(str) {
		var matches=/^<<set\s+[$](\w+)\s*[=]\s*(.+)\s*>>$/.exec(str);
		if (matches) {
			return "C.vars."+matches[1]+" = "+rewriteExpression(matches[2])+";";
		} else {
			return null;
		}
	}
	function checkActionStatement(str,i) {
		matches=/^<<action\s+(.+)\s*>>$/.exec(str);
		if (matches) {
			var actionparams = parseKeyValList(matches[1]);
			if (actionparams === null) {
				logError("error",i,
					"Cannot parse parameter string '"
					+matches[1]+"'");
				return null;
			}
			return "C.doAction("+JSON.stringify(actionparams)+");";
		} else {
			return null;
		}
	}
	// random ----------------------------------------------------------
	function startRandom() {
		self.randvars[self.nextrandID] = 0;
		self.randstack.push(self.nextrandID++);
	}
	function endRandom() {
		if (self.randstack.length == 0) {
			logError("error",i, "<<endrandom>> without matching <<random>>");
			return;
		}
		self.randstack.pop();
	}
	function getRandomID() {
		if (self.randstack.length == 0) {
			logError("error",i, "Not in <<random>> statement.");
			return 99999;
		}
		return self.randstack[self.randstack.length-1];
	}
	function getRandomWeight() {
		var id = getRandomID();
		return self.randvars[id];
	}
	function addRandomWeight(weight) {
		var id = getRandomID();
		if (self.randvars[id] == null) self.randvars[id] = 0;
		self.randvars[id] += weight;
	}
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
	var isEndNode = this.param.title == "End";
	if (!isEndNode && this.body.length==0) {
		logError("error",null,"Node has no body");
	}
	if (isEndNode) {
		if ( this.body.length >= 2
		||  (this.body.length == 1 && this.body[0].trim() != "")
		) {
			logError("error",null,"End node should have empty body");
		}
	}
	if (this.param.speaker) dialogue.speakers[speaker] = this.param.speaker;
	//dbg.debug(this.param);
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
	var alllines=""; // collect subsequent lines for translation
	var addlineprefix = ""; // code to add before addLine statement
	for (var i=0; i<this.body.length; i++) {
		var line = this.body[i];
		// XXX also matches string literals, so in this parser, it is
		// ignored inside << ... >> or [[ ... ]].
		var linecommentchar = line.indexOf('//');
		if (linecommentchar >= 0) {
			// comment found, check if not inside << ... >> or [[ ... ]]
			var lhs = line.substring(0,linecommentchar);
			var rhs = line.substring(linecommentchar+2);
			if ( (lhs.indexOf("<<") >= 0 && rhs.indexOf(">>") >= 0)
			||   (lhs.indexOf("[[") >= 0 && rhs.indexOf("]]") >= 0) ) {
				// inside, do nothing
				// NOTE: officially a comment can be inside an option or action
			} else {
				// not inside
				// check if slash is escaped
				if (linecommentchar > 0
				&& line.substring(linecommentchar-1,linecommentchar) == "\\") {
					// first character is escaped, ignore
				} else {
					//keep left part only
					line = lhs;
					// warn about unescaped http:// https://
					//if (line.match(/http:[\/][\/]/)) {
					if (line.endsWith("http:")) {
						logError("warning", i,
							"Unescaped URL, did you mean 'http:\\//'" );
					}
					//if (line.match(/https:[\/][\/]/)) {
					if (line.endsWith("https:")) {
						logError("warning", i,
							"Unescaped URL, did you mean 'https:\\//'" );
					}
				}
			}
		}

		// remove escape characters
		// this should be done right before passing the text to
		// the presentation layer
		//line = line.replace(/\\(.)/g,
		//	function(match, $1, offset, original) { return $1;} );

		var lineuntrimmed = line + "\n";
		line = line.trim();
		this.body[i] = line;
		// do not call addLine for empty lines
		//if (line == "") {
		//	alllines += lineuntrimmed;
		//	continue;
		//}
		var matches1 = /^<<random\s*(.*)\s*>>$/.exec(line);
		var matches2 = /^<<or\s*(.*)\s*>>$/.exec(line);
		if (matches1 || matches2) {
			if (alllines) {
				// flush text in between conditionals
				this.texts[alllines.trim()] = true;
				this.agenttexts[alllines.trim()] = true;
				addlineprefix = "C.newTextBlock();";
				alllines=""
			}
			var paramsstr = "";
			var weight = 1;
			if (matches1) {
				startRandom();
				paramsstr = matches1[1];
			} else {
				paramsstr = matches2[1];
			}
			params = parseKeyValList(paramsstr);
			if (params == null) {
				logError("error",i,"Cannot parse random parameters: '"
					+ paramsstr + "'");
			} else {
				// any other parameters are ignored
				if (params.weight) {
					if (isNaN(params.weight) || params.weight < 0) {
						logError("error",i,"Illegal random weight: '"
							+ params.weight + "'");
					} else {
						weight = parseFloat(params.weight);
					}
				}
			}
			addRandomWeight(weight);
			var weight = getRandomWeight();
			this.body[i] =
				(matches2 ? "} else " : "")
				+"if (C.getRandom("+getRandomID()
				+") <= "+weight+") {";
			continue;
		}
		var matches = /^<<endrandom\s*>>$/.exec(line);
		if (matches) {
			if (alllines) {
				// flush text in between conditionals
				addlineprefix = "C.newTextBlock();";
				this.texts[alllines.trim()] = true;
				this.agenttexts[alllines.trim()] = true;
				alllines=""
			}
			endRandom();
			this.body[i] = "}";
			continue;
		}
		var matches = /^<<(else)?if\s+(.+)\s*>>$/.exec(line);
		if (matches) {
			if (alllines) {
				// flush text in between conditionals
				addlineprefix = "C.newTextBlock();";
				this.texts[alllines.trim()] = true;
				this.agenttexts[alllines.trim()] = true;
				alllines=""
			}
			checkExpressionForSingleEquals(matches[2], i);
			checkExpressionForBareIds(matches[2], i);
			//dbg.debug("REWROTE EXPR: "+matches[2] + " => " + rewriteExpression(matches[2]));
			this.body[i] = (matches[1] ? "} else if (" : "if (")
				+ rewriteExpression(matches[2])
				+ ") {";
			continue;
		}
		var matches = /^<<else\s*>>$/.exec(line);
		if (matches) {
			if (alllines) {
				// flush text in between conditionals
				addlineprefix = "C.newTextBlock();";
				this.texts[alllines.trim()] = true;
				this.agenttexts[alllines.trim()] = true;
				alllines=""
			}
			this.body[i] = "} else {";
			continue;
		}
		var matches = /^<<endif\s*>>$/.exec(line);
		if (matches) {
			if (alllines) {
				// flush text in between conditionals
				addlineprefix = "C.newTextBlock();";
				this.texts[alllines.trim()] = true;
				this.agenttexts[alllines.trim()] = true;
				alllines=""
			}
			this.body[i] = "}";
			continue;
		}
		var actfuncitem = checkSetStatement(line);
		if (actfuncitem) {
			this.body[i] = actfuncitem;
			continue;
		}
		var actfuncitem = checkActionStatement(line,i);
		if (actfuncitem) {
			this.body[i] = actfuncitem;
			continue;
		}
		var matches = /^<<multimedia\s+type=image\s+name=(\w+)\s*>>$/.exec(line);
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
		// after checking all types of << .. >>, catch unrecognised << .. >>
		var matches = /^<<.*>>$/.exec(line);
		if (matches) {
			// ignore line, produce error
			this.body[i] = "";
			logError("error",i,"Cannot parse << ... >> statement");
			continue;
		}
		// Oude spec. Is nu aparte node "End"
		var matches = /^\s*\[EXIT_DIALOGUE\]\s*$/.exec(line);
		if (matches) {
			this.body[i] = "C.setNodeType('exit');";
			logError("error",i,"Deprecated EXIT_DIALOGUE statement");
			continue;
		}
		// autoforward reply, no '|'
		var matches = /^\[\[\s*([^|]+)\s*\]\]$/.exec(line);
		if (matches) {
			var optid = matches[1];
			this.links.push({line:i,node:optid});
			this.body[i] = "C.addAutoForwardReply('"+optid+"');";
			continue;
		}

		// normal reply
		var matches = /^\[\[\s*([^|]+)\s*\|\s*([a-zA-Z0-9_.\/-]+)\s*(|.*)?\]\]$/.exec(line);
		if (matches) {
			// XXX textinput also accepts min, max
			var desc = matches[1].trim();
			var origdesc = desc;
			if (__) {
				desc = __("_user|"+origdesc);
				// not found? try without context
				if (desc == origdesc) {
					desc = __(origdesc);
				}
			}
			desc = directServer.stripEscapes(desc);
			var optid = matches[2];
			var actionsstr = matches[3];
			this.links.push({line:i,node:optid});
			var action=null;
			if (actionsstr) {
				actionsstr = actionsstr.substring(1); // chop leading '|'
				// chop leading and trailing brackets
				matches = /^\s*<<(.*)>>\s*$/.exec(actionsstr);
				if (!matches) {
					logError("error",i,"Cannot parse action "+actionsstr);
				} else {
					actionsstr = matches[1];
					// XXX brackets in quotes not parsed properly
					var actions = actionsstr.split(/>>\s*<</);
					var actfunc = "";
					for (var j=0; j<actions.length; j++) {
						var actionstr = "<<"+actions[j]+">>";
						var actfuncitem = checkSetStatement(actionstr);
						if (actfuncitem) {
							actfunc += actfuncitem;
						} else {
							actfuncitem = checkActionStatement(actionstr,i);
							if (actfuncitem) {
								actfunc += actfuncitem;
							} else {
								logError("error",i,
									"Cannot parse action "+actionstr);
							}
						}
					}
					if (actfunc) {
						action = "function(C) { "+actfunc+" }";
					} else {
						action = null;
					}
				}
			}
			var matches = /^(.*)(<<input\s+)(.+)(\s*>>)(.*)$/.exec(desc);
			if (matches) {
				
				var beforeText = matches[1];
				var inputparams_str = matches[3];
				var afterText = matches[5];
				// new server style
				var textSegment = beforeText+matches[2]+inputparams_str+matches[4]+afterText;
				// old gettext stle
				//var textSegment = beforeText+"%1"+afterText;
				var inputparams = parseKeyValList(inputparams_str);
				if (inputparams === null) {
					logError("error",i,"Cannot parse parameter string '"
						+inputparams_str+"'");
					continue;
				}
				if (!inputparams.type) {
					logError("error",i,"Input: type missing");
					continue;
				}
				if (!inputparams.value) {
					logError("error",i,"Input: value missing");
					continue;
				}
				this.texts[origdesc] = true;
				this.usertexts[origdesc] = true;
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
				this.texts[origdesc] = true;
				this.usertexts[origdesc] = true;
				this.body[i] = "C.addReplyChoice("
					+JSON.stringify(optid)+","
					+JSON.stringify(desc)+","
					+action+");";
				continue;
			}
		}
		// after checking all types of [[ .. ]], catch unrecognised [[ .. ]]
		var matches = /^\[\[.*\]\]$/.exec(line);
		if (matches) {
			// ignore line, produce error
			this.body[i] = "";
			logError("error",i,"Cannot parse [[ ... ]] statement");
			continue;
		}
		// now, catch unclosed [[ .. ]]
		var matches = /^\[\[/.exec(line);
		if (matches) {
			logError("warning",i,"Unclosed [[ ... ]] statement");
		}
		// plain line
		var matches = /^([a-zA-Z0-9_]+):\s*(.*)$/.exec(line);
		//if (matches) {
		if (false) {
			// with speaker (is now obsolete)
			var speaker = matches[1];
			//if (alllines) alllines += "\n";
			//alllines += matches[2];
			alllines += matches[2] + "\n";
			dialogue.speakers[speaker] = speaker;
			this.body[i] = addlineprefix+"C.addLine("+
				JSON.stringify(matches[2])+","
				+JSON.stringify(speaker)+");";
			addlineprefix = "";
			continue;
		} else {
			// without speaker
			//if (alllines) alllines += "\n";
			//alllines += line;
			alllines += lineuntrimmed;
			this.body[i] = addlineprefix+"C.addLine("+JSON.stringify(line)+");";
			addlineprefix = "";
			continue;
		}
	}
	if (alllines) {
		this.texts[alllines.trim()] = true;
		this.agenttexts[alllines.trim()] = true;
	}
	//dbg.debug("Parsing function:");
	//dbg.debug(this.body.join("\n"));
	// turn code into function
	var funcprefix = "";
	for (var randvar in this.randvars) {
		if (!this.randvars.hasOwnProperty(randvar)) continue;
		var totalweight = this.randvars[randvar];
		funcprefix += "C.setRandom("+randvar+",0,"+totalweight+");\n";
	}
	try {
		this.func = new Function("C", funcprefix+this.body.join("\n") );
	} catch (e) {
		logError("fatal",null,
			"Script error: "+e);
		dbg.debug(e);
		dbg.debug(funcprefix+this.body.join("\n"));
	}
	dialogue.nodeMap[this.param.title.trim().toLowerCase()] = this;
}

function WoolDialogue(woolsource) {
	this.source = woolsource.split(/\r?\n/);
	this.nodes = [];
	this.speakers = {};
	this.nodeMap = {};
	var nodelines = [];
	for (var i=0; i<this.source.length; i++) {
		var line = this.source[i];
		if (line == "===") {
			if (nodelines.length > 0) {
				this.nodes.push(new WoolNode(this,nodelines));
			}
			nodelines = [];
		} else {
			nodelines.push(line);
		}
	}
	//dbg.debug(this.nodes);
};


// ------------------------------------------------------------------------
// client side functions to manipulate directServer directly

// load single dialogue from source client side --------------------------
// For client-side only handling (wool editor)

function directServerLoadDialogue(dialogueID,data) {
	directServer.dialogues[dialogueID] = new WoolDialogue(data);
}

function directServerLoadNodeDialogue(dialogueID,filepath,overwrite) {
    //if (typeof NodeFileSystem == "undefined") return;
	var fs = getPlatformFileSystem();
	var data = fs.readFileSync(filepath);
	if (!data) {
		dbg.error("Cannot load dialogue "+filepath);
	}
	if (overwrite || !directServer.dialogues[dialogueID]) {
		directServer.dialogues[dialogueID] = new WoolDialogue(data);
	}
	directServer.nodeHistory = [];
	return data;
}

function directServerLoadNodeTranslation(filepath) {
	dbg.debug("Loading translation: "+filepath);
    //if (typeof NodeFileSystem == "undefined") return;
	var fs = getPlatformFileSystem();
	var langDefs = fs.readFileSync(filepath);
	if (!langDefs && directServer.defaultTranslation) {
		langDefs = directServer.defaultTranslation;
	}
	_i18n.clearDictionary("nl");
	if (langDefs) {
		_i18n.ReadJSONFromString(langDefs,"nl");
	}
}

function directServerGetPath(newPath,languageCode) {
    //if (typeof NodeFileSystem == "undefined") return newPath;
	if (!languageCode) languageCode = "en";
	var fs = getPlatformFileSystem();
    if (newPath.indexOf("/") != 0 && newPath.indexOf("\\") != 0) {
        // relative path
        var curPath=fs.getPathAPI().dirname(directServer.currentdialogueId);
		// replace top level directory
		if (curPath.startsWith("/"+directServer.defaultLanguage)
		||  curPath.startsWith("\\"+directServer.defaultLanguage) ) {
			curPath = "/" + languageCode 
				+ curPath.substring(directServer.defaultLanguage.length+1);
		}
        newPath = fs.getPathAPI().normalize(fs.getPathAPI().join(
            "/", curPath, newPath) );
    } else {
        // absolute path -> add language
        newPath = fs.getPathAPI().normalize(fs.getPathAPI().join(
            "/",languageCode,newPath) );
    }
    return newPath;
}

// load set of dialogues from index file via ajax -------------------------
// For dialogue selection screen (get_available_dialogues)

function directServerLoadFile(i,filename,callback) {
	// XXX assumes file format is [dirtree]/filename.yarn.txt
	var basename = filename.split(/[\/.]/);
	basename = basename[basename.length-3];
	function handleDataLoaded() {
		dbg.debug("Wool-JS: all data loaded.");
		directServer.allDataLoaded = true;
		dbg.debug(directServer);
		if (callback) callback();
	}
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


if (typeof exports !== 'undefined') {
	// node.js require()
	exports.WoolNodeContext = WoolNodeContext;
	exports.WoolNode = WoolNode;
	exports.WoolDialogue = WoolDialogue;
	exports.directServerLoadDialogue = directServerLoadDialogue;
	exports.directServerLoadNodeDialogue = directServerLoadNodeDialogue;
	exports.directServerLoadNodeTranslation = directServerLoadNodeTranslation;
	exports.directServerGetPath = directServerGetPath;
	exports.directServerLoadFile = directServerLoadFile;
	exports.directServerLoadDialogues = directServerLoadDialogues;
}

