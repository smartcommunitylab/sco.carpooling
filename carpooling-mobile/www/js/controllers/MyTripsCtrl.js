angular.module('carpooling.controllers.mytrips', [])

.controller('MyTripsCtrl', function ($scope, $rootScope, $state, $stateParams, $filter, $interval, $ionicPopup, Config, CacheSrv, StorageSrv, DriverSrv, Utils, UserSrv, PassengerSrv, $ionicTabsDelegate) {
    $scope.tab = 0;

    $scope.selectTab = function (idx) {
        //if (idx == $scope.tab) return;
        if (idx !== $scope.tab) {
            $scope.tab = idx;
            $ionicTabsDelegate.$getByHandle('tabs-mytrips').select(idx);
        }
    }

    $scope.travelDateFormat = 'dd MMMM yyyy';
    $scope.travelTimeFormat = 'HH:mm';

    $scope.nonConfirmedTrips = CacheSrv.getNonConfirmedTrips();
    $scope.passengerTrips = CacheSrv.getPassengerTrips();
    $scope.driverTrips = CacheSrv.getDriverTrips();

    /*
     * Partecipo
     */
    var passengerTripsStart = 0;
    var passengerTripsCount = 20; // default
    $scope.passengerTripsCanHaveMore = false;

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
            CacheSrv.setPassengerTrips($scope.passengerTrips);
        }
    };

    $scope.loadMorePassengerTrips = function (reset) {
        if (passengerTripsStart === 0) {
            Utils.loading();
        }

        if (reset) {
            $scope.passengerTrips = null;
            CacheSrv.setPassengerTrips($scope.passengerTrips);
            passengerTripsStart = 0;
            passengerTripsCount = 20; // default
            $scope.passengerTripsCanHaveMore = false;
        }

        // read trips to confirm
        // FUTURE use CacheSrv for these trips too?
        PassengerSrv.getPassengerTrips(0, 100, false, true).then(
            function (toConfirm) {
                $scope.nonConfirmedTrips = toConfirm;
                CacheSrv.setNonConfirmedTrips($scope.nonConfirmedTrips);
                if (passengerTripsStart === 0) {
                    Utils.loaded();
                } else {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                }
            },
            errorGettingPassengerTrips
        );

        // read future trips
        PassengerSrv.getPassengerTrips(passengerTripsStart, passengerTripsCount, true).then(
            function (trips) {
                CacheSrv.setReloadPassengerTrips(false);

                trips.forEach(function (trip) {
                    enrichTrip(trip);
                });

                if (passengerTripsStart === 0) {
                    Utils.loaded();
                } else {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                }

                $scope.passengerTrips = !!$scope.passengerTrips ? $scope.passengerTrips.concat(trips) : trips;
                CacheSrv.setPassengerTrips($scope.passengerTrips);

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

    var doConfirm = function ($index, confirm) {
        Utils.loading();
        PassengerSrv.confirmTrip($scope.nonConfirmedTrips[$index].id, confirm).then(function () {
            Utils.loaded();
            $scope.nonConfirmedTrips.splice($index, 1);
            CacheSrv.setNonConfirmedTrips($scope.nonConfirmedTrips);
        }, function (error) {
            Utils.loaded();
            Utils.toast(Utils.getErrorMsg(error));
        });
    };

    $scope.confirmDialog = function ($index) {
        var confirmPopup = $ionicPopup.show({
            title: $filter('translate')('popup_confirm_boarding'),
            template: $filter('translate')('popup_confirm_boarding_body'),
            buttons: [
                {
                    text: $filter('translate')('cancel'),
                    //type: 'button-stable',
                    onTap: function (event) {}
                },
                {
                    text: $filter('translate')('no'),
                    type: 'button-carpooling',
                    onTap: function (event) {
                        doConfirm($index, false);
                    }
                },
                {
                    text: $filter('translate')('yes'),
                    type: 'button-carpooling',
                    onTap: function (event) {
                        doConfirm($index, true);
                    }
                }
            ]
        });
    };

    $scope.selectParticipatedTrip = function (index, coll) {
        $state.go('app.viaggio', {
            'travelId': coll[index].id
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
            CacheSrv.setDriverTrips($scope.driverTrips);
            driverTripsStart = 0;
            driverTripsCount = 20;
            $scope.driverTripsCanHaveMore = false;
        }

        DriverSrv.getDriverTrips(driverTripsStart, driverTripsCount, true).then(
            function (trips) {
                CacheSrv.setReloadDriverTrips(false);

                trips.forEach(function (trip) {
                    enrichTrip(trip);
                });

                if (driverTripsStart === 0) {
                    Utils.loaded();
                } else {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                }

                $scope.driverTrips = !!$scope.driverTrips ? $scope.driverTrips.concat(trips) : trips;
                CacheSrv.setDriverTrips($scope.driverTrips);

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

                if (CacheSrv.driverTrips === null) {
                    $scope.driverTrips = [];
                    CacheSrv.setDriverTrips($scope.driverTrips);
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
        if (!!$stateParams && $stateParams['type'] == 'driver') {
            $scope.selectTab(1);
        }

        if (!window.ParsePushPlugin) {
            $scope.interval = $interval($rootScope.initCounter, 10000);
        }

        if ($scope.tab === 0) {
            if (CacheSrv.reloadPassengerTrips()) {
                $scope.loadMorePassengerTrips(true);
            }
        } else if ($scope.tab === 1) {
            if (CacheSrv.reloadDriverTrips()) {
                $scope.loadMoreDriverTrips(true);
            } else if (!!CacheSrv.reloadDriverTrip()) {
                var tripId = CacheSrv.reloadDriverTrip();
                PassengerSrv.getTrip(tripId).then(
                    function (updatedTrip) {
                        CacheSrv.setReloadDriverTrip(null);
                        for (var i = 0; i < $scope.driverTrips.length; i++) {
                            if ($scope.driverTrips[i].id === updatedTrip.id) {
                                $scope.driverTrips[i] = updatedTrip;
                                enrichTrip($scope.driverTrips[i]);
                                CacheSrv.setDriverTrips($scope.driverTrips);
                                i = CacheSrv.driverTrips.length;
                            }
                        }
                        Utils.loaded();
                    },
                    function (error) {
                        CacheSrv.setReloadDriverTrip(null);
                        Utils.loaded();
                        $scope.loadMoreDriverTrips(true);
                    }
                );
            }
        }
    });

    /*
     * exit
     */
    $scope.$on('$ionicView.leave', function () {
        if ($scope.interval) $interval.cancel($scope.interval);
    });

});
