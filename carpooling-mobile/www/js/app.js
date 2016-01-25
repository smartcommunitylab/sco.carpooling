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
    'carpooling.controllers.storico',
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

    $rootScope.manageLocalNotification = function (local_notification) {
        var txt = local_notification.alert ?
            local_notification.alert : local_notification.aps && local_notification.aps.alert ? local_notification.aps.alert : null;
        if (txt) {
            if (cordova && cordova.plugins && cordova.plugins.notification) {
                try {
                    //console.log('initializing notifications...');
                    //cordova.plugins.notification.local.cancelAll();
                    var notific = {
                        id: local_notification.push_hash,
                        title: "CarPooling",
                        text: txt,
                        autoCancel: true,
                        //firstAt: monday_9_am,
                        //every: "week",
                        //sound: "file://sounds/reminder.mp3",
                        icon: "file://resources/icon.png",
                        data: {
                            id: local_notification.push_hash,
                            urlHash: local_notification.urlHash
                        },
                    }
                    if (notific) {
                        cordova.plugins.notification.local.schedule(notific);
                    }
                    cordova.plugins.notification.local.on("click", function (notification) {
                        var notific_data = JSON.parse(notification.data);
                        if (notific_data.urlHash) {
                            var s_path = notific_data.urlHash.replace(new RegExp("/", 'g'), ".");
                            s_path = s_path.substring(2, s_path.length);
                            $state.go(s_path);
                        } else {
                            // notific without a urlHash attribute but with a url attribute
                            if (notific_data.url) {
                                var s_path = notific_data.url.replace(new RegExp("/", 'g'), ".");
                                s_path = s_path.substring(2, s_path.length);
                                $state.go(s_path);
                            } else {}
                        }
                    });
                } catch (ex) {}
            }
        }
    };

    $rootScope.isChat = function (location_hash) {
        var params_chat = [];
        var loc_hash = location_hash + "";
        if (loc_hash.indexOf("#") > -1) {
            var curr_view_path = loc_hash.split("#");
            var curr_path = curr_view_path[1] + "";
            if (curr_path.indexOf("/chat/") > -1) {
                var params = curr_path.split("/chat/");
                params_chat = params[1].split("/");
            }
        }
        return params_chat;
    };

    $rootScope.isNotification = function (location_hash) {
        var corrPath = false;
        var loc_hash = location_hash + "";
        if (loc_hash.indexOf("#") > -1) {
            var curr_view_path = loc_hash.split("#");
            var curr_path = curr_view_path[1] + "";
            if (curr_path.indexOf("notifiche") > -1) {
                corrPath = true;
            }
        }
        return corrPath;
    };

    $rootScope.updateMyNotification = function (travelId, senderId) {
        UserSrv.readNotifications(0, 10).then(
            function (notifics) {
                var notifications = [];
                notifications = notifics ? notifics : [];
                for (var i = 0; i < notifications.length; i++) {
                    if (notifications[i].travelId == travelId && notifications[i].data.senderId == senderId) {
                        UserSrv.markNotification(notifications[i].id).then(
                            function () {},
                            function (err) {}
                        );
                    }
                }
            },
            function (err) {
                console.error(err);
            }
        );
    };

    $rootScope.pushRegistration = function (userId) {
        var channel = 'CarPooling_' + userId;
        try {
            if (window.ParsePushPlugin) {
                if (isAndroid) {
                    window.ParsePushPlugin.subscribe(channel, function () {
                        //console.log("Succes in channel " + channel + " creation");
                    });

                    window.ParsePushPlugin.on('openPN', function (pn) {
                        if (pn.urlHash) {
                            var s_path = pn.urlHash.replace(new RegExp("/", 'g'), ".");
                            s_path = s_path.substring(2, s_path.length);
                            //window.location.path = "/notifiche";
                            //window.location.reload(true);
                            $state.go(s_path);
                        } else {
                            // urlHash not present. I open the app in the last page/view
                        }
                    });
                    window.ParsePushPlugin.on('receivePN', function (pn) {
                        var chat_parameters = $rootScope.isChat(window.location);
                        if (chat_parameters.length > 0) {
                            var travelId = chat_parameters[0];
                            var senderId = chat_parameters[1];
                            if (pn.cp_senderId && pn.cp_travelId) {
                                if (pn.cp_senderId == senderId && pn.cp_travelId == travelId) {
                                    $state.go('app.chat', { //transitionTo
                                        travelId: travelId,
                                        personId: senderId
                                    }, {
                                        reload: true
                                    });
                                    $rootScope.updateMyNotification(travelId, senderId);
                                } else {
                                    $rootScope.manageLocalNotification(pn);
                                }
                            } else {
                                $rootScope.manageLocalNotification(pn);
                            }
                        } else {
                            if ($rootScope.isNotification(window.location)) {
                                // case app opened in notification list
                                //alert("In notification page update");
                                $state.go('app.notifiche', {}, {
                                    reload: true
                                });
                                //window.location.reload(true)
                            } else {
                                $rootScope.manageLocalNotification(pn);
                            }
                        }
                    });
                } else if (isIOS) {
                    window.ParsePushPlugin.register({
                        appId: Config.getAppId(),
                        clientKey: Config.getClientKey(),
                        ecb: "onNotification"
                    }, function () {
                        window.ParsePushPlugin.subscribe(channel, function () {
                            //console.log("Succes in channel " + channel + " creation");
                        });
                    }, function (e) {
                        console.log("Error in parse initialize");
                    });

                    onNotification = function (pn) {
                        var chat_parameters = $rootScope.isChat(window.location);
                        if (chat_parameters.length > 0) {
                            var travelId = chat_parameters[0];
                            var senderId = chat_parameters[1];
                            if (pn.cp_senderId && pn.cp_travelId) {
                                if (pn.cp_senderId == senderId && pn.cp_travelId == travelId) {
                                    $state.go('app.chat', { //transitionTo
                                        travelId: travelId,
                                        personId: senderId
                                    }, {
                                        reload: true
                                    });
                                    $rootScope.updateMyNotification(travelId, senderId);
                                } else {
                                    $rootScope.manageLocalNotification(pn);
                                }
                            } else {
                                $rootScope.manageLocalNotification(pn);
                            }
                        } else {
                            if ($rootScope.isNotification(window.location)) {
                                // case app opened in notification list
                                $state.go('app.notifiche', {}, {
                                    reload: true
                                });
                                //window.location.reload(true)
                            } else {
                                $rootScope.manageLocalNotification(pn);
                            }
                        }
                    }
                }

                /*if (window.ParsePushPlugin) {
                    window.ParsePushPlugin.subscribe(channel, function () {
                        //console.log("Succes in channel " + channel + " creation");
                    });
                    window.ParsePushPlugin.on('openPN', function (pn) {
                        //alert("in open notific" + JSON.stringify(pn));
                        if (pn.urlHash) {
                            var s_path = pn.urlHash.replace(new RegExp("/", 'g'), ".");
                            s_path = s_path.substring(2, s_path.length);
                            //window.location.path = "/notifiche";
                            //window.location.reload(true);
                            $state.go(s_path);
                        } else {
                            // urlHash not present. I open the app in the last page/view
                        }
                    });
                    window.ParsePushPlugin.on('receivePN', function (pn) {
                        var chat_parameters = $rootScope.isChat(window.location);
                        if (chat_parameters.length > 0) {
                            var travelId = chat_parameters[0];
                            var senderId = chat_parameters[1];
                            if (pn.cp_senderId && pn.cp_travelId) {
                                if (pn.cp_senderId == senderId && pn.cp_travelId == travelId) {
                                    $state.go('app.chat', { //transitionTo
                                        travelId: travelId,
                                        personId: senderId
                                    }, {
                                        reload: true
                                    });
                                    $rootScope.updateMyNotification(travelId, senderId);
                                } else {
                                    $rootScope.manageLocalNotification(pn);
                                }
                            } else {
                                $rootScope.manageLocalNotification(pn);
                            }
                        } else {
                            $rootScope.manageLocalNotification(pn);
                        }
                    });*/
                //
                //you can also listen to your own custom subevents
                //
                //ParsePushPlugin.on('receivePN:chat', chatEventHandler);
                //ParsePushPlugin.on('receivePN:serverMaintenance', serverMaintenanceHandler);*/
            }
        } catch (ex) {
            //console.log('Exception in parsepush registration ' + ex.message);
        }
    };

    $rootScope.isRecurrencyEnabled = Config.isRecurrencyEnabled;

    $rootScope.getNumber = function (num) {
        return Utils.getNumber(num);
    };

    $rootScope.loginStarted = false;
    $rootScope.login = function () {
        if ($rootScope.loginStarted) return;

        $rootScope.loginStarted = true;
        LoginSrv.login().then(
            function (data) {
                $rootScope.loginStarted = false;
                UserSrv.getUser(data.userId).then(function () {
                    $rootScope.pushRegistration(data.userId);
                    $state.go('app.home', {}, {
                        reload: true
                    });
                });
            },
            function (error) {
                $rootScope.loginStarted = false;
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

        //alert("Path app " + JSON.stringify(window.location));

        if (LoginSrv.userIsLogged()) {
            $rootScope.pushRegistration(StorageSrv.getUserId());
        } else {
            $rootScope.login();
        }
    });

    $rootScope.$on('$stateChangeStart',
        function (event, toState, toParams, fromState, fromParams) {
            if (!$rootScope.initialSetup && toState.name == 'app.home' && StorageSrv.getUserId() != null && !StorageSrv.isProfileComplete()) {
                $rootScope.initialSetup = !StorageSrv.isProfileComplete();
                event.preventDefault();
                $state.go('app.profilo');
            }
        }
    );
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

    .state('app.storico', {
        url: '/storico',
        cache: false,
        views: {
            'menuContent': {
                templateUrl: 'templates/storico.html',
                controller: 'StoricoCtrl'
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
        url: '/comunita/:communityId',
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
            'searchResults': {},
            'searchParams': {}
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
        params: {
            'communityId': null
        },
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
        params: {
            'user': null,
            'communityFrom': null,
            'editMode': null
        },
        views: {
            'menuContent': {
                templateUrl: 'templates/profilo.html',
                controller: 'UserInfoCtrl'
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
    $urlRouterProvider.otherwise(function ($injector) {
        var logged = $injector.get('LoginSrv').userIsLogged();
        if (!logged) {
            return '/';
        }
        return '/app/home';
    });
})

.config(function ($translateProvider) {
    $translateProvider.translations('it', {
        app_name: 'CARpooling',
        cancel: 'Annulla',
        yes: 'Si',
        no: 'No',
        ok: 'OK',
        action_chat: 'Chat',
        action_rate: 'Valuta',
        action_rejectbtn: 'Rifiuta',
        action_acceptbtn: 'Accetta',
        action_rate_passenger: 'Valuta passeggero',
        action_rate_driver: 'Valuta guidatore',
        action_reject: 'Rifiuta richiesta',
        menu_home: 'Home',
        menu_community: 'Comunità',
        menu_chat: 'Chat',
        menu_history: 'Storico viaggi',
        menu_notifications: 'Notifiche',
        menu_profile: 'Profilo',
        menu_logout: 'Logout',
        modal_map: 'Scegli da mappa',
        modal_map_confirm: 'Conferma selezione',
        msg_talk: 'dice',
        lbl_all: 'tutti',
        lbl_driver: 'guidatore',
        lbl_passenger: 'passeggero',
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
        lbl_when: 'Alle',
        lbl_halfwaystops: 'Fermate intermedie',
        lbl_halfwaystops_none: 'Nessuna',
        lbl_halfwaystops_agree: 'Da concordare con il conducente',
        lbl_halfwaystops_onrequest: 'Su richiesta',
        lbl_date: 'Data',
        lbl_time: 'Ora',
        lbl_time_departure: 'Ora di partenza',
        lbl_recurrenttrip: 'Viaggio ricorrente',
        lbl_recurrency_none: 'Nessuna',
        lbl_offri: 'Pubblica',
        lbl_cerca: 'Cerca',
        lbl_mycommunity: 'Nelle mie community',
        lbl_allcommunity: 'In tutte le community',
        lbl_allsearchnotifications: 'Desidero ricevere tutte le notifiche per questa ricerca',
        lbl_start_time: 'Orario di partenza',
        lbl_user_anonymous: 'Anonimo',
        lbl_user_car_owner: 'Automunito',
        lbl_user_car_info: 'Note auto',
        lbl_user_car_seats: 'Posti disponibili',
        lbl_user_car_seats_user: 'Posti disponibili ai passeggeri',
        lbl_end_time: 'Orario di arrivo',
        lbl_recurrency: 'Ricorrenza',
        lbl_passengers: 'Disponibilità posti',
        lbl_spaces_left: 'liberi',
        lbl_driver_contact: 'contatta il conducente',
        lbl_trip_ask: 'Richiedi passaggio',
        lbl_trip_rejected: 'Passaggio rifiutato',
        lbl_trip_requested: 'Passaggio richiesto',
        lbl_trip_accepted: 'Passaggio accettato',
        lbl_trip_rate: 'Valuta conducente',
        lbl_requests: 'Richeste di partecipazione',
        lbl_todaytrips: 'Viaggi di oggi',
        lbl_components: 'Componenti',
        lbl_number_pass_avg: 'Viaggi partecipati',
        lbl_number_driver_avg: 'Viaggi offerti ',
        lbl_rating_pass_avg: 'Voto medio viaggi partecipati',
        lbl_rating_driver_avg: 'Voto medio viaggi offerti ',
        lbl_tripstoconfirm: 'Viaggi da confermare',
        lbl_futuretrips: 'Prossimi viaggi',
        tab_participate: 'Partecipo',
        tab_offer: 'Offro',
        tab_participated: 'Partecipati',
        tab_offered: 'Offrerti',
        title_setrecurrency: 'Imposta ricorrenza settimanale',
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
        popup_rate_user: 'Valuta {{username}} come {{role}}',
        popup_confirm_accept: 'Vuoi davvero accettare la richiesta di {{username}}?',
        popup_confirm_reject: 'Vuoi davvero rifiutare la richiesta di {{username}}?',
        send_msg_placeholder: 'Scrivi un messaggio',
        notif_short_chat: 'Nuovo messaggio da {{name}}',
        notif_short_avail: 'Trovato un viaggio',
        notif_short_request: '{{name}} chiede di partecipare al tuo viaggio',
        notif_short_response_ok: 'Viaggio confermato',
        notif_short_response_ko: 'Viaggio rifiutato',
        notif_short_eval: 'Valuta il tuo viaggio',
        toast_error_generic: 'OPS! Problema...',
        toast_auto_disabled: 'Per offrire un viaggio devi aggiungere un\'auto al tuo profilo',
        toast_trip_offered: 'Il tuo viaggio è stato offerto',
        toast_booking_accepted: 'La prenotazione è stata accettata',
        toast_booking_rejected: 'La prenotazione è stata rifiutata',
        toast_notification_deleted: 'La notifica è stata cancellata',
        toast_rating_success: 'La valutazione è stata inviata',
        toast_err_empty_autodescription: 'Fornire la descrizione dell\'auto',
        popup_confirm_boarding: 'Conferma viaggio',
        popup_confirm_boarding_body: 'Confermi di aver partecipato a questo viaggio?'
    });

    $translateProvider.preferredLanguage('it');
    $translateProvider.useSanitizeValueStrategy('escape');
});
