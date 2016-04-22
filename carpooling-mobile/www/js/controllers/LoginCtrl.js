angular.module('carpooling.controllers.login', [])

.controller('LoginCtrl', function ($scope, $rootScope, $state, $ionicHistory, Utils, LoginSrv, UserSrv, StorageSrv) {
    var loginStarted = false;

    $scope.login = function (provider) {
        if (loginStarted) {
            return;
        };

        loginStarted = true;
        LoginSrv.login(provider).then(
            function (data) {
                loginStarted = false;
                UserSrv.getUser(data.userId).then(function () {
                    $rootScope.pushRegistration(data.userId);
                    $ionicHistory.nextViewOptions({
                        historyRoot: true,
                        disableBack: true
                    });

                    if (StorageSrv.getUserId() != null && !StorageSrv.isProfileComplete()) {
                        $rootScope.initialSetup = true;
                        $state.go('app.profilo');
                    } else {
                        $rootScope.initialSetup = false;
                        $state.go('app.home');
                    }
                });
            },
            function (error) {
                loginStarted = false;
                Utils.toast(Utils.getErrorMsg(error));
                StorageSrv.saveUser(null);
                ionic.Platform.exitApp();
            }
        );
    };
});
