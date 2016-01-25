angular.module('carpooling.controllers.home', [])

.controller('AppCtrl', function ($scope, $state) {})

.controller('HomeCtrl', function ($scope, $state, $filter, $ionicPopup, Config, StorageSrv, DriverSrv, Utils, UserSrv, PassengerSrv, $ionicTabsDelegate) {

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
    $scope.nonConfirmedTrips = null;
    $scope.driverTrips = null;

    var err = function (error) {
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
            };


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
        // read future trips
        PassengerSrv.getPassengerTrips(passengerTripsStart, passengerTripsCount, true).then(
            function (trips) {
                $scope.passengerTrips = !!$scope.passengerTrips ? $scope.passengerTrips.concat(trips) : trips;

                if (trips.length === passengerTripsCount) {
                    $scope.passengerTripsCanHaveMore = true;
                    passengerTripsStart++;
                } else {
                    $scope.passengerTripsCanHaveMore = false;
                }

                if (passengerTripsStart == 0) {
                  // read trips to confirm
                  PassengerSrv.getPassengerTrips(0, 100, false, true).then(function(toConfirm) {
                    $scope.nonConfirmedTrips = toConfirm;
                    if (passengerTripsStart === 0) {
                        Utils.loaded();
                    } else {
                        $scope.$broadcast('scroll.infiniteScrollComplete');
                    }
                  }, err);
                } else {
                  if (passengerTripsStart === 0) {
                      Utils.loaded();
                  } else {
                      $scope.$broadcast('scroll.infiniteScrollComplete');
                  }
                }
            },
          err
        );
    };

    var doConfirm = function($index, confirm) {
        Utils.loading();
        PassengerSrv.confirmTrip($scope.nonConfirmedTrips[$index].id, confirm).then(function() {
          Utils.loaded();
          $scope.nonConfirmedTrips.splice($index,1);
        }, function() {
          Utils.loaded();
          Utils.toast();
        });
    }
    $scope.confirmDialog = function($index) {
      var confirmPopup = $ionicPopup.show({
        title: $filter('translate')('popup_confirm_boarding'),
        template: $filter('translate')('popup_confirm_boarding_body'),
        buttons: [
            {
                text: $filter('translate')('cancel'),
                //type: 'button-stable',
                onTap: function (event) {

                }
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
            },

        ]
      });
    }

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

    $scope.loadMoreDriverTrips = function () {
        if (driverTripsStart === 0) {
            Utils.loading();
        }

        DriverSrv.getDriverTrips(driverTripsStart, driverTripsCount, true).then(
            function (trips) {
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
