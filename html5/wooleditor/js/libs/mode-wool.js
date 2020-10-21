define("ace/mode/wool",["require","exports","module","ace/lib/oop","ace/mode/text","ace/mode/text_highlight_rules","ace/mode/behaviour"], function(require, exports, module) {
"use strict";

var oop = require("../lib/oop");
var TextMode = require("./text").Mode;
var TextHighlightRules = require("./text_highlight_rules").TextHighlightRules;
var Behaviour = require("./behaviour").Behaviour;

var WoolHighlightRules = function() {

    this.$rules = {
        start: [
			// line comment symbol preceded by '\' is marked as plain text
            {
                token: "text",
                regex: "[\\\\](?://.+$)"
            },
			// otherwise, line comment symbol starts comment
            {
                token: "comment",
                regex: "//.+$"
            },
            {
                token: "paren.lcomm",
                regex: "<<",
                next: "comm"
            },
            {
                token: "paren.llink",
                regex: "^\\s*\\[\\[",
                next: "link"
            },
            {
                token: "inlinevariable",
                regex: "\\$[a-zA-Z0-9_]+"
            }
        ],
        link: [
            {
                token: "string.comm",
                regex: "<<[^>]*>>"
            },
            {
                token: "paren.llink",
                regex: "\\|"
            },
            {
                token: "string.rlink",
                regex: "[^<>\\]]"
            },
            //{
            //    token: "string.rlink",
            //    regex: "\\|\\w*[a-zA-Z0-9 ]+"
            //},
            //{
            //    token: "string.llink",
            //    regex: "[a-zA-Z0-9._ ]+"
            //},
            {
                token: "paren.rlink",
                regex: "\\]\\]",
                next: "start"
            }
        ],
        comm: [
            {
                token: "string.comm",
                regex: "[^>]+"
            },
            {
                token: "paren.rcomm",
                regex: ">>",
                next: "start"
            }
        ]
    }

};

var Mode = function() {
    this.HighlightRules = WoolHighlightRules;
    this.$behaviour = new Behaviour();
};

oop.inherits(WoolHighlightRules, TextHighlightRules);
oop.inherits(Mode, TextMode);

(function() {
    this.type = "text";
    this.getNextLineIndent = function(state, line, tab) {
        return '';
    };
    this.$id = "ace/mode/wool";
}).call(Mode.prototype);

exports.Mode = Mode;
});
