angular.module('carpooling.services.config', [])

.factory('Config', function ($http, $q, $filter) {
    var SERVER_URL = 'https://dev.smartcommunitylab.it/carpooling';
    var HTTP_CONFIG = {
        timeout: 5000
    };

    var DAYS_OF_WEEK = [
        {
            name: 'dow_monday',
            shortname: 'dow_monday_short',
            pos: 1,
            value: 2
        },
        {
            name: 'dow_tuesday',
            shortname: 'dow_tuesday_short',
            pos: 2,
            value: 3
        },
        {
            name: 'dow_wednesday',
            shortname: 'dow_wednesday_short',
            pos: 3,
            value: 4
        },
        {
            name: 'dow_thursday',
            shortname: 'dow_thursday_short',
            pos: 4,
            value: 5
        },
        {
            name: 'dow_friday',
            shortname: 'dow_friday_short',
            pos: 5,
            value: 6
        },
        {
            name: 'dow_saturday',
            shortname: 'dow_saturday_short',
            pos: 6,
            value: 7
        },
        {
            name: 'dow_sunday',
            shortname: 'dow_sunday_short',
            pos: 0,
            value: 1
        }
    ];

    return {
        getServerURL: function () {
            return SERVER_URL;
        },
        getHTTPConfig: function () {
            return HTTP_CONFIG;
        },
        getDoW: function () {
            return DAYS_OF_WEEK;
        },
        init: function () {
            /*
            var deferred = $q.defer();

            $http.get(Config.getServerURL()() + '/getparkingsbyagency/' + agencyId, Config.getHTTPConfig())

            .success(function (data) {
                deferred.resolve(data);
            })

            .error(function (err) {
                deferred.reject(err);
            });

            return deferred.promise;
            */
        }
    }
});
