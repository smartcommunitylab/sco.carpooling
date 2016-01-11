angular.module('carpooling.controllers.viaggio', [])

.controller('ViaggioCtrl', function ($scope, $rootScope, $state, $stateParams, $ionicPopup, $ionicActionSheet, MapSrv, Config, $filter, UserSrv, Utils, StorageSrv, PassengerSrv, DriverSrv) {
    $scope.travelDateFormat = 'dd MMMM yyyy';
    $scope.travelTimeFormat = 'HH:mm';

    $scope.isMine = null;
    $scope.driverInfo = {};

    $scope.selectedTrip = {};
    $scope.travelId = null;
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
        $scope.mainCommunity = mainCommunity();

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
            $scope.initMap();
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
     * Rating
     */
    $scope.rate = function (booking) {
        $scope.ratingMax = Config.getRatingMax();
        $scope.rating = 0;
        $scope.setRating = function (rating) {
            $scope.rating = rating;
        };

        var rateUserParams = {
            username: $scope.isMine ? booking.traveller.name + ' ' + booking.traveller.surname : $scope.driverInfo.name + ' ' + $scope.driverInfo.surname,
            role: $scope.isMine ? $filter('translate')('lbl_passenger') : $filter('translate')('lbl_driver')
        };

        // TODO show popup, choose rating and send it
        var ratingPopup = $ionicPopup.confirm({
            scope: $scope,
            title: $filter('translate')('lbl_rate_user', rateUserParams),
            templateUrl: 'templates/popup_rate.html',
            cancelText: $filter('translate')('cancel'),
            okText: $filter('translate')('action_rate'),
            okType: 'button-carpooling'
        });

        ratingPopup.then(
            function (action) {
                if (action === true) {
                    // OK
                    if ($scope.rating > 0) {
                        var success = function () {
                            // TODO rating successful feedback
                            ratingPopup.close();
                            Utils.toast('Rated!');
                        };

                        var failure = function (error) {
                            // TODO handle rating error
                            ratingPopup.close();
                            Utils.toast(error);
                        };

                        if ($scope.isMine) {
                            // Driver
                            DriverSrv.rateDriver($scope.driverInfo.userId, $scope.rating, booking).then(success, failure);
                        } else {
                            // Passenger
                            PassengerSrv.rateDriver(booking.traveller.userId, $scope.rating, booking).then(success, failure);
                        }
                    } else {
                        // TODO handle 'Vote' with value 0
                        Utils.toast('ZERO?!?');
                    }
                } else {
                    // cancel
                    ratingPopup.close();
                }
            }
        );
    };

    /*
     * Driver
     */
    $scope.reject = function (booking) {
        Utils.loading();
        var newBooking = angular.copy(booking);
        newBooking['accepted'] = -1;

        DriverSrv.decideTrip($scope.selectedTrip.id, newBooking).then(
            function (data) {
                Utils.loaded();
                refreshTrip(data);
                Utils.toast(($filter('translate')('toast_booking_rejected')));
            },
            function (error) {
                Utils.loaded();
                Utils.toast();
            }
        );
    };

    $scope.accept = function (booking) {
        Utils.loading();
        var newBooking = angular.copy(booking);
        newBooking['accepted'] = 1;

        DriverSrv.decideTrip($scope.selectedTrip.id, newBooking).then(
            function (data) {
                Utils.loaded();
                refreshTrip(data);
                Utils.toast(($filter('translate')('toast_booking_accepted')));
            },
            function (error) {
                Utils.loaded();
                Utils.toast();
            }
        );
    };

    $scope.chat = function (booking) {
        $state.go('app.chat', {
            travelId: $scope.travelId,
            personId: booking.traveller.userId
        });
    };

    $scope.showActions = function (booking) {
        $ionicActionSheet.show({
            titleText: booking.traveller.name + ' ' + booking.traveller.surname,
            buttons: [
                {
                    text: '<i class="icon ion-chatboxes"></i> ' + $filter('translate')('action_chat')
                },
                {
                    text: '<i class="icon ion-star"></i> ' + $filter('translate')('action_rate_passenger')
                }
            ],
            buttonClicked: function (index) {
                switch (index) {
                    case 0:
                        $scope.chat(booking);
                        return true;
                        break;
                    case 1:
                        $scope.rate(booking);
                        return true;
                        break;
                    default:
                        return false;
                }
            },
            destructiveText: '<i class="icon ion-close-round"></i> ' + $filter('translate')('action_reject'),
            destructiveButtonClicked: function () {
                $scope.reject(booking);
            },
            cancelText: $filter('translate')('cancel')
        });
    };

    $scope.positiveAction = function (booking) {
        // NOTE accept booking or show all the actions
        if (booking['accepted'] === 0) {
            $scope.accept(booking);
        } else if (booking.accepted === 1) {
            //$scope.chat(booking);
            $scope.showActions(booking);
        }
    };

    /*
     * Passenger
     */
    $scope.book = function () {
        Utils.loading();
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
            // FUTURE handle recurrency ('recurrent' and 'date' field, TBD)
        } else {
            booking.recurrent = false;
            booking.date = new Date($scope.selectedTrip.when);
        }

        PassengerSrv.bookTrip($scope.travelId, booking).then(
            function (updatedTrip) {
                Utils.loaded();
                refreshTrip(updatedTrip);
            },
            function (error) {
                Utils.loaded();
                Utils.toast();
            }
        );
    };

    $scope.bookingAction = function () {
        if ($scope.bookingState === null) {
            $scope.book();
        } else if ($scope.bookingState === 0) {
            // FUTURE the passenger can cancel the travelRequest
        }
    };

    $scope.chatWithDriver = function () {
        $state.go('app.chat', {
            travelId: $scope.travelId,
            personId: $scope.selectedTrip.userId
        });
    };

    /*
     * Map stuff
     */
    $scope.initMap = function () {
        //if ($scope.isMine === false) {
        MapSrv.initMap('tripMap').then(function () {
            //add polyline
        });
        //}
    };

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

    /* Show User */
    $scope.showUser = function (index) {
        $state.go('app.profilo', {
            'user': $scope.driverInfo
        });
    };

    var mainCommunity = function() {
        if (!!$scope.selectedTrip.communityIds && $scope.selectedTrip.communityIds.length === 1) {
            return StorageSrv.getCommunityById($scope.selectedTrip.communityIds[0]);
        }
        return null;
    };
    $scope.showCommunity = function() {
        $state.go('app.comunitainfo', {
            'communityId': $scope.mainCommunity.id
        });
    }
});
