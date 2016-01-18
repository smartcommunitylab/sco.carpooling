angular.module('carpooling.controllers.communityinfo', [])

.controller('CommunityInfoCtrl', function ($scope, $rootScope, $state, $stateParams, $ionicTabsDelegate, UserSrv, Utils, StorageSrv) {
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

    $scope.communityTrips = null;
    $scope.communityStyle = null;
    $scope.community = {};

    $scope.hasAuto = !!StorageSrv.getUser().auto;

    var init = function () {
        Utils.loading();
        UserSrv.getCommunityDetails($stateParams.community.id).then(
            function (community) {
                $scope.community = community;
                UserSrv.getCommunityTravels($scope.community.id, Date.now()).then(
                    function (todayCommunityTrips) {
                        $scope.communityTrips = todayCommunityTrips;
                        $scope.communityTrips.forEach(function (trip) {
                            trip.bookingCounters = Utils.getBookingCounters(trip);
                        });
                        Utils.loaded();
                    },
                    function (error) {
                        Utils.loaded();
                        Utils.toast();
                    }
                );
                $scope.communityStyle = {
                    'border-color': '#' + $scope.community.color + ' #' + $scope.community.color + ' transparent transparent'
                }
            },
            function (error) {
                Utils.loaded();
                Utils.toast();
            }
        );
    };

    init();

    $scope.changeAutoState = function () {
        $state.go('app.profilo', {
            'communityFrom': $scope.community,
            'editMode': true
        });
    };

    $scope.showUser = function (index) {
        var user = $scope.community.userObjs[index];
        var params = {};
        if (user.userId !== StorageSrv.getUser().userId) {
            // it's-a-not-me
            params.user = user;
        }
        $state.go('app.profilo', params);
    };

    $scope.selectTrip = function (index) {
        $state.go('app.viaggio', {
            'travelId': $scope.communityTrips[index].id
        });
    };

    $scope.addTrip = function () {
        $state.go('app.offri', {
            'communityId': $scope.community.id
        });
    }
});
