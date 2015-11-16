angular.module('carpooling', [
    'ionic',
    'ngIOS9UIWebViewPatch',
    'pascalprecht.translate',
    'carpooling.services',
    'carpooling.services.config',
    'carpooling.services.login',
    'carpooling.controllers'
    ])

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

    .state('app.comunitainfo.info', {
        url: '/info',
        views: {
            'tab-info': {
                templateUrl: 'templates/info.html'
            }
        }
    })

    .state('app.comunitainfo.viaggi', {
        url: '/viaggi',
        views: {
            'tab-viaggi': {
                templateUrl: 'templates/viaggi.html'
            }
        }
    })

    .state('app.comunitainfo.componenti', {
        url: '/componenti',
        views: {
            'tab-componenti': {
                templateUrl: 'templates/componenti.html'
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

    .state('app.cercacomunita', {
        url: '/cercacomunita',
        views: {
            'menuContent': {
                templateUrl: 'templates/cercacomunita.html'
            }
        }
    })

    .state('app.impostazioninotifiche', {
        url: '/impostazioninotifiche',
        views: {
            'menuContent': {
                templateUrl: 'templates/impostazioninotifiche.html'
            }
        }
    })

    .state('app.comunitainfo', {
        url: '/comunitainfo',
        views: {
            'menuContent': {
                templateUrl: 'templates/comunitainfo.html'
            }
        }
    })

    .state('app.cerca', {
        url: '/cerca',
        views: {
            'menuContent': {
                templateUrl: 'templates/cerca.html',
                controller: 'CercaViaggioCtrl'
            }
        }
    })

    .state('app.offri', {
        url: '/offri',
        views: {
            'menuContent': {
                templateUrl: 'templates/offri.html',
                controller: 'OffriCtrl'
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
        cancel: 'Annulla',
        ok: 'OK',
        menu_home: 'Home',
        menu_community: 'Comunità',
        menu_chat: 'Chat',
        menu_notifications: 'Notifiche',
        menu_profile: 'Profilo',
        lbl_notifications: 'Desidero ricevere notifiche per:',
        lbl_newmessage: 'Nuovo Messaggio',
        lbl_drivervalutation: 'Valutazione del conducente',
        lbl_passengervalutation: 'Valutazione del passeggero',
        lbl_notificationsettings: 'Impostazioni Notifiche',
        lbl_communityname: 'Nome Comunità',
        lbl_findcommunity: 'Cerca comunità',
        lbl_name: 'Nome',
        lbl_tripzone: 'Zona di viaggio',
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
        lbl_cerca: 'Cerca',
        lbl_mycommunity: 'Nelle mie community',
        lbl_allcommunity: 'In tutte le community',
        lbl_allsearchnotifications: 'Desidero ricevere tutte le notifiche per questa ricerca',
        tab_participate: 'Partecipo',
        tab_offer: 'Offro',
        title_setrecurrence: 'Imposta ricorrenza',
        radio_daily: 'Giornaliera',
        radio_weekly: 'Settimanale',
        radio_monthly: 'Mensile',
        dow_monday: 'Lunedì',
        dow_tuesday: 'Martedì',
        dow_wednesday: 'Mercoledì',
        dow_thursday: 'Giovedì',
        dow_friday: 'Venerdì',
        dow_saturday: 'Sabato',
        dow_sunday: 'Domenica'
    });

    $translateProvider.preferredLanguage('it');
});
