// manage resources. Resources can be added, removed, and associated with
// entity IDs (like avatar, scene name).

function ResourceUI(serialized,defaultlist, defaultidx) {
	this.defaultlist = defaultlist;
	this.currententity = null;
	// currentidx refers to currently selected index on currently selected
	// entity
	this.currentidx = serialized!=null && serialized.currentidx!==null 
		? serialized.currentidx
		: defaultidx;
	// all known resources
	this.list = serialized && serialized.list ? serialized.list : defaultlist;
	// mapping entityID -> resourceID
	this.mapping = serialized && serialized.mapping ? serialized.mapping : {};
}

ResourceUI.prototype.serialize = function() {
	return {
		"currentidx": this.currentidx,
		"list": this.list,
		"mapping": this.mapping,
	}
}

// set currentidx to entity's resource, or default value if not defined.
ResourceUI.prototype.switchEntity = function(entityID,defaultIndex) {
	//console.log("switchEntity "+entityID);
	this.currententity = entityID;
	if (typeof this.mapping[entityID] == 'undefined') {
		this.mapping[entityID] = this.list[defaultIndex];
		this.currentidx = defaultIndex;
		return false;
	} else {
		this.currentidx = this.list.indexOf(this.mapping[entityID]);
		if (this.currentidx < 0) this.currentidx = 0;
		return true;
	}
}

ResourceUI.prototype.getCurrentEntity = function() {
	return this.currententity;
}

// Cycle through resources for given entity
ResourceUI.prototype.inc = function(amount) {
	this.currentidx += amount;
	if (this.currentidx < 0) this.currentidx = this.list.length - 1;
	if (this.currentidx >= this.list.length) this.currentidx = 0;
	this.mapping[this.currententity] = this.list[this.currentidx];

}

ResourceUI.prototype.add = function(resourceID) {
	this.list.push(resourceID);
	this.currentidx = this.list.length-1;
	this.mapping[this.currententity] = this.list[this.currentidx];
}

ResourceUI.prototype.getCurrent = function() {
	return this.list[this.currentidx];
}

ResourceUI.prototype.getCurrentHumanReadable = function() {
	var prefix = this.currentIsNumber() ? "Preset " : "Custom: '";
	var suffix = this.currentIsNumber() ? "" : "'";
	return prefix + this.getCurrent() + suffix;
}

ResourceUI.prototype.currentIsNumber = function() {
	return !isNaN(this.list[this.currentidx]);
}

ResourceUI.prototype.removeCurrent = function() {
	if (this.list.length < 2) return; // refuse to delete last element
	this.list.splice(this.currentidx,1);
	if (this.currentidx > 0) this.currentidx--;
	this.inc(0); // set mapping
}

