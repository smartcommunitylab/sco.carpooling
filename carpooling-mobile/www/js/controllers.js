angular.module('carpooling.controllers', [])

.controller('AppCtrl', function ($scope) {})

.controller('HomeCtrl', function ($scope) {})

.controller('PartecipoCtrl', function ($scope, UserSrv, PassengerSrv) {
    $scope.travelProfile = 'empty';
    $scope.travelDateFormat = 'dd MMMM yyyy';
    $scope.travelTimeFormat = 'hh:mm';

    /*Just for example*/
    /*
    $scope.passengerTrips = [
        {
            "from": {
                "name": "Via Fiume",
                "address": "Via Fiume",
                "latitude": 46.065487,
                "longitude": 11.131346,
                "range": 1,
                "coordinates": [
                    46.065487,
                    11.131346
                ]
            },
            "to": {
                "name": "Muse",
                "address": "Muse",
                "latitude": 46.063266,
                "longitude": 11.113062,
                "range": 1,
                "coordinates": [
                    46.063266,
                    11.113062
                ]
            },
            "when": 1443425400000,
            "monitored": true
        }
    ];
    */

    $scope.getTravelProfile = function () {
        UserSrv.getTravelProfile().then(function (data) {
            $scope.travelProfile = data;
        });
    };

    $scope.passengerTrips = [];

    PassengerSrv.getPassengerTrips().then(
        function (trips) {
            $scope.passengerTrips = trips;
        },
        function (error) {
            // TODO: handle error
        }
    );
})

.controller('OffroCtrl', function ($scope, DriverSrv) {
    $scope.travelDateFormat = 'dd MMMM yyyy';
    $scope.travelTimeFormat = 'hh:mm';

    $scope.driverTrips = [];

    DriverSrv.getDriverTrips().then(
        function (trips) {
            $scope.driverTrips = trips;
        },
        function (error) {
            // TODO: handle error
        }
    );
})

