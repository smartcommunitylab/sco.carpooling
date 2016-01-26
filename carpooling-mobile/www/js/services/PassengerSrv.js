angular.module('carpooling.services.passenger', [])

.factory('PassengerSrv', function ($rootScope, $http, $q, Config, Utils, StorageSrv) {
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

    passengerService.getPassengerTrips = function (start, count, future, toConfirm) {
        var deferred = $q.defer();
        var httpConfig = angular.copy(Config.getHTTPConfig());
        httpConfig.params = {};


        if (future) {
            httpConfig.params.from = new Date().getTime();
            httpConfig.params.order = 1;
        } else {
            httpConfig.params.to = new Date().getTime();
            httpConfig.params.order = -1;
            if (toConfirm) {
                httpConfig.params.boarded = false;
                httpConfig.params.accepted = true;
            } else {
                httpConfig.params.boarded = true;
            }
        }
        if (start != null || count != null) {

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
                    response.data.data.forEach(function (trip) {
                        // booking counters
                        trip.bookingCounters = Utils.getBookingCounters(trip);

                        // booking state
                        trip.bookings.forEach(function (booking) {
                            if (booking.traveller.userId === StorageSrv.getUserId()) {
                                // my booking
                                trip.bookingState = booking.accepted;
                            }
                        });

                        trip.style = Utils.getTripStyle(trip);
                    });

                    deferred.resolve(response.data.data);
                }
            },
            function (responseError) {
                deferred.reject(responseError.data.error);
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

    passengerService.bookRecurrentTrip = function (tripId, recurrentBooking) {
        var deferred = $q.defer();

        if (!tripId) {
            deferred.reject('Invalid tripId');
        } else if (!recurrentBooking || !recurrentBooking.traveller || !recurrentBooking.traveller.userId || !recurrentBooking.traveller.name || !recurrentBooking.traveller.surname) {
            deferred.reject('Invalid travelRequest');
        } else {
            $http.post(Config.getServerURL() + '/api/passenger/recurrenttrips/' + tripId + '/book', recurrentBooking, Config.getHTTPConfig())

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

    passengerService.confirmTrip = function (tripId, boarded) {
        var deferred = $q.defer();
        $http.put(Config.getServerURL() + '/api/passenger/trips/' + tripId + '/boarding/' + (boarded ? 'yes' : 'no'), {}, Config.getHTTPConfig())
            .then(
                function (response) {
                    if (response.data[0] == '<') {
                        deferred.reject(Config.LOGIN_EXPIRED);
                        $rootScope.login();
                    } else {
                        deferred.resolve(true);
                    }
                },
                function (responseError) {
                    deferred.reject(responseError.data.error);
                }
            );
        return deferred.promise;
    }

    return passengerService;
});
