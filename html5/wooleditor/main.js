const { app, BrowserWindow, dialog, ipcMain } = require('electron')
const path = require("path");

var mainWindow = null;

function createWindow () {
	// Create the browser window.
	mainWindow = new BrowserWindow({
		width: 1366,
		height: 768,
		webPreferences: {
			nodeIntegration: true,
			contextIsolation: false,
		preload: path.join(__dirname, "preload.js"),
		}
	})


	// popup replacement, general ------------------------------

	var promptHtmlPrefix =
		"<html><head><style>\n"
		+"  body {padding: 0px; margin: 0px; font-family: sans-serif; height:100%;}"
		+"  button {float:right; margin-right: 20px; padding-left: 10px; padding-right: 10px;}"
		+"  input {font-size: 17px; margin-top: 10px; margin-bottom: 10px; width: 100%; display:block;}"
		+"  div.titlebar { font-size: 12px; padding: 10px; -webkit-app-region:drag; }"
		+"  div.content { border-top: 1px solid #ccc; }"
		+"  div.message { padding: 15px; }"
		+"  div.buttons { position: fixed; bottom: 0px; width: 100%; padding: 15px; background-color: #ddd; }"
		+"\n</style></head>\n"
		+"<body><div class='titlebar'>Wool Dialog</div>"
		+"<div class='content'>";

	var promptHtmlSuffix = "</div></body></html>\n";

	var createPromptWindow = function() {
		return new BrowserWindow({
			parent: mainWindow,
			width: 300,
			height: 200,
			show: false,
			resizable: true,
			movable: true,
			alwaysOnTop: true,
			frame: false,
			modal: true,
			webPreferences: {nodeIntegration:true,contextIsolation:false,},
		});
	};


	// replacement of prompt ------------------------------
	
	var promptResponse;

	ipcMain.on('prompt', function(eventRet, arg) {
		promptResponse = null
		var promptWindow = createPromptWindow();
		arg.val = arg.val || '';
		const promptHtml = promptHtmlPrefix
		+"<div class='message'>"
		+'<div>' + arg.title + '</div>'
		+'<input id="val" value="' + arg.val + '" autofocus />'
		+"</div>"
		+'<div class="buttons">\n'
		+'<button onclick="require(\'electron\').ipcRenderer.send(\'prompt-response\', document.getElementById(\'val\').value);window.close()">Ok</button>'
		+"<button onclick='window.close();'>Cancel</button>"
		+"<div style='clear:both;' />"
		+"</div>\n"
		+promptHtmlSuffix;
		promptWindow.loadURL('data:text/html,' + encodeURIComponent(promptHtml))
		promptWindow.show()
		promptWindow.on('closed', function() {
			eventRet.returnValue = promptResponse
			promptWindow = null
		})
	})

	ipcMain.on('prompt-response', function(event, arg) {
		if (arg === ''){ arg = null }
		promptResponse = arg
	})


	// replacement of confirm ------------------------------
	
	var confirmResponse;

	ipcMain.on('confirm', function(eventRet, arg) {
		confirmResponse = false
		var promptWindow = createPromptWindow();
		const promptHtml = promptHtmlPrefix
		+"<div class='message'>"
		+'<div>' + arg.title + '</div>'
		+"</div>"
		+'<div class="buttons">\n'
		+'<button onclick="require(\'electron\').ipcRenderer.send(\'confirm-response\', true);window.close()">Ok</button>'
		+"<button onclick='window.close();'>Cancel</button>"
		+"<div style='clear:both;' />"
		+"</div>\n"
		+promptHtmlSuffix;
		promptWindow.loadURL('data:text/html,' + encodeURIComponent(promptHtml));
		promptWindow.show();
		promptWindow.on('closed', function() {
			eventRet.returnValue = confirmResponse;
			promptWindow = null;
		})
	})

	ipcMain.on('confirm-response', function(event, arg) {
		confirmResponse = arg ? true : false;
	})


	// replacement of alert ------------------------------
	// We replace this to work around issue 20400 (see below)

	ipcMain.on('alert', function(eventRet, arg) {
		var promptWindow = createPromptWindow();
		const promptHtml = promptHtmlPrefix 
		+"<div class='message'>"
		+"<div>" + arg.title + "</div>"
		+"</div>"
		+'<div class="buttons">\n'
		+"<button onclick='window.close()'>Ok</button>"
		+"<div style='clear:both;' />"
		+"</div>\n"
		+promptHtmlSuffix;
		promptWindow.loadURL('data:text/html,' + encodeURIComponent(promptHtml))
		promptWindow.show()
		promptWindow.on('closed', function() {
			eventRet.returnValue = null
			promptWindow = null
		})
	})

	ipcMain.on('alert-response', function(event) {
	})


// Workaround for electron issue 20400.  This workaround has a bug of its own
// (the electron window can be sent to the back when blur/focus is called),
// so we now use our own implementation of alert/confirm as a workaround.
// https://github.com/electron/electron/issues/20400#issuecomment-539586029
/*
	const isWindows = process.platform === 'win32' || process.platform ===
	'win64'; let needsFocusFix = false; let triggeringProgrammaticBlur =
	false;

	mainWindow.on('blur', (event) => {
		if(!triggeringProgrammaticBlur) {
			needsFocusFix = true;
		}
	})

	mainWindow.on('focus', (event) => {
		if(isWindows && needsFocusFix) {
			needsFocusFix = false;
			triggeringProgrammaticBlur = true;
			setTimeout(function () {
				mainWindow.blur();
				mainWindow.focus();
				setTimeout(function () {
					triggeringProgrammaticBlur = false;
				}, 100);
			}, 100);
		}
	})
*/

	// and load the index.html of the app.
	mainWindow.loadFile(app.getAppPath()+'/wooleditor/index.html')
}


//window.$ = window.jQuery = require('wooleditor/js/libs/jquery-1.11.2.min.js');

app.on('ready', createWindow)


// In this file you can include the rest of your app's specific main process
// code. You can also put them in separate files and require them here.
ipcMain.on('select-dirs', async (event, arg) => {
	// reply to sendSync
	//event.returnValue = "RECEIVED";
	const result = await dialog.showOpenDialog(mainWindow, {
		properties: ['openDirectory']
	})
	event.reply("dirs-selected",JSON.stringify(result.filePaths))
})

