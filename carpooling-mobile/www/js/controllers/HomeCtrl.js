angular.module('carpooling.controllers.home', [])

.controller('AppCtrl', function ($scope) {})

.controller('HomeCtrl', function ($scope) {})

.controller('PartecipoCtrl', function ($scope, $state, UserSrv, PassengerSrv) {
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
        UserSrv.getTravelProfile().then(function (data) {
            $scope.travelProfile = data;
        });
    };

    $scope.passengerTrips = [];

    PassengerSrv.getPassengerTrips().then(
        function (trips) {
            $scope.passengerTrips = trips;
        },
        function (error) {
            // TODO: handle getPassengerTrips error
        }
    );

    $scope.selectTrip = function (index) {
        $state.go('app.viaggio', {
            'trip': $scope.passengerTrips[index]
        });
    };
})

.controller('OffroCtrl', function ($scope, $state, DriverSrv) {
    $scope.travelDateFormat = 'dd MMMM yyyy';
    $scope.travelTimeFormat = 'HH:mm';

    $scope.driverTrips = [];

    DriverSrv.getDriverTrips().then(
        function (trips) {
            $scope.driverTrips = trips;
        },
        function (error) {
            // TODO: handle getDriverTrips error
        }
    );

    $scope.selectTrip = function (index) {
        $state.go('app.viaggio', {
            'trip': $scope.driverTrips[index]
        });
    };
});
