angular.module('carpooling', [
    'ionic',
    'ionic-timepicker',
    'ionic-datepicker',
    'ngIOS9UIWebViewPatch',
    'pascalprecht.translate',
    'carpooling.services.config',
    'carpooling.services.login',
    'carpooling.services.user',
    'carpooling.services.passenger',
    'carpooling.services.driver',
    'carpooling.services.map',
    'carpooling.services.plan',
    'carpooling.services.geo',
    'carpooling.directives',
    'carpooling.controllers',
    'leaflet-directive'
])

.run(function ($ionicPlatform, LoginSrv, $rootScope, $q) {
    $rootScope.userIsLogged = (localStorage.userId != null && localStorage.userId != 'null');

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

        if (!LoginSrv.getUserId()) {
            // FIXME: toggle the lines below for browser devel
            LoginSrv.login();
            //localStorage.userId = '14';
        }
    });

    $rootScope.login = function () {
        var deferred = $q.defer();
        LoginSrv.login().then(
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
        LoginSrv.logout().then(
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
        cache: false,
        views: {
            'tab-partecipo': {
                templateUrl: 'templates/partecipo.html',
                controller: 'PartecipoCtrl'
            }
        }
    })

    .state('app.home.offro', {
        url: '/offro',
        cache: false,
        views: {
            'tab-offro': {
                templateUrl: 'templates/offro.html',
                controller: 'OffroCtrl'
            }
        }
    })

    .state('app.mioviaggio', {
        url: '/mioviaggio',
        cache: false,
        views: {
            'menuContent': {
                templateUrl: 'templates/mioviaggio.html',
            }
        }
    })

    .state('app.comunitainfo', {
        url: '/comunitainfo',
        cache: false,
        views: {
            'menuContent': {
                templateUrl: 'templates/comunitainfo.html'
            }
        }
    })

    .state('app.comunitainfo.info', {
        url: '/info',
        cache: false,
        views: {
            'tab-info': {
                templateUrl: 'templates/info.html'
            }
        }
    })

    .state('app.comunitainfo.viaggi', {
        url: '/viaggi',
        cache: false,
        views: {
            'tab-viaggi': {
                templateUrl: 'templates/viaggi.html'
            }
        }
    })

    .state('app.comunitainfo.componenti', {
        url: '/componenti',
        cache: false,
        views: {
            'tab-componenti': {
                templateUrl: 'templates/componenti.html'
            }
        }
    })

    .state('app.comunita', {
        url: '/comunita',
        cache: false,
        views: {
            'menuContent': {
                templateUrl: 'templates/comunita.html'
            }
        }
    })

    .state('app.cercacomunita', {
        url: '/cercacomunita',
        cache: false,
        views: {
            'menuContent': {
                templateUrl: 'templates/cercacomunita.html'
            }
        }
    })

    .state('app.cerca', {
        url: '/cerca',
        cache: false,
        views: {
            'menuContent': {
                templateUrl: 'templates/cerca.html',
                controller: 'CercaViaggioCtrl'
            }
        }
    })

    .state('app.offri', {
        url: '/offri',
        cache: false,
        views: {
            'menuContent': {
                templateUrl: 'templates/offri.html',
                controller: 'OffriCtrl'
            }
        }
    })

    .state('app.notifiche', {
        url: '/notifiche',
        cache: false,
        views: {
            'menuContent': {
                templateUrl: 'templates/notifiche.html',
                controller: 'NotificationCtrl'
            }
        }
    })

    .state('app.impostazioninotifiche', {
        url: '/impostazioninotifiche',
        cache: false,
        views: {
            'menuContent': {
                templateUrl: 'templates/impostazioninotifiche.html'
            }
        }
    })

    .state('app.profilo', {
        url: '/profilo',
        cache: false,
        views: {
            'menuContent': {
                templateUrl: 'templates/profilo.html'
            }
        }
    })

    .state('app.profilo.userinfo', {
        url: '/userinfo',
        cache: false,
        views: {
            'tab-userinfo': {
                templateUrl: 'templates/userinfo.html',
                controller: 'UserInfoCtrl'
            }
        }
    })

    .state('app.profilo.userstats', {
        url: '/userstats',
        cache: false,
        views: {
            'tab-userstats': {
                templateUrl: 'templates/userstats.html'
            }
        }
    })

    .state('app.chat', {
        url: '/chat',
        cache: false,
        views: {
            'menuContent': {
                templateUrl: 'templates/chat.html',
                controller: 'NotificationCtrl'
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
        msg_talk: 'dice',
        lbl_mytrip: 'Mio viaggio',
        lbl_notifications: 'Desidero ricevere notifiche per:',
        lbl_newmessage: 'Nuovo Messaggio',
        lbl_drivervalutation: 'Valutazione del conducente',
        lbl_passengervalutation: 'Valutazione del passeggero',
        lbl_drivervalutation_parenthesis: '(Dei viaggi a cui ho partecipato)',
        lbl_passengervalutation_parenthesis: '(Dei viaggi che ho offerto)',
        lbl_notificationsettings: 'Impostazioni Notifiche',
        lbl_communityname: 'Nome Comunità',
        lbl_findcommunity: 'Cerca comunità',
        lbl_name: 'Nome',
        lbl_tripzone: 'Zona di viaggio',
        lbl_start: 'Partenza',
        lbl_generalinformations: 'INFORMAZIONI GENERALI',
        lbl_numberauto: 'AUTO A DISPOSIZIONE',
        lbl_addauto: 'AGGIUNGI LA TUA AUTO',
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
        lbl_offri: 'Pubblica',
        lbl_cerca: 'Cerca',
        lbl_mycommunity: 'Nelle mie community',
        lbl_allcommunity: 'In tutte le community',
        lbl_allsearchnotifications: 'Desidero ricevere tutte le notifiche per questa ricerca',
        lbl_start_time: 'Orario di partenza',
        lbl_user_car_owner: 'Automunito',
        lbl_user_car_info: 'Note auto',
        lbl_user_car_seats: 'Posti disponibili',
        tab_participate: 'Partecipo',
        tab_offer: 'Offro',
        title_setrecurrence: 'Imposta ricorrenza',
        radio_daily: 'Giornaliera',
        radio_weekly: 'Settimanale',
        radio_monthly: 'Mensile',
        repeat_every_1: 'Ripeti ogni',
        repeat_every_2: 'days',
        dow_monday: 'Lunedì',
        dow_tuesday: 'Martedì',
        dow_wednesday: 'Mercoledì',
        dow_thursday: 'Giovedì',
        dow_friday: 'Venerdì',
        dow_saturday: 'Sabato',
        dow_sunday: 'Domenica',
        dow_monday_short: 'L',
        dow_tuesday_short: 'M',
        dow_wednesday_short: 'M',
        dow_thursday_short: 'G',
        dow_friday_short: 'V',
        dow_saturday_short: 'S',
        dow_sunday_short: 'D',
        month_jan: 'Gen',
        month_feb: 'Feb',
        month_mar: 'Mar',
        month_apr: 'Apr',
        month_may: 'Mag',
        month_jun: 'Giu',
        month_jul: 'Lug',
        month_ago: 'Ago',
        month_sep: 'Set',
        month_oct: 'Ott',
        month_nov: 'Nov',
        month_dic: 'Dic',
        popup_timepicker_title: 'Selezionare l\'ora',
        popup_datepicker_title: 'Selezionare il giorno',
        popup_datepicker_today: 'Oggi'
    });

    $translateProvider.preferredLanguage('it');
    $translateProvider.useSanitizeValueStrategy('escape');
});
