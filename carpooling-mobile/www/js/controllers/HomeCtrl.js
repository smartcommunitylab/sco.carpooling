angular.module('carpooling.controllers.home', [])

.controller('AppCtrl', function ($scope, $state) {
    $scope.reloadProfile = function () {
        $state.go('app.profilo.userinfo', {
            'user': null
        }, {
            reload: true
        });
    };
})

.controller('HomeCtrl', function ($scope, $state, StorageSrv, UserSrv) {
    if (StorageSrv.getUserId() != null && !StorageSrv.isProfileComplete()) {
        UserSrv.getUser(StorageSrv.getUserId()).then(
            function () {
                $state.go('app.profilo');
            }
        );
    }
})

.controller('PartecipoCtrl', function ($scope, $state, Config, StorageSrv, Utils, UserSrv, PassengerSrv) {
    $scope.travelProfile = 'empty';
    $scope.travelDateFormat = 'dd MMMM yyyy';
    $scope.travelTimeFormat = 'HH:mm';

    $scope.getTravelProfile = function () {
        Utils.loading();
        UserSrv.getTravelProfile().then(function (data) {
            $scope.travelProfile = data;
            Utils.loaded();
        });
    };

    $scope.passengerTrips = null;

    var init = function () {
        Utils.loading();
        PassengerSrv.getPassengerTrips().then(
            function (trips) {
                trips.forEach(function (trip) {
                    // booking counters
                    trip.bookingCounters = Utils.getBookingCounters(trip);

                    // booking state
                    trip.bookings.forEach(function (booking) {
                        if (booking.traveller.userId === StorageSrv.getUserId()) {
                            // my booking
                            trip.bookingState = booking.accepted;
                        }
                    });
                });

                Utils.loaded();
                $scope.passengerTrips = trips;
            },
            function (error) {
                Utils.loaded();
                if (error !== Config.LOGIN_EXPIRED) {
                    Utils.toast();
                }
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

    $scope.driverTrips = null;

    var init = function () {
        Utils.loading();
        DriverSrv.getDriverTrips().then(
            function (trips) {
                trips.forEach(function (trip) {
                    trip.bookingCounters = Utils.getBookingCounters(trip);
                });

                Utils.loaded();
                $scope.driverTrips = trips;
            },
            function (error) {
                Utils.loaded();
                Utils.toast();
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
