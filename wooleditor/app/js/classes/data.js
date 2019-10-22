var FILETYPE = { JSON: "json", XML: "xml", TWEE: "twee", TWEE2: "tw2", UNKNOWN: "none", YARNTEXT: "yarn.txt", WOOL: "wool" };

var data =
{
	editingPath: ko.observable(null),
	editingType: ko.observable(""),
	editingFolder: ko.observable(null),

	filename: "file",

	readFile: function(e, filename, clearNodes, element) {
		console.log(filename);
		var filebase = filename.match(/^.*[\/\\]([^.]*)[.][YWyw][aoAO][roRO][LNln][txt.]*$/i);
		if (filebase) {
			data.filename = filebase[1];
		}
		if (false && app.fs != undefined)
		{
			if (app.fs.readFile(filename, "utf-8", function(error, contents)
			{
				if (error)
				{

				}
				else
				{
					var type = data.getFileType(filename);
					if (type == FILETYPE.UNKNOWN)
						alert("Unknown filetype!");
					else
					{
						data.editingPath(filename);
						data.editingType(type);
						data.loadData(contents, type, clearNodes);
					}
				}
			}));
		}
		else
		{
			var reader = new FileReader();
			reader.onerror = function(e) {
				alert("Error reading file");
			}
			reader.onload = function(e) {
				var type = data.getFileType(filename);
				data.editingPath(filename);
				data.editingType(type);
				data.loadData(e.target.result, type, clearNodes);
			}
			reader.readAsText(element[0].files[0]);
		}
		/*
		else if (window.File && window.FileReader && window.FileList && window.Blob && e.target && e.target.files && e.target.files.length > 0)
		{
			var reader  = new FileReader();
			reader.onloadend = function(e) 
			{
				if (e.srcElement && e.srcElement.result && e.srcElement.result.length > 0)
				{
					var contents = e.srcElement.result;
					var type = data.getFileType(contents);
					alert("type(2): " + type);
					if (type == FILETYPE.UNKNOWN)
						alert("Unknown filetype!");
					else
						data.loadData(contents, type, clearNodes);
				}
			}
			reader.readAsText(e.target.files[0], "UTF-8");
		}
		*/
	},

	openFile: function(e, filename, element) {
		data.readFile(e, filename, true, element);

		app.resetUIState();
		app.refreshWindowTitle(filename);
	},

	openFolder: function(e, foldername)
	{
		editingFolder = foldername;
		alert("openFolder not yet implemented e: " + e + " foldername: " + foldername);
	},

	appendFile: function(e, filename, element) {
		data.readFile(e, filename, false, element);
	},

	getFileType: function(filename)
	{
		var clone = filename;

		if (filename.toLowerCase().indexOf(".json") > -1)
			return FILETYPE.JSON;
		else if (filename.toLowerCase().indexOf(".wool") > -1)
			return FILETYPE.WOOL;
		else if (filename.toLowerCase().indexOf(".yarn.txt") > -1)
			return FILETYPE.YARNTEXT;
		else if (filename.toLowerCase().indexOf(".xml") > -1)
			return FILETYPE.XML;
		else if (filename.toLowerCase().indexOf(".txt") > -1)
			return FILETYPE.TWEE;
        else if (filename.toLowerCase().indexOf(".tw2") > -1)
            return FILETYPE.TWEE2;
		return FILETYPE.UNKNOWN;
		/*
		// is json?
		if (/^[\],:{}\s]*$/.test(clone.replace(/\\["\\\/bfnrtu]/g, '@').
			replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g, ']').
			replace(/(?:^|:|,)(?:\s*\[)+/g, ''))) 
			return FILETYPE.JSON;

		// is xml?
		var oParser = new DOMParser();
		var oDOM = oParser.parseFromString(content, "text/xml");
		if (oDOM.documentElement["outerText"] == undefined)
			return FILETYPE.XML;

		// is twee?
		//console.log(content.substr(0, 2));
		console.log(content.indexOf("::"));
		if (content.trim().substr(0, 2) == "::")
			return FILETYPE.TWEE;
		return FILETYPE.UNKNOWN;
		*/
	},

	loadData: function(content, type, clearNodes, doNotCenter)
	{
		// clear all content
		if (clearNodes)
			app.nodes.removeAll();

		var objects = [];
		var i = 0;
		var warnings=[];
		if (type == FILETYPE.JSON)
		{
			content = JSON.parse(content);
			for (i = 0; i < content.length; i ++)
				objects.push(content[i]);
		}
		else if (type == FILETYPE.YARNTEXT
		||       type == FILETYPE.WOOL) {
			var convertSpeaker = type == FILETYPE.YARNTEXT;
			console.log("YARN: "+convertSpeaker);
			var lines = content.split("\n");
			var obj = null;
			var index  = 0;
			var readingBody = false;
			for  (var i = 0; i < lines.length; i ++) {
				// chop off any /r characters
				lines[i] = lines[i].replace("\r","");
				//lines[i] = lines[i].trim();

				if (lines[i].trim() == "===") {
					readingBody = false;
					if (obj != null)
					{
						objects.push(obj);
						obj = null;
					}
				} else if (readingBody) {
					if (convertSpeaker) {
						matches = lines[i].match(/^([a-zA-Z]+):\s*(.*)\s*$/);
						if (matches) {
							if (obj.speaker && matches[1]!=obj.speaker) {
								warnings.push("Line "+(i+1)
								+": Different speakers found in body: "
								+obj.speaker+" and "+matches[1]);
							}
							obj.speaker = matches[1];
							lines[i] = matches[2];
						}
					}
					obj.body += lines[i] + "\n";
				} else {
					// XXX do proper key:value parsing. Currently, there can
					// be a string before the key, and the value offset does
					// not match the ending position of the key
					if (lines[i].indexOf("title:") > -1)
					{
						if (obj == null)
							obj = {};
						obj.title = lines[i].substr(7, lines[i].length-7);
					}
					else if (lines[i].indexOf("position:") > -1)
					{
						if (obj == null)
							obj = {}
						var xy = lines[i].substr(9, lines[i].length-9).split(',');
						obj.position = { x: Number(xy[0].trim()), y: Number(xy[1].trim()) }
					}
					else if (lines[i].indexOf("colorID:") > -1)
					{
						if (obj == null)
							obj = {}
						obj.colorID = Number(lines[i].substr(9, lines[i].length-9).trim());
					}
					else if (lines[i].indexOf("tags:") > -1)
					{
						if (obj == null)
							obj = {}
						obj.tags = lines[i].substr(6, lines[i].length-6);
					}
					else if (lines[i].indexOf("speaker:") > -1)
					{
						if (obj == null)
							obj = {}
						obj.speaker = lines[i].substr(9, lines[i].length-9);
					}
					else if (lines[i].trim() == "---")
					{
						readingBody = true;
						obj.body = "";
					}
				}
			}
			if (obj != null)
			{
				objects.push(obj);
			}
		}
		else if (type == FILETYPE.TWEE || type == FILETYPE.TWEE2)
		{
			// XXX not updated, remove support?
			var lines = content.split("\n");
			var obj = null;
			var index  = 0;
			for  (var i = 0; i < lines.length; i ++)
			{
				lines[i] = lines[i].replace("\r","");
				//lines[i] = lines[i].trim();
				if (lines[i].substr(0, 2) == "::")
				{
					if (obj != null)
						objects.push(obj);

					obj = {};
					index ++;

					var title = "";
					var tags = "";
					var position = {x: index * 80, y: index * 80};

					// check if there are tags
					var openBracket = lines[i].indexOf("[");
					var closeBracket = lines[i].indexOf("]");
                    if (openBracket > 0 && closeBracket > 0)
					{
						tags = lines[i].substr(openBracket + 1, closeBracket - openBracket - 1);
					}

                    // check if there are positions (Twee2)
                    var openPosition = lines[i].indexOf("<");
                    var closePosition = lines[i].indexOf(">");

					if (openPosition > 0 && closePosition > 0)
                    {
                        var coordinates = lines[i].substr(openPosition + 1, closePosition - openPosition - 1).split(',');
                        position.x = parseInt(coordinates[0]);
                        position.y = parseInt(coordinates[1]);
                    }

                    var metaStart = 0;
					if (openBracket > 0) {
						metaStart = openBracket;
					} else if (openPosition > 0) {
                        // Twee2 dictates that tags must come before position, so we'll only care about this if we don't
						// have any tags for this Passage
                        metaStart = openPosition
                    }

                    console.log(openBracket, openPosition, metaStart);

                    if (metaStart) {
                        title = lines[i].substr(3, metaStart - 3);
                    } else {
                        title = lines[i].substr(3);
                    }

					obj.title = title;
					obj.tags = tags;
					obj.body = "";
                    obj.position = position;
				}
				else if (obj != null)
				{
					if (obj.body.length > 0)
						lines[i] += '\n';
					obj.body += lines[i];
				}
			}

			if (obj != null)
				objects.push(obj);
		}
		else if (type == FILETYPE.XML)
		{
			var oParser = new DOMParser();
			var xml = oParser.parseFromString(content, "text/xml");
			content = Utils.xmlToObject(xml);

			if (content != undefined)
				for (i = 0; i < content.length; i ++)
					objects.push(content[i]);
		}

		var avgX = 0, avgY = 0;
		var numAvg = 0;
		for (var i = 0; i < objects.length; i ++)
		{
			var node = new Node();
			app.nodes.push(node);
			
			var object = objects[i]
			if (object.title != undefined)
				node.title(object.title);
			if (object.body != undefined)
				node.body(object.body);
			if (object.tags != undefined)
				node.tags(object.tags);
			if (object.speaker != undefined)
				node.speaker(object.speaker);
			if (object.position != undefined && object.position.x != undefined)
			{
				node.x(object.position.x);
				avgX += object.position.x;
				numAvg ++;
			}	
			if (object.position != undefined && object.position.y != undefined)
			{
				node.y(object.position.y);
				avgY += object.position.y;
			}
			if (object.colorID != undefined)
				node.colorID(object.colorID);
		}

		if (numAvg > 0 && !doNotCenter) {
			app.warpToNodeXY(avgX/numAvg, avgY/numAvg);
		}

		$(".arrows").css({ opacity: 0 }).transition({ opacity: 1 }, 500);
		app.updateNodeLinks(true); // force update after loading
		// XXX save after a second because position is obtained from css
		// transform which is not updated immediately after style is set.
		// TODO maintain node position in different way
		setTimeout(data.saveToBuffer,1000);
		if (warnings.length) alert(warnings.join("\n"));
	},

	getSaveData: function(type,node) {
		var output = "";
		var content = [];
		var nodes = node ? [ node ] : app.nodes();

		for (var i = 0; i < nodes.length; i ++)
		{
			content.push({
				"title": nodes[i].title(), 
				"tags": nodes[i].tags(), 
				"speaker": nodes[i].speaker(), 
				"body": nodes[i].body(),
				"position": { "x": nodes[i].x(), "y": nodes[i].y() },
				"colorID": nodes[i].colorID()
			});
		}

		if (type == FILETYPE.JSON)
		{
			output = JSON.stringify(content, null, "\t");
		}
		else if (type == FILETYPE.WOOL)
		{
			for (i = 0; i < content.length; i++)
			{
				output += "title: " + content[i].title + "\n";
				output += "tags: " + content[i].tags + "\n";
				output += "speaker: " + content[i].speaker + "\n";
				output += "colorID: " + content[i].colorID + "\n";
				output += "position: " + content[i].position.x + "," + content[i].position.y + "\n";
				output += "---\n";
				output += content[i].body;
				var body = content[i].body
				if (!(body.length > 0 && body[body.length-1] == '\n'))
				{
					output += "\n";
				}
				output += "===\n";
			}
		}
		else if (type == FILETYPE.TWEE)
		{
			for (i = 0; i < content.length; i ++)
			{
				var tags = "";
				if (content[i].tags.length > 0)
					tags = " [" + content[i].tags + "]"
				output += ":: " + content[i].title + tags + "\n";
				output += content[i].body + "\n\n";
			}
		}
        else if (type == FILETYPE.TWEE2)
        {
            for (i = 0; i < content.length; i ++)
            {
                var tags = "";
                if (content[i].tags.length > 0)
                    tags = " [" + content[i].tags + "]"
				var position = " <" + content[i].position.x + "," + content[i].position.y + ">";
                output += ":: " + content[i].title + tags + position + "\n";
                output += content[i].body + "\n\n";
            }
        }
		else if (type == FILETYPE.XML)
		{
			output += '<nodes>\n';
			for (i = 0; i < content.length; i ++)
			{
				output += "\t<node>\n";
				output += "\t\t<title>" + content[i].title + "</title>\n";
				output += "\t\t<tags>" + content[i].tags + "</tags>\n";
				output += "\t\t<body>" + content[i].body + "</body>\n";
				output += '\t\t<position x="' + content[i].position.x + '" y="' + content[i].position.y + '"></position>\n';
				output += '\t\t<colorID>' + content[i].colorID + '</colorID>\n';
				output += "\t</node>\n";
			}
			output += '</nodes>\n';
		}

		return output;
	},

	saveTo: function(path, content)
	{
		if (app.fs != undefined)
		{
			app.fs.writeFile(path, content, {encoding: 'utf-8'}, function(err) 
			{
				data.editingPath(path);
				if(err)
					alert("Error Saving Data to " + path + ": " + err);
			});
		}
	},

	openFileDialog: function(dialog, callback)
	{
		dialog.bind("change", function(e)
		{
			// make callback
			callback(e, dialog.val(), dialog);

			// replace input field with a new identical one, with the value cleared
			// (html can't edit file field values)
			var saveas = '';
			var accept = '';
			if (dialog.attr("nwsaveas") != undefined)
				saveas = 'nwsaveas="' + dialog.attr("nwsaveas") + '"'
			if (dialog.attr("accept") != undefined)
				saveas = 'accept="' + dialog.attr("accept") + '"'

			dialog.parent().append('<input type="file" id="' + dialog.attr("id") + '" ' + accept + ' ' + saveas + '>');
			dialog.unbind("change");
			dialog.remove();
		});

		dialog.trigger("click");
	},

	saveFileDialog: function(dialog, type, content) {
		var file = data.filename + "." + type;

		if (app.fs)
		{
			dialog.attr("nwsaveas", file);
			data.openFileDialog(dialog, function(e, path)
			{
				data.saveTo(path, content);
				app.refreshWindowTitle(path);
			});
		}
		else
		{
			switch(type) {
				case 'json':
					content = "data:text/json," + content;
					break;
				case 'xml':
					content = "data:text/xml," + content;
					break;
				default:
					content = "data:text/plain," + encodeURIComponent(content);
					break;
			}
			dialog.download = file;
			dialog.href = content;
			return true;
			//window.open(content, "_blank");
		}
	},
	saveToBuffer: function(dialog, type, content) {
		var content = data.getSaveData(FILETYPE.WOOL);
		if (localStorage) {
			localStorage.setItem(app.LOCALSTORAGEPREFIX+"buffer",content);
		}
	},
	// returns true = data loaded
	loadFromBuffer: function(dialog, type, content) {
		if (localStorage) {
			var loaddata=localStorage.getItem(app.LOCALSTORAGEPREFIX+"buffer");
			if (loaddata) {
				data.loadData(loaddata, FILETYPE.WOOL, /*clearNodes*/true,
				true);
				return true;
			}
		}
		return false;
	},

	tryClearData: function() {
		if (!confirm("Clear all nodes?")) return;
		app.resetUIState();
		app.nodes.removeAll();
		app.newNode().title("Start");
		app.updateArrows();
	},

	tryOpenFile: function()
	{
		data.openFileDialog($('#open-file'), data.openFile);
	},

	tryOpenFolder: function()
	{
		data.openFileDialog($('#open-folder'), data.openFolder);
	},

	tryAppend: function()
	{
		data.openFileDialog($('#open-file'), data.appendFile);
	},

	trySave: function(type)
	{
		data.editingType(type);
		data.saveFileDialog($('#save-file'), type, data.getSaveData(type));
	},

	trySaveCurrent: function()
	{
		if (data.editingPath().length > 0 && data.editingType().length > 0)
		{
			data.saveTo(data.editingPath(), data.getSaveData(data.editingType()));
		}
	}

}
