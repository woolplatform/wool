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
	  preload: path.join(__dirname, "preload.js"),
    }
  })


  var promptResponse;

  ipcMain.on('prompt', function(eventRet, arg) {
    promptResponse = null
    var promptWindow = new BrowserWindow({
      width: 300,
      height: 100,
      show: false,
      resizable: true,
      movable: true,
      alwaysOnTop: true,
      frame: false,
	  webPreferences: {nodeIntegration:true,},
    });
    arg.val = arg.val || '';
    const promptHtml = '<label for="val">' + arg.title + '</label>'
    +'<input id="val" value="' + arg.val + '" autofocus />'
    +'<button onclick="require(\'electron\').ipcRenderer.send(\'prompt-response\', document.getElementById(\'val\').value);window.close()">Ok</button>'
    +'<button onclick="window.close()">Cancel</button>'
    +'<style>body {font-family: sans-serif;} button {float:right; margin-left: 10px;} label,input {margin-bottom: 10px; width: 100%; display:block;}</style>';
    promptWindow.loadURL('data:text/html,' + promptHtml)
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

