angular.module('carpooling.controllers.communityinfo', [])

.controller('CommunityInfoCtrl', function ($scope, $rootScope, $state, $stateParams) {
    $scope.community = $stateParams['community'];
    console.log($scope.community);
})

.controller('CommComponentsCtrl', function ($scope, $rootScope, $state, $stateParams, StorageSrv) {
    console.log('CommComponentsCtrl');
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

.controller('CommTripCtrl', function ($scope, $rootScope, $state, $stateParams) {
    console.log('CommTripCtrl');
})

.controller('CommInfoCtrl', function ($scope, $rootScope, $state, $stateParams, StorageSrv) {
    console.log('CommInfoCtrl');
    var haveAuto = !!StorageSrv.getUser().auto;
    if (!!haveAuto) {
        $scope.btnAutoText = 'lbl_editauto';
    } else {
        $scope.btnAutoText = 'lbl_addauto';
    }
    $scope.changeAutoState = function () {
        $state.go('app.profilo.userinfo', {
            'communityFrom': $scope.community,
            'editMode': true
        });
    };
});
