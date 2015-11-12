angular.module('carpooling.services.trips', [])

.factory('Trips', function ($http, $q, Config) {
    return {
        getProfile: function () {
            var deferred = $q.defer();

            $http.get(Config.getServerURL() + '/api/read/profile', Config.getHTTPConfig())

            .success(function (data) {
                deferred.resolve(data);
            })

            .error(function (err) {
                deferred.reject(err);
            });

            return deferred.promise;
        }
    }
});
