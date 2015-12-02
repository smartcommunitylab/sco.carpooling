angular.module('carpooling.services.login', [])

.factory('Login', function ($rootScope, $q, $http, $window, Config) {
    var UserID = null;

    var loginService = {};

    loginService.login = function () {
        var deferred = $q.defer();

        //log into the system and set UserID
        var authapi = {
            authorize: function (url) {
                var deferred = $q.defer();

                //Build the OAuth consent page URL
                var authUrl = Config.getServerURL() + '/userlogin';
                //Open the OAuth consent page in the InAppBrowser
                var authWindow = window.open(authUrl, '_blank', 'location=no,toolbar=no');

                authWindow.addEventListener('loadstart', function (e) {
                    console.log(e);
                    var url = e.url;
                    var success = /userloginsuccess\?profile=(.+)$/.exec(url);
                    var error = /userloginerror\?error=(.+)$/.exec(url);
                    if (success || error) {
                        //Always close the browser when match is found
                        authWindow.close();
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
                });

                return deferred.promise;
            }
        };

        authapi.authorize().then(function (data) {
            console.log("success:" + data.userId);
            //prendi google id , metti in local storage e abilita menu
            //log
            $rootScope.extLogging("AppCollaborate", "login");
            $rootScope.userIsLogged = true;
            localStorage.userId = data.userId;
            deferred.resolve(data);
        }, function (reason) {
            alert('Failed: ' + reason);
            //reset data
            $rootScope.userIsLogged = false;
            localStorage.userId = "null";
            deferred.reject(reason);
        });
        return deferred.promise;

    };

    loginService.logout = function () {
        var deferred = $q.defer();

        $http({
            method: 'GET',
            url: Config.getServerURL() + '/carpooling/logout',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        })

        .success(function (data, status, headers, config) {
            $rootScope.userIsLogged = false;
            localStorage.userIdalert("loggato");
            deferred.resolve(data);
        })

        .error(function (data, status, headers, config) {
            deferred.reject(data);
        });

        return deferred.promise;
    };

    loginService.getUserId = function () {
        //return UserID
        return localStorage.userId;
    };

    return loginService;
});
