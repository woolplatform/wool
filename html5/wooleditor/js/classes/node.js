var globalNodeIndex = 0;
const NodeExpandWidth = 300;
const NodeExpandHeight = 150;
const ClipNodeTextLength = 1024;

var Node = function() {
	var self = this;

	// primary values
	this.index = ko.observable(globalNodeIndex++);
	this.title = ko.observable("Node" + this.index());
	this.tags = ko.observable("");
	this.speaker = ko.observable("");
	this.body = ko.observable("");
	// alternative way to get x,y, because there may a be delay getting them
	// through css
	this.assignedx = 0;
	this.assignedy = 0;
	//this.x = ko.observable(128);
	//this.y = ko.observable(128);
	this.active = ko.observable(true);
	this.tempWidth;
	this.tempHeight;
	this.tempOpacity;
	this.style;
	this.colorID = ko.observable(0);
	this.checked = false;
	this.selected = false;
	this.compiledNode = null;
	this.lastCompiled = new Date().getTime() - 1000000;
	// clipped values for display
	this.clippedTags = ko.computed(function() {
		var tags = this.tags().split(" ");
		var output = "";
		if (this.tags().length > 0) {
			for (var i = 0; i < tags.length; i ++)
				output += '<span>' + tags[i] + '</span>';
		}
        return output;
    }, this);

	this.clippedBody = ko.computed(function() {
		var result = app.getHighlightedText(this.body());
		while (result.indexOf("\n") >= 0)
			result = result.replace("\n", "<br />");
		while (result.indexOf("\r") >= 0)
			result = result.replace("\r", "<br />");
		result = result.substr(0, ClipNodeTextLength);
        return result;
    }, this);

	// internal cache
	this.linkedTo = ko.observableArray();
	this.linkedFrom = ko.observableArray();
	// links to nonexistent nodes {line,node}
	this.linkedToUndefined = [];
	this.linkedToExternal = [];

	// reference to element containing us
	this.element = null;

	this.canDoubleClick = true;

	this.create = function() {
		EditorUtils.pushToTop($(self.element));
		self.style = window.getComputedStyle($(self.element).get(0));

		var parent = $(self.element).parent();
		self.x(-parent.offset().left + $(window).width() / 2 - 100);
		self.y(-parent.offset().top + $(window).height() / 2 - 100);


		//var updateArrowsInterval = setInterval(app.updateArrowsThrottled, 16);

		// replacement if transition goes wrong
		//$(self.element).css({opacity: 1, scale: 1});
		$(self.element)
			.css({opacity: 0, scale: 0.8, y: "-=80px", rotate: "45deg"})
			.transition(
				{
					opacity: 1,
					scale: 1,
					y: "+=80px",
					rotate: "0deg"
				},
				250,
				"easeInQuad",
				function() {
					//clearInterval(updateArrowsInterval);
					app.updateArrows();
				}
			);
		self.initDrag();

		$(self.element).on("dblclick", function()
		{
			if (self.canDoubleClick)
				app.editNode(self);
		});

		$(self.element).on("click", function(e)
		{
			if(e.ctrlKey)
			{
				if(self.selected)
					app.removeNodeSelection(self);
				else
					app.addNodeSelected(self);
			}
		});
	}

	this.setSelected = function(select)
	{
		self.selected = select;
		
		if(self.selected) 
			$(self.element).css({border: "3px solid #49eff1"});
		else 
			$(self.element).css({border: ""});
		
	}

	this.toggleSelected = function()
	{
		self.setSelected(!self.selected);
	}

	this.x = function(inX) {
		if (inX != undefined) {
			$(self.element).css({x:Math.floor(inX)});
			self.assignedx = inX;
		}
		return Math.floor((new WebKitCSSMatrix(self.style.webkitTransform)).m41);
	}

	this.y = function(inY) {
		if (inY != undefined) {
			$(self.element).css({y:Math.floor(inY)});
			self.assignedy = inY;
		}
		return Math.floor((new WebKitCSSMatrix(self.style.webkitTransform)).m42);
	}

	this.resetDoubleClick = function()
	{
		self.canDoubleClick = true;
	}

	this.tryRemove = function()
	{
		if (self.active())
			app.deleting(this);

		setTimeout(self.resetDoubleClick, 500);
		self.canDoubleClick = false;
	}

	this.cycleColorDown = function()
	{
		self.doCycleColorDown();

		setTimeout(self.resetDoubleClick, 500);
		self.canDoubleClick = false;

		if (app.shifted)
			app.matchConnectedColorID(self);

		if(self.selected)
			app.setSelectedColors(self);
	}

	this.cycleColorUp = function()
	{	
		self.doCycleColorUp();

		setTimeout(self.resetDoubleClick, 500);
		self.canDoubleClick = false;

		if (app.shifted)
			app.matchConnectedColorID(self);

		if(self.selected)
			app.setSelectedColors(self);
	}

	this.doCycleColorDown = function()
	{
		self.colorID(self.colorID() - 1);
		if (self.colorID() < 0)
			self.colorID(9);
	}

	this.doCycleColorUp = function()
	{
		self.colorID(self.colorID() + 1);
		if (self.colorID() > 9)
			self.colorID(0);
	}
	
	this.remove = function()
	{
		$(self.element).transition({opacity: 0, scale: 0.8, y: "-=80px", rotate: "-45deg"}, 250, "easeInQuad", function()
		{
			app.removeNode(self);
			// check if Start node is deleted, make new Start node
			if (app.addStartNodeIfMissing()) {
				alert("Start node required. Creating new one.");
			}
			app.updateArrows();
		});
		app.deleting(null);
	}

	this.initDrag = function() {
		var dragging = false;
		var groupDragging = false;

		var offset = [0, 0];
		var moved = false;

		// XXX the global event handlers are not removed when a node is deleted

		$(document.body).on("mousemove", function(e) 
		{
			if (dragging)
			{
				var parent = $(self.element).parent();
				var newX = (e.pageX / self.getScale() - offset[0]);
				var newY = (e.pageY / self.getScale() - offset[1]);
				var movedX = newX - self.x();
				var movedY = newY - self.y();

				moved = true;
				self.x(newX);
				self.y(newY);

				if (groupDragging)
				{
					var nodes = [];
					if(self.selected)
					{
						nodes = app.getSelectedNodes();
						nodes.splice(nodes.indexOf(self), 1);
					}	
					else
					{
						nodes = app.getNodesConnectedTo(self);
					}
					
					if (nodes.length > 0)
					{
						for (var i in nodes)
						{
							nodes[i].x(nodes[i].x() + movedX);
							nodes[i].y(nodes[i].y() + movedY);
						}
					}
				}


				//app.refresh();
				app.updateArrows();
			}
		});

		$(self.element).on("mousedown", function (e) 
		{
			if (!dragging && self.active())
			{
				var parent = $(self.element).parent();

				dragging = true;

				if (app.shifted || self.selected)
				{
					groupDragging = true;
				}

				offset[0] = (e.pageX / self.getScale() - self.x());
				offset[1] = (e.pageY / self.getScale() - self.y());
			}
		});

		$(self.element).on("mousedown", function(e)
		{
			e.stopPropagation();
		});

		$(self.element).on("mouseup", function (e)
		{
			//alert("" + e.target.nodeName);
			if (!moved)
				app.mouseUpOnNodeNotMoved();

			moved = false;
		});

		$(document.body).on("mouseup", function (e) 
		{
			dragging = false;
			groupDragging = false;
			moved = false;
			// XXX this call causes a huge number of updates, slowing down the
			// system. Moreover, there is a memory leak which causes the
			// number of calls to increase if new files are loaded.
			//app.updateArrows();
		});
	}

	this.moveTo = function(newX, newY) {
		console.log("moveTo");
		$(self.element).clearQueue();
		$(self.element).css('x',newX);
		$(self.element).css('y',newY);
		app.updateArrows();
		setTimeout(data.saveToBuffer,500);
		/*$(self.element).transition(
			{
				x: newX,
				y: newY
			},
			app.updateArrows,
			500
		);*/
	}

	this.isConnectedTo = function(otherNode, checkBack)
	{
		if (checkBack && otherNode.isConnectedTo(self, false))
			return true;

		var linkedNodes = self.linkedTo();
		for (var i in linkedNodes)
		{
			if (linkedNodes[i] == otherNode)
				return true;
			if (linkedNodes[i].isConnectedTo(otherNode, false))
				return true;
			if (otherNode.isConnectedTo(linkedNodes[i], false))
				return true;
		}

		return false;
	}

	this.updateLinks = function(force) {
		// obtain links from compiler
		self.compile(force);
		var errs = self.compiledNode.errors;
		self.resetDoubleClick();
		// clear existing links
		self.linkedTo.removeAll();
		self.linkedToUndefined = [];
		self.linkedToExternal = [];

		// find all the links
		var allLinks = self.compiledNode.links;
		// Same regex as used in woolserver-js
		/*
		var lines = self.body().split(/\r?\n/);
		var allLinks = []; // {line,node}
		var exists = {};
		for (var l=0; l<lines.length; l++) {
			// TODO let woolserver-js do this
			var links = lines[l].match(/^\[\[(.+)\]\]$/);
			if (links != undefined) {
				links = links[1].trim(); // first capture group

				if (links.indexOf("|") >= 0)
					links = links.split("|")[1];

				if (!exists[links]) {
					allLinks.push({line:l,node:links});
				}
				
				exists[links] = true;
			}
		}
		*/
		// update links
		for (var i = 0; i < allLinks.length; i ++) {
			var link = allLinks[i];
			var found = false;
			for (var index in app.nodes()) {
				var other = app.nodes()[index];
				if (other.title().toLowerCase().trim() == link.node.toLowerCase()) {
					self.linkedTo.push(other);
					found = true;
					break;
				}
			}
			if (!found) {
				if (link.node.indexOf(".") >= 0) {
					self.linkedToExternal.push(link);
					// dialogue.node
				} else {
					self.linkedToUndefined.push(link);
				}
			}
		}
	}

	this.getScale = function() {
		if (app && typeof app.cachedScale === 'number') {
			return app.cachedScale;
		} else {
			return 1;
		}
	}
	// compiles max once every 2 seconds, unless force=true
	this.compile = function(force) {
		var now = new Date().getTime();
		if (self.lastCompiled && !force && now - self.lastCompiled < 2000)
			return;
		self.lastCompiled = now;
		var nodesource = data.getSaveData(FILETYPE.WOOL,this);
		directServerLoadDialogue("dialogue",nodesource);
		self.compiledNode = directServer.dialogues["dialogue"].nodes[0];
	}
	// compiles max once every 2 seconds, unless force=true
	this.getErrors = function(force) {
		self.compile(force);
		// find undefined links for error reporting
		self.updateLinks();
		var errs = self.compiledNode.errors;
		var errannot = [];
		var errtexts = [];
		for (var i=0; i<errs.length; i++) {
			var err = errs[i];
			console.log(err);
			if (err.line!==null) {
				var errtype;
				var errtype = "error";
				if (err.level=="fatal") {
					errtype = "error";
				} else if (err.level=="error") {
					errtype = "error";
				} else if (err.level=="warning") {
					errtype = "warning";
				} else if (err.level=="notice") {
					errtype = "info";
				}
				errannot.push({
					row: err.line,
					column: 0,
					text: err.level+": "+err.msg,
					type: errtype,
				});
			} else {
				errtexts.push(err.level+": "+err.msg);
			}
		}
		for (i=0; i<self.linkedToUndefined.length; i++) {
			var link = self.linkedToUndefined[i];
			errannot.push({
				row: link.line,
				column: 0,
				text: "error: Link to nonexistent node "+link.node,
				type: "error",
			});
		}
		for (i=0; i<self.linkedToExternal.length; i++) {
			var link = self.linkedToExternal[i];
			if (!app.isNwjs) {
				errannot.push({
					row: link.line,
					column: 0,
					text: "notice: Link to another dialogue not supported in browser version",
					type: "warning",
				});
			}
		}
		// check duplicate names
		if (self.checkDuplicateTitles()) {
			errtexts.push("error: title is already used by another node");
		}
		//app.editor.getSession().addMarker(new Range(1,2,1,10),"myclass","line",false);
		app.editor.getSession().setAnnotations(errannot);
		document.getElementById("node-errors").innerHTML =
			errannot.length || errtexts.length ? "There are errors." : "";
		for (var i=0; i<errtexts.length; i++) {
			var div = document.createElement("div");
			div.innerText = errtexts[i];
			document.getElementById("node-errors").appendChild(div);
		}
		//app.editor.getSession().addMarker(new Range(1,2,1,2),"mycssclass",
		//	"background",false);
	}
	this.checkDuplicateTitles = function() {
		var nodes = app.nodes();
		for (var i=0; i<nodes.length; i++) {
			var node = nodes[i];
			if (node!=self
				&&     node.title().toLowerCase().trim()
				    == self.title().toLowerCase().trim()   ) {
				return true;
			}
		}
		return false;
	}
	this.hasErrors = function() {
		var nodesource = data.getSaveData(FILETYPE.WOOL,this);
		directServerLoadDialogue("dialogue",nodesource);
		var errs = directServer.dialogues["dialogue"].nodes[0].errors;
		self.updateLinks();
		return errs.length > 0 || self.linkedToUndefined.length > 0
			|| self.checkDuplicateTitles();
	}

	// translation ---------------------------------------------------------

	// called when user edits translation. Updates any copies of this
	// translation in other nodes.
	// "this" is translationTexts element {speaker,source,translation}
	this.updateTranslation = function(value) {
		var nodes = app.nodes();
		for (var i in nodes) {
			if (nodes[i].translationTexts) {
				//console.log("Updating node:"+nodes[i].title());
				var texts =  nodes[i].translationTexts();
				for (var j in texts) {
					if (texts[j].source != this.source) continue;
					if (texts[j].speaker != this.speaker) continue;
					// speaker and source matches => update translation
					texts[j].translation(value);
				}
			}
		}
		//console.log(this.speaker+"|"+this.source);
	}
	// defines translationTexts
	// make sure compiledNode is defined before calling (i.e. call compile())
	this.getTextsForTranslation = function() {
		var langDefs = app.getLangDefs(true);
		var ret = [];
		for (var text in self.compiledNode.agenttexts) {
			if (text == "") continue;
			var translation=app.getAndMarkLangDef(langDefs,self.speaker(),text);
			// XXX "text" seems to be a predefined field in KO
			var elem = {
				speaker: self.speaker() ? self.speaker() : "UNKNOWN",
				source: text,
				translation: ko.observable(translation),
			};
			elem.translation.subscribe(self.updateTranslation,elem);
			ret.push(elem);
		}
		for (var text in self.compiledNode.usertexts) {
			if (text == "") continue;
			var translation=app.getAndMarkLangDef(langDefs,"_user",text);
			// XXX "text" seems to be a predefined field in KO
			var elem = {
				speaker: "_user",
				source: text,
				translation: ko.observable(translation),
			};
			elem.translation.subscribe(self.updateTranslation,elem);
			ret.push(elem);
		}
		self.translationTexts = ko.observableArray(ret);
	}
}


// KO handlers ------------------------------------------------------

ko.bindingHandlers.nodeBind = {
	init: function(element, valueAccessor, allBindings, viewModel, bindingContext) 
	{
		bindingContext.$rawData.element = element;
		bindingContext.$rawData.create();
	},

	update: function(element, valueAccessor, allBindings, viewModel, bindingContext) 
	{
		$(element).on("mousedown", function() { EditorUtils.pushToTop($(element)); });
	}
};

ko.bindingHandlers.translationBind = {
	init: function(element, valueAccessor, allBindings, viewModel, bindingContext) 
	{
	},

	update: function(element, valueAccessor, allBindings, viewModel, bindingContext) 
	{
		//setTimeout(function() {
			var scroll_height = $(element).get(0).scrollHeight;
			$(element).css('height', scroll_height + 'px');
		//}, 1000);
		// auto-resize translation textareas
		//https://usefulangle.com/post/41/javascript-textarea-autogrow-adjust-height-based-on-content
		$(element).on('input', function(eventObject) {
			var scroll_height = $(element).get(0).scrollHeight;
			$(element).css('height', scroll_height + 'px');
		});

	}
};


