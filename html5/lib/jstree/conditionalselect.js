
// conditionalselect plugin
// From: https://groups.google.com/g/jstree/c/Uc-8009dW4s
//
// defines a conditionalselect function.
//
// When conditionalselect is enabled, the conditionalselect(nodeid) function
// is called before select_node(), and select_node is not called when the
// return value is false.
//
// usage:
//
// $("#tree")
//	  .jstree({
//			"conditionalselect" : function (node) {
//				return node.text.indexOf('x') !== -1;
//			},
//			"plugins" : [ ... ,"conditionalselect"],
//			...



(function ($) {
	$.jstree.defaults.conditionalselect = function () { return true; };

	$.jstree.plugins.conditionalselect = function (options, parent) {
		// redefine select_node
		// userParameter is an extra parameter that can be used to indicate
		// a payload when select_node was called by the user.
		this.select_node = function (obj, supress_event, prevent_open,
		e, userParameter) {
			if(this.settings.conditionalselect.call(this, this.get_node(obj),
			userParameter)) {
				parent.select_node.call(this, obj,supress_event,prevent_open,e);
			}
		};
	};
})(jQuery);


