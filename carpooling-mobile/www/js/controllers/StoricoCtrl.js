angular.module('carpooling.controllers.storico', [])

.controller('StoricoCtrl', function ($scope, $state, Config, StorageSrv, DriverSrv, Utils, UserSrv, PassengerSrv, $ionicTabsDelegate) {

    $scope.tab = 0;

    $scope.selectTab = function (idx) {
        //if (idx == $scope.tab) return;
        if (idx !== $scope.tab) {
            $scope.tab = idx;
            $ionicTabsDelegate.select(idx);
        }
    }
});
