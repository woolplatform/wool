const { app, BrowserWindow, dialog, ipcMain } = require('electron')
const path = require("path");

var mainWindow = null;

function createWindow () {
  // Create the browser window.
  mainWindow = new BrowserWindow({
    width: 1280,
    height: 768,
    webPreferences: {
      nodeIntegration: true,
	  preload: path.join(__dirname, "preload.js"),
    }
  })

  // and load the index.html of the app.
  mainWindow.loadFile(app.getAppPath()+'/wooleditor/app/index.html')
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

