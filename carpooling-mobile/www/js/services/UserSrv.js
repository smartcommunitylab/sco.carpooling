angular.module('carpooling.services.user', [])

.factory('UserSrv', function ($http, $q, Config, StorageSrv) {
    var user = null;

    var userService = {};

    // /api/read/user/{userId}
    userService.getUser = function (userId) {
        var deferred = $q.defer();

        $http.get(Config.getServerURL() + '/api/read/user/' + userId, Config.getHTTPConfig())

        .success(function (data) {
            if (data[0] == '<') {
                deferred.reject();
            } else {
                StorageSrv.saveUser(data.data).then(
                    function (data) {
                        deferred.resolve(data);
                    }
                );
            }
        })

        .error(function (err) {
            deferred.reject(err);
        });

        return deferred.promise;
    };

    userService.getTravelProfile = function () {
        var deferred = $q.defer();

        $http.get(Config.getServerURL() + '/api/read/profile', Config.getHTTPConfig())

        .success(function (data) {
            if (data[0] == '<') {
                deferred.reject();
            } else {
                deferred.resolve(data);
            }
        })

        .error(function (err) {
            deferred.reject(err);
        });

        return deferred.promise;
    };

    userService.saveTravelProfile = function (travelProfile) {
        var deferred = $q.defer();

        if (!travelProfile) {
            deferred.reject('Invalid travelProfile');
        } else {
            $http.post(Config.getServerURL() + '/api/save/profile', travelProfile, Config.getHTTPConfig())

            .success(function (data) {
                deferred.resolve(data);
            })

            .error(function (err) {
                deferred.reject(err);
            });
        }

        return deferred.promise;
    };

    userService.saveAuto = function (auto) {
        var deferred = $q.defer();

        $http.post(Config.getServerURL() + '/api/save/autoInfo', auto, Config.getHTTPConfig())

        .success(function (data) {
            deferred.resolve(data);
        })

        .error(function (err) {
            deferred.reject(err);
        });

        return deferred.promise;
    };

    userService.getCommunities = function () {
        var deferred = $q.defer();

        $http.get(Config.getServerURL() + '/api/read/communities', Config.getHTTPConfig())

        .success(function (data) {
            deferred.resolve(data);
        })

        .error(function (err) {
            deferred.reject(err);
        });

        return deferred.promise;
    };

    userService.getDiscussion = function (travelId, targetUserId) {
        var deferred = $q.defer();

        $http.get(Config.getServerURL() + '/api/read/' + travelId + '/' + targetUserId + '/discussion', Config.getHTTPConfig())

        .success(function (data) {
            deferred.resolve(data);
        })

        .error(function (err) {
            deferred.reject(err);
        });

        return deferred.promise;
    };

    userService.sendMessage = function (travelId, message) {
        var deferred = $q.defer();

        if (!travelId) {
            deferred.reject('Invalid travelId');
        } else if (!message || !message.userId || !message.timestamp || !message.message || !message.targetUserId) {
            deferred.reject('Invalid message');
        } else {
            $http.get(Config.getServerURL() + '/api/message/' + travelId + '/send', message, Config.getHTTPConfig())

            .success(function (data) {
                deferred.resolve(data);
            })

            .error(function (err) {
                deferred.reject(err);
            });
        }

        return deferred.promise;
    }

    return userService;
});
