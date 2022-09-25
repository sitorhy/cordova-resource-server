var exec = require('cordova/exec');

exports.start = function (contextPath, contextPort, servePath, servePort, success, error) {
    exec(success, error, 'ResourceServer', 'start', [contextPath, contextPort, servePath, servePort]);
};

exports.stop = function (success, error) {
    exec(success, error, 'ResourceServer', 'stop', []);
};

exports.redirect = function (url, success, error) {
    exec(success, error, 'ResourceServer', 'redirect', [url]);
};