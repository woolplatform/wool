// this class handles the project file tree using JSTree.

// dirtreeUpdatedCallback: function(dirtree)
function FileManager(dirtreeUpdatedCallback) {
	this.jsTreeInited=false;
	// indicates that next move_node is undo operation
	this.revertingMove = false;
	this.dirsSelectedCallback = null;
	// { <path>: { type: "FILE"/"DIR", name: <string>, content: [] }
	this.dirtree = { };
	this.dirtreeUpdatedCallback = dirtreeUpdatedCallback;
	this.lastDeselected = null;
}

FileManager.prototype.getRoot = function() {
	return localStorage.getItem(App.LOCALSTORAGEPREFIX+"root");
}

FileManager.prototype.setRoot = function(root) {
	localStorage.setItem(App.LOCALSTORAGEPREFIX+"root",root);
}

FileManager.prototype.init = function() {
	window.addEventListener('message', evt => {
		if (evt.data.type === 'dirs-selected') {
			//console.log("Dir selected: "+evt.data.data);
			if (!this.dirsSelectedCallback) {
				this.defaultDirsSelectedCallback(evt);
			} else {
				var callback = this.dirsSelectedCallback;
				this.dirsSelectedCallback = null;
				callback(evt);
			}
		}
	})
	this.updateDirTree();
}

// visually select the currently loaded file
FileManager.selectLoadedFile = function() {
	$('#filetree').jstree(true).select_node(
		app.filename()+".wool",true,false,null,true);
}

FileManager.prototype.getFileIcon  = function(filename) {
	if (filename.match(/[.]wool$/i)) return "jstreewoolfile";
	if (filename.match(/[.]json$/i)) return "jstreejsonfile";
	return "jstreefile";
}

