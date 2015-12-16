angular.module('carpooling.controllers.communityinfo', [])

.controller('CommunityInfoCtrl', function ($scope, $rootScope, $state, $stateParams) {
    $scope.selCommunity = $stateParams['selectResults'];
    console.log($scope.selCommunity);
})

.controller('CommComponentsCtrl', function ($scope, $rootScope, $state, $stateParams) {
    console.log('CommComponentsCtrl');
    /*$scope.showUser = function (index) {
        var selectUser = $scope.selCommunity.[index];
        $state.go('app.profilo.userinfo', {
            'selectResults': selectUser
        });
    };*/
})

.controller('CommTripCtrl', function ($scope, $rootScope, $state, $stateParams) {
    console.log('CommTripCtrl');
})

.controller('CommInfoCtrl', function ($scope, $rootScope, $state, $stateParams) {
    console.log('CommInfoCtrl');
});
