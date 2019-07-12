// elem - html node to place avatar under
// gender: "M" or "F", random if not supplied
// seed: random seed
function GenAvataaar(elem,seed,gender) {
	var mulberry32seed = seed ? Math.floor(seed) : 0;
	function mulberry32() {
		var t = mulberry32seed += 0x6D2B79F5;
		t = Math.imul(t ^ t >>> 15, t | 1);
		t ^= t + Math.imul(t ^ t >>> 7, t | 61);
		return ((t ^ t >>> 14) >>> 0) / 4294967296;
	}
	function myGetRandomInt(max) {
		return Math.floor(mulberry32()*max);
	}

	if (!gender) gender = ["M","F"][myGetRandomInt(2)];
	console.log(gender);

	var exclude = {
		"top": [ "winterHat1", "winterHat2", "winterHat4",
			"longHairFrida", "longHairShavedSides", "shortHairFrizzle",
			"shortHairTheCaesarSidePart", "longHairCurvy", "longHairDreads", ],
		"accessory": [ "kurt", "sunglasses", "eyepatch",
			"wayfarers", ],
		"body.color": [ "black", ], // contrasts badly with facial features
		"eyes": ["cry", "dizzy", "hearts","wink","winkWacky",
			"close", "happy", "eyeRoll", "surprised", ],
		"mouth": ["vomit", "sad", "disbelief", "grimace", "concerned",
			"screamOpen", "eating", "tongue"],
		"eyebrow": ["upDown","upDownNatural" ],
		"avatarStyle": [ "1" ],
	};

	if (gender=="F") {
		exclude["top"] = exclude["top"].concat(
			[ "blank", "turban", "shortHairSides",]);
		exclude["facialHair"] = [ "beardLight", "beardMagestic",
			"beardMedium", "moustacheFancy", "moustacheMagnum", ];
	} else { // "M"
		exclude["top"] = exclude["top"].concat(
			[ "hijab", 
			"longHairFroBand", "longHairBun", "longHairMiaWallace",
				"longHairBig" ]);
	}
	//console.log(exclude);
	c = document.createElement("div");
	c.innerHTML = Avataaar.random (Avataaar.avatar, null,
		function(_keys, part, target) {
			var keys = _keys;
			if (exclude[part.path]) {
				keys = [];
				var ex = exclude[part.path];
				for (var k=0; k<_keys.length; k++) {
					var found=false;
					for (var i=0; i<ex.length; i++) {
						if (_keys[k] == ex[i]) {
							found=true;
							break;
						}
					}
					if (!found) keys.push(_keys[k]);
				}
			}
			if (part.path == "accessory") {
				// increase probability of no glasses
				keys = keys.concat(["blank","blank","blank","blank",]);
			}
			//console.log(part);
			//console.log(keys);
			return keys[myGetRandomInt(keys.length)];
		}).render();
	elem.innerHTML = "";
	elem.appendChild(c.children[0]);
}