FileManager.prototype.updateDirTree  = function() {
	var root = this.getRoot();
	var self = this;
	if (!root) {
		$("#filetree").html("Please select Wool directory.");
		return;
	}
	$("#fileroot").html(root);
	app.fs.readdirtree(root,function(err,res) {
		if (err) {
			alert("Error reading directory tree: "+err);
			return;
		}
		// convert ["FILE"/"DIR",name,fullpath] to
		// [id:fullpath,text:name,parent,icon]
		self.dirtree = { };
		var jstdata = [];
		for (var i=0; i<res.length; i++) {
			var item = res[i];
			var basedir = item[2].substring(0,
				item[2].length - item[1].length - 1);
			if (basedir.length == 0) basedir = "#";
			if (!self.dirtree[basedir]) {
				self.dirtree[basedir] = {
					type: "DIR",
					content: [item[2]],
				};
			} else {
				self.dirtree[basedir].content.push(item[2]);
			}
			if (!self.dirtree[item[2]]) {
				self.dirtree[item[2]] = {
					type: item[0],
					name: item[1],
					content: [],
				}
			} else {
				self.dirtree[item[2]].name = item[1];
			}
			// NOTE icon is used to distinguish between dir and file, by
			// checking icon == "jstreedir". 
			jstdata.push({
				id: item[2],
				text: item[1],
				parent: basedir,
				icon: item[0]=="DIR" ? "jstreedir":self.getFileIcon(item[1]),
			});
		}

		if (!self.jsTreeInited) {
			//$.jstree.defaults.unique.duplicate = function(name,counter) {
			//	console.log("!!"+name+"#"+counter);
			//};
			$.jstree.defaults.dnd.is_draggable = function(nodedata,ev) {
				// only drag files (for now)
				for (var i=0; i<nodedata.length; i++) {
					var node = nodedata[i];
					if (node.icon == "jstreedir") return false;
				}
				return true;
			}
			// only drag single item
			$.jstree.defaults.dnd.drag_selection = false;
			// only allow move
			$.jstree.defaults.dnd.copy = false;
			$("#filetree").jstree({
				//"core": { "data": jstdata },
				"core": {
					"multiple": false,
			        //'check_callback' : true,
			        'check_callback' : function (operation, node, node_parent, node_position, more) {
						// operation can be 'create_node', 'rename_node', 'delete_node', 'move_node' or 'copy_node'
						// in case of 'rename_node' node_position is filled with the new node name
						//return operation === 'rename_node' ? true : false;
						// prevent dnd from moving a node inside a file node
						if (operation == "move_node") {
							if (node_parent.icon != "jstreedir") return false;
							//self.revertingMove = false;
						}
						return true;
					},
				},
				"types" : {
					"default" : {
						"icon" : "glyphicon glyphicon-flash"
					},
					"demo" : {
						"icon" : "glyphicon glyphicon-ok"
					}
				},
				"contextmenu": {
					"select_node": false,
					"items": self.createContextMenu
				},
				// conditionalselect is called by select_node, if it returns
				// false then select_node operation is aborted.
				// Before select_node is called, deselect_all is called.
				// What we do, we store the deselected node to be re-selected
				// when we want to undo the deselection.
				"conditionalselect": function(node,isUserCall) {
					if (node.icon != "jstreedir") {
						// check if this is called by us rather than jstree
						if (isUserCall === true) return true;
						var filename = node.id;
						if (filename.toLowerCase().indexOf(".wool") > -1) {
							// file is wool file
							if (filename.replace(/[.]wool/i,"")
							!= app.filename()) {
								// file is different from loaded file
								var loaded = self.loadWoolFile(filename);
								if (loaded) return true; // else load canceled
							} // else file already loaded
						} // else not a wool file
						// If we end up here, we cancel the selection.
						// Undo deselection
						if (self.lastDeselected
						&& self.lastDeselected.length == 1) {
							$('#filetree').jstree(true).select_node(
								self.lastDeselected[0],true,false,null,true);
						}
						// Cancel selection
						return false;
					}
				},
				"plugins" : [
					"types", "contextmenu", "state", "unique", "sort", "dnd",
					// homemade plugins
					"conditionalselect"
				]
			}).on("ready.jstree", function(e,nodedata) {
				/*$("#filetree").on("select_node.jstree",
				function(e,nodedata) {
					console.log("CONDFITIONALSELECT#$$$$$$$$$$$$$$$$$$$");
					var item = nodedata.node;
					if (nodedata.node.icon != "jstreedir") {
						var filename = nodedata.node.id;
						if (filename.toLowerCase().indexOf(".wool")
						> -1) {
							return self.loadWoolFile(filename);
						}
					}
				});*/
				$("#filetree").on("deselect_all.jstree",
				function(e,nodedata) {
					self.lastDeselected = nodedata.node;
					console.log("JSTree deselect node "+self.lastDeselected);
				});
				FileManager.selectLoadedFile();
			}).on("rename_node.jstree", function(e,nodedata) {
				var id = nodedata.node.id;
				var oldname = nodedata.old;
				var newname = nodedata.text;
				// rename cancelled or no change
				if (oldname==newname) return;
				var newpath = FileManager.getDir(id,oldname) + newname;
				// TODO re-append extension if removed by user
				console.log("New filename: "+newpath);
				app.fs.renameFile(
					data.appendRoot(id),
					data.appendRoot(newpath),
					true,
					function(e)	{
						self.doRenameFile(e,nodedata.node,oldname,id,newpath);
					}
				);
			}).on("create_node.jstree", function(e,nodedata) {
				console.log("create_node");
				if (nodedata.node.icon == "jstreedir") {
					// create folder
					app.fs.mkdir(data.appendRoot(nodedata.node.id),
						function(err) {
							self.doCreateFile(err,nodedata.node);
							//if (err) {
							//	alert("Error creating folder.");
							//	// TODO remove from jstree
							//}
						} );
				} else if (nodedata.node.original._sourceid) {
					// copy file
					app.fs.copyFile(
						data.appendRoot(nodedata.node.original._sourceid),
						data.appendRoot(nodedata.node.id),
						function(err) {
							self.doCreateFile(err,nodedata.node);
						}
					);
				} else {
					// create new file
					app.fs.writeFile(
						data.appendRoot(nodedata.node.id), "",
						function(err) {
							self.doCreateFile(err,nodedata.node);
						}
					);
				}
			}).on("delete_node.jstree", function(e,nodedata) {
				// called after copy_node
				console.log("delete_node");
				app.fs.deleteFile(
					data.appendRoot(nodedata.node.id),
					function(err) {
						// ignore error
					}
				);
				// update dirtree
				delete self.dirtree[nodedata.node.id];
				self.refreshDirtreeRefs();
				if (self.dirtreeUpdatedCallback) {
					self.dirtreeUpdatedCallback(self.dirtree);
				}
				console.log(nodedata);
			}).on("move_node.jstree", function(e,nodedata) {
				console.log("move_node");
				if (self.revertingMove) {
					// do not do file operation
					self.revertingMove = false;
					console.log("revertingMove");
					return;
				}
				var id = nodedata.node.id;
				var text = nodedata.node.text;
				var newdir = nodedata.parent;
				var newpath = newdir + app.fs.getPathAPI().sep + text;
				if (confirm("Move file '"+id+"' to "+newdir+"?")) {
					app.fs.renameFile(
						data.appendRoot(id),
						data.appendRoot(newpath),
						true,
						function(e)	{
							self.doRenameFile(e,nodedata.node,text,id,newpath);
						}
					);
				} else {
					self.doRenameFile(true,nodedata.node,text,id,newpath);
				}
			});
			self.jsTreeInited=true;
		}
		$('#filetree').jstree(true).settings.core.data=jstdata;
		$('#filetree').jstree(true).refresh();
		if (self.dirtreeUpdatedCallback) {
			self.dirtreeUpdatedCallback(self.dirtree);
		}
	});
}

