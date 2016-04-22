angular.module('carpooling.controllers.storico', [])

.controller('StoricoCtrl', function ($scope, $state, Config, CacheSrv, StorageSrv, DriverSrv, Utils, UserSrv, PassengerSrv, $ionicTabsDelegate) {

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

    var enrichTrip = function (trip) {
        trip.style = Utils.getTripStyle(trip);

        // booking counters
        trip.bookingCounters = Utils.getBookingCounters(trip);

        // booking state
        if (trip.userId !== StorageSrv.getUserId()) {
            trip.bookings.forEach(function (booking) {
                if (booking.traveller.userId === StorageSrv.getUserId()) {
                    // my booking
                    trip.bookingState = booking.accepted;
                }
            });
        }
    };

    var errorGettingPassengerTrips = function (error) {
        if (passengerTripsStart === 0) {
            Utils.loaded();
        } else {
            $scope.$broadcast('scroll.infiniteScrollComplete');
        }

        if (error !== Config.LOGIN_EXPIRED) {
            Utils.toast(Utils.getErrorMsg(error));
        }

        if ($scope.passengerTrips === null) {
            $scope.passengerTrips = [];
        }
    };


    /*
     * Partecipo
     */
    var passengerTripsStart = 0;
    var passengerTripsCount = 20; // default
    $scope.passengerTripsCanHaveMore = false;

    $scope.loadMorePassengerTrips = function (reset) {
        if (passengerTripsStart === 0) {
            Utils.loading();
        }

        if (reset) {
            $scope.passengerTrips = null;
        }

        // read past trips
        PassengerSrv.getPassengerTrips(passengerTripsStart, passengerTripsCount, false, false).then(
            function (trips) {
                CacheSrv.setReloadStoricoPassengerTrips(false);

                trips.forEach(function (trip) {
                    enrichTrip(trip);
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
            errorGettingPassengerTrips
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

    $scope.loadMoreDriverTrips = function (reset) {
        if (driverTripsStart === 0) {
            Utils.loading();
        }

        if (reset) {
            $scope.driverTrips = null;
        }

        DriverSrv.getDriverTrips(driverTripsStart, driverTripsCount, false).then(
            function (trips) {
                CacheSrv.setReloadStoricoDriverTrips(false);

                trips.forEach(function (trip) {
                    enrichTrip(trip);
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
                    Utils.toast(Utils.getErrorMsg(error));
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
    $scope.$on('$ionicView.enter', function () {
        if ($scope.tab === 0) {
            if (CacheSrv.reloadStoricoPassengerTrips()) {
                $scope.loadMorePassengerTrips(true);
            }
        } else if ($scope.tab === 1) {
            if (CacheSrv.reloadStoricoDriverTrips()) {
                $scope.loadMoreDriverTrips(true);
            } else if (!!CacheSrv.reloadStoricoDriverTrip()) {
                var tripId = CacheSrv.reloadStoricoDriverTrip();
                PassengerSrv.getTrip(tripId).then(
                    function (updatedTrip) {
                        CacheSrv.setReloadStoricoDriverTrip(null);
                        for (var i = 0; i < $scope.driverTrips.length; i++) {
                            if ($scope.driverTrips[i].id === updatedTrip.id) {
                                $scope.driverTrips[i] = updatedTrip;
                                enrichTrip($scope.driverTrips[i]);
                                i = $scope.driverTrips.length;
                            }
                        }
                        Utils.loaded();
                    },
                    function (error) {
                        CacheSrv.setReloadStoricoDriverTrip(null);
                        Utils.loaded();
                        $scope.loadMoreDriverTrips(true);
                    }
                );
            }
        }
    });
});
