const { ipcRenderer } = require('electron')


process.once('loaded', () => {
	window.addEventListener('message', evt => {
		if (evt.data.type === 'select-dirs') {
			console.log("MESSAGE sent "+evt.data.type);
			console.log(ipcRenderer.send('select-dirs'))
		}
	})
	ipcRenderer.on("dirs-selected", (event,arg) => {
		var dirs = JSON.parse(arg);
		if (dirs && dirs.length > 0) {
			console.log("Dir selected:" + dirs[0]);
			window.postMessage({type: "dirs-selected", data: dirs[0]});
		} else {
			console.log("Selection canceled");
		}
	})
	
	// own prompt / alert / confirm implementations
	window.prompt = function(title, val){
	  return ipcRenderer.sendSync('prompt', {title, val})
	}

	window.alert = function(title){
	  return ipcRenderer.sendSync('alert', {title})
	}

	window.confirm = function(title){
	  return ipcRenderer.sendSync('confirm', {title})
	}

})


