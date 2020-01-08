// Simple class for playing animations exported from Adobe after effects
// using the lottie animation system.

// containerid - id of containing html element
// basedir - base directory for animations, no trailing slash, default is .
function WoolAnimPlayer(containerid,basedir) {
	this.containerid = containerid;
	this.basedir = basedir ? basedir+"/" : "";
	this.container = document.getElementById(containerid);
	this.anims = {};
	this.playing = [];
	this.playpos = 0;
	this.activeanim = null;
}

WoolAnimPlayer.prototype.addAnimation = function(avatar,animname) {
	var containerdiv = document.createElement("div");
	containerdiv.className = "woolanimationlayercontainer";
	var div = document.createElement("div");
	div.className = "woolanimationlayer hidden";
	div.id = "_animationlayer_"+avatar+"_"+animname;
	containerdiv.appendChild(div);
	this.container.appendChild(containerdiv);
	console.log("Adding animation from path:"+avatar+"_"+animname+"/data.json");
	var anim = bodymovin.loadAnimation({
		container: div, // Required
		path: this.basedir+avatar+"_"+animname+"/data.json", // Required
		renderer: 'canvas', // Required
		loop: true, // Optional
		autoplay: false, // Optional
		//onComplete: function() {
		//	console.log("Anim complete");
		//},
		//onLoopComplete: function() {
		//	console.log("Anim complete");
		//},
	});
	this.anims[avatar+"_"+animname] = {
		containerdiv: containerdiv,
		div: div,
		anim: anim,
	};
	anim.setSpeed(6.0);
	var self=this;
	anim.addEventListener("loopComplete",function(e) {
		console.log("Anim complete");
		self.playNextAnimation();
	});
}

WoolAnimPlayer.prototype.playAnimations = function(avatar,animations) {
	this.stopAnimation();
	this.playpos = -1;
	this.playing = {
		avatar: avatar,
		animations: animations,
	};
	this.playNextAnimation();
}
WoolAnimPlayer.prototype.playNextAnimation = function() {
	this.playpos++;
	if (this.playpos >= this.playing.animations.length) return;
	var animdef = this.anims[this.playing.avatar+"_"+this.playing.animations[this.playpos]];
	animdef.div.className = "woolanimationlayer";
	animdef.anim.play();
	this.stopAnimation();
	this.activeanim = animdef;
}

WoolAnimPlayer.prototype.stopAnimation = function() {
	if (this.activeanim) {
		this.activeanim.anim.stop();
		this.activeanim.div.className="woolanimationlayer hidden";
	}
	this.activeanim=null;
}

