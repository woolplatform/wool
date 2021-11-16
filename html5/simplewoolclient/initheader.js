// valid parameters:
// config - display config
// code - source code of dialogue
// editable - if defined, make display config editable

// vars - json of key-value pairs, to be entered into the variable store.
//     Keys are variable names without the "$". Ex:
//     { "var1": "value1", "var2": 2, "var3": true }
// resources - json string with list of resources to be preloaded for the
//     browser version. Example:
//     [ "nl/file1.wool", "nl/file2.wool", "en/file1.json" ]
// dialoguepath - path of dialogue to start with. Only applicable when
//     resources are supplied.
// defaultlanguage - defaultLanguage and currentLanguage are set to this
//     value. Is only applicable when resources are supplied.
// redirecturl - url to redirect to when reaching end of dialogue. All variables
//     in the variable store are supplied as a GET parameter containing
//     key-value pairs for each of the variables, following the same format as
//     the vars input parameter.
//     Ex: if redirect = "/process.php?dialogue=1&", the full URL will be
//     "/process.php?dialogue=1&vars=..."
// style (optional) - style name to add.  Will add css/customstyle_<style>.css

var LOCALSTORAGEPREFIX="wool_js_";

var NARRATOR = "Narrator";

var RESOURCEBASEDIR = "dialogues";

// get urlParams and config ------------------------------------------------
var urlParams = Utils.getUrlParameters();


if (urlParams.style) {
	document.head.innerHTML += '<link rel="stylesheet" type="text/css" href="css/custom/'
		 + urlParams.style + '.css">';
}

