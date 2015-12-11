angular.module('carpooling.controllers.home', [])

.controller('AppCtrl', function ($scope) {})

.controller('HomeCtrl', function ($scope, $state, StorageSrv) {
    if (!StorageSrv.isProfileComplete()) {
        $state.go('app.profilo');
    }
})

.controller('PartecipoCtrl', function ($scope, $state, UserSrv, PassengerSrv, Utils) {
    $scope.travelProfile = 'empty';
    $scope.travelDateFormat = 'dd MMMM yyyy';
    $scope.travelTimeFormat = 'HH:mm';

    /*Just for example*/
    /*
    $scope.passengerTrips = [
        {
            "from": {
                "name": "Via Fiume",
                "address": "Via Fiume",
                "latitude": 46.065487,
                "longitude": 11.131346,
                "range": 1,
                "coordinates": [
                    46.065487,
                    11.131346
                ]
            },
            "to": {
                "name": "Muse",
                "address": "Muse",
                "latitude": 46.063266,
                "longitude": 11.113062,
                "range": 1,
                "coordinates": [
                    46.063266,
                    11.113062
                ]
            },
            "when": 1443425400000,
            "monitored": true
        }
    ];
    */

    $scope.getTravelProfile = function () {
        Utils.loading();
        UserSrv.getTravelProfile().then(function (data) {
            $scope.travelProfile = data;
            Utils.loaded();
        });
    };

    $scope.passengerTrips = [];

    var init = function () {
        Utils.loading();
        PassengerSrv.getPassengerTrips().then(
            function (trips) {
                Utils.loaded();
                $scope.passengerTrips = trips;
            },
            function (error) {
                Utils.loaded();
                // TODO: handle getPassengerTrips error
            }
        );
    };

    init();

    $scope.selectTrip = function (index) {
        $state.go('app.viaggio', {
            'travelId': $scope.passengerTrips[index].id
        });
    };
})

.controller('OffroCtrl', function ($scope, $state, DriverSrv, Utils) {
    $scope.travelDateFormat = 'dd MMMM yyyy';
    $scope.travelTimeFormat = 'HH:mm';

    $scope.driverTrips = [];

    var init = function () {
        Utils.loading();
        DriverSrv.getDriverTrips().then(
            function (trips) {
                Utils.loaded();
                $scope.driverTrips = trips;
            },
            function (error) {
                Utils.loaded();
                // TODO: handle getDriverTrips error
            }
        );
    };

    init();

    $scope.selectTrip = function (index) {
        $state.go('app.viaggio', {
            'travelId': $scope.driverTrips[index].id
        });
    };
});
