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

  $scope.loginInternal = function() {
    $state.go('app.signin');
  }
  $scope.goRegister = function() {
    $state.go('app.signup');
  }
})

.controller('RegisterCtrl', function ($scope, $rootScope, $state, $filter, $ionicHistory, $ionicPopup, Utils, LoginSrv, UserSrv, Config, StorageSrv, $translate) {
    $scope.user = {
      lang : $translate.preferredLanguage(),
      name: '',
      surname: '',
      email: '',
      password: ''
    };

    var validate = function() {
      if (!$scope.user.name.trim() || !$scope.user.surname.trim() || !$scope.user.email.trim() || !$scope.user.password.trim()) {
        return 'error_required_fields';
      }
      if ($scope.user.password.trim().length < 6) {
        return 'error_password_short';
      }
      return null;
    };

    $scope.resend = function () {
      window.open(Config.getAACURL()+'/internal/resend', '_blank', 'location=no,toolbar=no')
    }


    $scope.register = function () {
        var msg = validate();
        if (msg) {
          $ionicPopup.alert({
            title: $filter('translate')('error_popup_title'),
            template: $filter('translate')(msg)
          });
          return;
        }

        Utils.loading();
        LoginSrv.register($scope.user).then(
            function (data) {
              $state.go('app.signupsuccess');
            },
            function (error) {
              var errorMsg = 'error_generic';
              if (error == 409) {
                errorMsg = 'error_email_inuse';
              }
              $ionicPopup.alert({
                title: $filter('translate')('error_popup_title'),
                template: $filter('translate')(errorMsg)
              });
            }
        ).finally(Utils.loaded);
    };
})

.controller('SigninCtrl', function ($scope, $rootScope, $state, $filter, $ionicHistory, $ionicPopup, Utils, Config, LoginSrv, UserSrv, StorageSrv, $translate) {
    $scope.user = {
      email: '',
      password: ''
    };

    $scope.passwordRecover = function () {
      window.open(Config.getAACURL()+'/internal/reset', '_blank', 'location=no,toolbar=no')
    }
    $scope.signin = function () {
        Utils.loading();
        LoginSrv.signin($scope.user).then(
            function (data) {
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
              StorageSrv.saveUser(null);
              $ionicPopup.alert({
                title: $filter('translate')('error_popup_title'),
                template: $filter('translate')('error_signin')
              });

            }
        ).finally(Utils.loaded);
    };
})
;