// NOTE OffriCtrl
.controller('OffriCtrl', function ($scope, $filter, $ionicModal, $ionicPopup, $ionicLoading, Config, MapSrv, GeoSrv, PlanSrv, DriverSrv, StorageSrv) {
    $scope.locations = {
        'from': {
            'name': '',
            'latlng': ''
        },
        'to': {
            'name': '',
            'latlng': ''
        }
    };

    /*
     * Autocompletion stuff
     */
    // names: array with the names of the places
    // coordinates: object that maps a place name with an object that has its coordinate in key 'latlng'
    $scope.places = {
        'names': [],
        'coordinates': {}
    };

    $scope.typing = function (typedthings) {
        var result = typedthings;
        var newPlaces = PlanSrv.getTypedPlaces(typedthings);
        newPlaces.then(function (data) {
            //merge with favorites and check no double values
            $scope.places.names = data;
            $scope.places.coordinates = PlanSrv.getNames();
        });
    };

    $scope.setLocationFrom = function (name) {
        $scope.locations['from'].name = name;
        $scope.locations['from'].latlng = $scope.places.coordinates[name].latlng;
    };
    $scope.setLocationTo = function (name) {
        $scope.locations['to'].name = name;
        $scope.locations['to'].latlng = $scope.places.coordinates[name].latlng;
    };

    /*
     * Map stuff
     */
    var mapId = 'modalMap';
    var selectedField = null;

    $scope.modalMap = null;

    angular.extend($scope, {
        center: {
            lat: 46.067819,
            lng: 11.121306,
            zoom: 8
        },
        defaults: {
            scrollWheelZoom: false
        },
        events: {
            map: {
                enable: ['click']
            }
        }
    });

    // Modal Map
    $ionicModal.fromTemplateUrl('templates/modal_map.html', {
        id: '1',
        scope: $scope,
        backdropClickToClose: true,
        animation: 'slide-in-up'
    }).then(function (modal) {
        $scope.modalMap = modal;
    });

    $scope.initMap = function () {
        MapSrv.initMap(mapId).then(function () {
            $scope.$on('leafletDirectiveMap.' + mapId + '.click', function (event, args) {

                $ionicLoading.show();

                // TODO: strings, actions
                var confirmPopup = null;
                var confirmPopupOptions = {
                    title: $filter('translate')('modal_map_confirm'),
                    template: '',
                    buttons: [
                        {
                            text: $filter('translate')('cancel'),
                            type: 'button'
                        },
                        {
                            text: $filter('translate')('ok'),
                            type: 'button-carpooling'
                        }
                    ]
                };

                var fillConfirmPopupOptions = function (placeName, coordinates) {
                    confirmPopupOptions.template = placeName;
                    confirmPopupOptions.buttons[1].onTap = function () {
                        if (!!selectedField) {
                            $scope.locations[selectedField].name = placeName;
                            $scope.locations[selectedField].coordinates = coordinates;
                        }
                        $scope.hideModalMap();
                    };
                };

                GeoSrv.geolocate([args.leafletEvent.latlng.lat, args.leafletEvent.latlng.lng]).then(
                    function (data) {
                        $ionicLoading.hide();
                        var placeName = '';
                        var coordinates = '';

                        if (!!data.response.docs[0]) {
                            placeName = PlanSrv.generatePlaceString(data.response.docs[0]);
                            coordinates = data.response.docs[0].coordinate;
                        } else {
                            placeName = args.leafletEvent.latlng.lat + ',' + args.leafletEvent.latlng.lng;
                            coordinates = args.leafletEvent.latlng.lat + ',' + args.leafletEvent.latlng.lng;
                        }

                        fillConfirmPopupOptions(placeName, coordinates);
                        confirmPopup = $ionicPopup.confirm(confirmPopupOptions);
                        console.log(placeName + ' (' + coordinates + ')');
                    },
                    function (err) {
                        $ionicLoading.hide();
                        var placeName = args.leafletEvent.latlng.lat + ',' + args.leafletEvent.latlng.lng;
                        var coordinates = args.leafletEvent.latlng.lat + ',' + args.leafletEvent.latlng.lng;
                        fillConfirmPopupOptions(placeName, coordinates);
                        confirmPopup = $ionicPopup.confirm(confirmPopupOptions);
                        console.log(placeName + ' (' + coordinates + ')');
                    }
                );
            });
        });
    };

    $scope.showModalMap = function (field) {
        selectedField = field;

        $scope.modalMap.show().then(function () {
            // resize map!
            var modalMapElement = document.getElementById('modal-map-container');
            if (modalMapElement != null) {
                MapSrv.resizeElementHeight(modalMapElement, mapId);
                MapSrv.refresh(mapId);
            }
        });
    };

    /* Time Picker */
    $scope.timepickerObj = {
        inputEpochTime: ((new Date()).getHours() * 60 * 60) + ((new Date()).getMinutes() * 60),
        step: 1,
        format: 24,
        titleLabel: $filter('translate')('popup_timepicker_title'),
        setLabel: $filter('translate')('ok'),
        closeLabel: $filter('translate')('cancel'),
        setButtonType: 'button-carpooling',
        closeButtonType: '',
        callback: function (val) { //Mandatory
            if (typeof (val) === 'undefined') {
                console.log('Time not selected');
            } else {
                var selectedTime = new Date(val * 1000);
                console.log('Selected epoch is : ', val, 'and the time is ', selectedTime.getUTCHours(), ':', selectedTime.getUTCMinutes(), 'in UTC');
                $scope.timepickerObj.inputEpochTime = val;
            }
        }
    };

    /* Date Picker */
    $scope.dateMask = 'dd MMMM yyyy';
    var yesterday = new Date();
    yesterday.setMilliseconds(yesterday.getMilliseconds() - (1000 * 60 * 60 * 24));

    $scope.datepickerObj = {
        titleLabel: $filter('translate')('popup_datepicker_title'),
        todayLabel: $filter('translate')('popup_datepicker_today'),
        closeLabel: $filter('translate')('cancel'),
        setLabel: $filter('translate')('ok'),
        errorMsgLabel: null,
        setButtonType: 'button-carpooling',
        todayButtonType: 'button-carpooling',
        closeButtonType: '',
        templateType: 'popup',
        modalHeaderColor: '',
        modalFooterColor: '',
        from: yesterday,
        to: new Date(2019, 12, 31),
        inputDate: new Date(),
        weekDaysList: Config.getDoWList(),
        monthList: Config.getMonthList(),
        mondayFirst: true,
        disableDates: null,
        callback: function (val) { //Mandatory
            if (typeof (val) === 'undefined') {
                console.log('No date selected');
            } else {
                console.log('Selected date is : ', val);
                $scope.datepickerObj.inputDate = val;
            }
        },
    };

    /*
     * Recurrence popup stuff
     */
    $scope.hideModalMap = function () {
        $scope.modalMap.hide();
    };

    $scope.getDoW = function () {
        return Config.getDoW();
    };

    $scope.getArray = function (num) {
        var array = new Array(num);
        for (var i = 0; i < num; i++) {
            array[i] = i + 1;
        }
        return array;
    }

    $scope.recurrence = {
        isRecurrent: false,
        recurrenceType: 'w',
        recurrenceD: '1',
        recurrenceDoW: [],
        recurrenceDoWstring: ''
    };

    $scope.recurrentPopupDoW = [
        {
            name: 'dow_monday',
            shortname: 'dow_monday_short',
            value: 2,
            checked: false
        },
        {
            name: 'dow_tuesday',
            shortname: 'dow_tuesday_short',
            value: 3,
            checked: false
        },
        {
            name: 'dow_wednesday',
            shortname: 'dow_wednesday_short',
            value: 4,
            checked: false
        },
        {
            name: 'dow_thursday',
            shortname: 'dow_thursday_short',
            value: 5,
            checked: false
        },
        {
            name: 'dow_friday',
            shortname: 'dow_friday_short',
            value: 6,
            checked: false
        },
        {
            name: 'dow_saturday',
            shortname: 'dow_saturday_short',
            value: 7,
            checked: false
        },
        {
            name: 'dow_sunday',
            shortname: 'dow_sunday_short',
            value: 1,
            checked: false
        }
    ];

    $scope.updateRecurrence = function () {
        // update recurrenceDoW and recurrenceDoWstring
        $scope.recurrence.recurrenceDoW = [];
        $scope.recurrence.recurrenceDoWstring = '';

        for (var i = 0; i < $scope.recurrentPopupDoW.length; i++) {
            var dow = $scope.recurrentPopupDoW[i];
            if (dow.checked) {
                $scope.recurrence.recurrenceDoW.push(dow.value);
                if (!!$scope.recurrence.recurrenceDoWstring) {
                    $scope.recurrence.recurrenceDoWstring = $scope.recurrence.recurrenceDoWstring + ', ';
                }
                $scope.recurrence.recurrenceDoWstring = $scope.recurrence.recurrenceDoWstring + $filter('translate')(dow.name);
            }
        }

        $scope.recurrence.isRecurrent = $scope.recurrence.recurrenceDoW.length > 0;
    };

    var recurrentPopup = {
        templateUrl: 'templates/popup_offri.html',
        title: $filter('translate')('title_setrecurrence'),
        scope: $scope,
        buttons: [
            {
                text: $filter('translate')('cancel')
            },
            {
                text: $filter('translate')('ok'),
                type: 'button-carpooling'
            }
        ]
    };

    $scope.showRecurrentPopup = function () {
        $ionicPopup.show(recurrentPopup).then(
            function (res) {
                $scope.updateRecurrence();
            }
        );
    };

    $scope.$watch('recurrence.isRecurrent', function (newValue, oldValue) {
        if (newValue !== oldValue && !!newValue) {
            $scope.showRecurrentPopup();
        }
    });

    $scope.offer = function () {
        // TODO: create Travel object and send it using DriverSrv
        var posts = StorageSrv.getUser().auto.posts;
        console.log('posts: ' + posts);
    };
})