// refresh this.dirtree content fields
FileManager.prototype.refreshDirtreeRefs = function() {
	for (var path in this.dirtree) {
		this.dirtree[path].content = [];
	}
	for (var path in this.dirtree) {
		if (path == "#") continue;
		var filename = this.dirtree[path].name;
		var theparent = FileManager.getDir(path,filename,true);
		this.dirtree[theparent].content.push(path);
	}
}


// get directory of node, given id (full path) and text (filename)
FileManager.getDir = function(id,text,noTrailingSeparator) {
	var cutlen = text.length;
	if (noTrailingSeparator) cutlen++
	var ret = id.substr(0, id.length-cutlen);
	if (ret=="") ret = "#";
	return ret;
}

// get children of dir node as associative array {nodeID => node}
FileManager.getChildren = function(dir) {
	var tree = $('#filetree').jstree(true);
	var ret = {};
	var othernodes = tree.get_json(dir);
	console.log(othernodes);
	for (var i=0; i<othernodes.children.length; i++) {
		var child = othernodes.children[i];
		ret[child.id] = child;
	}
	return ret;
}

FileManager.createUniqueFile = function(dir,dirwithsep,text) {
	var siblings = FileManager.getChildren(dir);
	if (!siblings[dirwithsep + text]) return text;
	var num = 2;
	var filename = "";
	while (true) {
		filename = FileManager.getBaseFilename(text)
			+ " (" + num + ")"
			+ FileManager.getFileExtension(text);
		if (siblings[dirwithsep + filename]) {
			num++;
		} else {
			break;
		}
	}
	return filename;
}


FileManager.getBaseFilename = function(id) {
	var dotpos = id.lastIndexOf(".");
	var slashpos = id.lastIndexOf(app.fs.getPathAPI().sep);
	if (dotpos == -1 || dotpos < slashpos) return id;
	return id.substring(0,dotpos);
}

FileManager.getFileExtension = function(id) {
	var dotpos = id.lastIndexOf(".");
	var slashpos = id.lastIndexOf(app.fs.getPathAPI().sep);
	if (dotpos == -1 || dotpos < slashpos) return "";
	return id.substring(dotpos);
}

