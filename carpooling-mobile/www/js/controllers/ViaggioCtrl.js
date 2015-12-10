angular.module('carpooling.controllers.viaggio', [])

.controller('ViaggioCtrl', function ($scope, PassengerSrv, $state, $stateParams, $filter, UserSrv) {
    $scope.travelDateFormat = 'dd MMMM yyyy';
    $scope.travelTimeFormat = 'HH:mm';
    // TODO: build UI and fill it here
    $scope.selectedTrip = $stateParams['trip'];
    console.log($scope.selectedTrip);
    $scope.driverInfo = {};
    $scope.passengerCount = 0;
    $scope.freeSpaces = angular.copy($scope.selectedTrip.places);

    $scope.dowString = '';
    $scope.onRequest = '';
    $scope.recurrencyDoW = ['dow_monday', 'dow_tuesday', 'dow_wednesday', 'dow_thursday', 'dow_friday', 'dow_saturday', 'dow_sunday'];

    // TODO: To put in a function
    // Check if there are intermediateStops or not in the selected trip
    if (!!$scope.selectedTrip.intermediateStops) {
        $scope.onRequest = $filter('translate')('lbl_on_request');
    } else {
        $scope.onRequest = $filter('translate')('lbl_no_inter_stops');
    }
    /////////////////////////////////

    // TODO: To put in a function
    // Check if there are recurrency in the selected trip
    for (var i = 0; i < $scope.selectedTrip.recurrency.days.length; i++) {
        var dayOfW = $scope.selectedTrip.recurrency.days[i];
        if (!!$scope.dowString) {
            $scope.dowString = $scope.dowString + ', ';
        }
        $scope.dowString = $scope.dowString + $filter('translate')($scope.recurrencyDoW[dayOfW]);
    }
    //////////////////////////////

    $scope.selectedTrip.bookings.forEach(function (booking) {
        //TODO: availability logic
        if (booking.accepted >= 0) {
            $scope.passengerCount++;
            $scope.freeSpaces--;
        }
    });

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
