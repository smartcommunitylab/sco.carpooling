angular.module('carpooling.controllers.user', [])

.controller('UserInfoCtrl', function ($scope, $rootScope, $state, $stateParams, StorageSrv, UserSrv, Utils) {
    var hasAuto = function (auto) {
        // return !!$scope.user.auto
        if (!!auto && !!auto.description && auto.posts !== 0) {
            return true;
        }
        return false;
    };

    var initUser = function () {
        $scope.itsMe = $scope.user['userId'] === StorageSrv.getUser()['userId'];

        if (!$scope.user) {
            $scope.user = {
                auto: null
            };
        }

        $scope.edit = {
            hasAuto: hasAuto($scope.user.auto),
            postsAvailable: [1, 2, 3, 4, 5, 6, 7]
        };
    };

    if (!!$stateParams['user']) {
        $scope.user = $stateParams['user'];
        initUser();
    } else {
        //$scope.user = angular.copy(StorageSrv.getUser());
        //initUser();
        Utils.loading();
        UserSrv.getUser(StorageSrv.getUserId()).then(
            function () {
                $scope.user = angular.copy(StorageSrv.getUser());
                initUser();
                Utils.loaded();
            }
        );
    }

    $rootScope.initialSetup = !StorageSrv.isProfileComplete();

    $scope.editMode = false || $rootScope.initialSetup || !!$stateParams['editMode'];

    $scope.toggleEditMode = function () {
        $scope.editMode = !$scope.editMode;
    };

    var goToCommunityInfo = function (saved) {
        var communityToGo = null;
        if (!!saved) {
            UserSrv.getCommunitiesDetails().then(function (communities) {
                for (var i = 0; i < communities.length; i++) {
                    var c = communities[i];
                    if (c.id === $stateParams['communityFrom'].id) {
                        communityToGo = c;
                        i = communities.length;
                    }
                    $state.go('app.comunitainfo', {
                        'community': communityToGo
                    });
                }
            });
        } else {
            communityToGo = $stateParams['communityFrom'];
            $state.go('app.comunitainfo', {
                'community': communityToGo
            });
        }
    };

    $scope.cancelChanges = function () {
        $scope.toggleEditMode();
        $scope.user = angular.copy(StorageSrv.getUser());
        $scope.edit.hasAuto = hasAuto($scope.user.auto);
        if (!!$stateParams['communityFrom']) {
            goToCommunityInfo(false);
        }
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

                            console.log(!!$stateParams['communityFrom']);
                            if (!!$stateParams['communityFrom']) {
                                goToCommunityInfo(true);
                            }
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

.controller('UserStatsCtrl', function ($scope, $rootScope, DriverSrv, PassengerSrv, Utils, StorageSrv) {
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
    $scope.user = StorageSrv.getUser();
    $scope.driverRating = $scope.user.gameProfile.driverRating;
    $scope.passengerRating = $scope.user.gameProfile.passengerRating;
    Utils.loading();
    DriverSrv.getDriverTrips().then(function (driverTrips) {
        $scope.totalDriverTrips = driverTrips.length;
        PassengerSrv.getPassengerTrips().then(function (passengerTrips) {
            $scope.totalPassengerTrips = passengerTrips.length;
            Utils.loaded();
        });
    });

    $scope.ratingOffer = 4;
    $scope.ratingAccepted = 4.5;
});