// err: true (cancel silently) or error description
FileManager.prototype.doRenameFile = function(err,node,oldtext,oldpath,newpath){
	if (err) {
		// revert jstree changes
		// make sure next call to move_node callback does not call this func
		this.revertingMove = true;
		$('#filetree').jstree(true).set_id(node, oldpath);
		$('#filetree').jstree(true).set_text(node, oldtext);
		$('#filetree').jstree(true).move_node(node,
			FileManager.getDir(oldpath, oldtext, true) );
		if (err !== true) alert("Error renaming file: "+err);
	} else {
		// rename currently loaded file
		if (App.getCurrentPath(true) == oldpath) {
			app.setCurrentPath(newpath);
		}
		// update jstree and dirtree
		$('#filetree').jstree(true).set_id(node, newpath);
		if (node.icon == "jstreedir") {
			this.updateDirTree();
		} else {
			if (oldpath != newpath) {
				this.dirtree[newpath] = {
					type: node.icon!="jstreedir" ? "FILE" : "DIR",
					name: node.text,
					content: this.dirtree[oldpath].content,
				}
				delete this.dirtree[oldpath];
				this.refreshDirtreeRefs();
				if (this.dirtreeUpdatedCallback) {
					this.dirtreeUpdatedCallback(this.dirtree);
				}
			}
		}
	}
}

// called afer creating file or dir
FileManager.prototype.doCreateFile = function(err,node){
	if (err) {
		// revert jstree changes
		$('#filetree').jstree(true).delete_node(node);
		alert("Error creating file or folder: "+err);
	} else {
		// success -> nothing needs to be done in jstree
		this.dirtree[node.id] = {
			type: node.icon!="jstreedir" ? "FILE" : "DIR",
			name: node.text,
			content: [],
		};
		this.refreshDirtreeRefs();
		if (this.dirtreeUpdatedCallback) {
			this.dirtreeUpdatedCallback(this.dirtree);
		}
	}
}

