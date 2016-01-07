angular.module('carpooling.services.user', [])

.factory('UserSrv', function ($rootScope, $http, $q, Config, StorageSrv) {
    var user = null;

    var userService = {};

    userService.getUser = function (userId) {
        var deferred = $q.defer();

        $http.get(Config.getServerURL() + '/api/read/user/' + userId, Config.getHTTPConfig())

        .then(
            function (response) {
                if (response[0] == '<') {
                    deferred.reject(Config.LOGIN_EXPIRED);
                    $rootScope.login();
                } else {
                    if (StorageSrv.getUserId() === userId) {
                        // It's-a-me!
                        StorageSrv.saveUser(response.data.data).then(
                            function (user) {
                                deferred.resolve(user);
                            },
                            function (err) {
                                deferred.reject(err);
                            }
                        );

                        // My communities
                        userService.getCommunities().then(
                            function (communities) {
                                communities.forEach(function (community) {
                                    delete community['color'];
                                    delete community['zone'];
                                    delete community['cars'];
                                    delete community['users'];
                                    delete community['userObjs'];
                                });
                                StorageSrv.saveCommunities(communities);
                                deferred.resolve(communities);
                            }
                        );
                    } else {
                        deferred.resolve(response.data.data);
                    }
                }
            },
            function (responseError) {
                deferred.reject(responseError.data.error);
            }
        );

        return deferred.promise;
    };

    userService.getTravelProfile = function () {
        var deferred = $q.defer();

        $http.get(Config.getServerURL() + '/api/read/profile', Config.getHTTPConfig())

        .then(function (response) {
                if (response.data[0] == '<') {
                    deferred.reject(Config.LOGIN_EXPIRED);
                    $rootScope.login();
                } else {
                    deferred.resolve(response.data.data);
                }
            },
            function (responseError) {
                deferred.reject(responseError.data.error);
            }
        );

        return deferred.promise;
    };

    userService.saveTravelProfile = function (travelProfile) {
        var deferred = $q.defer();

        if (!travelProfile) {
            deferred.reject('Invalid travelProfile');
        } else {
            $http.post(Config.getServerURL() + '/api/save/profile', travelProfile, Config.getHTTPConfig())

            .then(
                function (response) {
                    if (response.data[0] == '<') {
                        deferred.reject(Config.LOGIN_EXPIRED);
                        $rootScope.login();
                    } else {
                        deferred.resolve(response.data);
                    }
                },
                function (responseError) {
                    deferred.reject(responseError.data.error);
                }
            );
        }

        return deferred.promise;
    };

    userService.saveAuto = function (auto) {
        var deferred = $q.defer();

        $http.post(Config.getServerURL() + '/api/save/autoInfo', auto, Config.getHTTPConfig())

        .then(
            function (response) {
                if (response.data[0] == '<') {
                    deferred.reject(Config.LOGIN_EXPIRED);
                    $rootScope.login();
                } else {
                    deferred.resolve(response.data);
                }
            },
            function (responseError) {
                deferred.reject(responseError.data.error);
            }
        );

        return deferred.promise;
    };

    userService.getCommunities = function () {
        var deferred = $q.defer();

        $http.get(Config.getServerURL() + '/api/read/communities', Config.getHTTPConfig())

        .then(
            function (response) {
                if (response.data[0] == '<') {
                    deferred.reject(Config.LOGIN_EXPIRED);
                    $rootScope.login();
                } else {
                    deferred.resolve(response.data.data);
                }
            },
            function (responseError) {
                deferred.reject(responseError.data.error);
            }
        );

        return deferred.promise;
    };

    userService.getCommunitiesDetails = function () {
        var deferred = $q.defer();

        $http.get(Config.getServerURL() + '/api/read/communities/details', Config.getHTTPConfig())

        .then(
            function (response) {
                if (response.data[0] == '<') {
                    deferred.reject(Config.LOGIN_EXPIRED);
                    $rootScope.login();
                } else {
                    deferred.resolve(response.data.data);
                }
            },
            function (responseError) {
                deferred.reject(responseError.data.error);
            }
        );

        return deferred.promise;
    };

    userService.getCommunityTravels = function (communityId, timeinmillis) {
        var deferred = $q.defer();

        $http.get(Config.getServerURL() + '/api/community/' + communityId + '/' + timeinmillis + '/travel', Config.getHTTPConfig())

        .then(
            function (response) {
                if (response.data[0] == '<') {
                    deferred.reject(Config.LOGIN_EXPIRED);
                    $rootScope.login();
                } else {
                    deferred.resolve(response.data.data);
                }
            },
            function (responseError) {
                deferred.reject(responseError.data.error);
            }
        );

        return deferred.promise;
    };

    userService.getDiscussion = function (travelId, targetUserId) {
        var deferred = $q.defer();

        $http.get(Config.getServerURL() + '/api/read/' + travelId + '/' + targetUserId + '/discussion', Config.getHTTPConfig())

        .then(
            function (response) {
                if (response.data[0] == '<') {
                    deferred.reject(Config.LOGIN_EXPIRED);
                    $rootScope.login();
                } else {
                    deferred.resolve(response.data.data);
                }
            },
            function (responseError) {
                deferred.reject(responseError.data.error);
            }
        );

        return deferred.promise;
    };

    userService.sendMessage = function (travelId, message) {
        var deferred = $q.defer();

        if (!travelId) {
            deferred.reject('Invalid travelId');
        } else if (!message || !message.userId || !message.timestamp || !message.message || !message.targetUserId) {
            deferred.reject('Invalid message');
        } else {
            $http.post(Config.getServerURL() + '/api/message/' + travelId + '/send', message, Config.getHTTPConfig())

            .then(
                function (response) {
                    if (response.data[0] == '<') {
                        deferred.reject(Config.LOGIN_EXPIRED);
                        $rootScope.login();
                    } else {
                        deferred.resolve(response.data);
                    }
                },
                function (responseError) {
                    deferred.reject(responseError.data.error);
                }
            );
        }

        return deferred.promise;
    }

    userService.readNotifications = function (start, count) {
        var deferred = $q.defer();

        /*
        $http.get(Config.getServerURL() + '/api/read/' + travelId + '/' + targetUserId + '/discussion', Config.getHTTPConfig())

        .success(function (data) {
            deferred.resolve(data.data);
        })

        .error(function (err) {
            deferred.reject(err);
        });

        return deferred.promise;
        */

        var httpConfig = Config.getHTTPConfig();

        if (!!start || !!count) {
            httpConfig.params = {};

            if (!!start) {
                if (Number.isInteger(start) && start >= 0) {
                    httpConfig.params['start'] = start;
                } else {
                    deferred.reject('Invalid "start" value');
                    return deferred.promise;
                }
            }

            if (!!count) {
                if (Number.isInteger(count) && count > 0) {
                    httpConfig.params['count'] = count;
                } else {
                    deferred.reject('Invalid "count" value');
                    return deferred.promise;
                }
            }
        }

        $http.get(Config.getServerURL() + '/api/read/notifications', httpConfig)

        .then(function (response) {
                if (response.data[0] == '<') {
                    deferred.reject(Config.LOGIN_EXPIRED);
                    $rootScope.login();
                } else {
                    deferred.resolve(response.data.data);
                }
            },
            function (responseError) {
                deferred.reject(responseError.data.error);
            }
        );


        return deferred.promise;
    }

    userService.markNotification = function (id) {
        var deferred = $q.defer();

        if (!id) {
            deferred.reject('Invalid notification id');
        } else {
            $http.post(Config.getServerURL() + '/api/mark/read/notification/' + id, id, Config.getHTTPConfig())

            .then(
                function (response) {
                    if (response.data[0] == '<') {
                        deferred.reject(Config.LOGIN_EXPIRED);
                        $rootScope.login();
                    } else {
                        deferred.resolve(response.data);
                    }
                },
                function (responseError) {
                    deferred.reject(responseError.data.error);
                }
            );
        }

        return deferred.promise;
    }

    userService.deleteNotification = function (id) {
        var deferred = $q.defer();

        if (!id) {
            deferred.reject('Invalid notification id');
        } else {
            $http.delete(Config.getServerURL() + '/api/delete/notification/' + id, id, Config.getHTTPConfig())

            .then(
                function (response) {
                    if (response.data[0] == '<') {
                        deferred.reject(Config.LOGIN_EXPIRED);
                        $rootScope.login();
                    } else {
                        deferred.resolve(response.data);
                    }
                },
                function (responseError) {
                    deferred.reject(responseError.data.error);
                }
            );
        }

        return deferred.promise;
    }

    return userService;
});
