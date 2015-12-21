angular.module('carpooling.services.passenger', [])

.factory('PassengerSrv', function ($rootScope, $http, $q, Config) {
    var passengerService = {};

    passengerService.getTrip = function (travelId) {
        var deferred = $q.defer();

        $http.get(Config.getServerURL() + '/api/passenger/trips/' + travelId, Config.getHTTPConfig())

        .success(function (data) {
            if (data[0] == '<') {
                deferred.reject();
                $rootScope.login();
            } else {
                deferred.resolve(data.data);
            }
        })

        .error(function (err) {
            deferred.reject(err);
        });

        return deferred.promise;
    };

    passengerService.getPassengerTrips = function () {
        var deferred = $q.defer();

        $http.get(Config.getServerURL() + '/api/passenger/trips', Config.getHTTPConfig())

        .success(function (data) {
            if (data[0] == '<') {
                deferred.reject();
                $rootScope.login();
            } else {
                deferred.resolve(data.data);
            }
        })

        .error(function (err) {
            deferred.reject(err);
        });

        return deferred.promise;
    };

    passengerService.getPassengerMonitored = function () {
        var deferred = $q.defer();

        $http.get(Config.getServerURL() + '/api/passenger/monitored', Config.getHTTPConfig())

        .success(function (data) {
            if (data[0] == '<') {
                deferred.reject();
                $rootScope.login();
            } else {
                deferred.resolve(data.data);
            }
        })

        .error(function (err) {
            deferred.reject(err);
        });

        return deferred.promise;
    };

    passengerService.searchTrip = function (travelRequest) {
        var deferred = $q.defer();

        if (!travelRequest || !travelRequest.from || !travelRequest.to || !travelRequest.when) {
            deferred.reject('Invalid travelRequest');
        } else {
            $http.post(Config.getServerURL() + '/api/passenger/trips', travelRequest, Config.getHTTPConfig())

            .success(function (data) {
                if (data[0] == '<') {
                    deferred.reject();
                    $rootScope.login();
                } else {
                    deferred.resolve(data.data);
                }
            })

            .error(function (err) {
                deferred.reject(err);
            });
        }

        return deferred.promise;
    };

    passengerService.bookTrip = function (tripId, booking) {
        var deferred = $q.defer();

        if (!tripId) {
            deferred.reject('Invalid tripId');
        } else if (!booking || !booking.traveller || !booking.traveller.userId || !booking.traveller.name || !booking.traveller.surname) {
            deferred.reject('Invalid travelRequest');
        } else {
            $http.post(Config.getServerURL() + '/api/passenger/trips/' + tripId + '/book', booking, Config.getHTTPConfig())

            .success(function (data) {
                if (data[0] == '<') {
                    deferred.reject();
                    $rootScope.login();
                } else {
                    deferred.resolve(data.data);
                }
            })

            .error(function (err) {
                deferred.reject(err);
            });
        }

        return deferred.promise;
    };

    passengerService.deleteTripRequest = function (travelRequestId) {
        var deferred = $q.defer();

        if (!!travelRequestId) {
            deferred.reject('Invalid tripId');
        } else {
            // /api/delete/tripRequest/{travelRequestId}
            $http.delete(Config.getServerURL() + '/api/delete/tripRequest/' + travelRequestId, Config.getHTTPConfig())

            .success(function (data) {
                if (data[0] == '<') {
                    deferred.reject();
                    $rootScope.login();
                } else {
                    deferred.resolve(data);
                }
            })

            .error(function (err) {
                deferred.reject(err);
            });
        }

        return deferred.promise;
    };

    passengerService.rateDriver = function (driverId, rating) {
        var deferred = $q.defer();

        if (!driverId) {
            deferred.reject('Invalid driverId');
        } else if (!rating || (rating < 1 || rating > 5)) {
            deferred.reject('Invalid rating');
        } else {
            $http.post(Config.getServerURL() + '/api/rate/driver/' + driverId + '/' + rating, booking, Config.getHTTPConfig())

            .success(function (data) {
                if (data[0] == '<') {
                    deferred.reject();
                    $rootScope.login();
                } else {
                    deferred.resolve(data);
                }
            })

            .error(function (err) {
                deferred.reject(err);
            });
        }

        return deferred.promise;
    }

    return passengerService;
});
