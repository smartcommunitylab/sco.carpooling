angular.module('carpooling.services.config', [])

.factory('Config', function ($http, $q, $filter) {
    var SERVER_URL = 'https://dev.smartcommunitylab.it/carpooling';
    var GEOCODER_URL = 'https://os.smartcommunitylab.it/core.geocoder/spring';

    var HTTP_CONFIG = {
        timeout: 5000
    };

    var ttJsonConfig = null;
    var DISTANCE_AUTOCOMPLETE = '6';
    var LAT = 46.069672;
    var LON = 11.121270;
    var ZOOM = 15;

    var monthList = [
        $filter('translate')('popup_datepicker_jan'),
        $filter('translate')('popup_datepicker_feb'),
        $filter('translate')('popup_datepicker_mar'),
        $filter('translate')('popup_datepicker_apr'),
        $filter('translate')('popup_datepicker_may'),
        $filter('translate')('popup_datepicker_jun'),
        $filter('translate')('popup_datepicker_jul'),
        $filter('translate')('popup_datepicker_ago'),
        $filter('translate')('popup_datepicker_sep'),
        $filter('translate')('popup_datepicker_oct'),
        $filter('translate')('popup_datepicker_nov'),
        $filter('translate')('popup_datepicker_dic')
    ];

    var weekDaysList = [
        $filter('translate')('popup_datepicker_sun'),
        $filter('translate')('popup_datepicker_mon'),
        $filter('translate')('popup_datepicker_tue'),
        $filter('translate')('popup_datepicker_wed'),
        $filter('translate')('popup_datepicker_thu'),
        $filter('translate')('popup_datepicker_fri'),
        $filter('translate')('popup_datepicker_sat')
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
        getmonthList: function () {
            return monthList;
        },
        getweekList: function () {
            return weekDaysList;
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
