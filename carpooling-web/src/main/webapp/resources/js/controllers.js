var consoleControllers = angular.module('consoleControllers', [])

.controller('MainCtrl', ['$scope', '$rootScope', '$location', 'DataService', 
  function ($scope, $rootScope, $location, DataService) {
    DataService.getProfile().then(function(p){
    	$scope.profile = p;
    });
   
  }]);
  