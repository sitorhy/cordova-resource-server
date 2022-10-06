var exec = cordova.require('cordova/exec');

function newProgressEvent(result) {
    var event = {
            loaded: result.loaded,
            total: result.total
    };
    return event;
}

exports.unzip = function(fileName, outputDirectory, successCallback, failCallback, progressCallback) {
    var win = function(result) {
        if (result && typeof result.loaded != "undefined") {
            if (progressCallback) {
                return progressCallback(newProgressEvent(result));
            }
        } else if (successCallback) {
            successCallback(0);
        }
    };
    var fail = function(result) {
        if (failCallback) {
            failCallback(result);
        }
    };
    exec(win, fail, 'Zip', 'unzip', [fileName, outputDirectory]);
};