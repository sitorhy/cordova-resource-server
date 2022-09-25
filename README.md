# cordova-resource-server
Cordova Plugin to automatically update and off-line running.<br/>
Redirect resource "www/index.html" to website which publish in device storage.<br/>
It not offer unzip and fetch fearure.<br/>

# Usage
+ app.js
```javascript
var contextPath = (cordova.file.dataDirectory).replace('file://', '') + 'web';
var contextPort = 10429;
var servePath = (cordova.file.dataDirectory).replace('file://', '') + 'resources';
var servePort = 10433;
ResourceServer.start(contextPath, contextPort, servePath, servePort, function () {
    // ...
}, function (err) {
    // ...
});
```

# Resource Directory
Offline files location.<br/>
+ http://localhost:10429


# Context Directory
Local website location.<br/>
## Request Path
+ List directory
  - http://localhost:10433/list/${path}
+ Read file (image/audio...)
  - http://localhost:10433/resource/${path}

# Sample
```
/data/data/com.app.domain/files
├─resources
  ├─test.mp3
├─web
  ├─index.html
```

<br/>
// index.html  （http://localhost:10429） <br/>
&lt;audio src="http://localhost:10433/resources/test.mp3" controls/>

# iOS
developing
