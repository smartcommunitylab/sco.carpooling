angular.module('carpooling.services.login', [])

.factory('LoginSrv', function ($rootScope, $q, $http, $window, Config) {
    var loginService = {};

    loginService.login = function () {
        var deferred = $q.defer();

        // log into the system and set userId
        var authapi = {
            authorize: function (url) {
                var deferred = $q.defer();

                //Build the OAuth consent page URL
                var authUrl = Config.getServerURL() + '/userlogin';
                //Open the OAuth consent page in the InAppBrowser
                var authWindow = window.open(authUrl, '_blank', 'location=no,toolbar=no');

                var processURL = function (url, deferred, w) {
                    var success = /userloginsuccess\?profile=(.+)$/.exec(url);
                    var error = /userloginerror\?error=(.+)$/.exec(url);
                    if (w && (success || error)) {
                        //Always close the browser when match is found
                        w.close();
                    }

                    if (success) {
                        var str = success[1];
                        if (str.substring(str.length - 1) == '#') {
                            str = str.substring(0, str.length - 1);
                        }
                        console.log('success:' + decodeURIComponent(str));
                        deferred.resolve(JSON.parse(decodeURIComponent(str)));
                    } else if (error) {
                        //The user denied access to the app
                        deferred.reject({
                            error: error[1]
                        });
                    }
                }

                if (ionic.Platform.isWebView()) {
                    authWindow.addEventListener('loadstart', function (e) {
                        //console.log(e);
                        var url = e.url;
                        processURL(url, deferred, authWindow);
                    });
                } else {
                    angular.element($window).bind('message', function (event) {
                        $rootScope.$apply(function () {
                            processURL(event.data, deferred);
                        });
                    });
                }

                return deferred.promise;
            }
        };

        authapi.authorize().then(
            function (data) {
                //console.log('success: ' + data.userId);
                localStorage.userId = data.userId;
                deferred.resolve(data);
            },
            function (reason) {
                //reset data
                localStorage.userId = null;
                deferred.reject(reason);
            }
        );

        return deferred.promise;
    };

    loginService.logout = function () {
        var deferred = $q.defer();

        $http.get(Config.getServerURL() + '/carpooling/logout', {
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        })

        .success(function (data, status, headers, config) {
            localStorage.userId = null;
            deferred.resolve(data);
        })

        .error(function (data, status, headers, config) {
            deferred.reject(data);
        });

        return deferred.promise;
    };

    loginService.getUserId = function () {
        // return userId
        return localStorage.userId;
    };

    return loginService;
});
