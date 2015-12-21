angular.module('carpooling', [
    'ionic',
    'ngCordova',
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
    'carpooling.services.storage',
    'carpooling.services.utils',
    'carpooling.directives',
    'carpooling.controllers.home',
    'carpooling.controllers.offri',
    'carpooling.controllers.cercaviaggi',
    'carpooling.controllers.viaggio',
    'carpooling.controllers.notifications',
    'carpooling.controllers.user',
    'carpooling.controllers.communities',
    'carpooling.controllers.communityinfo',
    'leaflet-directive'
])

.run(function ($ionicPlatform, $rootScope, $state, $q, StorageSrv, LoginSrv, UserSrv, Config, Utils) {

    var isIOS = ionic.Platform.isIOS();
    var isAndroid = ionic.Platform.isAndroid();

    $rootScope.manageLocalNotification = function(local_notification) {
        if (local_notification.alert) {
            if (cordova && cordova.plugins && cordova.plugins.notification) {
                try {
                    //console.log('initializing notifications...');
                    cordova.plugins.notification.local.cancelAll();

                    var notific = {
                        id: local_notification.push_hash,
                        title: "CarPooling",
                        text: local_notification.alert,
                        //autoCancel: true,
                        //firstAt: monday_9_am,
                        //every: "week",
                        //sound: "file://sounds/reminder.mp3",
                        //icon: "http://icons.com/?cal_id=1",
                        data: {
                            id: local_notification.push_hash
                        }
                    }
                    if (notific) {
                        cordova.plugins.notification.local.schedule(notific);
                    }
                } catch (ex) {}
            }
            //alert('Reveived notification:' + JSON.stringify(pn));
        }
    };

    $rootScope.pushRegistration = function (userId) {
        var channel = 'CarPooling_' + userId;
        try {
            if (isAndroid) {
                if (window.ParsePushPlugin) {
                    window.ParsePushPlugin.subscribe(channel, function () {
                        //console.log("Succes in channel " + channel + " creation");
                    });
                    window.ParsePushPlugin.on('openPN', function (pn) {
                        if (pn != null && pn.urlHash != null) {
                            window.location.hash = hash;
                        }
                    });
                    window.ParsePushPlugin.on('receivePN', function (pn) {
                        $rootScope.manageLocalNotification(pn);
                    });
                    //
                    //you can also listen to your own custom subevents
                    //
                    //ParsePushPlugin.on('receivePN:chat', chatEventHandler);
                    //ParsePushPlugin.on('receivePN:serverMaintenance', serverMaintenanceHandler);*/
                }
            } else if (isIOS) {
                //window.parsepushnotification.setUp(Config.getAppId(), Config.getClientKey());
                //window.parsepushnotification.onRegisterAsPushNotificationClientSucceeded = function() {
                //var channel = 'CarPooling_' + userId;
                //    window.parsepushnotification.subscribeToChannel(channel); //parameter: channel
                //    //console.log('successfully created channel ' + channel);
                //};
                if (window.ParsePushPlugin) {
                    windows.ParsePushPlugin.initialize(Config.getAppId(), Config.getClientKey(), function() {
                        window.ParsePushPlugin.subscribe(channel, function () {
                            //console.log("Succes in channel " + channel + " creation");
                        });
                    }, function(e) {
                       console.log("Error in parse initialize");
                    });

                }
            }
        } catch (ex) {
            //console.log('Exception in parsepush registration ' + ex.message);
        }
    };

    $rootScope.isRecurrencyEnabled = Config.isRecurrencyEnabled;

    $rootScope.getNumber = function (num) {
        return Utils.getNumber(num);
    };

    $rootScope.login = function () {
        LoginSrv.login().then(
            function (data) {
                UserSrv.getUser(data.userId);
                $rootScope.pushRegistration(data.userId);
                $state.go('app.home', {}, {
                    reload: true
                });
            },
            function (error) {
                Utils.toast();
                StorageSrv.saveUser(null);
                ionic.Platform.exitApp();
            }
        );
    };

    $rootScope.logout = function () {
        LoginSrv.logout().then(
            function (data) {
                ionic.Platform.exitApp();
            },
            function (error) {
                Utils.toast();
            }
        );
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

        if (!LoginSrv.userIsLogged()) {
            $rootScope.login();
        } else {
            $rootScope.pushRegistration(StorageSrv.getUserId());
        }

    });
})

