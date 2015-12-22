angular.module('carpooling.controllers.communityinfo', [])

.controller('CommunityInfoCtrl', function ($scope, $rootScope, $state, $stateParams, UserSrv, Utils) {
    $scope.community = $stateParams['community'];

    if (!!$scope.community) {
        Utils.loading();

        UserSrv.getCommunityTravels($scope.community.id, Date.now()).then(
            function (todayCommunities) {
                $scope.communityList = todayCommunities;
                $scope.communityList.forEach(function (travel) {
                    travel.bookingCounters = Utils.getBookingCounters(travel);
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
        if (user.userId == StorageSrv.getUser().userId) {
            $state.go('app.profilo.userinfo');
        } else {
            $state.go('app.profilo.userinfo', {
                'user': user
            });
        }
    };
})

.controller('CommTripCtrl', function ($scope, $rootScope, $state, $stateParams, Utils, UserSrv) {
    $scope.selectTrip = function (index) {
        $state.go('app.viaggio', {
            'travelId': $scope.communityList[index].id
        });
    };

    $scope.addTrip = function () {
        $state.go('app.offri');
    }
});
