angular.module('carpooling.controllers.viaggio', [])

.controller('ViaggioCtrl', function ($scope, PassengerSrv, $state, $stateParams, $filter, UserSrv, Utils, StorageSrv) {
    $scope.travelDateFormat = 'dd MMMM yyyy';
    $scope.travelTimeFormat = 'HH:mm';
    $scope.driverInfo = {};

    var init = function() {
      $scope.travelId = $stateParams.travelId;
      Utils.loading();
      PassengerSrv.getTrip($scope.travelId).then(function(data) {
        $scope.selectedTrip = data;
        console.log($scope.selectedTrip);

        $scope.isMine = $scope.selectedTrip.userId == StorageSrv.getUserId();
        $scope.bookingCounters = Utils.getBookingCounters($scope.selectedTrip);
        $scope.dowString = Utils.getRecurrencyString($scope.selectedTrip);

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
                  // TODO: handle search error
                  console.log(error);
                  Utils.loaded();
              }
          );
        } else {
          Utils.loaded();
        }
      }, function() {
        // TODO
        Utils.loaded();
      });
    }

    init();

    $scope.getNumber = function (num) {
        return new Array(num);
    }
});
