angular.module('carpooling.services.storage', [])

.factory('StorageSrv', function ($http, $q, Config) {
    var storageService = {};

    storageService.isProfileComplete = function () {
        if (!!localStorage['profileComplete']) {
            return true;
        }
        return false;
    };

    storageService.setProfileComplete = function () {
        localStorage['profileComplete'] = true;
    };

    storageService.getUserId = function () {
        if (!!localStorage['userId']) {
            return localStorage['userId'];
        }
        return null;
    };

    storageService.saveUserId = function (userId) {
        var deferred = $q.defer();

        if (!!userId) {
            localStorage['userId'] = userId;
        } else {
            localStorage.removeItem('userId');
        }

        deferred.resolve(userId);
        return deferred.promise;
    };

    storageService.getUser = function () {
        if (!!localStorage['user']) {
            return JSON.parse(localStorage['user']);
        }
        return null;
    };

    storageService.saveUser = function (user) {
        var deferred = $q.defer();

        if (!!user) {
            localStorage['user'] = JSON.stringify(user);
        } else {
            localStorage.removeItem('user');
        }

        deferred.resolve(user);
        return deferred.promise;
    };

    storageService.getCommunities = function () {
        if (!!localStorage['communities']) {
            return JSON.parse(localStorage['communities']);
        }
    };

    storageService.getCommunityIds = function () {
        var communityIds = [];
        var communities = storageService.getCommunities();

        if (!!communities) {
            communities.forEach(function (community) {
                communityIds.push(community.id);
            });
        }
        return communityIds;
    };

    storageService.getCommunityById = function (communityId) {
        var community = null;
        var communities = storageService.getCommunities();

        for (var i = 0; i < communities.length; i++) {
            if (communities[i].id === communityId) {
                community = communities[i];
                i = communities.length;
            }
        }

        return community;
    };

    storageService.saveCommunities = function (communities) {
        if (!!communities) {
            localStorage['communities'] = JSON.stringify(communities);
        }
    };

    storageService.reset = function () {
        var deferred = $q.defer();
        localStorage.removeItem('userId');
        localStorage.removeItem('user');
        localStorage.removeItem('profileComplete');
        localStorage.removeItem('communities');
        localStorage.removeItem('chooseCommunity');
        deferred.resolve(true);
        return deferred.promise;
    };

    return storageService;
});