// NOTE: CercaViaggioCtrl
.controller('CercaViaggioCtrl', function ($scope, Config, $q, $http, $ionicModal, $ionicLoading, $filter, $state, $window, PlanSrv, GeoSrv, MapSrv, $ionicPopup, PassengerSrv) {
    $scope.dateTimestamp = null;
    $scope.hourTimestamp = null;

    $scope.locations = {
        'from': {
            'name': '',
            'latlng': ''
        },
        'to': {
            'name': '',
            'latlng': ''
        }
    };

    /*
     * Autocompletion stuff
     */
    // names: array with the names of the places
    // coordinates: object that maps a place name with an object that has its coordinate in key 'latlng'
    $scope.places = {
        'names': [],
        'coordinates': {}
    };

    $scope.typing = function (typedthings) {
        var result = typedthings;
        var newPlaces = PlanSrv.getTypedPlaces(typedthings);
        newPlaces.then(function (data) {
            //merge with favorites and check no double values
            $scope.places.names = data;
            $scope.places.coordinates = PlanSrv.getNames();
        });
    };

    $scope.setLocationFrom = function (name) {
        $scope.locations['from'].name = name;
        $scope.locations['from'].latlng = $scope.places.coordinates[name].latlng;
    };
    $scope.setLocationTo = function (name) {
        $scope.locations['to'].name = name;
        $scope.locations['to'].latlng = $scope.places.coordinates[name].latlng;
    };

    /*
     * Map stuff
     */
    var mapId = 'modalMap';
    var selectedField = null;

    $scope.modalMap = null;

    angular.extend($scope, {
        center: {
            lat: 46.067819,
            lng: 11.121306,
            zoom: 8
        },
        defaults: {
            scrollWheelZoom: false
        },
        events: {}
    });

    // Modal Map
    $ionicModal.fromTemplateUrl('templates/modal_map.html', {
        id: '1',
        scope: $scope,
        backdropClickToClose: true,
        animation: 'slide-in-up'
    }).then(function (modal) {
        $scope.modalMap = modal;
    });

    $scope.initMap = function () {
        MapSrv.initMap(mapId).then(function () {
            $scope.$on('leafletDirectiveMap.' + mapId + '.click', function (event, args) {

                $ionicLoading.show();

                // TODO: strings, actions
                var confirmPopup = null;
                var confirmPopupOptions = {
                    title: $filter('translate')('modal_map_confirm'),
                    template: '',
                    buttons: [
                        {
                            text: $filter('translate')('cancel'),
                            type: 'button'
                        },
                        {
                            text: $filter('translate')('ok'),
                            type: 'button-carpooling'
                        }
                    ]
                };

                var fillConfirmPopupOptions = function (placeName, coordinates) {
                    confirmPopupOptions.template = placeName;
                    confirmPopupOptions.buttons[1].onTap = function () {
                        if (!!selectedField) {
                            $scope.locations[selectedField].name = placeName;
                            $scope.locations[selectedField].coordinates = coordinates;
                        }
                        $scope.hideModalMap();
                    };
                };

                GeoSrv.geolocate([args.leafletEvent.latlng.lat, args.leafletEvent.latlng.lng]).then(
                    function (data) {
                        $ionicLoading.hide();
                        var placeName = '';
                        var coordinates = '';

                        if (!!data.response.docs[0]) {
                            placeName = PlanSrv.generatePlaceString(data.response.docs[0]);
                            coordinates = data.response.docs[0].coordinate;
                        } else {
                            placeName = args.leafletEvent.latlng.lat + ',' + args.leafletEvent.latlng.lng;
                            coordinates = args.leafletEvent.latlng.lat + ',' + args.leafletEvent.latlng.lng;
                        }

                        fillConfirmPopupOptions(placeName, coordinates);
                        confirmPopup = $ionicPopup.confirm(confirmPopupOptions);
                        console.log(placeName + ' (' + coordinates + ')');
                    },
                    function (err) {
                        $ionicLoading.hide();
                        var placeName = args.leafletEvent.latlng.lat + ',' + args.leafletEvent.latlng.lng;
                        var coordinates = args.leafletEvent.latlng.lat + ',' + args.leafletEvent.latlng.lng;
                        fillConfirmPopupOptions(placeName, coordinates);
                        confirmPopup = $ionicPopup.confirm(confirmPopupOptions);
                        console.log(placeName + ' (' + coordinates + ')');
                    }
                );
            });
        });
    };

    $scope.showModalMap = function (field) {
        selectedField = field;

        $scope.modalMap.show().then(function () {
            // resize map!
            var modalMapElement = document.getElementById('modal-map-container');
            if (modalMapElement != null) {
                MapSrv.resizeElementHeight(modalMapElement, mapId);
                MapSrv.refresh(mapId);
            }
        });
    };

    /*
     * Halfway stops stuff
     */
    $scope.hasHalfwayStops = false;

    /*
     * Recurrence popup stuff
     */
    $scope.hideModalMap = function () {
        $scope.modalMap.hide();
    };

    /* Time Picker */
    $scope.timepickerObj = {
        inputEpochTime: ((new Date()).getHours() * 60 * 60) + ((new Date()).getMinutes() * 60),
        step: 1,
        format: 24,
        titleLabel: $filter('translate')('popup_timepicker_title'),
        setLabel: $filter('translate')('ok'),
        closeLabel: $filter('translate')('cancel'),
        setButtonType: 'button-carpooling',
        closeButtonType: '',
        callback: function (val) { //Mandatory
            if (typeof (val) === 'undefined') {
                console.log('Time not selected');
            } else {
                var selectedTime = new Date(val * 1000);
                console.log('Selected epoch is : ', val, 'and the time is ', selectedTime.getUTCHours(), ':', selectedTime.getUTCMinutes(), 'in UTC');
                $scope.timepickerObj.inputEpochTime = val;
            }
        }
    };

    /* Date Picker */
    $scope.dateMask = 'dd MMMM yyyy';
    $scope.datepickerObj = {
        titleLabel: $filter('translate')('popup_datepicker_title'),
        todayLabel: $filter('translate')('popup_datepicker_today'),
        closeLabel: $filter('translate')('cancel'),
        setLabel: $filter('translate')('ok'),
        errorMsgLabel: null,
        setButtonType: 'button-carpooling',
        todayButtonType: 'button-carpooling',
        closeButtonType: '',
        templateType: 'popup',
        modalHeaderColor: '',
        modalFooterColor: '',
        from: new Date(),
        to: new Date(2019, 12, 31),
        inputDate: new Date(),
        weekDaysList: Config.getDoWList(),
        monthList: Config.getMonthList(),
        mondayFirst: true,
        disableDates: null,
        callback: function (val) { //Mandatory
            if (typeof (val) === 'undefined') {
                console.log('No date selected');
            } else {
                console.log('Selected date is : ', val);
                $scope.datepickerObj.inputDate = val;
            }
        },
    };

    $scope.allSearchNotifications = function () {
        console.log('Push Notification Change', $scope.allSearchNotifications.checked);
    };

    /* Search Trip */
    $scope.searchTravel = function () {
        var travelRequest = {
            'from': {
                'name': $scope.locations['from'].name,
                'address': $scope.locations['from'].name,
                'latitude': parseFloat($scope.locations['from'].latlng.split(',')[0]),
                'longitude': parseFloat($scope.locations['from'].latlng.split(',')[1])
            },
            'to': {
                'name': $scope.locations['to'].name,
                'address': $scope.locations['to'].name,
                'latitude': parseFloat($scope.locations['to'].latlng.split(',')[0]),
                'longitude': parseFloat($scope.locations['to'].latlng.split(',')[1])
            },
            'when': ($scope.timepickerObj.inputEpochTime * 1000) + ($scope.datepickerObj.inputDate.getTime()),
            'monitored': ($scope.allSearchNotifications.checked)
        };

        console.log(travelRequest);

        PassengerSrv.searchTrip(travelRequest).then(
            function (searchResults) {
                console.log('Done trip search');
                $state.go('app.cercaviaggi', {'searchResults': searchResults});
            },
            function (error) {
                // TODO: handle search error
                console.log(error);
            }
        );
    };
})

