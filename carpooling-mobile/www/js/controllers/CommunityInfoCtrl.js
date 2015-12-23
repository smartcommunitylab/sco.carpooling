angular.module('carpooling.controllers.communityinfo', [])

.controller('CommunityInfoCtrl', function ($scope, $rootScope, $state, $stateParams, UserSrv, Utils) {
    $scope.community = $stateParams['community'];
    $scope.communityTrips = null;

    if (!!$scope.community) {
        Utils.loading();

        UserSrv.getCommunityTravels($scope.community.id, Date.now()).then(
            function (todayCommunityTrips) {
                $scope.communityTrips = todayCommunityTrips;
                $scope.communityTrips.forEach(function (trip) {
                    travel.bookingCounters = Utils.getBookingCounters(trip);
                });
                Utils.loaded();
            },
            function (error) {
                Utils.loaded();
                Utils.toast();
            }
        );
    } else {
        Utils.toast();
    }
})

.controller('CommInfoCtrl', function ($scope, $rootScope, $state, $stateParams, StorageSrv) {
    var hasAuto = !!StorageSrv.getUser().auto;

    $scope.changeAutoState = function () {
        $state.go('app.profilo.userinfo', {
            'communityFrom': $scope.community,
            'editMode': true
        });
    };
})

.controller('CommComponentsCtrl', function ($scope, $rootScope, $state, $stateParams, StorageSrv) {
    $scope.showUser = function (index) {
        var user = $scope.community.userObjs[index];
        var params = {};
        if (user.userId !== StorageSrv.getUser().userId) {
            // it's-a-not-me
            params.user = user;
        }
        $state.go('app.profilo.userinfo', params);
    };
})

.controller('CommTripCtrl', function ($scope, $rootScope, $state, $stateParams, Utils, UserSrv) {
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
