angular.module('carpooling.controllers.user', [])

.controller('UserInfoCtrl', function ($scope, $rootScope, $state, $stateParams, StorageSrv, UserSrv, Utils) {
    if (!!$stateParams['user']) {
        $scope.user = $stateParams['user'];
    } else {
        $scope.user = angular.copy(StorageSrv.getUser());
    }

    $scope.itsMe = $scope.user['userId'] === StorageSrv.getUser()['userId'];

    if(!$scope.user){
        $scope.user = {
            auto: null
        };
    }

    $rootScope.initialSetup = !StorageSrv.isProfileComplete();

    var hasAuto = function (auto) {
        // return !!$scope.user.auto
        if (!!auto && !!auto.description && auto.posts !== 0) {
            return true;
        }
        return false;
    };

    $scope.edit = {
        hasAuto: hasAuto($scope.user.auto),
        postsAvailable: [1, 2, 3, 4, 5, 6, 7]
    };

    $scope.editMode = false || $rootScope.initialSetup;

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
        var auto = $scope.user.auto;
        if (!auto) {
            auto = {
                description: '',
                posts: -1
            };
        }
        // UserSrv.saveAuto(!!$scope.user.auto ? $scope.user.auto : {}).then(
        UserSrv.saveAuto(auto).then(
            function (data) {
                if ($rootScope.initialSetup) {
                    UserSrv.getUser($scope.user.userId).then(
                        function () {
                            Utils.loaded();
                            StorageSrv.setProfileComplete();
                            $rootScope.initialSetup = false;
                            $state.go('app.home');
                        }
                    );
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
                Utils.toast();
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
})
.controller('UserStatsCtrl', function ($scope, $rootScope,DriverSrv,PassengerSrv,Utils,StorageSrv) {
    var getStars = function (vote) {
        var stars = [];

            var fullStars = Math.floor(vote);
            for (var i = 0; i < fullStars; i++) {
                stars.push('full');
            }

            var halfStars = Math.ceil((vote % 1).toFixed(4));
            for (var i = 0; i < halfStars; i++) {
                stars.push('half');
            }

            var emptyStars = 5 - vote;
            for (var i = 0; i < emptyStars; i++) {
                stars.push('empty');
            }


        return stars;
    };
     $scope.getStars = function (vote) {
        return getStars(vote);
    };
    $scope.user=StorageSrv.getUser();
    $scope.driverRating = $scope.user.gameProfile.driverRating;
    $scope.passengerRating = $scope.user.gameProfile.passengerRating;
    Utils.loading();
    DriverSrv.getDriverTrips().then(function(driverTrips) {
        $scope.totalDriverTrips =driverTrips.length;
        PassengerSrv.getPassengerTrips().then(function(passengerTrips) {
        $scope.totalPassengerTrips =passengerTrips.length;
                Utils.loaded();

    });
    });


   $scope.ratingOffer =4;
   $scope.ratingAccepted=4.5;

});