.controller('CercaViaggiCtrl', function ($scope, $state, $stateParams, PassengerSrv) {
    $scope.passengerTripsFound = $stateParams['searchResults'];

    // FIXME: temporary mock results TO BE REMOVED
    $scope.passengerTripsFound = [
        {
            "from": {
                "name": "Via Fiume",
                "address": "Via Fiume",
                "latitude": 46.065487,
                "longitude": 11.131346,
                "range": 1,
                "coordinates": [
                    46.065487,
                    11.131346
                ]
            },
            "to": {
                "name": "Muse",
                "address": "Muse",
                "latitude": 46.063266,
                "longitude": 11.113062,
                "range": 1,
                "coordinates": [
                    46.063266,
                    11.113062
                ]
            },
            "when": 1443425400000,
            "monitored": true
        }
    ];

    console.log($scope.passengerTripsFound);

    $scope.selectTrip = function (index) {
        $state.go('app.viaggio', {
            'trip': $scope.passengerTripsFound[index]
        });
    };
})

.controller('ViaggioCtrl', function ($scope, PassengerSrv, $state, $stateParams) {
    // TODO: build UI and fill it here
    console.log($stateParams['trip']);
})

.controller('NotificationCtrl', function ($scope, $filter, $state) {
    $scope.notificationType = [
        {
            name: 'message',
            value: 'Messaggio',
            image: 'ion-android-chat'
        },
        {
            name: 'trip_request',
            value: 'Richiesta di viaggio',
            image: 'ion-android-car'
        },
        {
            name: 'trip_response',
            value: 'Risposta ricerca viaggio',
            image: 'ion-android-search'
        },
        {
            name: 'driver_rating',
            value: 'Valutazione conducente',
            image: 'ion-android-star'
        },
        {
            name: 'passenger_rating',
            value: 'Valutazione passeggero',
            image: 'ion-android-star'
        }
    ];

    $scope.notifications = [
        {
            id: '1',
            type: $scope.notificationType[0],
            short_text: 'Nuovo messaggio da Mario Rossi',
            data_object: null,
            timestamp: '1447865802692'
        },
        {
            id: '2',
            type: $scope.notificationType[1],
            short_text: 'Giulia Bianchi chiede di partecipare al tuo viaggio Trento - Rovereto',
            data_object: null,
            timestamp: '1447865802692'
        },
        {
            id: '3',
            type: $scope.notificationType[2],
            short_text: 'Trovato un viaggio Trento - Pergine',
            data_object: null,
            timestamp: '1447918789919'
        },
        {
            id: '4',
            type: $scope.notificationType[3],
            short_text: 'Valuta il conducente del viaggio Rovereto - Mattarello',
            data_object: null,
            timestamp: '1447918789919'
        },
        {
            id: '5',
            type: $scope.notificationType[4],
            short_text: 'Valuta i passeggeri del tuo viaggio Verona - Rovereto',
            data_object: null,
            timestamp: '1447918789919'
        }
    ];

    $scope.showNotification = function (notific) {
        switch (notific.type) {
            case $scope.notificationType[0]:
                // messages - to chat
                $state.go('app.chat');
                break;
            case $scope.notificationType[1]:
                // trip request - to mytrip
                $state.go('app.mioviaggio');
                break;
            case $scope.notificationType[2]:
                // trip response - to trip
                $state.go('app.home.partecipo');
                break;
            case $scope.notificationType[3]:
                // driver rating - to driver profile (trip data)
                $state.go('app.home.partecipo');
                break;
            case $scope.notificationType[4]:
                // passenger rating - to passenger profile (mytrip data)
                $state.go('app.mioviaggio');
                break;
            default:
                break;
        };
    };

    $scope.messages = [
        {
            id: '1',
            userId: 1,
            text: 'Ciao Mario',
            timestamp: '1447865802692',
            userId_target: 2
        },
        {
            id: '2',
            userId: 1,
            text: 'E\' possibile aggiungere una tappa intermedia a Mattarello nel tuo viaggio? Grazie',
            timestamp: '1447865802692',
            userId_target: 2
        },
        {
            id: '3',
            userId: 2,
            text: 'Ciao Stefano, certo nessun problema. Passo davanti alla Coop mi puoi aspettare li',
            timestamp: '1447918789919',
            userId_target: 1
        }
    ];

    // test data for users
    $scope.tmp_users = [
        {
            id: 1,
            name: 'Stefano',
            surname: 'Bianchi',
            email: 'stefano.bianchi@prova.it'
        },
        {
            id: 2,
            name: 'Mario',
            surname: 'Rossi',
            email: 'mario.rossi@prova.it'
        }
    ];

    $scope.getUserById = function (id) {
        for (var i = 0; i < $scope.tmp_users.length; i++) {
            var us = $scope.tmp_users[i];
            if (us.id == id) {
                return us;
            }
        }
    }

    $scope.me = {
        id: 1
    };

    $scope.isMe = function (id) {
        return id == $scope.me.id;
    };
})

