angular.module('carpooling.services.cache', [])

.factory('CacheSrv', function ($rootScope, $q) {
    var cacheService = {};

    var rPassengerTrips = true;
    var rDriverTrips = true;
    var rDriverTrip = null;
    var rCommunityTrips = true;
    var rCommunityTrip = null;
    var rStoricoPassengerTrips = true;
    var rStoricoDriverTrips = true;

    cacheService.setReloadPassengerTrips = function (reload) {
        rPassengerTrips = reload;
    };

    cacheService.reloadPassengerTrips = function () {
        return rPassengerTrips;
    };

    cacheService.setReloadDriverTrips = function (reload) {
        rDriverTrips = reload;
    };

    cacheService.reloadDriverTrips = function () {
        return rDriverTrips;
    };

    cacheService.setReloadDriverTrip = function (tripId) {
        rDriverTrip = tripId;
    };

    cacheService.reloadDriverTrip = function () {
        return rDriverTrip;
    };

    cacheService.setReloadCommunityTrips = function (reload) {
        rCommunityTrips = reload;
    };

    cacheService.reloadCommunityTrips = function () {
        return rCommunityTrips;
    };

    cacheService.setReloadCommunityTrip = function (tripId) {
        rCommunityTrip = tripId;
    };

    cacheService.reloadCommunityTrip = function () {
        return rCommunityTrip;
    };

    cacheService.setReloadStoricoPassengerTrips = function (reload) {
        rStoricoPassengerTrips = reload;
    };

    cacheService.reloadStoricoPassengerTrips = function () {
        return rStoricoPassengerTrips;
    };

    cacheService.setReloadStoricoDriverTrips = function (reload) {
        rStoricoDriverTrips = reload;
    };

    cacheService.reloadStoricoDriverTrips = function () {
        return rStoricoDriverTrips;
    };

    return cacheService;
});
