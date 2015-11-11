angular.module('starter.controllers', [])

.controller('AppCtrl', function ($scope) {})

.controller('PartecipoCtrl', function ($scope) {})

.controller('OffroCtrl', function ($scope) {})

.controller('OffriCtrl', function ($scope, $filter, $ionicPopup) {
    $scope.recurrence = {
        isRecurrent: false,
        recurrenceType: 'd',
        recurrenceDoW: new Array(7)
    };

    var recurrentPopup = {
        templateUrl: 'templates/popup_offri.html',
        title: $filter('translate')('title_setrecurrence'),
        scope: $scope,
        buttons: [
            {
                text: $filter('translate')('cancel')
            },
            {
                text: $filter('translate')('ok'),
                type: 'button-positive',
                onTap: function (e) {
                    // don't allow the user to close unless he enters wifi password
                    //e.preventDefault();
                    return true;
                }
            }
        ]
    };

    $scope.$watch('recurrence.isRecurrent', function (newValue, oldValue) {
        if (newValue !== oldValue && !!newValue) {
            $ionicPopup.show(recurrentPopup).then(
                function (res) {
                    console.log($scope.recurrence.recurrenceDoW);
                    if (!!!res) {
                        $scope.recurrence.isRecurrent = false;
                    }
                }
            );
        }
    });
})

.controller('CercaViaggioCtrl', function ($scope, $filter) {
    $scope.date = $filter("date")(Date.now(), 'yyyy-MM-dd');
    $scope.time = '10:30';
})

.controller('HomeCtrl', function ($scope) {});
