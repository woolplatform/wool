function AgentProperties(name,gender,id) {
	this.name = name;
	// male, female, dynamic, null is undefined
	this.gender = gender;
	// in case gender is dynamic, gender variable prefix
	// otherwise, can be null
	this.id = id;
}

function Metadata() {
	this._agents = {}; // agentname -> AgentProperties
}

// load from local storage or metadata.xml file
// rootdir - project root dir, undefined = use local storage
Metadata.prototype.load = function(rootdir) {
	var xml;
	if (!rootdir || !app.isNwjs) {
		xml = localStorage.getItem(App.LOCALSTORAGEPREFIX+"metadata");
	} else {
		xml = app.fs.readFileSync(data.appendRoot("metadata.xml"),null,
			function(err) {console.log("Error reading metadata: "+err.code);} );
	}
	console.log(xml);
	//var parser = new DOMParser();
	//var xmlDoc = parser.parseFromString(xml,"text/xml");

	//document.getElementById("demo").innerHTML =
	//xmlDoc.getElementsByTagName("title")[0].childNodes[0].nodeValue;		

}

Metadata.prototype.save = function(rootdir) {
	xml = "<metadata>\n\t<agents>\n";
	for (var agentname in this._agents) {
		var agent = this._agents[agentname];
		xml += "\t\t<agent name='"+agent.name+"' "
			+ (agent.gender ? "gender='"+agent.gender+"' " : "")
			+ (agent.id ? "id='"+agent.id+"' " : "")
			+" />\n";
	}
	xml += "\t</agents>\n</metadata>\n";
	console.log(xml);
	// always save to localStorage
	localStorage.setItem(App.LOCALSTORAGEPREFIX+"metadata",xml);
	if (rootdir && app.isNwjs) {
		app.fs.writeFile(data.appendRoot("metadata.xml"),null, function(err) {
			alert("Error writing metadata.xml: "+err);
		});
	}
}

Metadata.prototype.addAgent = function(agent) {
	this._agents[agent.name] = agent;
}

Metadata.prototype.getAgent = function(agentname) {
	return this._agents[agent.name];
}

Metadata.prototype.removeAgent = function(agentname) {
	delete this._agents[agentname];
}


