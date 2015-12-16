angular.module('carpooling.controllers.communities', [])

.controller('CommunityCtrl', function ($scope, $rootScope, $state, StorageSrv, UserSrv, Utils) {

    $scope.communities = null;
    Utils.loading();
    UserSrv.getCommunitiesDetails().then(function (data) {
            Utils.loaded();
            $scope.communities = data.data;
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
            'selectResults': selectResults
        });
    };

})

.controller('CommunityInfoCtrl', function ($scope, $rootScope, $state, $stateParams) {
    $scope.selCommunity = $stateParams['selectResults'];
    console.log($scope.selCommunity);
});
