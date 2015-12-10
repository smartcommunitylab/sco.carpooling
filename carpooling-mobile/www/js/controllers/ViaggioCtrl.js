angular.module('carpooling.controllers.viaggio', [])

.controller('ViaggioCtrl', function ($scope, PassengerSrv, $state, $stateParams, $filter, UserSrv, Utils) {
    $scope.travelDateFormat = 'dd MMMM yyyy';
    $scope.travelTimeFormat = 'HH:mm';

    // TODO: build UI and fill it here
    $scope.selectedTrip = $stateParams['trip'];
    console.log($scope.selectedTrip);

    $scope.driverInfo = {};
    $scope.bookingCounters = Utils.getBookingCounters($scope.selectedTrip);

    $scope.dowString = Utils.getRecurrencyString($scope.selectedTrip);

    $scope.getNumber = function (num) {
        return new Array(num);
    }

    UserSrv.getUser($scope.selectedTrip.userId).then(
        function (userInfo) {
            console.log('User found');
            $scope.driverInfo = userInfo;
            if (!!userInfo.auto) {
                $scope.passengerNum = userInfo.auto.posts - $scope.selectedTrip.places;
                console.log($scope.passengerNum);
            }
            console.log($scope.driverInfo);
        },
        function (error) {
            // TODO: handle search error
            console.log(error);
        }
    );
});
