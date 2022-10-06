# cordova-resource-server
Cordova Plugin to automatically update and off-line running.<br/>

# Usage
## Install Unzip Plugin (Optional)
#### Add the local plugin "libs/cordova-plugin-zip", find it in repository.
+ Fix iOS unzip file with file name including URL escape characters.
+ Fix Android unzip file with file name including unicode characters.
```
cordova plugin add /path/libs/cordova-plugin-zip

cordova build ios
cordova build android
```

## Download Program Archive File (Optional)
#### You can also define a default archive as resource in the app.
#### Optional dependencies
+ **cordova-plugin-file-transfer**
+ **cordova-plugin-file**
+ **cordova-plugin-file-md5**

## Create Context Directory and Resource Directory
```javascript
document.addEventListener('deviceready', function() {
    resolveLocalFileSystemURL(
        cordova.file.dataDirectory,
        function (dataDirectoryEntity) {
          // resouce directory 
          Entity.getDirectory("resources", {create: true, exclusive: false}, success, fail);

          // context directory
          Entity.getDirectory("web", {create: true, exclusive: false}, success, fail);
        }
    );
}, false);
```

## Plublish Program In Entry Program
#### **www/app.js**
```javascript
document.addEventListener('deviceready', function() {
    // create context / resouce directory
    // ...

    resolveLocalFileSystemURL(
        cordova.file.cacheDirectory,
        function (cacheDirectoryEntity) {
            // download archive
        }
    );
}, false);
```
```javascript
// download archive
// use with "cordova-plugin-file-transfer"
var fileTransfer = new FileTransfer();

fileTransfer.download(
    "https://www.domain.com/path/update.zip",
    cacheDirectoryEntity.nativeURL + "update.zip",
    function (entity) {
        // optional, md5 checksum for the file downloaded
        // ...
        
        console.log(entity.toURL());

        // uncompress the zip file to context directory
        var contextDirectory = cordova.file.dataDirectory + "web";
        var archiveFilePath = cordova.file.cacheDirectory + "update.zip";
        var resourceDirectory = cordova.file.dataDirectory + "resources";

        // use with "cordova-plugin-zip"
        window.zip.unzip(archiveFilePath, contextDirectory,
            function () {
              // use with "cordova-resource-server"
              // server startup
              ResourceServer.start(contextDirectory, 10429, resourceDirectory, 10433,    
                function () {
                  var url = "http://localhost:10429");
                  ResourceServer.redirect(url, function () {});
                },
                function (err) {}
              );
            },
            function (err) {},
            function (progressEvent) {}
        );
    }
);
```

# Context Directory
#### Program static files root directory.
#### Server address
+ http://localhost:10429
#### Access program for testing in computer.
+ Android 
  - adb forward tcp:10429 tcp:10429
  - access http://localhost:10429 in Chrome / FireFox / Edge
+ iOS
  - access http://localhost:10429 in Safari / Chrome



# Resource Directory
#### Resource files root directory.
#### You can storage any files in this directory.
#### Server Address
+ http://localhost:10433

### API
+ List a directory files
  - /list
  - /list/{dir}
+ Get the basic file attributes
  - /list/{dir}/{file}
+ Reference resource file
  - /resource/{file}
+ App resources
  - /assets/{dir}/{name}

# Sample
## Data directory structure

 ```
web/
    ├── index.html
    └── index.js
resources/
    └── images/
        └── logo.png
    └── audio/
        └── music.mp3
 ```
#### **web/index.html**
```html
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=<device-width>, initial-scale=1.0">
    <title>Document</title>
</head>
<body>
    <audio src="http://localhost:10433/resource/audio/music.mp3" controls/>
    <img src="http://localhost:10433/resource/images/logo.png"/>
</body>
</html>
```
> `cordova.js` will automatically inject into `index.html` <br>
> + response result : 
> ```html
> <html>
>   <body>
>     <script src="other.js"></script>
>     <script src="http://localhost:10433/assets/www/cordova.js"></script>      
>   </body>
> </html>
> ```

#### **web/index.js**
```javascript
window.onload = function() {
  // check cordova plugin accessibility
  console.log(cordova.file.dataDirectory);
}
```

#### API sample
```javascript
import axios from "axios";

axios.get("http://localhost:10433/list/images").then((res)=>{
  const {children} = res.data.data;
  const imagesDir = cordova.file.dataDirectory + "resources/images";
  children.forEach((i)=>{
    const imageURI = imagesDir + "/" + i.name;

    // resize or compress local image
    // ...
  });
});
```

## iOS Background Modes
+ [Configuring background execution modes](https://developer.apple.com/documentation/xcode/configuring-background-execution-modes/)
+ 开启 `Audio, AirPlay and Picture in Picture`
+ 由于iOS假后台机制，切出前台后服务不能访问，开启后台模式方便测试
+ Not discuss about Apple Store Review