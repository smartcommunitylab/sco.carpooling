angular.module('carpooling.controllers.communities', [])

.controller('CommunityCtrl', function ($scope, $rootScope, $state, StorageSrv, UserSrv, Utils) {
    $scope.communities = angular.copy(StorageSrv.getCommunities());
    console.log($scope.communities);
    /*TODO: get the number of people of each community, the number of autos for each community, the zone of each community*/

    $scope.selectCommunity = function (index) {
        var selectResults = $scope.communities[index];
        $state.go('app.comunitainfo', {
            'selectResults': selectResults
        });
        /*TODO: give to app.comunitainfo the selectetResults (which contain the infos of the community I have selected) after the $state.go*/
    };

})

.controller('CommunityInfoCtrl', function ($scope, $rootScope, $state, $stateParams) {
    $scope.selCommunity = $stateParams['selectResults'];
    console.log($scope.selCommunity);
});
