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
});
