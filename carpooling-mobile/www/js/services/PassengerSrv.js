angular.module('carpooling.services.passenger', [])

.factory('PassengerSrv', function ($rootScope, $http, $q, Config) {
    var passengerService = {};

    passengerService.getTrip = function (travelId) {
        var deferred = $q.defer();

        $http.get(Config.getServerURL() + '/api/passenger/trips/' + travelId, Config.getHTTPConfig())

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

    passengerService.getPassengerTrips = function (start, count) {
        var deferred = $q.defer();
        var httpConfig = Config.getHTTPConfig();

        if (start != null || count != null) {
            httpConfig.params = {};

            if (start != null) {
                if (start >= 0) {
                    httpConfig.params['start'] = start;
                } else {
                    deferred.reject('Invalid "start" value');
                    return deferred.promise;
                }
            }

            if (count != null) {
                if (count > 0) {
                    httpConfig.params['count'] = count;
                } else {
                    deferred.reject('Invalid "count" value');
                    return deferred.promise;
                }
            }
        }

        $http.get(Config.getServerURL() + '/api/passenger/trips', httpConfig)

        .then(function (response) {

                if (response.data[0] == '<') {
                    deferred.reject(Config.LOGIN_EXPIRED);
                    $rootScope.login();
                } else {
                    deferred.resolve(response.data.data);
                }
            },
            function (responseError) {
                if (!!responseError.data.error) {
                    deferred.reject(responseError.data.error);
                } else {
                    deferred.reject(responseError.data);
                }
            }
        );

        return deferred.promise;
    };

    passengerService.getPassengerMonitored = function () {
        var deferred = $q.defer();

        $http.get(Config.getServerURL() + '/api/passenger/monitored', Config.getHTTPConfig())

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

    passengerService.searchTrip = function (travelRequest) {
        var deferred = $q.defer();

        if (!travelRequest || !travelRequest.from || !travelRequest.to || !travelRequest.when) {
            deferred.reject('Invalid travelRequest');
        } else {
            $http.post(Config.getServerURL() + '/api/passenger/trips', travelRequest, Config.getHTTPConfig())

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
        }

        return deferred.promise;
    };

    passengerService.deleteTripRequest = function (travelRequestId) {
        var deferred = $q.defer();

        if (!!travelRequestId) {
            deferred.reject('Invalid travelRequestId');
        } else {
            // /api/delete/tripRequest/{travelRequestId}
            $http.delete(Config.getServerURL() + '/api/delete/tripRequest/' + travelRequestId, Config.getHTTPConfig())

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

    passengerService.rateDriver = function (driverId, rating, booking) {
        var deferred = $q.defer();

        if (!driverId) {
            deferred.reject('Invalid driverId');
        } else if (!rating || (rating < 1 || rating > 5)) {
            deferred.reject('Invalid rating');
        } else {
            $http.post(Config.getServerURL() + '/api/rate/driver/' + driverId + '/' + rating, booking, Config.getHTTPConfig())

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

    return passengerService;
});
