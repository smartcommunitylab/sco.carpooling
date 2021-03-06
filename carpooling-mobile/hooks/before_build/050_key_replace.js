#!/usr/bin/env node

// this plugin replaces arbitrary text in arbitrary files
//
// Look for the string CONFIGURE HERE for areas that need configuration
//

var fs = require('fs');
var path = require('path');

var rootdir = process.argv[2];

function replace_string_in_file(filename, to_replace, replace_with) {
    var data = fs.readFileSync(filename, 'utf8');

    var result = data.replace(new RegExp(to_replace, "g"), replace_with);
    fs.writeFileSync(filename, result, 'utf8');
}

if (rootdir) {
    var ourconfigfile = path.join(rootdir, "www/js/services", "ConfigSrv.js");
    //var configobj = JSON.parse(fs.readFileSync(ourconfigfile, 'utf8'));
	var alldata = fs.readFileSync(ourconfigfile, 'utf8');
	var partial1 = alldata.split("APPID");
	var partial2 = partial1[1].split("RECURRENCY");
	var appandclient = partial2[0].split("CLIENTKEY");
	var appId = appandclient[0].substring(4,44);
	var clientKey = appandclient[1].substring(4,44);

	//console.log("Api key :" + configobj["APPID"]);
	//console.log("Client key :" + configobj["CLIENTKEY"]);
	console.log("Api key :" + appId);
	console.log("Client key :" + clientKey);

    // CONFIGURE HERE
    // with the names of the files that contain tokens you want replaced.  Replace files that have been copied via the prepare step.
    var filestoreplace = [
        // android
        "platforms/android/src/it/smartcommunitylab/carpooling/MainApplication.java",
    ];
    filestoreplace.forEach(function(val, index, array) {
        var fullfilename = path.join(rootdir, val);
        if (fs.existsSync(fullfilename)) {
            // CONFIGURE HERE
            // with the names of the token values. For example, below we are looking for the token /*REP*/ 'api.example.com' /*REP*/ and will replace that token
            replace_string_in_file(fullfilename, "appId = \"\"", "appId = \"" + appId + "\"");
			replace_string_in_file(fullfilename, "clientKey = \"\"", "clientKey = \"" + clientKey + "\"");
            // ... any other configuration
        } else {
            console.log("missing: "+fullfilename);
        }
    });

}
