
// abstract class, simulating nodejs fs functions

function FileSystem(fstype) {
	this.fstype = fstype;
}

// blob - file data blob, as obtained from file input, null means use filename
// callback - function(err,data), data is the file content as an utf8 string
FileSystem.prototype.readFile = function(path,blob,callback){}

// callback - function(err)
FileSystem.prototype.writeFile = function(path,data,callback) {}

// callback - function(err,files), files is an array of filenames/dirnames
FileSystem.prototype.readdir = function(path,callback) {}

// callback - function(err,files), files is an array of relative paths.
//                                 Trailing slash indicates item is a dir
FileSystem.prototype.readdirtree = function(path,callback) {}



// node.js implementation. Assumes node.js is available.

function NodeFileSystem() {
	FileSystem.apply(this,["node"]);
	this.fs = require('fs');
	this.path = require('path');
	console.log("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
	console.log(this.fs);
}
NodeFileSystem.prototype = new FileSystem();

NodeFileSystem.prototype.readFile = function(path,blob,callback){
	this.fs.readFile(path, "utf-8", callback);
}

NodeFileSystem.prototype.writeFile = function(path,data,callback) {
	this.fs.writeFile(path,data,{encoding: 'utf-8'},callback);
}

NodeFileSystem.prototype.readdir = function(path,callback) {}

NodeFileSystem.prototype.readdirtree = function(path,callback) {
	var self=this;
	//From: https://stackoverflow.com/questions/5827612/node-js-fs-readdir-recursive-directory-search
	var walk = function(dir, callback) {
		var results = [];
		self.fs.readdir(dir, function(err, list) {
			if (err) return callback(err);
			var pending = list.length;
			if (!pending) return callback(null, results);
			list.forEach(function(file) {
				var filepath = self.path.resolve(dir, file);
				self.fs.stat(filepath, function(err, stat) {
					if (stat && stat.isDirectory()) {
						results.push(["DIR",file,filepath]);
						walk(filepath, function(err, res) {
							results = results.concat(res);
							if (!--pending) callback(null, results);
						});
					} else {
						results.push([file,filepath]);
						if (!--pending) callback(null, results);
					}
				});
			});
		});
	};
	walk(path,callback);
}



// browser-only implementation

function BrowserFileSystem() {
	FileSystem.apply(this,["browser"]);
}
BrowserFileSystem.prototype = new FileSystem();

BrowserFileSystem.prototype.readFile = function(path,blob,callback) {
	var reader = new FileReader();
	reader.onerror = function(e) {
		callback("Error reading file",null);
	}
	reader.onload = function(e) {
		callback(null,e.target.result);
	}
	reader.readAsText(blob);
}

BrowserFileSystem.prototype.writeFile = function(path,data,callback) {}

// not available

BrowserFileSystem.prototype.readdir = null;

BrowserFileSystem.prototype.readdirtree = null;


