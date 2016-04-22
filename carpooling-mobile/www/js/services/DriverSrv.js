angular.module('carpooling.services.driver', [])

.factory('DriverSrv', function ($rootScope, $http, $q, Config, Utils) {
    var driverService = {};

    driverService.getDriverTrips = function (start, count, future) {
        var deferred = $q.defer();

        var httpConfig = angular.copy(Config.getHTTPConfig());
        httpConfig.params = {};
        if (future) {
          httpConfig.params.from = new Date().getTime();
          httpConfig.params.order = 1;
        } else {
          httpConfig.params.to = new Date().getTime();
          httpConfig.params.order = -1;
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

        $http.get(Config.getServerURL() + '/api/driver/trips', httpConfig)

        .then(
            function (response) {
                if (response.data[0] == '<') {
                    deferred.reject(Config.LOGIN_EXPIRED);
                    $rootScope.login();
                } else {
                    response.data.data.forEach(function (trip) {
                        trip.bookingCounters = Utils.getBookingCounters(trip);
                        trip.style = Utils.getTripStyle(trip);
                    });

                    deferred.resolve(response.data.data);
                }
            },
            function (responseError) {
                deferred.reject(responseError.data? responseError.data.errorMessage : responseError);
            }
        );

        return deferred.promise;
    };

    driverService.createTrip = function (travel) {
        var deferred = $q.defer();

        if (!travel || !travel.from || !travel.to && !travel.userId) {
            deferred.reject('Invalid travel');
        } else {
            $http.post(Config.getServerURL() + '/api/driver/trips', travel, Config.getHTTPConfig())

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
                    deferred.reject(responseError.data? responseError.data.errorMessage : responseError);
                }
            );
        }

        return deferred.promise;
    };

    driverService.createRecurrentTrip = function (travel) {
        var deferred = $q.defer();

        if (!travel || !travel.from || !travel.to && !travel.userId) {
            deferred.reject('Invalid travel');
        } else {
            $http.post(Config.getServerURL() + '/api/driver/recurrenttrips', travel, Config.getHTTPConfig())

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
                    deferred.reject(responseError.data? responseError.data.errorMessage : responseError);
                }
            );
        }

        return deferred.promise;
    };

    driverService.decideTrip = function (tripId, booking) {
        var deferred = $q.defer();

        if (!tripId) {
            deferred.reject('Invalid tripId');
        } else if (!booking || !booking.traveller || !booking.traveller.userId || !booking.traveller.dpName) {
            deferred.reject('Invalid traveller');
        } else {
            $http.post(Config.getServerURL() + '/api/driver/trips/' + tripId + '/accept', booking, Config.getHTTPConfig())

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
                    deferred.reject(responseError.data? responseError.data.errorMessage : responseError);
                }
            );
        }

        return deferred.promise;
    };

    driverService.decideRecurrentTrip = function (recurrentId, recurrentBooking) {
        var deferred = $q.defer();

        if (!recurrentId) {
            deferred.reject('Invalid recurrentId');
        } else if (!recurrentBooking || !recurrentBooking.traveller || !recurrentBooking.traveller.userId || !recurrentBooking.traveller.dpName) {
            deferred.reject('Invalid traveller');
        } else {
            $http.post(Config.getServerURL() + '/api/driver/recurrenttrips/' + recurrentId + '/accept', recurrentBooking, Config.getHTTPConfig())

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
                    deferred.reject(responseError.data? responseError.data.errorMessage : responseError);
                }
            );
        }

        return deferred.promise;
    };

    driverService.ratePassenger = function (passengerId, rating, booking) {
        var deferred = $q.defer();

        if (!passengerId) {
            deferred.reject('Invalid driverId');
        } else if (!rating || (rating < 1 || rating > 5)) {
            deferred.reject('Invalid rating');
        } else {
            $http.post(Config.getServerURL() + '/api/rate/passenger/' + passengerId + '/' + rating, booking, Config.getHTTPConfig())

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
                    deferred.reject(responseError.data? responseError.data.errorMessage : responseError);
                }
            );
        }

        return deferred.promise;
    };

    driverService.deleteTravel = function (id) {
        var deferred = $q.defer();

        if (!id) {
            deferred.reject('Invalid travel id');
        } else {
            $http.delete(Config.getServerURL() + '/api/delete/trip/' + id, Config.getHTTPConfig())

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
                    deferred.reject(responseError.data? responseError.data.errorMessage : responseError);
                }
            );
        }

        return deferred.promise;
    };

    driverService.deleteRecurrentTravel = function (id) {
        var deferred = $q.defer();

        if (!id) {
            deferred.reject('Invalid travel id');
        } else {
            $http.delete(Config.getServerURL() + '/api/delete/recurrenttrip/' + id, Config.getHTTPConfig())

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
                    deferred.reject(responseError.data? responseError.data.errorMessage : responseError);
                }
            );
        }

        return deferred.promise;
    };

    return driverService;
});
