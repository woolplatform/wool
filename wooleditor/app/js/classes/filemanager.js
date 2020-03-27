function FileManager() {
	this.jsTreeInited=false;
}

FileManager.prototype.getRoot = function() {
	return localStorage.getItem(App.LOCALSTORAGEPREFIX+"root");
}

FileManager.prototype.init = function() {
	window.addEventListener('message', evt => {
		if (evt.data.type === 'dirs-selected') {
			//console.log("Dir selected: "+evt.data.data);
			localStorage.setItem(App.LOCALSTORAGEPREFIX+"root",
				evt.data.data);
			this.updateDirTree();
		}
	})
	this.updateDirTree();
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
		var jstdata = [];
		for (var i=0; i<res.length; i++) {
			var item = res[i];
			var basedir = item[2].substring(0,
				item[2].length - item[1].length - 1);
			if (basedir.length == 0) basedir = "#";
			jstdata.push({
				id: item[2],
				text: item[1],
				parent: basedir,
				icon: item[0]=="DIR" ? "jstreedir":"jstreefile",
			});
		}
		if (!this.jsTreeInited) {
			$("#filetree").jstree({
				//"core": { "data": jstdata },
				"core": {
			        //'check_callback' : true,
			        'check_callback' : function (operation, node, node_parent, node_position, more) {
						// operation can be 'create_node', 'rename_node', 'delete_node', 'move_node' or 'copy_node'
						// in case of 'rename_node' node_position is filled with the new node name
						//return operation === 'rename_node' ? true : false;
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
				"plugins" : [ "types", "contextmenu" ]
			}).on("ready.jstree", function(e,data) {
			//$("#filetree").jstree(true).set_type("/file1","demo");
			//alert($("#filetree").jstree(true).get_type("/file1"));
				$("#filetree").on("select_node.jstree",
				function(e,data) {
					var item = data.node;
					if (data.node.icon == "jstreefile") {
						var filename = data.node.id;
						if (filename.toLowerCase().indexOf(".wool")
						> -1) {
							self.loadWoolFile(filename);
						}
					}
				});
			}).on("rename_node.jstree", function(e,nodedata) {
				var id = nodedata.node.id;
				var oldname = nodedata.old;
				var newname = nodedata.text;
				var newpath = id.substr(0,
					id.length-oldname.length) + newname;
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
			});
			this.jsTreeInited=true;
		}
		console.log(jstdata);
		$('#filetree').jstree(true).settings.core.data=jstdata;
		$('#filetree').jstree(true).refresh();
	});
}

FileManager.prototype.doRenameFile = function(err,node,oldtext,oldpath,newpath){
	if (err) {
		// revert jstree changes
		$('#filetree').jstree(true).set_text(node, oldtext);
		alert("Error renaming file: "+err);
	} else {
		// rename currently loaded file
		if (App.getCurrentPath(true) == oldpath) {
			app.setCurrentPath(newpath);
		}
		// update jstree
		$('#filetree').jstree(true).set_id(node, newpath);
	}
}
	
FileManager.prototype.createContextMenu = function(node) {
	if ($(node).attr("icon") == "jstreedir") {
		return {
			"createWoolFile" : {
				"label"             : "[TODO] Create Wool file",
				"action"            : function (obj) {
					// TODO create file
					// TODO add node or refresh tree
					console.log(node);
					console.log($(node).attr("id"));
					console.log(obj);
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
		}
	} else { // jstreefile
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
			"deleteFile" : {
				// The item label
				"label"             : "[TODO] Delete file",
				// The function to execute upon a click
				"action"            : function (obj) {
					console.log($(node).attr("id"));
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
	if (!app.areChangesSaved()) {
		var confirm = window.confirm("Changes not saved, proceed?");
		if (!confirm) return;
	}
	data.openFile(null, filename,[ {files: [filename]} ]);
}

FileManager.prototype.sendSelectBaseDir = function(elem) {
	window.postMessage({type: 'select-dirs'})
}

FileManager.prototype.selectBaseDir = function(elem) {
	alert(elem.value);
	//localStorage.getItem(App.LOCALSTORAGEPREFIX+"root");
}

