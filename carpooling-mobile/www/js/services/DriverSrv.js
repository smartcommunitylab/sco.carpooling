angular.module('carpooling.services.driver', [])

.factory('DriverSrv', function ($http, $q, Config) {
    var driverService = {};

    driverService.getDriverTrips = function () {
        var deferred = $q.defer();

        $http.get(Config.getServerURL() + '/api/driver/trips', Config.getHTTPConfig())

        .success(function (data) {
            if (data[0] == '<') {
                deferred.reject();
            } else {
                deferred.resolve(data.data);
            }
        })

        .error(function (err) {
            deferred.reject(err);
        });

        return deferred.promise;
    };

    driverService.createTrip = function (travel) {
        var deferred = $q.defer();

        if (!travel || !travel.from || !travel.to && !travel.userId) {
            deferred.reject('Invalid travel');
        } else {
            $http.post(Config.getServerURL() + '/api/driver/trips', travel, Config.getHTTPConfig())

            .success(function (data) {
                if (data[0] == '<') {
                    deferred.reject();
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

    driverService.decideTrip = function (tripId, booking) {
        var deferred = $q.defer();

        if (!tripId) {
            deferred.reject('Invalid tripId');
        } else if (!booking || !booking.traveller || !booking.traveller.userId || !booking.traveller.name || !booking.traveller.surname) {
            deferred.reject('Invalid travel');
        } else {
            $http.post(Config.getServerURL() + '/api/driver/trips/' + tripId + '/accept', booking, Config.getHTTPConfig())

            .success(function (data) {
                deferred.resolve(data.data);
            })

            .error(function (err) {
                deferred.reject(err);
            });
        }

        return deferred.promise;
    };

    driverService.ratePassenger = function (passengerId, rating) {
        var deferred = $q.defer();

        if (!passengerId) {
            deferred.reject('Invalid driverId');
        } else if (!rating || (rating < 1 || rating > 5)) {
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
    };

    return driverService;
});
