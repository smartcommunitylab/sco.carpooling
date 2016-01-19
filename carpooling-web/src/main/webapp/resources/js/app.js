var consoleApp = angular.module('console', [,'ui.bootstrap',
'ngRoute',
'consoleControllers',
'DataService'
]);

consoleApp.run([ '$rootScope', '$q', '$modal', '$location', 'DataService',
  function($localize, $rootScope, $q, $modal, $location, DataService, CodeProcessor, ValidationService){
    
    $rootScope.logout = function(url) {
      DataService.logout().then(function(){
        window.location.reload();
      });
    };
  }]);


consoleApp.config(['$routeProvider',
  function($routeProvider) {
    $routeProvider.
      when('/home', {
        templateUrl: 'templates/main.html',
        controller: 'MainCtrl'
      }).
      otherwise({
        redirectTo: '/home'
      });
  }]);