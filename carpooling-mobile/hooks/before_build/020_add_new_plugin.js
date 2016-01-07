#!/usr/bin/env node

//this hook installs all your plugins

// add your plugins to this list--either the identifier, the filesystem location or the URL
var pluginlisttoadd = [
    "de.appplant.cordova.plugin.local-notification"
];

var pluginlisttoaddios = [
    "https://github.com/cookys/parse-push-plugin"
];

// no need to configure below

var fs = require('fs');
var path = require('path');
var sys = require('sys')
var exec = require('child_process').exec;

var platforms = process.env.CORDOVA_PLATFORMS;

function puts(error, stdout, stderr) {
    sys.puts(stdout)
}
if(platforms == "android"){
    pluginlisttoadd.forEach(function(plug) {
        exec("cordova plugin add " + plug, puts);
    });
} else if (platforms == "ios"){
    pluginlisttoaddios.forEach(function(plug) {
        exec("cordova plugin add " + plug, puts);
    });
} else {

}


