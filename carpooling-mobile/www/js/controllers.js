angular.module('starter.controllers', [])

.controller('AppCtrl', function ($scope) {


})

.controller('PartecipoCtrl', function ($scope) {

})

.controller('OffroCtrl', function ($scope) {

})


.controller('CercaViaggioCtrl', function ($scope, $filter) {
    $scope.date = $filter("date")(Date.now(), 'yyyy-MM-dd');
    $scope.time = '10:30';

})

.controller('HomeCtrl', function ($scope) {

});
