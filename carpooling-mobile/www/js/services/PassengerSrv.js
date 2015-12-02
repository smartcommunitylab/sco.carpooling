angular.module('carpooling.services.passenger', [])

.factory('PassengerSrv', function ($http, $q, Config) {
    var isTravelRequestValid = function (travelRequest) {
        if (!!travelRequest && !!travelRequest.from && !!travelRequest.to && !!travelRequest.when) {
            return true;
        }
        return false;
    };

    var isBookingTravellerValid = function (booking) {
        var traveller = booking.traveller;

        if (!!traveller && !!traveller.userId && !!traveller.name && !!traveller.surname && !!traveller.email) {
            return true;
        }
        return false;
    };

    return {
        getPassengerTrips: function () {
            var deferred = $q.defer();

            $http.get(Config.getServerURL() + '/api/passenger/trips', Config.getHTTPConfig())

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
        },
        getPassengerMonitored: function () {
            var deferred = $q.defer();

            $http.get(Config.getServerURL() + '/api/passenger/monitored', Config.getHTTPConfig())

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
        },
        searchTrip: function (travelRequest) {
            var deferred = $q.defer();

            if (!(isTravelRequestValid(travelRequest))) {
                deferred.reject('Invalid travelRequest');
            } else {
                $http.post(Config.getServerURL() + '/api/passenger/trips', travelRequest, Config.getHTTPConfig())

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
            }

            return deferred.promise;
        },
        bookTrip: function (tripId, booking) {
            var deferred = $q.defer();

            if (!tripId) {
                deferred.reject('Invalid tripId');
            } else if (!booking || !(isBookingTravellerValid(booking))) {
                deferred.reject('Invalid travelRequest');
            } else {
                $http.post(Config.getServerURL() + '/api/passenger/trips/' + tripId + '/book', booking, Config.getHTTPConfig())

                .success(function (data) {
                    deferred.resolve(data);
                })

                .error(function (err) {
                    deferred.reject(err);
                });
            }

            return deferred.promise;
        },
        rateDriver: function (driverId, rating) {
            var deferred = $q.defer();

            if (!driverId) {
                deferred.reject('Invalid driverId');
            } else if (!rating || (rating < 1 || rating > 5)) {
                deferred.reject('Invalid rating');
            } else {
                $http.post(Config.getServerURL() + '/api/rate/driver/' + driverId + '/' + rating, booking, Config.getHTTPConfig())

                .success(function (data) {
                    deferred.resolve(data);
                })

                .error(function (err) {
                    deferred.reject(err);
                });
            }

            return deferred.promise;
        }
    }
});