.config(function ($httpProvider, $ionicConfigProvider) {
    $httpProvider.defaults.withCredentials = true;
    $ionicConfigProvider.backButton.text('');
    $ionicConfigProvider.backButton.previousTitleText(false);
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

    .state('app.viaggio', {
        url: '/viaggio/:travelId',
        cache: false,
        views: {
            'menuContent': {
                templateUrl: 'templates/viaggio.html',
                controller: 'ViaggioCtrl'
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
        params: {
            'community': {}
        },
        views: {
            'menuContent': {
                templateUrl: 'templates/communityinfo.html',
                controller: 'CommunityInfoCtrl'
            }
        }
    })

    .state('app.comunitainfo.info', {
        url: '/info',
        cache: false,
        views: {
            'tab-info': {
                templateUrl: 'templates/communityinfo_info.html',
                controller: 'CommInfoCtrl'
            }
        }
    })

    .state('app.comunitainfo.viaggi', {
        url: '/viaggi',
        cache: false,
        views: {
            'tab-viaggi': {
                templateUrl: 'templates/communityinfo_trips.html',
                controller: 'CommTripCtrl'
            }
        }
    })

    .state('app.comunitainfo.componenti', {
        url: '/componenti',
        cache: false,
        views: {
            'tab-componenti': {
                templateUrl: 'templates/communityinfo_components.html',
                controller: 'CommComponentsCtrl'
            }
        }
    })

    .state('app.comunita', {
        url: '/comunita',
        cache: false,
        views: {
            'menuContent': {
                templateUrl: 'templates/comunita.html',
                controller: 'CommunityCtrl'
            }
        }
    })

    .state('app.cercacomunita', {
        url: '/cercacomunita',
        cache: false,
        params: {
            'myCommunities': {}
        },
        views: {
            'menuContent': {
                templateUrl: 'templates/cercacomunita.html',
                controller: 'FindCommunityCtrl'
            }
        }
    })

    .state('app.risultaticercaviaggi', {
        url: '/risultaticercaviaggi',
        cache: false,
        params: {
            'searchResults': {}
        },
        views: {
            'menuContent': {
                templateUrl: 'templates/risultaticercaviaggi.html',
                controller: 'RisultatiCercaViaggiCtrl'
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
                controller: 'NotificationsCtrl'
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
        params: {
            'user': null,
            'communityFrom': null,
            'editMode': null
        },
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
                templateUrl: 'templates/userstats.html',
                controller: 'UserStatsCtrl'
            }
        }
    })

    .state('app.chat', {
        url: '/chat/:travelId/:personId',
        cache: false,
        views: {
            'menuContent': {
                templateUrl: 'templates/chat.html',
                controller: 'ChatCtrl'
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
        menu_logout: 'Logout',
        modal_map: 'Scegli da mappa',
        modal_map_confirm: 'Conferma selezione',
        msg_talk: 'dice',
        lbl_no_results: 'Nessun risultato.',
        lbl_trips_found: 'Viaggi trovati',
        lbl_mytrip: 'Mio viaggio',
        lbl_trip: 'Viaggio',
        lbl_notifications: 'Desidero ricevere notifiche per:',
        lbl_old_chat: 'Carica vecchi messaggi',
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
        lbl_addtrip: 'AGGIUNGI UN VIAGGIO',
        lbl_editauto: 'MODIFICA LA TUA AUTO',
        lbl_search: 'Cerca viaggio',
        lbl_offer: 'Offri un viaggio',
        lbl_from: 'Da',
        lbl_to: 'A',
        lbl_halfwaystops: 'Fermate intermedie',
        lbl_halfwaystops_none: 'Nessuna',
        lbl_halfwaystops_agree: 'Da concordare con il conducente',
        lbl_halfwaystops_onrequest: 'Su richiesta',
        lbl_date: 'Data',
        lbl_time: 'Ora',
        lbl_recurrenttrip: 'Viaggio ricorrente',
        lbl_recurrency_none: 'Nessuna',
        lbl_offri: 'Pubblica',
        lbl_cerca: 'Cerca',
        lbl_mycommunity: 'Nelle mie community',
        lbl_allcommunity: 'In tutte le community',
        lbl_allsearchnotifications: 'Desidero ricevere tutte le notifiche per questa ricerca',
        lbl_start_time: 'Orario di partenza',
        lbl_user_car_owner: 'Automunito',
        lbl_user_car_info: 'Note auto',
        lbl_user_car_seats: 'Posti disponibili',
        lbl_end_time: 'Orario di arrivo',
        lbl_recurrency: 'Ricorrenza',
        lbl_passenger: 'Passeggeri',
        lbl_spaces_left: 'liberi',
        lbl_driver_contact: 'contatta il conducente',
        lbl_trip_ask: 'Richiedi passaggio',
        lbl_trip_rejected: 'Passaggio rifiutato',
        lbl_trip_requested: 'Passaggio richiesto',
        lbl_trip_accepted: 'Passaggio accettato',
        lbl_requests: 'Richeste di partecipazione',
        lbl_todaytrips: 'Viaggi di oggi',
        lbl_components: 'Componenti',
        lbl_rating_pass_avg: 'Voto medio viaggi accettati',
        lbl_rating_driver_avg: 'Voto medio viaggi offerti ',
        tab_participate: 'Partecipo',
        tab_offer: 'Offro',
        title_setrecurrency: 'Imposta ricorrenza',
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
        popup_datepicker_today: 'Oggi',
        send_msg_placeholder: 'Scrivi un messaggio',
        notif_short_chat: 'Nuovo messaggio da {{name}}',
        notif_short_avail: 'Trovato un viaggio',
        notif_short_request: '{{name}} chiede di partecipare al tuo viaggio',
        notif_short_response_ok: 'Viaggio confermato',
        notif_short_response_ko: 'Viaggio rifiutato',
        toast_error_generic: 'OPS! Problema...',
        toast_auto_disabled: 'Per offrire un viaggio devi aggiungere un\'auto al tuo profilo',
        toast_trip_offered: 'Il tuo viaggio è stato offerto',
        toast_booking_accepted: 'La prenotazione è stata accettata',
        toast_booking_rejected: 'La prenotazione è stata rifiutata',
        toast_notification_deleted: 'La notifica è stata cancellata'
    });

    $translateProvider.preferredLanguage('it');
    $translateProvider.useSanitizeValueStrategy('escape');
});
