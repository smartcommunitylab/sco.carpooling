angular.module('carpooling.services.config', [])

.factory('Config', function ($http, $q, $filter) {
    var SERVER_URL = 'https://dev.smartcommunitylab.it/carpooling';
    var GEOCODER_URL = 'https://os.smartcommunitylab.it/core.geocoder/spring';
    var APPID = '1E6tkJ4KTgmxaJUAjkhz59ML92XxxGJmfU2JUFmr';
    var CLIENTKEY = 'p2RufG05OrYKX78LOYEEoRbMgEFSfZNfbYBOWr28';

    var HTTP_CONFIG = {
        timeout: 5000
    };

    var ttJsonConfig = null;
    var DISTANCE_AUTOCOMPLETE = '6';
    var LAT = 46.069672;
    var LON = 11.121270;
    var ZOOM = 15;

    var monthList = [
        $filter('translate')('month_jan'),
        $filter('translate')('month_feb'),
        $filter('translate')('month_mar'),
        $filter('translate')('month_apr'),
        $filter('translate')('month_may'),
        $filter('translate')('month_jun'),
        $filter('translate')('month_jul'),
        $filter('translate')('month_ago'),
        $filter('translate')('month_sep'),
        $filter('translate')('month_oct'),
        $filter('translate')('month_nov'),
        $filter('translate')('month_dic')
    ];

    var weekDaysList = [
        $filter('translate')('dow_sunday_short'),
        $filter('translate')('dow_monday_short'),
        $filter('translate')('dow_tuesday_short'),
        $filter('translate')('dow_wednesday_short'),
        $filter('translate')('dow_thursday_short'),
        $filter('translate')('dow_friday_short'),
        $filter('translate')('dow_saturday_short')
    ];

    return {
        getServerURL: function () {
            return SERVER_URL;
        },
        getGeocoderURL: function () {
            return GEOCODER_URL;
        },
        getHTTPConfig: function () {
            return HTTP_CONFIG;
        },
        getLat: function () {
            return LAT;
        },
        getLon: function () {
            return LON;
        },
        getZoom: function () {
            return ZOOM;
        },
        getDistanceForAutocomplete: function () {
            return DISTANCE_AUTOCOMPLETE;
        },
        getMonthList: function () {
            return monthList;
        },
        getDoWList: function () {
            return weekDaysList;
        },
        getAppId: function () {
            return APPID;
        },
        getClientKey: function () {
            return CLIENTKEY;
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
