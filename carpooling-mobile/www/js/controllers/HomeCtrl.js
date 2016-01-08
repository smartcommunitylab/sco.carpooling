angular.module('carpooling.controllers.home', [])

.controller('AppCtrl', function ($scope, $state) {})

.controller('HomeCtrl', function ($scope, $state, Config, StorageSrv, DriverSrv, Utils, UserSrv, PassengerSrv, $ionicTabsDelegate) {

    $scope.tab = 0;

    $scope.selectTab = function (idx) {
        //if (idx == $scope.tab) return;
        if (idx !== $scope.tab) {
            $scope.tab = idx;
            $ionicTabsDelegate.select(idx);
        }
    }

    $scope.travelDateFormat = 'dd MMMM yyyy';
    $scope.travelTimeFormat = 'HH:mm';

    $scope.passengerTrips = null;
    $scope.driverTrips = null;

    /*
     * Partecipo
     */
    var passengerTripsStart = 0;
    var passengerTripsCount = 20; // default
    $scope.passengerTripsCanHaveMore = false;

    $scope.loadMorePassengerTrips = function () {
        if (passengerTripsStart === 0) {
            Utils.loading();
        }

        PassengerSrv.getPassengerTrips(passengerTripsStart, passengerTripsCount).then(
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

                if (passengerTripsStart === 0) {
                    Utils.loaded();
                } else {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                }

                $scope.passengerTrips = !!$scope.passengerTrips ? $scope.passengerTrips.concat(trips) : trips;

                if (trips.length === passengerTripsCount) {
                    $scope.passengerTripsCanHaveMore = true;
                    passengerTripsStart++;
                } else {
                    $scope.passengerTripsCanHaveMore = false;
                }
            },
            function (error) {
                if (passengerTripsStart === 0) {
                    Utils.loaded();
                } else {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                }

                if (error !== Config.LOGIN_EXPIRED) {
                    Utils.toast();
                }

                if ($scope.passengerTrips === null) {
                    $scope.passengerTrips = [];
                }
            }
        );
    };

    $scope.selectParticipatedTrip = function (index) {
        $state.go('app.viaggio', {
            'travelId': $scope.passengerTrips[index].id
        });
    };

    /*
     * Offro
     */
    var driverTripsStart = 0;
    var driverTripsCount = 20; // default
    $scope.driverTripsCanHaveMore = false;

    $scope.loadMoreDriverTrips = function () {
        if (driverTripsStart === 0) {
            Utils.loading();
        }

        DriverSrv.getDriverTrips(driverTripsStart, driverTripsCount).then(
            function (trips) {
                trips.forEach(function (trip) {
                    trip.bookingCounters = Utils.getBookingCounters(trip);
                });

                if (driverTripsStart === 0) {
                    Utils.loaded();
                } else {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                }

                $scope.driverTrips = !!$scope.driverTrips ? $scope.driverTrips.concat(trips) : trips;

                if (trips.length === driverTripsCount) {
                    $scope.driverTripsCanHaveMore = true;
                    driverTripsStart++;
                } else {
                    $scope.driverTripsCanHaveMore = false;
                }
            },
            function (error) {
                if (driverTripsStart === 0) {
                    Utils.loaded();
                } else {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                }

                if (error !== Config.LOGIN_EXPIRED) {
                    Utils.toast();
                }

                if ($scope.driverTrips === null) {
                    $scope.driverTrips = [];
                }
            }
        );
    };

    $scope.selectOfferedTrip = function (index) {
        $state.go('app.viaggio', {
            'travelId': $scope.driverTrips[index].id
        });
    };

    /*
     * init
     */
    $scope.loadMorePassengerTrips();
});