// NOTE: is called statically by jstree, so "this" is a jstree class
FileManager.prototype.createContextMenu = function(node) {
	var self = this;
	if ($(node).attr("icon") == "jstreedir") {
		return {
			"createWoolFile" : {
				"label"             : "Create Wool file",
				"action"            : function (obj) {
					// actual file is created by create_node.jstree callback
					var tree = $('#filetree').jstree(true);
					var dir = $(node).attr("id");
					var text = "New File.wool";
					var filename = FileManager.createUniqueFile(dir,
						dir + app.fs.getPathAPI().sep  , text);
					tree.create_node(dir, {
						id: dir + app.fs.getPathAPI().sep + filename,
						text: filename,
						icon: self.getFileIcon(filename),
					});
				},
				// All below are optional
				"_disabled"         : false,
				"_class"            : "class",  // class is applied to the item LI node
				"separator_before"  : false,
				"separator_after"   : false,
				// false or string - if does not contain `/` - used as classname
				"icon"              : false,
				//"submenu"           : {
				//    /* Collection of objects (the same structure) */
				//}
			},
			"createDir" : {
				"label"             : "Create folder",
				"action"            : function (obj) {
					// actual folder is created by create_node.jstree callback
					var tree = $('#filetree').jstree(true);
					var dir = $(node).attr("id");
					var text = "New Folder";
					var filename = FileManager.createUniqueFile(dir,
						dir + app.fs.getPathAPI().sep, text);
					tree.create_node(dir, {
						id: dir + app.fs.getPathAPI().sep + filename,
						text: filename,
						icon: "jstreedir",
					});
				},
				// All below are optional
				"_disabled"         : false,
				"_class"            : "class",  // class is applied to the item LI node
				"separator_before"  : false,
				"separator_after"   : false,
				// false or string - if does not contain `/` - used as classname
				"icon"              : false,
				//"submenu"           : {
				//    /* Collection of objects (the same structure) */
				//}
			},
			"renameDir" : {
				// The item label
				"label"             : "Rename folder",
				// The function to execute upon a click
				"action"            : function (obj) {
					$('#filetree').jstree(true).edit($(node).attr("id"));
					//console.log($('#filetree').jstree(true).last_error());
				},
				// All below are optional
				"_disabled"         : false,
				"_class"            : "class",  // class is applied to the item LI node
				"separator_before"  : false,
				"separator_after"   : false,
				// false or string - if does not contain `/` - used as classname
				"icon"              : false,
			},
		}
	} else { // jstree*file
		return {
			"renameFile" : {
				// The item label
				"label"             : "Rename file",
				// The function to execute upon a click
				"action"            : function (obj) {
					$('#filetree').jstree(true).edit($(node).attr("id"));
					//console.log($('#filetree').jstree(true).last_error());
				},
				// All below are optional
				"_disabled"         : false,
				"_class"            : "class",  // class is applied to the item LI node
				"separator_before"  : false,
				"separator_after"   : false,
				// false or string - if does not contain `/` - used as classname
				"icon"              : false,
			},
			"duplicateFile" : {
				// The item label
				"label"             : "Duplicate file",
				// The function to execute upon a click
				"action"            : function (obj) {
					var tree = $('#filetree').jstree(true);
					var id = $(node).attr("id");
					var text = $(node).attr("text");
					var dirwithsep = FileManager.getDir(id,text,false);
					var dir = FileManager.getDir(id,text,true);
					//tree.copy_node(tree.get_node(id),
					//	FileManager.getDir(id,text,true),
					//	function() {
					//		console.log("CALLBACK");
					//	}
					//);
					// check duplicates
					var filename = FileManager.createUniqueFile(dir,
						dirwithsep, text);
					tree.create_node(dir, {
						id: dirwithsep + filename,
						text: filename,
						icon: self.getFileIcon(filename),
						_sourceid: id,
					});
				},
				// All below are optional
				"_disabled"         : false,
				"_class"            : "class",  // class is applied to the item LI node
				"separator_before"  : false,
				"separator_after"   : false,
				// false or string - if does not contain `/` - used as classname
				"icon"              : false,
			},
			"deleteFile" : {
				// The item label
				"label"             : "Delete file",
				// The function to execute upon a click
				"action"            : function (obj) {
					var id = $(node).attr("id");
					if (confirm("Delete file "+id+"?")) {
						$('#filetree').jstree(true).delete_node(id);
					}
				},
				// All below are optional
				"_disabled"         : false,
				"_class"            : "class",  // class is applied to the item LI node
				"separator_before"  : false,
				"separator_after"   : false,
				// false or string - if does not contain `/` - used as classname
				"icon"              : false,
			},
		}
	}
}

FileManager.prototype.loadWoolFile = function(filename) {
	if (app.waitSpinnerShowing) return; // do nothing until file loaded
	if (!app.areChangesSaved()) {
		var confirm = window.confirm("Changes may not have been saved, proceed?");
		if (!confirm) return false;
	}
	data.openFile(null, filename, null); //[ {files: [filename]} ]);
	return true;
}

FileManager.prototype.sendSelectBaseDir = function(elem) {
	this.dirsSelectedCallback = null;
	window.postMessage({type: 'select-dirs'})
}

FileManager.prototype.sendSelectDirCustom = function(elem,callback) {
	this.dirsSelectedCallback = callback;
	window.postMessage({type: 'select-dirs'})
}


FileManager.prototype.defaultDirsSelectedCallback = function(evt) {
	this.setRoot(evt.data.data);
	this.updateDirTree();
	wizard.closeAll();
}


// currently unused
FileManager.prototype.selectBaseDir = function(elem) {
	alert(elem.value);
	//localStorage.getItem(App.LOCALSTORAGEPREFIX+"root");
}

