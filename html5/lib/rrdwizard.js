
/*
* wizard style user interface
*/


/* containerid - container that is made visible when wizard pops up
 * toplevelid - top level element where wizard elements can be found,
 *              default is the same as containerid
 */
function Wizard(containerid,toplevelid) {
	this.containerid = containerid;
	this.container = document.getElementById(this.containerid);
	if (!toplevelid) toplevelid = containerid;
	this.toplevelid = toplevelid;
	this.toplevel = document.getElementById(this.toplevelid);
	this.closeAll();
}

Wizard.prototype.closeAll = function(elemid) {
	this.container.style.display = "none";
	for (c in this.toplevel.children) {
		var elem = this.toplevel.children[c];
		if (elem.className && elem.className.split(" ").indexOf("wizard") >= 0) {
			//elem.style.opacity = 0;
			elem.style.display = "none";
		}
	}

}

Wizard.prototype.open = function(elemid) {
	this.closeAll();
	var elem = document.getElementById(elemid);
	elem.style.display = "block";
	//elem.style.opacity = 1;
	this.container.style.display = "block";
}
