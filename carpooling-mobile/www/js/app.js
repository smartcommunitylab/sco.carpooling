// Ionic Starter App

// angular.module is a global place for creating, registering and retrieving Angular modules
// 'starter' is the name of this angular module example (also set in a <body> attribute in index.html)
// the 2nd parameter is an array of 'requires'
// 'starter.controllers' is found in controllers.js
angular.module('starter', [
  'ionic',
  'ngIOS9UIWebViewPatch',
  'starter.controllers',
  'pascalprecht.translate'])

.run(function($ionicPlatform) {
  $ionicPlatform.ready(function() {
    // Hide the accessory bar by default (remove this to show the accessory bar above the keyboard
    // for form inputs)
    if (window.cordova && window.cordova.plugins.Keyboard) {
      cordova.plugins.Keyboard.hideKeyboardAccessoryBar(true);
      cordova.plugins.Keyboard.disableScroll(true);

    }
    if (window.StatusBar) {
      // org.apache.cordova.statusbar required
      StatusBar.styleDefault();
    }
  });
})

.config(function($stateProvider, $urlRouterProvider) {
  $stateProvider

    .state('app', {
    url: '/app',
    abstract: true,
    templateUrl: 'templates/menu.html',
    controller: 'AppCtrl'
  })

  .state('app.home', {
    url: '/home',
    cache: false,
    views: {
      'menuContent': {
        templateUrl: 'templates/home.html',
        controller: 'HomeCtrl'
      }
    }
  })

  .state('app.home.partecipo', {
      url: '/partecipo',
      views: {
        'tab-partecipo': {
          templateUrl: 'templates/partecipo.html',
          controller: 'PartecipoCtrl'
        }
      }
    })

  .state('app.home.offro', {
      url: '/offro',
      views: {
        'tab-offro': {
          templateUrl: 'templates/offro.html',
          controller: 'OffroCtrl'
        }
      }
    })

  .state('app.comunita', {
      url: '/comunita',
      views: {
        'menuContent': {
          templateUrl: 'templates/comunita.html'
        }
      }
    })

  .state('app.cerca', {
      url: '/cerca',
      views: {
        'menuContent': {
          templateUrl: 'templates/cerca.html'
        }
      }
    })

  .state('app.notifiche', {
      url: '/notifiche',
      views: {
        'menuContent': {
          templateUrl: 'templates/notifiche.html'
        }
      }
    })
  .state('app.profilo', {
      url: '/profilo',
      views: {
        'menuContent': {
          templateUrl: 'templates/profilo.html'
        }
      }
    })
    .state('app.chat', {
      url: '/chat',
      views: {
        'menuContent': {
          templateUrl: 'templates/chat.html',
        }
      }
    });
  // if none of the above states are matched, use this as the fallback
  $urlRouterProvider.otherwise('/app/home');
})

.config(function($translateProvider) {
  $translateProvider.translations('it',{
    app_name:'CARpooling',
    menu_home: 'Home',
    menu_comm: 'Comunità',
    menu_chat: 'Chat',
    menu_notif: 'Notifiche',
    menu_profile: 'Profilo',
    lbl_search: 'Cerca viaggio'
  });
  $translateProvider.preferredLanguage('it');

});
