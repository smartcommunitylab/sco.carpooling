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

.controller('HomeCtrl', function ($scope, $state, Config, StorageSrv, DriverSrv, Utils, UserSrv, PassengerSrv, $ionicTabsDelegate) {
    if (StorageSrv.getUserId() != null && !StorageSrv.isProfileComplete()) {
        UserSrv.getUser(StorageSrv.getUserId()).then(
            function () {
                $state.go('app.profilo');
            }
        );
        return;
    }

    $scope.tab = 0;

    $scope.selectTab = function(idx) {
      if (idx == $scope.tab) return;
      $scope.tab = idx;
      $ionicTabsDelegate.select(idx);
    }

    $scope.travelDateFormat = 'dd MMMM yyyy';
    $scope.travelTimeFormat = 'HH:mm';

    $scope.driverTrips = null;
    $scope.passengerTrips = null;

    $scope.initParticipated = function () {
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

    $scope.selectParticipatedTrip = function (index) {
        $state.go('app.viaggio', {
            'travelId': $scope.passengerTrips[index].id
        });
    };

    $scope.initOffered = function () {
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

    $scope.initParticipated();

    $scope.selectOfferedTrip = function (index) {
        $state.go('app.viaggio', {
            'travelId': $scope.driverTrips[index].id
        });
    };
});
