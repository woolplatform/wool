// ------------------------------------------------------------------
// helpers

function detectNodeJS() {
	return typeof(process) !== "undefined";
}

function getPlatformFileSystem() {
	if (detectNodeJS()) {
		//this.gui = require('nw.gui');
		return new NodeFileSystem();
	} else {
		return new BrowserFileSystem();
	}
}

// ------------------------------------------------------------------
// abstract class, simulating nodejs fs functions

function FileSystem(fstype) {
	this.fstype = fstype;
}

// blob - file data blob, as obtained from file input, null means use filename
// callback - function(err,data), data is the file content as an utf8 string
FileSystem.prototype.readFile = function(path,blob,callback){}

FileSystem.prototype.readFileSync = function(path,blob,callback){}

// callback - function(err)
FileSystem.prototype.writeFile = function(path,data,callback) {}

// callback - function(err)
FileSystem.prototype.copyFile = function(srcpath,dstpath,callback) {}

// callback - function(err)
FileSystem.prototype.deleteFile = function(path,callback) {}

// callback - function(err,files), files is an array of filenames/dirnames
FileSystem.prototype.readdir = function(path,callback) {}

// callback - function(err,files), files is an array of relative paths.
//                                 Trailing slash indicates item is a dir
FileSystem.prototype.readdirtree = function(path,callback) {}

FileSystem.prototype.existsSync = function(path) {}

FileSystem.prototype.mkdir = function(path,callback) {}

FileSystem.prototype.mkdirSync = function(path) {}


// safe - true = check if file exists before overwriting
// callback - function(err)
FileSystem.prototype.renameFile = function(oldpath,newpath,safe,callback) {}

FileSystem.prototype.getPathAPI = function(path) { 
	return {
		sep: "/",
		dirname: function(path) {
			if (path.indexOf("/") === false) {
				return "";
			} else {
				return path.substring(0,path.lastIndexOf("/"));
			}
		},
		join: function(sep,pathprefix,pathsuffix) {
			return pathprefix+sep+pathsuffix;
		},
		normalize: function(path) { return path; },
	}
}

// for BrowserFileSystem, enables readFileSync for cached files
FileSystem.cacheFile = function(url,path,success,failure){}

// ------------------------------------------------------------------
// node.js implementation. Assumes node.js is available.

function NodeFileSystem() {
	FileSystem.apply(this,["node"]);
	this.fs = require('fs');
	this.path = require('path');
}
NodeFileSystem.prototype = new FileSystem();

NodeFileSystem.prototype.readFile = function(path,blob,callback){
	if (blob) {
		BrowserFileSystem.readFileStatic(path,blob,callback);
	} else {
		this.fs.readFile(path, "utf-8", callback);
	}
}

NodeFileSystem.prototype.readFileSync = function(path,blob,callback) {
	try {
		return this.fs.readFileSync(path, "utf-8");
	} catch (err) {
		if (callback) callback(err);
		return null;
	}
}

NodeFileSystem.prototype.writeFile = function(path,data,callback) {
	this.fs.writeFile(path,data,{encoding: 'utf-8'},callback);
}

NodeFileSystem.prototype.copyFile = function(srcpath,dstpath,callback) {
	this.fs.copyFile(srcpath,dstpath,this.fs.constants.COPYFILE_EXCL,
		callback);
}

NodeFileSystem.prototype.deleteFile = function(path,callback) {
	this.fs.unlink(path,callback);
}

NodeFileSystem.prototype.readdir = function(path,callback) {}

NodeFileSystem.prototype.readdirtree = function(path,callback) {
	var self=this;
	//From: https://stackoverflow.com/questions/5827612/node-js-fs-readdir-recursive-directory-search
	var walk = function(root, dir, callback) {
		var results = [];
		var dirall = dir ? self.path.join(root,dir) : root;
		self.fs.readdir(dirall, function(err, list) {
			if (err) return callback(err);
			var pending = list.length;
			if (!pending) return callback(null, results);
			list.forEach(function(file) {
				var relpath = self.path.join(dir, file);
				var abspath = self.path.resolve(dirall, file);
				self.fs.stat(abspath, function(err, stat) {
					if (stat && stat.isDirectory()) {
						results.push(["DIR",file,self.path.join(dir,file)]);
						walk(root, relpath, function(err, res) {
							results = results.concat(res);
							if (!--pending) callback(null, results);
						});
					} else {
						results.push(["FILE",file,relpath]);
						if (!--pending) callback(null, results);
					}
				});
			});
		});
	};
	walk(path,"",callback);
}

NodeFileSystem.prototype.existsSync = function(path) {
	return this.fs.existsSync(path);
}

NodeFileSystem.prototype.mkdir = function(path,callback) {
	return this.fs.mkdir(path,{recursive:true},callback);
}

NodeFileSystem.prototype.mkdirSync = function(path) {
	return this.fs.mkdirSync(path,{recursive:true});
}


// also renames directories
NodeFileSystem.prototype.renameFile = function(oldpath,newpath,safe,callback) {
	var self=this;
	if (safe) {
		this.fs.access(newpath,this.fs.constants.F_OK,function(err) {
			if (err) {
				// file does not exist -> OK
				self.renameFile(oldpath,newpath,false,callback);
			} else {
				callback("File already exists.");
			}
		});
	} else {
		this.fs.rename(oldpath, newpath, callback);
	}
}

