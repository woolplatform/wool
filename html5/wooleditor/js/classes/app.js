var App = function(name, version, filename) {
	var self = this;
	this.instance = this;
	this.domroot = null; // DOM root of visual editor
	this.name = ko.observable(name);
	this.version = ko.observable(version);
	this.filename = ko.observable(filename);
	this.editing = ko.observable(null);
	this.editor = null; // ace editor object
	this.deleting = ko.observable(null);
	this.translating = ko.observable(null);
	this.languages = ko.observableArray([]);
	this.defaultLanguage = ko.observable(
		localStorage.getItem(App.LOCALSTORAGEPREFIX+"defaultlanguage") );
	this.selectedLanguage = ko.observable(
		localStorage.getItem(App.LOCALSTORAGEPREFIX+"language") );
	this.editingAgent= ko.observable(null);
	this.nodes = ko.observableArray([]);
	this.cachedScale = 1;
	this.canvas;
	this.context;
	this.nodeHistory = ko.observableArray();
	this.nodeFuture = ko.observableArray();
	this.editingHistory = [];
	//this.appleCmdKey = false;
	this.editingSaveHistoryTimeout = null;
	this.dirty = false;
	this.woolFileHasCR = ko.observable(false); // true -> crlf [dos], false -> cr [unix]
	this.focusedNodeIdx = -1;
	this.zoomSpeed = .005;
	this.zoomLimitMin = .05;
	this.zoomLimitMax = 1;
	this.transformOrigin = [
		0,
		0
	];
	this.shifted = false;
  	this.isNwjs = false;

	this.lastSavedSource = null;
	//this.unsavedChanges = false; // TODO

	this.urlParameters = Utils.getUrlParameters();

	this.UPDATE_ARROWS_THROTTLE_MS = 50;

	this.waitSpinnerShowing = false;

	//this.editingPath = ko.observable(null);

	this.nodeSelection = [];

	this.$searchField = $(".search-field");

	this.metadata = new Metadata();

	// node-webkit
	this.isNwjs = detectNodeJS();
	this.fs = getPlatformFileSystem();

	// getters / setters / helpers -------------------------------------

	this.numberOfNodes = function() {
		var nr = self.nodes().length;
		return "("+nr+" node"+(nr==1 ? "" : "s")+")";
	}

	this.crlfFormat = function() {
		return self.woolFileHasCR() ? "[dos]" : "[unix]";
	}

	this.closeEditors = function() {
		self.editing(null);
		self.translating(null);
	}

	this.getBasicLanguages = function(){
		var ret = [];
		for (i=0; i<basicLanguageCodes.length; i++) {
			var code = basicLanguageCodes[i];
			ret.push(code[0]+" ("+code[1]+")");
		}
		return ret;
	}

	this.getAllLanguages = function(){
		var ret = [];
		for (i=0; i<allLanguageRegionCodes.length; i++) {
			var code = allLanguageRegionCodes[i];
			ret.push(code[0]+" ("+code[1]+", "+code[2]+")");
		}
		return ret;
	}

	this.getAllSpeakers = function() {
		var speakers = [];
		var nodes = self.nodes();
		for (var i in nodes) {
			var speaker = nodes[i].speaker();
			if (speakers.indexOf(speaker) == -1)
				speakers.push(speaker);
		}
		return speakers;
	}
	// returns: true if start node was added
	this.addStartNodeIfMissing = function() {
		// check if Start node present
		var nodes = self.nodes();
		var foundStart=false;
		for (var i in nodes) {
			if (nodes[i].title()=="Start") {
				foundStart=true;
				break;
			}
		}
		if (!foundStart) {
			self.newNode().title("Start");
			return true;
		}
		return false;
	}

	this.trim = function(x) {
		return x.replace(/^\s+|\s+$/gm,'');
	}

	// INIT --------------------------------------------------------

	this.run = function() {
		//TODO(Al):
		// delete mutliple nodes at the same time

		var osName = "Unknown OS";
		if (navigator.platform.indexOf("Win")!=-1) osName="Windows";
		if (navigator.platform.indexOf("Mac")!=-1) osName="MacOS";
		if (navigator.platform.indexOf("X11")!=-1) osName="UNIX";
		if (navigator.platform.indexOf("Linux")!=-1) osName="Linux";

		if (osName == "Windows")
			self.zoomSpeed = .1;

		if (osName == "Linux")
			self.zoomSpeed = .1;

		this.domroot = $("#app")
		this.domroot.show();
		this.domroot.focus();
		ko.applyBindings(self, this.domroot[0]);

		self.canvas = $(".arrows")[0];
		self.context = self.canvas.getContext('2d');
		// load old defs here
		var dataloaded = data.loadFromBuffer();
		var startadded = self.addStartNodeIfMissing();
		if (dataloaded && startadded) {
			alert("Missing Start node added.");
		}

		// Title bar
		self.refreshWindowTitle();
		// Mac title bar
		if (osName != "Windows" && osName != "Linux" && self.gui != undefined){
			var win = self.gui.Window.get();
			var nativeMenuBar = new self.gui.Menu({ type: "menubar" });
			if(nativeMenuBar.createMacBuiltin) {
				nativeMenuBar.createMacBuiltin("Wool");
			}
			win.menu = nativeMenuBar;
		}

		// search field enter
		self.$searchField.on("keydown", function (e) {
				// enter
				if (e.keyCode == 13)
					self.searchWarp();

				// escape
				if (e.keyCode == 27)
					self.clearSearch();
			});

		// prevent click bubbling
		ko.bindingHandlers.preventBubble = {
			init: function(element, valueAccessor) {
				var eventName = ko.utils.unwrapObservable(valueAccessor());
				ko.utils.registerEventHandler(element,eventName,function(event){
					event.cancelBubble = true;
					if (event.stopPropagation)
						event.stopPropagation();
				});
			}
		};

		ko.bindingHandlers.mousedown = {
			init: function(element, valueAccessor, allBindings, viewModel, bindingContext) {
				var value = ko.unwrap(valueAccessor());
				$(element).mousedown(function() {
					value();
				});
			}
		};

		// updateArrows
		// setInterval(function() { self.updateArrows(); }, 16);

		// handle drag view and selection marquee
		(function() {
			var dragging = false;
			var offset = { x: 0, y: 0 };
			var MarqueeOn = false;
			var MarqueeSelection = [];
			var MarqRect = {x1:0,y1:0,x2:0,y2:0};
			var MarqueeOffset = [0, 0];

			function stopDragging() {
				dragging = false;

				if(MarqueeOn && MarqueeSelection.length == 0) {
					self.deselectAllNodes();
				}

				MarqueeSelection = [];
				MarqRect = {x1:0,y1:0,x2:0,y2:0};
				$("#marquee").css({x:0, y:0, width:0, height:0});
				MarqueeOn = false;

				//XXX save after a second because position is obtained from css
				//transform which is not updated immediately after style is set.
				// TODO maintain node position in different way
				// Coords are no longer changed, so not needed
				//setTimeout(data.saveToBuffer,500);
			}

			$(".nodes").on("mousedown", function(e) {
				// start either drag view or draw marquee
				if (e.buttons&2) return; // right button is create node
				// reset marquee and offset
				$("#marquee").css({x:0, y:0, width:0, height:0});
				dragging = true;
				var rootofs = self.domroot.offset();
				offset.x = e.pageX - rootofs.left;
				offset.y = e.pageY - rootofs.top;
				MarqueeSelection = [];
				MarqRect = {x1:0,y1:0,x2:0,y2:0};

				var scale = self.cachedScale;

				MarqueeOffset[0] = 0;
				MarqueeOffset[1] = 0;
				// choose between marquee drag or view drag
				if (!e.altKey && !e.shiftKey && !(e.buttons&4)) {
					self.deselectAllNodes();
					MarqueeOn = true;
				}
			});

			$(".nodes").on("mousemove", function(e) {
				var rootofs = self.domroot.offset();
				var erootX = e.pageX - rootofs.left;
				var erootY = e.pageY - rootofs.top;
				if (dragging) {
					// middle button or shift/alt: drag the view
					// We added shiftKey because altKey interferes with
					// xfce metakey
					//if (e.shiftKey || e.altKey || e.buttons & 4) {
					if (!MarqueeOn) {
						// drag view
						self.transformOrigin[0] += (erootX - offset.x);
						self.transformOrigin[1] += (erootY - offset.y);

						offset.x = erootX;
						offset.y = erootY;
						self.translate();
					} else {
						// drag marquee
						var scale = self.cachedScale;

						if(erootX > offset.x && erootY < offset.y) {
							MarqRect.x1 = offset.x;
							MarqRect.y1 = erootY;
							MarqRect.x2 = erootX;
							MarqRect.y2 = offset.y;
						} else if(erootX > offset.x && erootY > offset.y) {
							MarqRect.x1 = offset.x;
							MarqRect.y1 = offset.y;
							MarqRect.x2 = erootX;
							MarqRect.y2 = erootY;
						} else if(erootX < offset.x && erootY < offset.y) {
							MarqRect.x1 = erootX;
							MarqRect.y1 = erootY;
							MarqRect.x2 = offset.x;
							MarqRect.y2 = offset.y;
						} else {
							MarqRect.x1 = erootX;
							MarqRect.y1 = offset.y;
							MarqRect.x2 = offset.x;
							MarqRect.y2 = erootY;
						}

						$("#marquee").css({ x:MarqRect.x1, 
							y:MarqRect.y1,
							width:Math.abs(MarqRect.x1-MarqRect.x2),
							height:Math.abs(MarqRect.y1-MarqRect.y2)});

						//Select nodes which are within the marquee
						// MarqueeSelection is used to prevent it from deselecting already
						// selected nodes and deselecting onces which have been selected
						// by the marquee 
						var nodes = self.nodes();
						for (var i in nodes) {
							var index = MarqueeSelection.indexOf(nodes[i]);
							var inMarqueeSelection = (index >= 0);

							//test the Marquee scaled to the nodes x,y values

							var holder = $(".nodes-holder").offset(); 
							holder.left -= rootofs.left;
							var marqueeOverNode = (MarqRect.x2 - holder.left) / scale > nodes[i].x()  
											   && (MarqRect.x1 - holder.left) / scale < nodes[i].x() + nodes[i].tempWidth
        									   && (MarqRect.y2 - holder.top) / scale > nodes[i].y()   
        									   && (MarqRect.y1 - holder.top) / scale < nodes[i].y() + nodes[i].tempHeight;

							if(marqueeOverNode) {
								if(!inMarqueeSelection) {
									self.addNodeSelected(nodes[i]);
									MarqueeSelection.push(nodes[i]);
								}
							} else {
								if(inMarqueeSelection) {
									self.removeNodeSelection(nodes[i]);
									MarqueeSelection.splice(index, 1);
								}
							}
						}
					}
					
				}

			});

			$(".nodes").on("mouseup", function(e) {
				stopDragging();
			});
			$(".nodes").on("mouseleave", function(e) {
			});
			$(".nodes").on("mouseenter", function(e) {
				// stop dragging if buttons were released outside of element
				if (!(e.buttons&4 || e.buttons&1)) {
					stopDragging();
				}
			});
		})();

		// search field
		self.$searchField.on('input', self.updateSearch);
		$(".search-title input").click(self.updateSearch);
		$(".search-body input").click(self.updateSearch);
		$(".search-tags input").click(self.updateSearch);

		// using the event helper
		$('.nodes').mousewheel(function(event) {
			// https://github.com/InfiniteAmmoInc/Yarn/issues/40
			if (event.altKey) {
				return;
			} else {
				event.preventDefault();
			}

			var lastZoom = self.cachedScale,
				scaleChange = event.deltaY * self.zoomSpeed * self.cachedScale;

			if (self.cachedScale + scaleChange > self.zoomLimitMax) {
				self.cachedScale = self.zoomLimitMax;
			} else if (self.cachedScale + scaleChange < self.zoomLimitMin) {
				self.cachedScale = self.zoomLimitMin;
			} else {
				self.cachedScale += scaleChange;
			};

			var rootofs = self.domroot.offset();
			var mouseX = event.pageX - rootofs.left - self.transformOrigin[0],
				mouseY = event.pageY - rootofs.top - self.transformOrigin[1],
				newX = mouseX * (self.cachedScale / lastZoom),
				newY = mouseY * (self.cachedScale / lastZoom),
				deltaX = (mouseX - newX),
				deltaY = (mouseY - newY);

			self.transformOrigin[0] += deltaX;
			self.transformOrigin[1] += deltaY;

			self.translate();
		});

		this.domroot.on('keyup keydown', function(e) {
			self.shifted = e.shiftKey; 
		} );

		this.domroot.contextmenu( function(e){
			var isAllowedEl = (
					$(e.target).hasClass('nodes') ||
					$(e.target).parents('.nodes').length
				);

			if( e.button == 2 && isAllowedEl ) {
				var x = self.transformOrigin[0] * -1 / self.cachedScale,
					y = self.transformOrigin[1] * -1 / self.cachedScale;

				var rootofs = self.domroot.offset();
				x += (e.pageX - rootofs.left) / self.cachedScale;
				y += (e.pageY - rootofs.top) / self.cachedScale;

				self.newNode(x, y); 
			} 

			return !isAllowedEl; 
		}); 

		// global control-key shortkuts
		this.domroot.on('keydown', function(e){
			if((e.metaKey || e.ctrlKey) && !self.editing()) {
				switch(e.keyCode) {
					// ctrl z
					case 90: self.historyDirection("undo");
					break;
					// ctrl y
					case 89: self.historyDirection("redo");
					break;
					// ctrl d
					case 68: self.deselectAllNodes();
				}
			}
		});

        // cursors / wsad = move view
		// space: center successive nodes
		this.domroot.on('keydown', function(e) {
			if (self.translating() || self.editing() || self.$searchField.is(':focus') || e.ctrlKey || e.metaKey) return;                                                    
			var scale = self.cachedScale || 1,
				movement = scale * 400;

			if(e.shiftKey) {
				movement = scale * 100;
			}

			if (e.keyCode === 65 || e.keyCode === 37) {  // a or left arrow
				self.transformOrigin[0] += movement;
			} else if (e.keyCode === 68 || e.keyCode === 39) {  // d or right arrow
				self.transformOrigin[0] -= movement;
			} else if (e.keyCode === 87 || e.keyCode === 38) {  // w or up arrow
				self.transformOrigin[1] += movement;
			} else if (e.keyCode === 83 || e.keyCode === 40) {  // w or down arrow
				self.transformOrigin[1] -= movement;
			} else if (e.keyCode === 32) { // space
				var selectedNodes = self.getSelectedNodes();
				var nodes = selectedNodes.length > 0
							? selectedNodes
							: self.nodes();
				var isNodeSelected = selectedNodes.length > 0;
				if (self.focusedNodeIdx > -1 && nodes.length > self.focusedNodeIdx
					&& (self.transformOrigin[0] != -nodes[self.focusedNodeIdx].x() + self.canvas.width / 2 - $(nodes[self.focusedNodeIdx].element).width() / 2
						|| self.transformOrigin[1] != -nodes[self.focusedNodeIdx].y() + self.canvas.height / 2 - $(nodes[self.focusedNodeIdx].element).height() / 2))
				{
					self.focusedNodeIdx = -1;
				}
				
				if (++self.focusedNodeIdx >= nodes.length) {
					self.focusedNodeIdx = 0;
				}
				self.cachedScale = 1;
				if (isNodeSelected) {
					self.warpToSelectedNodeIdx(self.focusedNodeIdx);
				}
				else {
					self.warpToNodeIdx(self.focusedNodeIdx);
				}
			}

			self.translate(100);
		} );

		$(window).on('resize', function() {
			self.translate();
		});

		this.domroot.on('keyup keydown mousedown mouseup', function(e) {
			if(self.editing() != null) {
				self.updateEditorStats();
			}
		});
		//self.transformOrigin += 100;
		// apple command key
		//$(window).on('keydown', function(e) { if (e.keyCode == 91 || e.keyCode == 93) { self.appleCmdKey = true; } });
		//$(window).on('keyup', function(e) { if (e.keyCode == 91 || e.keyCode == 93) { self.appleCmdKey = false; } });


		// init after nodes loaded

		if (this.urlParameters.editnode) {
			var nodes = this.nodes();
			for (var i=0; i<nodes.length; i++) {
				var node = nodes[i];
				if (node.title() == this.urlParameters.editnode) {
					this.warpToNodeXY(node.assignedx,node.assignedy);
					// XXX if we editNode immediately, there is a size problem,
					// causing shaking. Not sure what part needs time, so not
					// sure if we can find a proper callback. Could
					// be the css transform.
					setTimeout(function() { app.editNode(node) },500);
					break;
				}
			}
		}

		self.translate();

	} // END this.run()


	// UI / various ----------------------------------------------------

	this.getNodesConnectedTo = function(toNode) {
		var connectedNodes = [];
		var nodes = self.nodes();
		for (var i in nodes) {
			if (nodes[i] != toNode && nodes[i].isConnectedTo(toNode, true)) {
				var hasNode = false;
				for (var j in connectedNodes) {
					if (connectedNodes[j] == nodes[i]) {
						hasNode = true;
						break;
					}
				}
				if (!hasNode)
					connectedNodes.push(nodes[i]);
			}
		}
		return connectedNodes;
	}

	this.recordSavedChanges = function(content) {
		this.lastSavedSource = content;
		// XXX coordinates are not set properly with this function
		// (they are all the same)
		//this.lastSavedSource = data.getSaveData(FILETYPE.WOOL);
	}

	// NOTE: check does not work for file format changes,
	// in particular the "tags:" field was added later, and old files will
	// always report unsaved.
	this.areChangesSaved = function() {
		var newsource = data.normalizeSource(data.getSaveData(FILETYPE.WOOL));
		var oldsource = data.normalizeSource(this.lastSavedSource);
		return !oldsource || newsource == oldsource;
	}

	this.showWaitSpinner = function(show) {
		var spinner = document.getElementById("waitoverlay");
		spinner.style.display = show ? "block" : "none";
		self.waitSpinnerShowing  = show;
	}


	this.mouseUpOnNodeNotMoved = function() {
		self.deselectAllNodes();
	}

	this.matchConnectedColorID = function(fromNode) {
		var nodes = self.getNodesConnectedTo(fromNode);
		for (var i in nodes)
			nodes[i].colorID(fromNode.colorID());
	}

	this.quit = function() {
		if (self.gui != undefined) {
			self.gui.App.quit();
		}
	}

	// XXX does not seem to work
	this.refreshWindowTitle = function(editingPath) {
		if (!editingPath) {
			editingPath = App.getCurrentPath();
			if (!editingPath) return;
		}
		var gui = null;//require('nw.gui');

		if (!gui) return;

		// Get the current window
		var win = gui.Window.get();

		win.title = "Wool - [" + editingPath + "] ";// + (self.dirty?"*":"");
		// XXX it's complicated
		//localStorage.setItem(App.LOCALSTORAGEPREFIX+"path",editingPath);
	}


	// undo / redo -------------------------------------------------------

	this.recordNodeAction = function(action, node) {
		//we can't go forward in 'time' when
		//new actions have been made
		if(self.nodeFuture().length > 0) {
			for (var i = 0; i < self.nodeFuture().length; i++) {
				var future = self.nodeFuture.pop();
				delete future.node;
			};
		}

		var historyItem = {
			action: action,
			node: node,
			lastX: node.x(),
			lastY: node.y()
		};

		if (action == "removed") {
			historyItem.lastY+=80;
		}

		self.nodeHistory.push(historyItem);
	}

	this.getLastNodeOp = function(opstring) {
		return self.getLastNextNodeOp(self.nodeHistory,opstring);
	}

	this.getNextNodeOp = function(opstring) {
		return self.getLastNextNodeOp(self.nodeFuture,opstring);
	}

	this.getLastNextNodeOp = function(variable,opstring) {
		if (variable().length == 0) {
			return "[No "+opstring+"]";
		}
		if (variable()[variable().length-1].action=="created") {
			return opstring+" Add";
		} else {
			return opstring+" Del";
		}
	}

	this.historyDirection = function(direction) {
		function removeNode(node) {
			var index = self.nodes.indexOf(node);
			if  (index >= 0) {
				self.nodes.splice(index, 1);
			}
			self.updateNodeLinks();
		}

		var historyItem = null;

		if (direction == "undo") 
			historyItem = self.nodeHistory.pop();
		else
			historyItem = self.nodeFuture.pop();
		
		if (!historyItem) return;

		var action = historyItem.action;
		var node = historyItem.node;
		
		if (direction == "undo") {
			if (action == "created") {
				historyItem.lastX = node.x();
				historyItem.lastY = node.y();
				removeNode(node);
			} else if(action == "removed") {
				self.recreateNode(node, historyItem.lastX, historyItem.lastY);
			}
			self.nodeFuture.push(historyItem);
		} else { //redo undone actions
			if(action == "created") {
				self.recreateNode(node, historyItem.lastX, historyItem.lastY);
			} else if(action == "removed") {
				removeNode(node);
			}
			self.nodeHistory.push(historyItem);
		}		
	}

	this.recreateNode = function(node, x, y) {
		self.nodes.push(node);
		node.moveTo(x, y);
		self.updateNodeLinks(); 
	}


	// Node ops -----------------------------------------------------

	this.setSelectedColors = function(node) {
		var nodes = self.getSelectedNodes();
		nodes.splice(nodes.indexOf(node), 1);

		for(var i in nodes)
			nodes[i].colorID(node.colorID());		
	}

	this.getSelectedNodes = function() {
		var selectedNode = [];
		for(var i in self.nodeSelection) {
			selectedNode.push(self.nodeSelection[i]);
		}
		return selectedNode;
	}

	this.deselectAllNodes = function()
	{
		var nodes = self.nodes();
		for (var i in nodes)
		{
			self.removeNodeSelection(nodes[i]);
		}
	}

	this.addNodeSelected = function(node)
	{
		var index = self.nodeSelection.indexOf(node);
		if(index < 0)
		{
			self.nodeSelection.push(node);
			node.setSelected(true);
		}
	}

	this.removeNodeSelection = function(node)
	{
		var index = self.nodeSelection.indexOf(node);
		if  (index >= 0)
		{
			self.nodeSelection.splice(index, 1);
			node.setSelected(false);
		}
	}

	this.deleteSelectedNodes = function()
	{
		var nodes = self.getSelectedNodes();
		for(var i in nodes)
		{
			self.removeNodeSelection(nodes[i]);
			nodes[i].remove();
		}
	}

	this.newNode = function(x, y) {
		var node = new Node();
		
		self.nodes.push(node);
		//var center = this.getNodesCenter();
		// default coordinate is center of screen
		var center = {
			x: self.transformOrigin[0] * -1 / self.cachedScale,
			y: self.transformOrigin[1] * -1 / self.cachedScale,
		};
		var rootofs = self.domroot.offset();
		center.x += (self.domroot.width()/2) / self.cachedScale;
		center.y += (self.domroot.height()/2) / self.cachedScale;
		if (!isNaN(x)) {
			node.x(x-100);
		} else {
			console.log("center"+center.x);
			node.x(center.x);
		}
		if (!isNaN(y)) {
			node.y(y-100);
		} else {
			node.y(center.y);
		}
		self.updateNodeLinks();
		self.recordNodeAction("created", node);

		return node;
	}

	this.removeNode = function(node) {	
		if(node.selected) {
			self.deleteSelectedNodes();
		}
		var index = self.nodes.indexOf(node);
		if  (index >= 0) {
			self.recordNodeAction("removed", node);
			self.nodes.splice(index, 1);
		}
		self.updateNodeLinks();
	}

	this.editNode = function(node) {
		if (node.active()) {
			self.editing(node);

			$(".node-editor").css({ opacity: 0 }).transition({ opacity: 1 }, 250);
			$(".node-editor .form").css({ y: "-100" }).transition({ y: "0" }, 250);

			//enable_spellcheck();
			contents_modified = true;
			//spell_check();

			self.updateEditorStats();
			//self.editor.on("change",function() {
			//	self.updateEditorStats();
			//});
		}
	}

	this.saveNode = function() {
		if (self.editing() != null) {
			data.saveToBuffer();
			self.updateNodeLinks();

			self.editing().title(self.trim(self.editing().title()));

			$(".node-editor").transition({ opacity: 0 }, 250);
			$(".node-editor .form").transition({ y: "-100" }, 250, function()
			{
				self.editing(null);
			});

			setTimeout(self.updateSearch, 100);
		}
	}


	// UI -----------------------------------------------------

	this.updateSearch = function() {
		// Start node is checked after saveNode is finished, this is because
		// node.title is not updated immediately
		if (self.addStartNodeIfMissing()) {
			alert("Missing Start node added.");
		}
		var search = self.$searchField.val().toLowerCase();
		var speaker= $(".search-speaker input").is(':checked');
		var title = $(".search-title input").is(':checked');
		var body = $(".search-body input").is(':checked');
		var tags = $(".search-tags input").is(':checked');

		var on = 1;
		var off = 0.25;

		for (var i = 0; i < app.nodes().length; i ++) {
			var node = app.nodes()[i];
			var element = $(node.element);

			if (search.length > 0 && (speaker || title || body || tags)) {
				var matchSpeaker = (speaker && node.speaker().toLowerCase().indexOf(search) >= 0);
				var matchTitle = (title && node.title().toLowerCase().indexOf(search) >= 0);
				var matchBody = (body && node.body().toLowerCase().indexOf(search) >= 0);
				var matchTags = (tags && node.tags().toLowerCase().indexOf(search) >= 0);

				if (matchSpeaker || matchTitle || matchBody || matchTags) {
					node.active(true);
					element.clearQueue();
					element.transition({opacity: on}, 500);
				} else {
					node.active(false);
					element.clearQueue();
					element.transition({opacity: off}, 500);
				}
			} else {
				node.active(true);
				element.clearQueue();
				element.transition({opacity: on}, 500);
			}
		}
	}

	this.updateNodeLinks = function(force) {
		for  (var i in self.nodes()) {
			self.nodes()[i].updateLinks(force);
		}
		self.updateArrows();
	}

	this.updateArrows = function() {
		//console.log("updateArrows");
		// function can be called before app is inited
		if (!this.domroot) return;
		self.canvas.width = this.domroot.width();
		self.canvas.height = this.domroot.height();

		var scale = self.cachedScale;
		var offset = $(".nodes-holder").offset();
		// add width of file selector + gutter
		if ($(".gutter").length > 0) {
			offset.left -= $("#filepanel").width()+$($(".gutter")[0]).width();
		}
		self.context.clearRect(0, 0, self.canvas.width, self.canvas.height);
		self.context.lineWidth = 4 * scale;

		var nodes = self.nodes();

		for(var i in nodes) {
			var node = nodes[i];
			nodes[i].tempWidth = $(node.element).width();
			nodes[i].tempHeight = $(node.element).height();
			nodes[i].tempOpacity = $(node.element).css("opacity");
		}

		for(var index in nodes) {
			var node = nodes[index];
			if (node.linkedTo().length > 0) {
				for(var link in node.linkedTo()) {
					var linked = node.linkedTo()[link];

					// get origins
					var fromX = (node.x() + node.tempWidth/2) * scale + offset.left;
					var fromY = (node.y() + node.tempHeight/2) * scale + offset.top;
					var toX = (linked.x() + linked.tempWidth/2) * scale + offset.left;
					var toY = (linked.y() + linked.tempHeight/2) * scale + offset.top;

					// get the normal
					var distance = Math.sqrt((fromX - toX) * (fromX - toX) + (fromY - toY) * (fromY - toY));
					var normal = { x: (toX - fromX) / distance, y: (toY - fromY) / distance };

					var dist = 110 + 160 * (1 - Math.max(Math.abs(normal.x), Math.abs(normal.y)));

					// get from / to
					var from = { x: fromX + normal.x * dist * scale, y: fromY + normal.y * dist * scale };
					var to = { x: toX - normal.x * dist * scale, y: toY - normal.y * dist * scale };

					self.context.strokeStyle = "rgba(0, 0, 0, " + (node.tempOpacity * 0.6) + ")";
					self.context.fillStyle = "rgba(0, 0, 0, " + (node.tempOpacity * 0.6) + ")";

					// draw line
					self.context.beginPath();
					self.context.moveTo(from.x, from.y);
					self.context.lineTo(to.x, to.y);
					self.context.stroke();
					//console.log("Line: "+from.x+" "+from.y+" "+to.x+" "+to.y);

					// draw arrow
					self.context.beginPath();
					self.context.moveTo(to.x + normal.x * 4, to.y + normal.y * 4);
					self.context.lineTo(to.x - normal.x * 16 * scale - normal.y * 12 * scale, to.y - normal.y * 16 * scale + normal.x * 12 * scale);
					self.context.lineTo(to.x - normal.x * 16 * scale + normal.y * 12 * scale, to.y - normal.y * 16 * scale - normal.x * 12 * scale);
					self.context.fill();
				}
			}
		}
	}

	this.updateArrowsThrottled = EditorUtils.throttle(this.updateArrows, this.UPDATE_ARROWS_THROTTLE_MS);

	this.getHighlightedText = function(text)
	{
		text = text.replace(/\</g, '&lt;');
		text = text.replace(/\>/g, '&gt;');
		text = text.replace(/\[\[([^\]]*)\]\]/g, '<span class="linkbounds">[[</span><span class=" linkname ">$1</span><span class="linkbounds">]]</span>');
		// why is <p> used? It cannot be nested.
		text = text.replace(/\&lt;\&lt;(.*?)\&gt;\&gt;/g, '<p class="conditionbounds">&lt;&lt;</p><p class="condition">$1</p><p class="conditionbounds">&gt;&gt;</p>');
		//text = text.replace(/\[\[([^\|]*?)\]\]/g, '<p class="linkbounds">[[</p><p class="linkname">$1</p><p class="linkbounds">]]</p>');
		//text = text.replace(/\[\[([^\[\]]*?)\|([^\[\]]*?)\]\]/g, '<p class="linkbounds">[[</p>$1<p class="linkbounds">|</p><p class="linkname">$2</p><p class="linkbounds">]]</p>');
		text = text.replace(/\/\/(.*)?($|\n)/g, '<span class="comment">//$1</span>\n');
		// does not work, because it highlights variables in statements
		//text = text.replace(/(\$[a-zA-Z0-9_]+)/g, '<p class="inlinevariable">$1</p>');
		// multiline comments not supported
		//text = text.replace(/\/\*((.|[\r\n])*)?\*\//gm, '<span class="comment">/*$1*/</span>');
		//text = text.replace(/\/\%((.|[\r\n])*)?\%\//gm, '<span class="comment">/%$1%/</span>');

		// create a temporary document and remove all styles inside comments
		var div = $("<div>");
		div[0].innerHTML = text;
		div.find(".comment").each(function()
		{
			$(this).find("p").each(function()
			{
				$(this).removeClass();
			})
		})

		// unhighlight links that don't exist
		// not working, in Wool we cannot determine easily which part is the link
		/*div.find(".linkname").each(function() {
			var name = $(this).text();
			var found = false;
			for (var i in self.nodes())
			{
				if (self.nodes()[i].title().toLowerCase() == name.toLowerCase())
				{
					found = true;
					break;
				}
			}
			if (!found)
				$(this).removeClass("linkname");
		});*/

		text = div[0].innerHTML;
		return text;
	}

	this.updateLineNumbers = function(text)
	{
		// update line numbers
		var lines = text.split("\n");
		var lineNumbers = "";
		for (var i = 0; i < Math.max(1, lines.length); i ++)
		{
			if (i == 0 || i < lines.length - 1 || lines[i].length > 0)
				lineNumbers += (i + 1) + "<br />";
		}
		$(".editor-container .lines").html(lineNumbers);
	}

	this.updateHighlights = function(e)
	{
		if (e.keyCode == 17 || (e.keyCode >= 37 && e.keyCode <= 40))
			return;

		// get the text
		var editor = $(".editor");
		var text = editor[0].innerText;
		var startOffset, endOffset;

		// ctrl + z
		if ((e.metaKey || e.ctrlKey) && e.keyCode == 90)
		{
			if (self.editingHistory.length > 0)
			{
				var last = self.editingHistory.pop();
				text = last.text;
				startOffset = last.start;
				endOffset = last.end;
			}
			else
			{
				return;
			}
		}
		else
		{
			// get the current start offset
			var range = window.getSelection().getRangeAt(0);
			var preCaretStartRange = range.cloneRange();
			preCaretStartRange.selectNodeContents(editor[0]);
			preCaretStartRange.setEnd(range.startContainer, range.startOffset);
			startOffset = preCaretStartRange.toString().length;

			// get the current end offset
			var preCaretEndRange = range.cloneRange();
			preCaretEndRange.selectNodeContents(editor[0]);
			preCaretEndRange.setEnd(range.endContainer, range.endOffset);
			endOffset = preCaretEndRange.toString().length;

			// ctrl + c
			if ((e.metaKey || e.ctrlKey) && e.keyCode == 67)
			{
				if (self.gui != undefined)
				{
					var clipboard = self.gui.Clipboard.get();
					clipboard.set(text.substr(startOffset, (endOffset - startOffset)), 'text');
				}
			}
			else
			{
				// ctrl + v
				if ((e.metaKey || e.ctrlKey) && e.keyCode == 86)
				{
					var clipboard = self.gui.Clipboard.get();
					console.log(clipboard);
					text = text.substr(0, startOffset) + clipboard.get('text') + text.substr(endOffset);
					startOffset = endOffset = (startOffset + clipboard.get('text').length);
				}
				// ctrl + x
				else if ((e.metaKey || e.ctrlKey) && e.keyCode == 88)
				{
					if (self.gui != undefined)
					{
						var clipboard = self.gui.Clipboard.get();
						clipboard.set(text.substr(startOffset, (endOffset - startOffset)), 'text');
						text = text.substr(0, startOffset) + text.substr(endOffset);
						endOffset = startOffset;
					}
				}
				// increment if we just hit enter
				else if (e.keyCode == 13)
				{
					startOffset ++;
					endOffset ++;
					if (startOffset > text.length)
						startOffset = text.length;
					if (endOffset > text.length)
						endOffset = text.length;
				}
				// take into account tab character
				else if (e.keyCode == 9)
				{
					text = text.substr(0, startOffset) + "\t" + text.substr(endOffset);
					startOffset ++;
					endOffset = startOffset;
					e.preventDefault();
				}

				// save history (in chunks)
				if ((self.editingHistory.length == 0 || text != self.editingHistory[self.editingHistory.length - 1].text))
				{
					if (self.editingSaveHistoryTimeout == null)
						self.editingHistory.push({ text: text, start: startOffset, end: endOffset });
					clearTimeout(self.editingSaveHistoryTimeout);
					self.editingSaveHistoryTimeout = setTimeout(function() { self.editingSaveHistoryTimeout = null; }, 500);
				}
			}
		}

		// update text
		//editor[0].innerHTML = self.getHighlightedText(text);

		self.updateLineNumbers(text);

		// reset offsets
		if (document.createRange && window.getSelection)
		{
			function getTextNodesIn(node)
			{
				var textNodes = [];
				if (node.nodeType == 3)
					textNodes.push(node);
				else
				{
					var children = node.childNodes;
					for (var i = 0, len = children.length; i < len; ++i)
						textNodes.push.apply(textNodes, getTextNodesIn(children[i]));
				}
				return textNodes;
			}

			var range = document.createRange();
			range.selectNodeContents(editor[0]);
			var textNodes = getTextNodesIn(editor[0]);
			var charCount = 0, endCharCount;
			var foundStart = false;
			var foundEnd = false;

			for (var i = 0, textNode; textNode = textNodes[i++]; )
			{
				endCharCount = charCount + textNode.length;
				if (!foundStart && startOffset >= charCount && (startOffset <= endCharCount || (startOffset == endCharCount && i < textNodes.length)))
				{
					range.setStart(textNode, startOffset - charCount);
					foundStart = true;
				}
				if (!foundEnd && endOffset >= charCount && (endOffset <= endCharCount || (endOffset == endCharCount && i < textNodes.length)))
				{
					range.setEnd(textNode, endOffset - charCount);
					foundEnd = true;
				}
				if (foundStart && foundEnd)
					break;
				charCount = endCharCount;
			}

			var sel = window.getSelection();
			sel.removeAllRanges();
			sel.addRange(range);
		}
	}

	// currently unused, zoom controls disabled
	this.zoom = function(zoomLevel) {
		switch (zoomLevel) {
			case 1:
				self.cachedScale = 0.25;
				break;
			case 2:
				self.cachedScale = 0.5;
				break;
			case 3:
				self.cachedScale = 0.75;
				break;
			case 4:
				self.cachedScale = 1;
				break;
		}
		self.translate(200);
	}

	this.translate = function(speed) {
		$(".nodes-holder").css('transform',
				"matrix(" +
					self.cachedScale + ",0,0," +
					self.cachedScale + "," +
					self.transformOrigin[0] +"," +
					self.transformOrigin[1] +
				")"
		);
		//console.log("Translating to ...");
		//console.log(self.transformOrigin);
		self.updateArrows();

		self.storeUIState();
		
		return;
		// XXX this part doesn't work so I disabled it
		var updateArrowsInterval = setInterval(self.updateArrowsThrottled, 16);

		$(".nodes-holder").transition(
			{
				transform: (
					"matrix(" +
						self.cachedScale + ",0,0," +
						self.cachedScale + "," +
						self.transformOrigin[0] +"," +
						self.transformOrigin[1] +
					")"
				)
			},
			speed || 0,
			"easeInQuad",
			function() {
				// the finished function may be called just before the
				// animation is finished.
				clearInterval(updateArrowsInterval);
				self.updateArrowsThrottled();
			}
		);
	}

	/**
	 * Align selected nodes relative to a node with the lowest x-value
	 */
	this.arrangeX = function()
	{
		var SPACING = 250;

		var selectedNodes = self.nodes().filter(function(el) {
				return el.selected;
			})
			.sort(function(a, b) {
				if (a.x() > b.x()) return 1;
				if (a.x() < b.x()) return -1;
				return 0;
			}),
			referenceNode = selectedNodes.shift();

		if (!selectedNodes.length) {
			alert('Select nodes to align');
			return;
		}

		selectedNodes.forEach(function(node, i) {
			var x = referenceNode.x() + (SPACING * (i + 1));
			node.moveTo(x, referenceNode.y());
		});
	}

	/**
	 * Align selected nodes relative to a node with the lowest y-value
	 */
	this.arrangeY = function()
	{
		var SPACING = 250;

		var selectedNodes = self.nodes().filter(function(el) {
				return el.selected;
			})
			.sort(function(a, b) {
				if (a.y() > b.y()) return 1;
				if (a.y() < b.y()) return -1;
				return 0;
			}),
			referenceNode = selectedNodes.shift();

		if (!selectedNodes.length) {
			alert('Select nodes to align');
			return;
		}

		selectedNodes.forEach(function(node, i) {
			var y = referenceNode.y() + (SPACING * (i + 1));
			node.moveTo(referenceNode.x(), y);
		});
	}

	this.center = function() {
		self.resetUIState();
		var avg = this.getNodesCenter()
		if (avg) 
			self.warpToNodeXY(avg.x, avg.y);
	}

	this.getNodesCenter = function() {
		var nrNodes = self.nodes().length;
		if (nrNodes == 0) return null;
		var avgx=0;
		var avgy=0;
		for (var i=0; i<nrNodes; i++) {
			var node = self.nodes()[i];
			avgx += node.x();
			avgy += node.y();
		}
		return { x: avgx / nrNodes, y: avgy/nrNodes }
	}

	// currently unused, ui control disabled
	this.arrangeSpiral = function() {
		for (var i in self.nodes())
		{
			var node = self.nodes()[i];
			var y = Math.sin(i * .5) * (600 + i * 30);
			var x = Math.cos(i * .5) * (600 + i * 30);
			node.moveTo(x, y);
		}
	}

	// currently unused, ui control disabled
	this.sortAlphabetical = function() {
		console.log(self.nodes.sort);
		self.nodes.sort(function(a, b) { return a.title().localeCompare(b.title()); });
	}

	this.moveNodes = function(offX, offY)
	{
		for (var i in self.nodes())
		{
			var node = self.nodes()[i];
			node.moveTo(node.x() + offX, node.y() + offY);
		}
	}

	this.warpToNodeIdx = function(idx)
	{
		if (self.nodes().length > idx)
		{
			var node = self.nodes()[idx];
			var nodeXScaled = -( node.x() * self.cachedScale ),
				nodeYScaled = -( node.y() * self.cachedScale ),
				winXCenter = self.canvas.width / 2,
				winYCenter = self.canvas.height / 2,
				nodeWidthShift = node.tempWidth * self.cachedScale / 2,
				nodeHeightShift = node.tempHeight * self.cachedScale / 2;

			self.transformOrigin[0] = nodeXScaled + winXCenter - nodeWidthShift;
			self.transformOrigin[1] = nodeYScaled + winYCenter - nodeHeightShift;
			self.translate(100);
			self.focusedNodeIdx = idx;
		}
	}

	this.warpToSelectedNodeIdx = function(idx)
	{
		if (self.getSelectedNodes().length > idx)
		{
			var node = self.getSelectedNodes()[idx];
			var nodeXScaled = -( node.x() * self.cachedScale ),
				nodeYScaled = -( node.y() * self.cachedScale ),
				winXCenter = self.canvas.width / 2,
				winYCenter = self.canvas.height / 2,
				nodeWidthShift = node.tempWidth * self.cachedScale / 2,
				nodeHeightShift = node.tempHeight * self.cachedScale / 2;

			self.transformOrigin[0] = nodeXScaled + winXCenter - nodeWidthShift;
			self.transformOrigin[1] = nodeYScaled + winYCenter - nodeHeightShift;
			self.translate(100);
			self.focusedNodeIdx = idx;
		}
	}

	this.warpToNodeXY = function(x, y) {
		//console.log("warp to x, y: " + x + ", " + y);
		const nodeWidth = 100, nodeHeight = 100;
		var nodeXScaled = -( x * self.cachedScale ),
			nodeYScaled = -( y * self.cachedScale ),
			winXCenter = self.canvas.width / 2,
			winYCenter = self.canvas.height / 2,
			nodeWidthShift = nodeWidth * self.cachedScale / 2,
			nodeHeightShift = nodeHeight * self.cachedScale / 2;

		self.transformOrigin[0] = nodeXScaled + winXCenter - nodeWidthShift;
		self.transformOrigin[1] = nodeYScaled + winYCenter - nodeHeightShift;

		//alert("self.transformOrigin[0]: " + self.transformOrigin[0]);
		self.translate(100);
	}

	this.searchWarp = function()
	{
		// if search field is empty
		if (self.$searchField.val() == "")
		{
			// warp to the first node
			self.warpToNodeIdx(0);
		}
		else
		{
			var search = self.$searchField.val().toLowerCase();
			for (var i in self.nodes())
			{
				var node = self.nodes()[i];
				if (node.title().toLowerCase() == search)
				{
					self.warpToNodeIdx(i);
					return;
				}
			}
		}
	}

	this.clearSearch = function()
	{
		self.$searchField.val("");
		self.updateSearch();
	}


	this.updateEditorStats = function() {
		self.editor = ace.edit('editor');
		// the folllowing line is supposed to prevent this issue:
		// Automatically scrolling cursor into view after selection change
		// this will be disabled in the next version 
		// set editor.$blockScrolling = Infinity to disable this message
		// However it doesn't suppress the warning ebven though this way of
		// setting it is recommended.
		self.editor.$blockScrolling = Infinity;
		var text = self.editor.getSession().getValue();
		var cursor = self.editor.getCursorPosition();

		var lines = text.split("\n");

		$(".editor-footer .character-count").html(text.length);
		$(".editor-footer .line-count").html(lines.length);
		$(".editor-footer .row-index").html(cursor.row);
		$(".editor-footer .column-index").html(cursor.column);
		self.editing().getErrors();
	}

	this.contDialogue = function() {
		self.doRunDialogue(true);
	}

	this.runDialogue = function() {
		self.doRunDialogue(false);
	}

	this.doRunDialogue = function(doContinue) {
		//document.getElementById('woolclient-popup-iframe').contentWindow.location.reload();
		data.saveToBuffer();
		var urlFileParam = "";
		if (self.isNwjs) {
			urlFileParam = "&woolRoot="+encodeURIComponent(data.getRoot())
				+"&filepath="+encodeURIComponent("/"+self.filename())
		}
		var content = data.getSaveData(FILETYPE.WOOL);
		window.name = JSON.stringify({
			sourceCode: content,
			langDefs: self.getLangDefs(),
		});
		//var elem = document.getElementById("woolclient-popup");
		//elem.style.display="block";
		//elem = document.getElementById("woolclient-popup-iframe");
		//elem.src = "../../html5/simplewoolclient/index.html?editable=true&rand="
		//	+Math.random()+"&code="+encodeURIComponent(
		//		data.getSaveData(FILETYPE.WOOL)
		//	);
		location.href = "../simplewoolclient/index.html?editable=true&rand="
			+Math.random()
			+"&editurl="+encodeURIComponent("../wooleditor/index.html")
			+(doContinue ? "&docontinue=true" : "")
			+urlFileParam
			//+"&code="+encodeURIComponent(
			//	data.getSaveData(FILETYPE.WOOL)
			//)
			;
	}

	// also store history, other info?

	this.resetUIState = function() {
		self.cachedScale = 1;
		self.transformOrigin = [
			0,
			0
		];
		self.translate();
	}

	this.loadUIState = function() {
		var uistate = localStorage.getItem(App.LOCALSTORAGEPREFIX+"uistate");
		if (uistate) {
			console.log("Loaded uistate:");
			console.log(uistate);
			uistate = JSON.parse(uistate);
			self.transformOrigin = uistate.transformOrigin;
			self.transformOrigin[0] = parseFloat(self.transformOrigin[0]);
			self.transformOrigin[1] = parseFloat(self.transformOrigin[1]);
			self.cachedScale = parseFloat(uistate.cachedScale);
		}
	}

	this.storeUIState = function() {
		var uistate = {
			transformOrigin: self.transformOrigin,
			cachedScale: self.cachedScale,
		};
		console.log("Storing ui state:");
		console.log(uistate);
		localStorage.setItem(App.LOCALSTORAGEPREFIX+"uistate",
			JSON.stringify(uistate));
	}

	this.loadUIState();

	this.setCurrentPath = function(newpath) {
		// normalize path
		// XXX cannot handle filenames with more than one "."
		var dnewpath = newpath;
		var filebase = null;
		if (this.fs.fstype == "browser") {
			// remove fake path
			dnewpath = dnewpath.split("\\");
			dnewpath = dnewpath[dnewpath.length-1];
			dnewpath = dnewpath.split("/");
			dnewpath = dnewpath[dnewpath.length-1];
			// remove path, extension
			filebase = newpath.match(/^.*[\/\\]([^.]*)[.][YWyw][aoAO][roRO][LNln][txt.]*$/i);
		} else {
			// remove extension
			// with path
			filebase = newpath.match(/^(.*[\/\\][^.]*)[.][YWyw][aoAO][roRO][LNln][txt.]*$/i);
			// without path
			if (!filebase) filebase = newpath.match(/^([^.]*)[.][YWyw][aoAO][roRO][LNln][txt.]*$/i);
		}
		if (filebase) {
			dnewpath = filebase[1];
		}
		this.filename(dnewpath);
		localStorage.setItem(App.LOCALSTORAGEPREFIX+"path", dnewpath);
		this.refreshWindowTitle();
	}

	/* Translation --------------------------------------- */
	/* Current language defs and phrases are stored in localStorage.
	 * For Desktop, every time a new wool file or a language is selected,
	 * it tries to load the corresponding json file, and store the results
	 * in localStorage. */

	// localStorage -------------------------------------------------

	this.clearLangDefs = function() {
		localStorage.removeItem(App.LOCALSTORAGEPREFIX+"langDefs");
	}
	this.setLangDefs = function(data) {
		localStorage.setItem(App.LOCALSTORAGEPREFIX+"langDefs",data);
	}
	this.getLangDefs = function(jsonify) {
		var langDefs = localStorage.getItem(App.LOCALSTORAGEPREFIX+"langDefs");
		if (!jsonify) return langDefs;
		if (langDefs) {
			langDefs = JSON.parse(langDefs);
			langDefs = _i18n.flattenKeyValueJSON(langDefs);
		} else {
			langDefs = {};
		}
		return langDefs;
	}

	// misc translation functions ------------------------------------------

	this.getTranslationJsonPath = function() {
		var woolpath = App.getCurrentPath();
		var jsonpath = self.selectedLanguage()
			+ woolpath.substring(self.defaultLanguage().length)
			+ ".json";
		//jsonpath = jsonpath.replace(/[.]wool$/i, ".json");
		return jsonpath;
	}

	// Called when language or wool file changed. If language is
	// default language, clears langDefs.
	this.loadLangDefsFromJSON = function() {
		if (!app.isNwjs) return;
		if (self.selectedLanguage() == self.defaultLanguage()) {
			self.clearLangDefs();
		} else {
			var jsonpath = self.getTranslationJsonPath();
			app.fs.readFile(data.appendRoot(jsonpath), null,
				function(error,contents) {
					if (error) {
						self.clearLangDefs();
						// prevent disruption of init animation
						setTimeout(function() {
							alert("No translation json file '"
								+jsonpath+"'.");
						}, 500 );
					} else {
						console.log("Loaded translation file "+jsonpath);
						self.setLangDefs(contents);
					}
				}
			);
			
		}
	}

	// called when FileManager updated its dir tree. 
	// Updates languages, defaultLanguage, selectedLanguage
	this.dirtreeUpdated = function(dirtree) {
		console.log("Dirtree updated. Getting languages.");
		// need delay before file can be visually selected
		setTimeout(function() { FileManager.selectLoadedFile(); }, 200);
		var rootcontents = dirtree["#"].content;
		var languages = [];
		var foundSelected = false;
		var foundDefault = false;
		var newDefault = false;
		var nrWoolFilesInDefault = -1;
		for (var idx in rootcontents) {
			var dir = rootcontents[idx];
			if (dirtree[dir].type != "DIR") continue;
			languages.push(dir);
			// count wool files in subtree
			var nrWoolFiles = 0;
			for (var path in dirtree) {
				if (path.startsWith(dir)&&path.toLowerCase().endsWith(".wool"))
					nrWoolFiles++;
			}
			console.log("nrWoolFiles in "+dir+"="+nrWoolFiles);
			if (nrWoolFiles > nrWoolFilesInDefault) {
				newDefault = dir;
				nrWoolFilesInDefault = nrWoolFiles;
			}
			if (self.selectedLanguage() == dir) foundSelected = true;
			// first language found is new default if needed
		}
		if (newDefault) {
			self.defaultLanguage(newDefault);
			localStorage.setItem(App.LOCALSTORAGEPREFIX+"defaultlanguage",
				newDefault);
			console.log("Default language is set to "+newDefault);
		}
		// make sure selectLanguage is called
		if (!foundSelected && newDefault) {
			self.selectLanguage(newDefault);
		} else {
			self.selectLanguage(self.selectedLanguage());
		}
		self.languages(languages);
	}

	// split translation term, returns {context, phrase}
	this.parseGettextPhrase = function(string) {
		var textspkr = string.split("|");
		if (textspkr.length == 1) return {
			context: "",
			phrase: string,
		};
		var context = textspkr.shift();
		var phrase = textspkr.join("|");
		return {
			context: context,
			phrase: phrase,
		};
	}


	// translation UI --------------------------------------------------

	// call if language is selected, editor is inited, filetree is refreshed
	this.selectLanguage = function(lang) {
		console.log("Language selected: "+lang);
		localStorage.setItem(App.LOCALSTORAGEPREFIX+"language", lang);
		self.selectedLanguage(lang);
		self.loadLangDefsFromJSON();
	}

	/* Get translation nodes for editor screen
	* Context flags:
	* - formal / informal
	* - male_speaker / female_speaker
	* - male_addressee / female_addressee
	* Phrase metadata:
	* - source nodes
	* - agent or user (name of agent can be derived from source node)
	* Same phrase can be in multiple nodes, but can also be uttered by
	* the user and multiple agents.  The front end knows the speaker and
	* recipient for each phrase, so it can add the appropriate context.
	*/
	this.getTranslationNodes = function() {
		var nodes = self.nodes();
		var ret = [];
		for (var i in nodes) {
			var node = nodes[i];
			if (node.title() == "End") continue;
			node.compile(true);
			node.getTextsForTranslation();
			ret.push(nodes[i]);
		}
		// get orphaned translations
		var orphans = [];
		var langDefs = app.getLangDefs(true);
		for (text in langDefs) {
			if (self.langDefMarks[text]) continue;
			var textspkr = app.parseGettextPhrase(text);
			if (langDefs[text] == "") continue;
			orphans.push({
				speaker: textspkr.context ? textspkr.context : "UNKNOWN",
				source: textspkr.phrase,
				translation: ko.observable(langDefs[text]),
			});
		}
		// orphaned translations are in a mock node for KO template's sake
		if (orphans.length > 0) {
			var orphansNode = {
				isOrphansNode: true,
				title: "Orphaned translations",
				//compile: function(force) {/* dummy */},
				//getTextsForTranslation: function() { /* dummy */},
				translationTexts: ko.observableArray(orphans),
			};
			ret.push(orphansNode);
			self.translationOrphansNode = orphansNode;
		} else {
			self.translationOrphansNode = null;
		}
		return ret;
	}
	this.startTranslating = function() {
		if (this.defaultLanguage() == this.selectedLanguage()) {
			alert("Please set language other than '"
				+this.defaultLanguage()+"'");
			return;
		}
		self.clearLangDefMarks();
		self.translating(true);
	}
	this.saveTranslation = function() {
		// copy nodes into new array and add orphans node if applicable
		var nodes0 = self.nodes();
		var nodes = [];
		for (var i in nodes0) {
			if (nodes0[i].title() == "End") continue;
			nodes.push(nodes0[i]);
		}
		if (self.translationOrphansNode)
			nodes.push(self.translationOrphansNode);
		var jsondefs = {};
		for (var i in nodes) {
			var texts = nodes[i].translationTexts();
			for (var j in texts) {
				// delete empty texts when in orphans node
				if (nodes[i].isOrphansNode && texts[j].translation()=="")
					continue;
				if (!jsondefs[texts[j].speaker])
					jsondefs[texts[j].speaker] = {};
				jsondefs[texts[j].speaker][texts[j].source] = 
					texts[j].translation();
			}
		}
		var jsonpath = self.getTranslationJsonPath();
		var jsonpathdir = app.fs.getPathAPI().dirname(jsonpath);
		var jsonstring = JSON.stringify(jsondefs, null, 4);
		self.setLangDefs(jsonstring);
		// make sure directory exists
		app.fs.mkdirSync(data.appendRoot(jsonpathdir));
		app.fs.writeFile(data.appendRoot(jsonpath),
			jsonstring,
			function(err) {
				if (err) {
					alert("Error saving translation: "+err);
				} else {
					alert("Translation saved.");
					if (filemgr) filemgr.updateDirTree();
				}
			}
		);
		this.translating(null);
	}
	this.discardTranslation = function() {
		var accept = confirm("Discard changes?");
		if (!accept) return;
		this.translating(null);

	}
	// clear marks
	this.clearLangDefMarks = function() {
		self.langDefMarks = {};
	}
	// langDefs - {phrase:translation}, phrase can be with or w/o speaker
	// context.
	// return translation, or "" if none found
	this.getAndMarkLangDef = function(langDefs,speaker,text) {
		if (!speaker) speaker = "UNKNOWN";
		var trans = langDefs[speaker+"|"+text];
		if (trans) {
			self.langDefMarks[speaker+"|"+text] = true;
		} else {
			trans = langDefs[text];
			if (trans) {
				self.langDefMarks[text] = true;
			}
		}
		if (!trans) trans = "";
		return trans;
	}


	/* Agents (unused) --------------------------------------- */

	this.editAgent = function(agentname) {
		var agent = this.metadata.getAgent(agentname);
		if (!agent) {
			agent = new AgentProperties(agentname,"dynamic",agentname);
			this.metadata.addAgent(agent);
		}
		this.editingAgent(this.metadata.getAgent(agentname));
	}
	/* not used yet */
	this.saveAgent = function() {
		alert("Save agent");
		this.metadata.addAgent(this.editingAgent());
		this.metadata.save(data.getRoot())
		this.editingAgent(null);
	}
}


// static defs ---------------------------------------------------

App.LOCALSTORAGEPREFIX="wool_js_";


// addExtension: true = add ".wool" extension
App.getCurrentPath = function(addExtension) {
	var ret = localStorage.getItem(App.LOCALSTORAGEPREFIX+"path");
	if (!ret) return "";
	return ret + (addExtension ? ".wool" : "");
}

