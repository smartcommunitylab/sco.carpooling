angular.module('carpooling.services.driver', [])

.factory('DriverSrv', function ($http, $q, Config) {

    var isTravelValid = function (travel) {
        if (!!travel && !!travel.from && !!travel.to && !!travelRequest.userId) {
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
        getDriverTrips: function () {
            var deferred = $q.defer();

            $http.get(Config.getServerURL() + '/api/driver/trips', Config.getHTTPConfig())

            .success(function (data) {
                deferred.resolve(data);
            })

            .error(function (err) {
                deferred.reject(err);
            });

            return deferred.promise;
        },
        createTrip: function (travel) {
            var deferred = $q.defer();

            if (!!!travel || !(isTravelValid(travel))) {
                deferred.reject('Invalid travel');
            } else {
                $http.post(Config.getServerURL() + '/api/driver/trips', travel, Config.getHTTPConfig())

                .success(function (data) {
                    deferred.resolve(data);
                })

                .error(function (err) {
                    deferred.reject(err);
                });
            }

            return deferred.promise;
        },
        acceptTrip: function (tripId, booking) {
            var deferred = $q.defer();

            if (!!!tripId) {
                deferred.reject('Invalid tripId');
            } else if (!!!booking || !(isBookingTravellerValid(booking))) {
                deferred.reject('Invalid travelRequest');
            } else {
                $http.post(Config.getServerURL() + '/api/driver/trips/' + tripId + '/accept', booking, Config.getHTTPConfig())

                .success(function (data) {
                    deferred.resolve(data);
                })

                .error(function (err) {
                    deferred.reject(err);
                });
            }

            return deferred.promise;
        },
        // /api/driver/trips/{tripId}/accept
        ratePassenger: function (passengerId, rating) {
            var deferred = $q.defer();

            if (!!!passengerId) {
                deferred.reject('Invalid driverId');
            } else if (!!!rating || (rating < 1 || rating > 5)) {
                deferred.reject('Invalid rating');
            } else {
                $http.post(Config.getServerURL() + '/api/rate/passenger/' + passengerId + '/' + rating, booking, Config.getHTTPConfig())

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
