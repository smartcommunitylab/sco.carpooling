angular.module('carpooling.services.login', [])

.factory('LoginSrv', function ($rootScope, $q, $http, $window, StorageSrv, UserSrv, Config, CacheSrv, Utils) {
    var loginService = {};

    var authWindow = null;

    loginService.userIsLogged = function () {
        return (StorageSrv.getUserId() != null && StorageSrv.getUser() != null);
    };

    loginService.login = function (provider) {
        var deferred = $q.defer();

        if (provider != 'google' && provider != 'googlelocal' && provider != 'facebook' && provider != 'facebooklocal') {
            provider = '';
//        } else if (provider == 'googlelocal' && !$rootScope.login_googlelocal) {
//            provider = 'google';
        }

        // log into the system and set userId
        var authapi = {
            authorize: function (token) {
                var deferred = $q.defer();

                var processThat = false;

                // Build the OAuth consent page URL
                var authUrl = Config.getServerURL() + '/userlogin' + (!!provider ? '/' + provider : '');

                if ((provider == 'googlelocal' || provider == 'facebooklocal') && !!token) {
                    authUrl += '?token=' + encodeURIComponent(token);
                }

                //Open the OAuth consent page in the InAppBrowser
                if (!authWindow) {
                    authWindow = window.open(authUrl, '_blank', 'location=no,toolbar=no');
                    processThat = !!authWindow;
                }

                var processURL = function (url, deferred, w) {
                    var success = /userloginsuccess\?profile=(.+)$/.exec(url);
                    var error = /userloginerror\?error=(.+)$/.exec(url);
                    if (w && (success || error)) {
                        //Always close the browser when match is found
                        w.close();
                        authWindow = null;
                    }

                    if (success) {
                        var str = success[1];
                        if (str.indexOf('#') != -1) {
                            str = str.substring(0, str.indexOf('#'));
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
                    if (processThat) {
                        authWindow.addEventListener('loadstart', function (e) {
                            //console.log(e);
                            var url = e.url;
                            processURL(url, deferred, authWindow);
                        });
                    }
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

        if (provider == 'googlelocal') {
            window.plugins.googleplus.login({
                    'scopes': 'profile email',
                    'offline': true
                },
                function (obj) {
                    var token = obj.oauthToken;
                    if (!token) token = obj.accessToken;
                    authapi.authorize(token).then(
                        function (data) {
                            StorageSrv.saveUserId(data.userId).then(function () {
                                UserSrv.getUser(data.userId).then(function () {
                                    deferred.resolve(data);
                                }, function (reason) {
                                    StorageSrv.saveUserId(null).then(function () {
                                        deferred.reject(reason);
                                    });
                                });
                            });
                        },
                        function (reason) {
                            //reset data
                            StorageSrv.saveUserId(null).then(function () {
                                deferred.reject(reason);
                            });
                        }
                    );
                },
                function (msg) {
                    console.log('Login googlelocal error: ' + msg);
                }
            );
        } else if (provider == 'facebooklocal') {
            /*
            if (!!window.cordova && window.cordova.platformId == 'browser') {
                facebookConnectPlugin.browserInit('182684742123091');
                //facebookConnectPlugin.browserInit(appId, version);
                // version is optional. It refers to the version of API you may want to use.
            }*/

            facebookConnectPlugin.login(['public_profile', 'email'],
                function (userData) {
                    Utils.loading();
                    facebookConnectPlugin.getAccessToken(function (token) {
                        authapi.authorize(token).then(
                            function (data) {
                                //console.log('success: ' + data.userId);
                                StorageSrv.saveUserId(data.userId).then(function () {
                                    UserSrv.getUser(data.userId).then(function () {
                                        Utils.loaded();
                                        deferred.resolve(data);
                                    }, function (reason) {
                                        StorageSrv.saveUserId(null).then(function () {
                                            Utils.loaded();
                                            deferred.reject(reason);
                                        });
                                    });
                                });
                            },
                            function (reason) {
                                //reset data
                                StorageSrv.saveUserId(null).then(function () {
                                    Utils.loaded();
                                    deferred.reject(reason);
                                });
                            }
                        );
                    }, function (err) {
                        // TODO handle error
                        Utils.loaded();
                    });
                },
                function (error) {
                    // TODO handle error
                }
            );
        } else {
            authapi.authorize().then(
                function (data) {
                    StorageSrv.saveUserId(data.userId).then(function () {
                        UserSrv.getUser(data.userId).then(function () {
                            deferred.resolve(data);
                        }, function (reason) {
                            StorageSrv.saveUserId(null).then(function () {
                                deferred.reject(reason);
                            });
                        });
                    });
                },
                function (reason) {
                    //reset data
                    StorageSrv.saveUserId(null).then(function () {
                        deferred.reject(reason);
                    });
                }
            );
        }

        return deferred.promise;
    };

    loginService.logout = function () {
        var deferred = $q.defer();

        var complete = function (response) {
            StorageSrv.reset().then(function () {
                try {
                    cookieMaster.clear(
                        function () {
                            console.log('Cookies have been cleared');
                            deferred.resolve(response.data);
                        },
                        function () {
                            console.log('Cookies could not be cleared');
                            deferred.resolve(response.data);
                        });
                } catch (e) {
                    deferred.resolve(e);
                }
            });
        };

        CacheSrv.reset();
        $http.get(Config.getServerURL() + '/logout', {
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                }
            })
            .then(
                function (response) {
                    complete(response);
                },
                function (responseError) {
                    deferred.reject(responseError.data ? responseError.data.errorMessage : responseError);
                }
            );

        return deferred.promise;
    };

    return loginService;
});
