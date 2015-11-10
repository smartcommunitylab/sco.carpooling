angular.module('starter', [
  'ionic',
  'ngIOS9UIWebViewPatch',
  'starter.controllers',
  'pascalprecht.translate',
  'starter.services.login'])

.run(function ($ionicPlatform, Login, $rootScope, $q) {
    $rootScope.userIsLogged = (localStorage.userId != null && localStorage.userId != "null");

    $rootScope.getUserId = function () {
        if ($rootScope.userIsLogged) {
            return localStorage.userId;
        }
        return null;
    };

    $ionicPlatform.ready(function () {
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

        if (!Login.getUserId()) {
            Login.login();
        }
    });

    $rootScope.login = function () {
        var deferred = $q.defer();
        Login.login().then(
            function (data) {
                deferred.resolve(data);
            },
            function (error) {
                deferred.reject(error);
            }
        );

        return deferred.promise;
    }

    $rootScope.logout = function () {
        var deferred = $q.defer();
        Login.logout().then(
            function (data) {
                deferred.resolve(data);
            },
            function (error) {
                deferred.reject(error);
            }
        );

        return deferred.promise;
    };
})

.config(function ($stateProvider, $urlRouterProvider) {
    $stateProvider.state('app', {
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

    .state('app.offri', {
        url: '/offri',
        views: {
            'menuContent': {
                templateUrl: 'templates/offri.html'
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

.config(function ($translateProvider) {
    $translateProvider.translations('it', {
        app_name: 'CARpooling',
        menu_home: 'Home',
        menu_community: 'Comunità',
        menu_chat: 'Chat',
        menu_notifications: 'Notifiche',
        menu_profile: 'Profilo',
        lbl_search: 'Cerca viaggio',
        lbl_offer: 'Offri un viaggio',
        lbl_from: 'Da',
        lbl_to: 'A',
        lbl_halfwaystops: 'Fermate intermedie',
        lbl_halfwaystops_none: 'Nessuna',
        lbl_halfwaystops_agree: 'Da concordare con il conducente',
        lbl_date: 'Data',
        lbl_time: 'Ora',
        lbl_recurrenttrip: 'Viaggio ricorrente',
        tab_participate: 'Partecipo',
        tab_offer: 'Offro'
    });

    $translateProvider.preferredLanguage('it');
});
