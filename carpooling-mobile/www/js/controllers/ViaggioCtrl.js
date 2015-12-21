angular.module('carpooling.controllers.viaggio', [])

.controller('ViaggioCtrl', function ($scope, $rootScope, $state, $stateParams, MapSrv, Config, $filter, UserSrv, Utils, StorageSrv, PassengerSrv, DriverSrv) {
    $scope.travelDateFormat = 'dd MMMM yyyy';
    $scope.travelTimeFormat = 'HH:mm';
    $scope.driverInfo = {};

    $scope.selectedTrip = {};
    $scope.bookingCounters = {};

    // -1 rejected, 0 requested, 1 accepted
    $scope.bookingState = null;

    var addPathToMap = function (selectedTrip) {
        $scope.pathLine = MapSrv.getTripPolyline(selectedTrip.route);
        $scope.markers = {
            start: {
                lat: Number(selectedTrip.route.from.lat),
                lng: Number(selectedTrip.route.from.lon),
                icon: {
                    iconUrl: 'img/ic_start.png',
                    iconSize: [18, 25],
                    iconAnchor: [9, 25]
                }
            },
            stop: {
                lat: Number(selectedTrip.route.to.lat),
                lng: Number(selectedTrip.route.to.lon),
                icon: {
                    iconUrl: 'img/ic_flag.png',
                    iconSize: [18, 25],
                    iconAnchor: [0, 25]
                }
            }
        }

        var boundsArray = [];
        var boundstart = [$scope.markers.start.lat, $scope.markers.start.lng];
        boundsArray.push(boundstart);
        var boundstop = [$scope.markers.stop.lat, $scope.markers.stop.lng];
        boundsArray.push(boundstop);

        if (boundsArray.length > 0) {
            var bounds = L.latLngBounds(boundsArray);
            MapSrv.getMap('tripMap').then(function (map) {
                map.fitBounds(bounds);
            });
        }
    }
    var refreshTrip = function (trip) {
        $scope.selectedTrip = trip;

        if (!!$scope.selectedTrip) {
            addPathToMap($scope.selectedTrip);
        }

        $scope.isMine = $scope.selectedTrip.userId === StorageSrv.getUserId();
        $scope.bookingCounters = Utils.getBookingCounters($scope.selectedTrip);
        $scope.dowString = Utils.getRecurrencyString($scope.selectedTrip);

        if (!$scope.isMine) {
            $scope.selectedTrip.bookings.forEach(function (booking) {
                if (booking.traveller.userId === StorageSrv.getUserId()) {
                    // my booking
                    $scope.bookingState = booking.accepted;
                }
            });

            UserSrv.getUser($scope.selectedTrip.userId).then(
                function (userInfo) {
                    $scope.driverInfo = userInfo;
                    Utils.loaded();
                },
                function (error) {
                    Utils.loaded();
                    console.error(error);
                    Utils.toast();
                }
            );
        } else {
            Utils.loaded();
        }
    };

    var init = function () {
        Utils.loading();

        $scope.travelId = $stateParams.travelId;

        PassengerSrv.getTrip($scope.travelId).then(
            function (data) {
                refreshTrip(data);
            },
            function (error) {
                Utils.loaded();
                Utils.toast();
            }
        );
    };

    init();

    /*
     * Driver
     */
    $scope.reject = function (booking) {
        var newBooking = angular.copy(booking);
        newBooking['accepted'] = -1;
        DriverSrv.decideTrip($scope.selectedTrip.id, newBooking).then(
            function (data) {
                refreshTrip(data);
                Utils.toast(($filter('translate')('toast_booking_rejected')));
            },
            function (error) {
                Utils.toast();
            }
        );
    };

    $scope.positiveAction = function (booking) {
        // NOTE accept booking or go to chat
        var newBooking = angular.copy(booking);

        if (booking['accepted'] === 0) {
            newBooking['accepted'] = 1;
            DriverSrv.decideTrip($scope.selectedTrip.id, newBooking).then(
                function (data) {
                    refreshTrip(data);
                    Utils.toast(($filter('translate')('toast_booking_accepted')));
                },
                function (error) {
                    Utils.toast();
                }
            );
        } else if (booking.accepted === 1) {
            // TODO go to chat
            console.log('go to chat');
        }
    };

    /*
     * Passenger
     */
    $scope.book = function () {
        var me = StorageSrv.getUser();

        var booking = {
            traveller: {
                userId: me.userId,
                name: me.name,
                surname: me.surname,
                email: me.email
            }
        };

        if ($rootScope.isRecurrencyEnabled()) {
            // TODO handle recurrency ('recurrent' and 'date' field, TBD)
        } else {
            booking.recurrent = false;
            booking.date = new Date($scope.selectedTrip.when);
        }

        PassengerSrv.bookTrip($scope.travelId, booking).then(
            function (updatedTrip) {
                refreshTrip(updatedTrip);
            },
            function (error) {
                Utils.toast();
            }
        );
    };

    $scope.bookingAction = function () {
        if ($scope.bookingState === null) {
            $scope.book();
        } else if ($scope.bookingState === -1) {
            // TODO rejected
            console.log('rejected');
        } else if ($scope.bookingState === 0) {
            // TODO requested
            console.log('requested');
        } else if ($scope.bookingState === 1) {
            // TODO accepted
            console.log('accepted');
        }
    };

    $scope.initMap = function () {
        MapSrv.initMap('tripMap').then(function () {
            //add polyline

        })
    }




    angular.extend($scope, {
        center: {
            lat: Config.getLat(),
            lng: Config.getLon(),
            zoom: Config.getZoom()
        },
        events: {},
        markers: {},
        pathLine: {}
    });
});