NodeFileSystem.prototype.getPathAPI = function() { return this.path; }

// ------------------------------------------------------------------
// browser-only implementation

function BrowserFileSystem() {
	FileSystem.apply(this,["browser"]);
}

BrowserFileSystem.prototype = new FileSystem();

// implements both sync and async
BrowserFileSystem.readFileStatic = function(path,blob,callback) {
	if (!blob) {
		// see if we have a cached file
		var cachedfile = BrowserFileSystem.filecache[path];
		if (cachedfile) {
			if (callback) callback(null,cachedfile);
			return cachedfile;
		} else {
			if (callback) callback("File not found in cache",null);
		}
	} else {
		var reader = new FileReader();
		reader.onerror = function(e) {
			if (callback) callback("Error reading file",null);
		}
		reader.onload = function(e) {
			if (callback) callback(null,e.target.result);
		}
		reader.readAsText(blob);
	}
}
BrowserFileSystem.prototype.readFile = BrowserFileSystem.readFileStatic;

BrowserFileSystem.prototype.readFileSync = function(path,blob,callback) {
	return BrowserFileSystem.readFileStatic(path,blob,null);
}

BrowserFileSystem.prototype.writeFile = function(path,data,callback) {}

// not available

BrowserFileSystem.prototype.readdir = null;

BrowserFileSystem.prototype.readdirtree = function(path,callback) {
	callback(null, [
		{
			"id": "/file1",
			"text": "file1",
			"parent": "#",
			"icon": "jstreefile",
		},
		{
			"id": "/file2",
			"text": "file2",
			"parent": "#",
			"icon": "jstreefile",
		},
		{
			"id": "/dir1",
			"text": "dir1",
			"parent": "#",
			"icon": "jstreedir",
		},
		{
			"id": "/dir1/d1file1",
			"text": "d1file1",
			"parent": "/dir1",
			"icon": "jstreefile",
		},
		{
			"id": "/dir1/d1file2",
			"text": "d1file2",
			"parent": "/dir1",
			"icon": "jstreefile",
		},
		{
			"id": "/dir2",
			"text": "dir2",
			"parent": "#",
			"icon": "jstreedir",
		},
		{
			"id": "/dir2/d2file1",
			"text": "d1file1",
			"parent": "/dir2",
			"icon": "jstreefile",
		},
		{
			"id": "/dir2/d1file2",
			"text": "d2file2",
			"parent": "/dir2",
			"icon": "jstreefile",
		},
		{
			"id": "/dir2/dir2-2",
			"text": "dir2-2",
			"parent": "/dir2",
			"icon": "jstreedir",
		},
		{
			"id": "/dir2/dir2-2/d2-2file_aap",
			"text": "d2-2file_aap",
			"parent": "/dir2/dir2-2",
			"icon": "jstreefile",
		},
		{
			"id": "/dir2/dir2-2/d2-2file_noot",
			"text": "d2-2file_noot",
			"parent": "/dir2/dir2-2",
			"icon": "jstreefile",
		},
	]);
	/*callback(null, {
		"file1": null,
		"file2": null,
		"dir1", [
			["d1file1"],
			["d1file2"],
		]],
		["dir2", [
			["d2file1"],
			["d2file2"],
			["dir2-2", [
				["d2-2fileajdlkajdlkakldjklad"],
				["d2-2fileqwlqwjlkazx-lksjlaks"],
			]],
		]],
	]);*/
	/*
	callback(null, [
		["file1"],
		["file2"],
		["dir1", [
			["d1file1"],
			["d1file2"],
		]],
		["dir2", [
			["d2file1"],
			["d2file2"],
			["dir2-2", [
				["d2-2fileajdlkajdlkakldjklad"],
				["d2-2fileqwlqwjlkazx-lksjlaks"],
			]],
		]],
	]);*/
}


BrowserFileSystem.prototype.renameFile = null;


BrowserFileSystem.filecache = {}; // dirname => content(string)

BrowserFileSystem.cacheFile = function(url,path,success,failure) {
    var request = new XMLHttpRequest();
	// https://stackoverflow.com/questions/51000009/i-keep-getting-this-error-xml-parsing-error-syntax-error-but-still-the-website/51000139
	request.overrideMimeType("text/plain");
    request.open('GET', url, true);
    request.onreadystatechange = function () {
        if (request.readyState === 4) {
			if (request.status === 200) {
				if (request.responseText) {
				//var type = request.getResponseHeader('Content-Type');
				//if (type.indexOf("text") !== 1) {
					BrowserFileSystem.filecache[path] = request.responseText;
					success();
				} else {
					failure("Not a text file.");
				}
			} else {
				failure("Response code: "+request.status);
			}
        }
    }
    request.send(null);
}


if (typeof exports !== 'undefined') {
	// node.js require()
	exports.detectNodeJS = detectNodeJS;
	exports.getPlatformFileSystem = getPlatformFileSystem;
	exports.FileSystem = FileSystem;
	exports.NodeFileSystem = NodeFileSystem;
	exports.BrowserFileSystem = BrowserFileSystem;
}

