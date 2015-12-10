angular.module('carpooling.services.storage', [])

.factory('StorageSrv', function ($http, $q, Config) {
    var storageService = {};

    storageService.isProfileComplete = function () {
        if (!!localStorage.profileComplete) {
            return true;
        }
        return false;
    };
    storageService.setProfileComplete = function () {
      localStorage.profileComplete = true;
    };
    storageService.getUserId = function () {
        if (!!localStorage.userId) {
            return localStorage.userId;
        }
        return null;
    };

    storageService.saveUserId = function (userId) {
        var deferred = $q.defer();

        if (!!userId) {
            localStorage.userId = userId;
        } else {
            localStorage.removeItem('userId');
        }

        deferred.resolve(userId);
        return deferred.promise;
    };

    storageService.getUser = function () {
        if (!!localStorage.user) {
            return JSON.parse(localStorage.user);
        }
        return null;
    };

    storageService.saveUser = function (user) {
        var deferred = $q.defer();

        if (!!user) {
            localStorage.user = JSON.stringify(user);
        } else {
            localStorage.removeItem('user');
        }

        deferred.resolve(user);
        return deferred.promise;
    };

    return storageService;
});
