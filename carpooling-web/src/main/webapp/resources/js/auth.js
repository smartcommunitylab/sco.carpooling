var authapi = {
    authorize: function(url) {
        var deferred = $.Deferred();

        //Build the OAuth consent page URL
        var authUrl = url+'/cityreport/userlogin';
        //Open the OAuth consent page in the InAppBrowser
        var authWindow = window.open(authUrl, '_blank', 'location=no,toolbar=no');
        authWindow.addEventListener('loadstart', function(e) {
            var url = e.originalEvent.url;
            var success = /userloginsuccess$/.exec(url);
            var error = /userloginerror\?error=(.+)$/.exec(url);

            if (success || error) {
                //Always close the browser when match is found
                authWindow.close();
            }

            if (success) {
            	console.log('success');
            	deferred.resolve('success');
            } else if (error) {
                //The user denied access to the app
                deferred.reject({
                    error: error[1]
                });
            }
        });

        return deferred.promise();
    }
};