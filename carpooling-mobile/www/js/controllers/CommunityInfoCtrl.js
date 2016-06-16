angular.module('carpooling.controllers.communityinfo', [])

.controller('CommunityInfoCtrl', function ($scope, $rootScope, $state, $stateParams, $ionicTabsDelegate, CacheSrv, UserSrv, Utils, StorageSrv, $filter) {
    $scope.tab = 0;

    $scope.selectTab = function (idx) {
        //if (idx == $scope.tab) return;
        if (idx !== $scope.tab) {
            $scope.tab = idx;
            $ionicTabsDelegate.select(idx);
        }
    }

    $scope.travelDateFormat = 'dd MMMM yyyy';
    $scope.travelTimeFormat = 'HH:mm';

    $scope.iAmMember = false;
    $scope.communityTrips = null;
    $scope.communityStyle = null;
    $scope.community = {};
    $scope.travelTimeFormat = 'HH:mm';
    $scope.selectDate = Date.now();
    $scope.hasAuto = StorageSrv.getUser().auto.posts == -1 ? false : true;
    $scope.lbl_day = $filter('translate')('lbl_todaytrips');
    var start = new Date();
    var end = new Date();
    var startTomorrow = new Date();
    var endTomorrow = new Date();

    /* Get the start and the end of the current day in milliseconds */
    var toTimestamp = function () {
        start.setHours(0, 0, 0, 0);
        end.setHours(23, 59, 59, 999);
        startTomorrow = angular.copy(start);
        endTomorrow = angular.copy(end);
        startTomorrow.setDate(startTomorrow.getDate() + 1);
        endTomorrow.setDate(endTomorrow.getDate() + 1);
        start = start.getTime();
        end = end.getTime();
        startTomorrow = startTomorrow.getTime();
        endTomorrow = endTomorrow.getTime();
    }

    toTimestamp();

    /* Check if the selected date is Today */
    var compareDate = function () {
        if ($scope.selectDate >= start && $scope.selectDate <= end) {
            $scope.lbl_day = $filter('translate')('lbl_todaytrips');
        } else if ($scope.selectDate >= startTomorrow && $scope.selectDate <= endTomorrow) {
            $scope.lbl_day = $filter('translate')('lbl_tomorrowtrips');
        } else {
            $scope.lbl_day = $filter('date')($scope.selectDate, 'EEE dd/MM/yyyy');
        }
    };

    var init = function () {
        if (!$stateParams.community || !$stateParams.community.id) {
            return;
        }

        Utils.loading();
        UserSrv.getCommunityDetails($stateParams.community.id).then(
            function (community) {
                $scope.community = community;
                $scope.iAmMember = $scope.amIaMember();

                UserSrv.getCommunityTravels($scope.community.id, $scope.selectDate).then(
                    function (todayCommunityTrips) {
                        $scope.communityTrips = todayCommunityTrips;
                        $scope.communityTrips.forEach(function (trip) {
                            trip.bookingCounters = Utils.getBookingCounters(trip);
                        });
                        Utils.loaded();
                    },
                    function (error) {
                        Utils.loaded();
                        Utils.toast(Utils.getErrorMsg(error));
                    }
                );
                $scope.communityStyle = {
                    'border-color': '#' + $scope.community.color + ' #' + $scope.community.color + ' transparent transparent'
                }
            },
            function (error) {
                Utils.loaded();
                Utils.toast(Utils.getErrorMsg(error));
            }
        );
    };

    $scope.changeDay = function (num) {
        if (num === 0) {
            $scope.selectDate -= 24 * 60 * 60 * 1000;
        } else {
            $scope.selectDate += 24 * 60 * 60 * 1000;
        }
        compareDate();
        init();
    };

    $scope.hideYesterday = function () {
        return $scope.selectDate <= Date.now();
    };

    init();

    $scope.changeAutoState = function () {
        $state.go('app.profilo', {
            'communityFrom': $scope.community,
            'editMode': true
        });
    };

    $scope.showUser = function (index) {
        var user = $scope.community.userObjs[index];
        var params = {};
        if (user.userId !== StorageSrv.getUser().userId) {
            // it's-a-not-me
            params.user = user;
        }
        $state.go('app.profilo', params);
    };

    $scope.selectTrip = function (index) {
        $state.go('app.viaggio', {
            'travelId': $scope.communityTrips[index].id
        });
    };

    $scope.addTrip = function () {
        $state.go('app.offri', {
            'communityId': $scope.community.id
        });
    };

    $scope.amIaMember = function () {
        var member = false;
        for (var i = 0; i < $scope.community.userObjs.length; i++) {
            var user = $scope.community.userObjs[i];
            if (user.userId === StorageSrv.getUser().userId) {
                // it's-a-me!
                member = true;
                i = $scope.community.userObjs.length;
            }
        };
        return member;
    };

    $scope.isMe = function (id) {
        return id == localStorage['userId'];
    };

    $scope.joinCommunity = function () {
        if (!!$scope.community.id) {
            Utils.loading();
            UserSrv.joinCommunity($scope.community.id).then(
                function () {
                    CacheSrv.setReloadMyCommunities(true);
                    Utils.loaded();
                    init();
                },
                function (reason) {
                    Utils.loaded();
                    Utils.toast(Utils.getErrorMsg(reason));
                }
            );
        } else {
            Utils.toast(Utils.getErrorMsg(error));
        }
    };

    $scope.leaveCommunity = function () {
        if (!!$scope.community.id) {
            Utils.loading();
            UserSrv.leaveCommunity($scope.community.id).then(
                function () {
                    CacheSrv.setReloadMyCommunities(true);
                    Utils.loaded();
                    init();
                },
                function (reason) {
                    Utils.loaded();
                    Utils.toast(Utils.getErrorMsg(reason));
                }
            );
        } else {
            Utils.toast();
        }
    };
});
