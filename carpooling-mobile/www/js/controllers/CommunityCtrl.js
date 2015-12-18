angular.module('carpooling.controllers.communities', [])

.controller('CommunityCtrl', function ($scope, $rootScope, $state, StorageSrv, UserSrv, Utils) {
    $scope.communities = null;
    Utils.loading();

    UserSrv.getCommunitiesDetails().then(function (communities) {
            Utils.loaded();
            $scope.communities = communities;
            console.log($scope.communities);
        },
        function (error) {
            Utils.loaded();
            Utils.toast();
            $scope.communities = [];
        }
    );

    $scope.selectCommunity = function (index) {
        var selectResults = $scope.communities[index];
        $state.go('app.comunitainfo', {
            'community': selectResults
        });
    };

    $scope.searchCommunity = function () {
        var myCommunities = $scope.communities;
        $state.go('app.cercacomunita', {
            'myCommunities': myCommunities
        });
    };
})

.controller('FindCommunityCtrl', function ($scope, $rootScope, $state, StorageSrv, UserSrv, Utils, $stateParams) {
    $scope.communitiesToFilter = $stateParams['myCommunities'];
    var filteredCommunities = [];
    /*$scope.findCommunity = function () {
        for (var i = 0; i < $scope.communitiesToFilter.length; i++) {
            if ($scope.communitiesToFilter[i].name == || ) {

            }
        }
    }*/
});
