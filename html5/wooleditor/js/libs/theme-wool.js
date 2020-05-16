define("ace/theme/wool",["require","exports","module","ace/lib/dom"], function(require, exports, module) {

exports.isDark = false;
exports.cssClass = "ace-wool";
exports.cssText = "\
.ace-wool .ace_gutter {\
background: #e8e8e8;\
color: #AAA;\
}\
.ace-wool  {\
background: #fff;\
color: #000;\
}\
.ace-wool .ace_comment {\
color: #2a2;\
font-style: italic;\
}\
.ace-wool .ace_inlinevariable {\
color: #22f;\
}\
.ace-wool .ace_variable.ace_language  {\
color: #0086B3;\
}\
.ace-wool .ace_paren {\
font-weight: bold;\
}\
.ace-wool .ace_string.ace_llink {\
color: #000;\
}\
.ace-wool .ace_string.ace_rlink {\
color: #1ab;\
}\
.ace-wool .ace_string.ace_comm {\
color: #e93ecf;\
}\
.ace-wool .ace_paren.ace_lcomm, .ace-wool .ace_paren.ace_rcomm {\
color: #e00ec0;\
}\
.ace-wool .ace_paren.ace_llink, .ace-wool .ace_paren.ace_rlink {\
color: #1cd;\
font-weight: bold;\
}\
.ace-wool .ace_variable.ace_instance {\
color: teal;\
}\
.ace-wool .ace_constant.ace_language {\
font-weight: bold;\
}\
.ace-wool .ace_cursor {\
color: black;\
}\
.ace-wool .ace_marker-layer .ace_active-line {\
background: rgb(255, 255, 204);\
}\
.ace-wool .ace_marker-layer .ace_selection {\
background: rgb(181, 213, 255);\
}\
.ace-wool.ace_multiselect .ace_selection.ace_start {\
box-shadow: 0 0 3px 0px white;\
border-radius: 2px;\
}\
.ace-wool.ace_nobold .ace_line > span {\
font-weight: normal !important;\
}\
.ace-wool .ace_marker-layer .ace_step {\
background: rgb(252, 255, 0);\
}\
.ace-wool .ace_marker-layer .ace_stack {\
background: rgb(164, 229, 101);\
}\
.ace-wool .ace_marker-layer .ace_bracket {\
margin: -1px 0 0 -1px;\
border: 1px solid rgb(192, 192, 192);\
}\
.ace-wool .ace_gutter-active-line {\
background-color : rgba(0, 0, 0, 0.07);\
}\
.ace-wool .ace_marker-layer .ace_selected-word {\
background: rgb(250, 250, 255);\
border: 1px solid rgb(200, 200, 250);\
}\
.ace-wool .ace_print-margin {\
width: 1px;\
background: #e8e8e8;\
}\
.ace-wool .ace_indent-guide {\
background: url(\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAACCAYAAACZgbYnAAAAE0lEQVQImWP4////f4bLly//BwAmVgd1/w11/gAAAABJRU5ErkJggg==\") right repeat-y;\
}";

    var dom = require("../lib/dom");
    dom.importCssString(exports.cssText, exports.cssClass);
});
