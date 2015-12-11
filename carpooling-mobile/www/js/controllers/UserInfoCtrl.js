angular.module('carpooling.controllers.user', [])

.controller('UserInfoCtrl', function ($scope, $rootScope, $state, StorageSrv, UserSrv, Utils) {
    $scope.user = angular.copy(StorageSrv.getUser());

    $rootScope.initialSetup = !StorageSrv.isProfileComplete();

    var hasAuto = function (auto) {
        // return !!$scope.user.auto
        if (!!auto && !!auto.description && auto.posts !== 0) {
            return true;
        }
        return false;
    };

    $scope.editMode = false || $rootScope.initialSetup;
    $scope.edit = {
        hasAuto: $rootScope.initialSetup ? false : hasAuto($scope.user.auto),
        postsAvailable: [1, 2, 3, 4, 5, 6, 7]
    };

    $scope.toggleEditMode = function () {
        $scope.editMode = !$scope.editMode;
    };

    $scope.cancelChanges = function () {
        $scope.toggleEditMode();
        $scope.user = angular.copy(StorageSrv.getUser());
        $scope.edit.hasAuto = hasAuto($scope.user.auto);
    };

    $scope.saveProfile = function () {
        Utils.loading();
        // UserSrv.saveAuto(!!$scope.user.auto ? $scope.user.auto : {}).then(
        UserSrv.saveAuto($scope.user.auto).then(
            function (data) {
                if ($rootScope.initialSetup) {
                    Utils.loaded();
                    StorageSrv.setProfileComplete();
                    $rootScope.initialSetup = false;
                    $state.go('app.home');
                } else {
                    Utils.loaded();
                    $scope.toggleEditMode();
                    UserSrv.getUser($scope.user.userId).then(
                        function () {
                            $scope.user = angular.copy(StorageSrv.getUser());
                            $scope.edit.hasAuto = hasAuto($scope.user.auto);
                        }
                    );
                }
            },
            function (error) {
                Utils.loaded();
                // TODO: handle saveAuto error
            }
        );
    };

    $scope.$watch('edit.hasAuto', function (newValue, oldValue) {
        if (newValue === oldValue) {
            return;
        }

        if (newValue) {
            // true
            $scope.user.auto = {
                posts: 4,
                description: '...'
            };
        } else {
            // false
            $scope.user.auto = null;
        }
    });
});
