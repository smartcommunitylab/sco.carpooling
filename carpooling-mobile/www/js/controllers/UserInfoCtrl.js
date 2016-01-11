angular.module('carpooling.controllers.user', [])

.controller('UserInfoCtrl', function ($scope, $rootScope, $state, $stateParams, $ionicHistory, StorageSrv, DriverSrv, PassengerSrv, UserSrv, Utils) {

    $scope.editMode = false || $rootScope.initialSetup || !!$stateParams['editMode'];

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
        var auto = $scope.user.auto;
        if (!auto) {
            auto = {
                description: '',
                posts: -1
            };
        }
        UserSrv.saveAuto(auto).then(
            function (data) {
                if ($rootScope.initialSetup) {
                    UserSrv.getUser($scope.user.userId).then(
                        function () {
                            Utils.loaded();
                            StorageSrv.setProfileComplete();
                            $rootScope.initialSetup = false;
                            $ionicHistory.nextViewOptions({
                              historyRoot: true,
                              disableBack: true
                            });
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

    $scope.toggleHasAuto = function() {
      if ($scope.edit.hasAuto) {
        if (!$scope.user.auto) {
          $scope.user.auto = {
              posts: 4,
              description: ''
          };
        }
      } else {
            $scope.user.auto = null;
      }
    }

    $scope.drivertripsheight = "";
    $scope.passengertripsheight = "";

    $scope.ratingOffer = 0;
    $scope.ratingAccepted = 0;
    var calculateHeight = function (driver, passenger) {
        maxValue = Math.max(driver, passenger);
        minValue = Math.min(driver, passenger);
        maxHeight = 100;
        minHight = (100 * minValue) / maxValue;
        if (driver == maxValue) {
            $scope.drivertripsheight = 100 - maxHeight + 'px';
            $scope.passengertripsheight = 100 - minHight + 'px';
        } else {
            $scope.passengertripsheight = 100 - maxHeight + 'px';
            $scope.drivertripsheight = 100 - minHight + 'px';
        }
    }
    var getStars = function (vote) {
        if (!vote) {
            vote = 0;
        }

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

    $scope.initStats = function() {
      $scope.totalDriverTrips = 0;
      $scope.totalPassengerTrips = 0;
      Utils.loading();

      UserSrv.getUser($scope.user.userId).then(function(user){
        $scope.driverRating = user.gameProfile.driverRating;
        $scope.passengerRating = user.gameProfile.passengerRating;
        $scope.totalDriverTrips = user.offeredTravels;
        $scope.totalPassengerTrips = user.participatedTravels;
        calculateHeight($scope.totalDriverTrips, $scope.totalPassengerTrips);
        Utils.loaded();
      }, function(err) {
        Utils.loaded();
      });

    }
});
