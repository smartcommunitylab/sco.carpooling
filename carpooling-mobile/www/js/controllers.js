angular.module('starter.controllers', [])

.controller('AppCtrl', ['$scope', function($scope) {

}])

.controller('PartecipoCtrl', function($scope) {

})

.controller('OffroCtrl', function($scope) {

})

.controller('LoginCtrl', ['$scope','$state', function($scope, $state) {

    $scope.logged = true;
    $scope.login = function() {
        //alert($scope.logged);
        if($scope.logged){
            $state.go('app.home');
        }
    }

}])

.controller('CercaViaggioCtrl', function($scope) {

})

.controller('HomeCtrl', function($scope) {

});

