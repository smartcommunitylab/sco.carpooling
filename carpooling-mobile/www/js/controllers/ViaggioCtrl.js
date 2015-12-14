angular.module('carpooling.controllers.viaggio', [])

.controller('ViaggioCtrl', function ($scope, PassengerSrv, $state, $stateParams, $filter, UserSrv, Utils, StorageSrv) {
    $scope.travelDateFormat = 'dd MMMM yyyy';
    $scope.travelTimeFormat = 'HH:mm';
    $scope.driverInfo = {};

    var init = function () {
        $scope.travelId = $stateParams.travelId;

        Utils.loading();
        PassengerSrv.getTrip($scope.travelId).then(
            function (data) {
                $scope.selectedTrip = data;
                console.log($scope.selectedTrip);

                $scope.isMine = $scope.selectedTrip.userId === StorageSrv.getUserId();
                $scope.bookingCounters = Utils.getBookingCounters($scope.selectedTrip);
                $scope.dowString = Utils.getRecurrencyString($scope.selectedTrip);

                // TODO: handle getUser that isn't me
                if (!$scope.isMine) {
                    UserSrv.getUser($scope.selectedTrip.userId).then(
                        function (userInfo) {
                            Utils.loaded();
                            console.log('User found');
                            $scope.driverInfo = userInfo;
                            if (!!userInfo.auto) {
                                $scope.passengerNum = userInfo.auto.posts - $scope.selectedTrip.places;
                                console.log($scope.passengerNum);
                            }
                            console.log($scope.driverInfo);
                        },
                        function (error) {
                            Utils.loaded();
                            // TODO: handle getUser error
                            console.log(error);
                        }
                    );
                } else {


                    Utils.loaded();
                }
            },
            function (error) {
                Utils.loaded();
                // TODO: handle getTrip error
                Utils.toast();
            }
        );
    };

    init();

    $scope.getNumber = function () {
        return Utils.getNumber();
    }
});