.controller('UserInfoCtrl', function ($scope, $rootScope, StorageSrv, UserSrv) {
    $scope.user = angular.copy(StorageSrv.getUser());

    // FIXME: temporary control, waiting for the deploy
    var hasAuto = function (auto) {
        // return !!$scope.user.auto
        if (!!auto && !!auto.description && auto.posts !== 0) {
            return true;
        }
        return false;
    };

    $scope.editMode = false;
    $scope.edit = {
        hasAuto: hasAuto($scope.user.auto),
        postsAvailable: [1, 2, 3, 4, 5, 6, 7]
    };

    $scope.toggleEditMode = function () {
        $scope.editMode = !$scope.editMode;
    };

    $scope.cancelChanges = function () {
        $scope.toggleEditMode();
        $scope.user = angular.copy(StorageSrv.getUser());
        $scope.edit.hasAuto = hasAuto($scope.user.auto);
    };

    $scope.saveProfile = function () {
        UserSrv.saveAuto(!!$scope.user.auto ? $scope.user.auto : {}).then(
            function (data) {
                $scope.toggleEditMode();
                UserSrv.getUser($scope.user.userId).then(
                    function () {
                        $scope.user = angular.copy(StorageSrv.getUser());
                        $scope.edit.hasAuto = hasAuto($scope.user.auto);
                    }
                );
            },
            function (error) {
                // TODO: handle saveAuto error
            }
        );
    };

    $scope.$watch('edit.hasAuto', function (newValue, oldValue) {
        if (newValue === oldValue) {
            return;
        }

        if (newValue) {
            // true
            $scope.user.auto = {
                posts: 4,
                description: '...'
            };
        } else {
            // false
            $scope.user.auto = null;
        }
    });
});
