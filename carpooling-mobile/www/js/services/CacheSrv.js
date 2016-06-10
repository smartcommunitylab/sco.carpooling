angular.module('carpooling.services.cache', [])

.factory('CacheSrv', function ($rootScope, $q) {
    var cacheService = {};

    var nonConfirmedTrips = null;
    var passengerTrips = null;
    var driverTrips = null;

    var myCommunities = null;
    var allCommunities = null;

    var rMyCommunities = true;
    var rAllCommunities = true;
    var rPassengerTrips = true;
    var rDriverTrips = true;
    var rDriverTrip = null;
    var rCommunityTrips = true;
    var rCommunityTrip = null;
    var rStoricoPassengerTrips = true;
    var rStoricoDriverTrips = true;
    var rStoricoDriverTrip = null;

    cacheService.reset = function () {
        rMyCommunities = true;
        rAllCommunities = true;
        rPassengerTrips = true;
        rDriverTrips = true;
        rDriverTrip = null;
        rCommunityTrips = true;
        rCommunityTrip = null;
        rStoricoPassengerTrips = true;
        rStoricoDriverTrips = true;
        rStoricoDriverTrip = null;
    };

    cacheService.getNonConfirmedTrips = function () {
        return nonConfirmedTrips;
    };

    cacheService.setNonConfirmedTrips = function (list) {
        nonConfirmedTrips = list;
    };

    cacheService.getPassengerTrips = function () {
        return passengerTrips;
    };

    cacheService.setPassengerTrips = function (list) {
        passengerTrips = list;
    };

    cacheService.setReloadPassengerTrips = function (reload) {
        rPassengerTrips = reload;
    };

    cacheService.reloadPassengerTrips = function () {
        return rPassengerTrips;
    };

    cacheService.getDriverTrips = function () {
        return driverTrips;
    };

    cacheService.setDriverTrips = function (list) {
        driverTrips = list;
    };

    cacheService.setReloadMyCommunities = function (reload) {
        rMyCommunities = reload;
    };

    cacheService.reloadMyCommunities = function () {
        return rMyCommunities;
    };

    cacheService.setReloadAllCommunities = function (reload) {
        rAllCommunities = reload;
    };

    cacheService.reloadAllCommunities = function () {
        return rAllCommunities;
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

    cacheService.getMyCommunities = function () {
        return myCommunities;
    };

    cacheService.setMyCommunities = function (myComs) {
       myCommunities = myComs;
    };

    cacheService.getAllCommunities = function () {
        return allCommunities;
    };

    cacheService.setAllCommunities = function (allComs) {
       allCommunities = allComs;
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

    cacheService.setReloadStoricoDriverTrip = function (tripId) {
        rStoricoDriverTrip = tripId;
    };

    cacheService.reloadStoricoDriverTrip = function () {
        return rStoricoDriverTrip;
    };

    return cacheService;
});
