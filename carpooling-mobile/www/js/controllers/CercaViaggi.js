angular.module('carpooling.controllers.cercaviaggi', [])

.controller('CercaViaggioCtrl', function ($scope, $q, $http, $ionicModal, $ionicLoading, $filter, $state, $window, Config, Utils, PlanSrv, GeoSrv, MapSrv, $ionicPopup, PassengerSrv) {
    $scope.travelRequest = {
        'from': {
            'name': '',
            'address': '',
            'latitude': 0,
            'longitude': 0
        },
        'to': {
            'name': '',
            'address': '',
            'latitude': 0,
            'longitude': 0
        },
        'when': 0,
        'monitored': false
    };

    // FIXME: communities are not used right now in client-side search
    $scope.communities = {
        enabled: false,
        useMyCommunities: false
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
        $scope.travelRequest['from'].name = name;
        $scope.travelRequest['from'].address = name;
        var coordinates = $scope.places.coordinates[name].latlng.split(',');
        $scope.travelRequest['from'].latitude = parseFloat(coordinates[0]);
        $scope.travelRequest['from'].longitude = parseFloat(coordinates[1]);
    };

    $scope.setLocationTo = function (name) {
        $scope.travelRequest['to'].name = name;
        $scope.travelRequest['to'].address = name;
        var coordinates = $scope.places.coordinates[name].latlng.split(',');
        $scope.travelRequest['to'].latitude = parseFloat(coordinates[0]);
        $scope.travelRequest['to'].longitude = parseFloat(coordinates[1]);
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

                var fillConfirmPopupOptions = function (name, coordinates) {
                    confirmPopupOptions.template = name;
                    confirmPopupOptions.buttons[1].onTap = function () {
                        if (!!selectedField) {
                            $scope.travelRequest[selectedField].name = name;
                            $scope.travelRequest[selectedField].address = name;
                            var splittedCoords = coordinates.split(',');
                            $scope.travelRequest[selectedField].latitude = parseFloat(splittedCoords[0]);
                            $scope.travelRequest[selectedField].longitude = parseFloat(splittedCoords[1]);
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
     * Recurrence popup stuff
     */
    $scope.hideModalMap = function () {
        $scope.modalMap.hide();
    };

    /* Date Picker */
    $scope.dateMask = 'dd MMMM yyyy';
    var today = new Date();
    today.setHours(0, 0, 0, 0);
    var yesterday = angular.copy(today);
    yesterday.setHours(-24);

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
        to: new Date(2019, 12, 31, 23, 59, 59),
        inputDate: today,
        weekDaysList: Utils.getSDoWList(),
        monthList: Utils.getMonthList(),
        mondayFirst: true,
        disableDates: null,
        callback: function (val) { //Mandatory
            if (typeof (val) === 'undefined') {
                console.log('[datepicker] Date not selected');
            } else {
                /*console.log('Selected date is : ', val);*/
                $scope.datepickerObj.inputDate = val;
            }
        },
    };

    /* Time Picker */
    var now = new Date();
    $scope.timepickerObj = {
        inputEpochTime: (now.getHours() * 60 * 60) + (now.getMinutes() * 60),
        step: 1,
        format: 24,
        titleLabel: $filter('translate')('popup_timepicker_title'),
        setLabel: $filter('translate')('ok'),
        closeLabel: $filter('translate')('cancel'),
        setButtonType: 'button-carpooling',
        closeButtonType: '',
        callback: function (val) { //Mandatory
            if (typeof (val) === 'undefined') {
                console.log('[timepicker] Time not selected');
            } else {
                $scope.timepickerObj.inputEpochTime = val;
            }
        }
    };

    /* Search Trip */
    $scope.searchTravel = function () {
        // NOTE: 'from', 'to' and 'monitored' is updated directly on $scope.travelRequest
        var selectedDateTime = angular.copy($scope.datepickerObj.inputDate);
        selectedDateTime.setSeconds(selectedDateTime.getSeconds() + $scope.timepickerObj.inputEpochTime);
        $scope.travelRequest['when'] = selectedDateTime.getTime();

        //console.log($scope.travelRequest);

        Utils.loading();
        PassengerSrv.searchTrip($scope.travelRequest).then(
            function (searchResults) {
                Utils.loaded();
                // console.log('Done trip search');
                $state.go('app.risultaticercaviaggi', {
                    'searchResults': searchResults
                });
            },
            function (error) {
                Utils.loaded();
                // TODO: handle searchTrip error
                //console.log(error);
                Utils.toast();
            }
        );
    };
})

.controller('RisultatiCercaViaggiCtrl', function ($scope, $state, $stateParams, Utils, PassengerSrv) {
    $scope.passengerTripsFound = $stateParams['searchResults'];
    $scope.travelDateFormat = 'dd MMMM yyyy';
    $scope.travelTimeFormat = 'HH:mm';

    // NOTE: temporary mock results TO BE REMOVED
    /*
    $scope.passengerTripsFoundFAKE = [
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
            "bookings": [
                {
                    accepted: 1
                },
                {
                    accepted: -1
                }
            ],
            "userId": 73,
            "places": 4,
            "when": 1443425400000,
            "monitored": false
        }
    ];
    */

    console.log($scope.passengerTripsFound);

    $scope.passengerTripsFound.forEach(function (travel) {
        travel.bookingCounters = Utils.getBookingCounters(travel);
    });

    $scope.selectTrip = function (index) {
        $state.go('app.viaggio', {
            'travelId': $scope.passengerTripsFound[index].id
        });
    };
});
