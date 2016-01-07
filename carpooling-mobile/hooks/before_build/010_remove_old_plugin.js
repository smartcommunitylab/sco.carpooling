#!/usr/bin/env node

//this hook installs all your plugins

// add your plugins to this list--either the identifier, the filesystem location or the URL
var pluginlisttoremove = [
    "de.appplant.cordova.plugin.local-notification"
];

var pluginlisttoremoveios = [
    "de.appplant.cordova.plugin.local-notification",
    "com.phonegap.parsepushplugin"
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
    pluginlisttoremove.forEach(function(plug) {
        exec("cordova plugin rm " + plug, puts);
    });
} else if (platforms == "ios"){
    pluginlisttoremoveios.forEach(function(plug) {
        exec("cordova plugin rm " + plug, puts);
    });
} else {

}
