angular.module('carpooling.controllers.offri', [])

.controller('OffriCtrl', function ($scope, $state, $stateParams, $filter, $ionicModal, $ionicPopup, $ionicLoading, Config, CacheSrv, Utils, MapSrv, GeoSrv, PlanSrv, DriverSrv, StorageSrv, UserSrv) {
    // 'from' and 'to' are 'zone' objects
    $scope.formTravel = {
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
        'places': 0,
        'intermediateStops': false,
        'communityIds': []
    };

    $scope.travel = angular.copy($scope.formTravel);

    $scope.equalFormAndTravelFields = {
        'from': false,
        'to': false
    };

    $scope.communities = [];
    UserSrv.getCommunities().then(function (communities) {
        $scope.communities = communities
    });

    /*
     * Autocompletion stuff
     */
    // names: array with the names of the places
    // coordinates: object that maps a place name with an object that has its coordinate in key 'latlng'
    $scope.afterMapSelection = false;

    $scope.places = {
      from : {
        'names': [],
        'coordinates': {}
      },
      to : {
        'names': [],
        'coordinates': {}
      }
    };

    var typing = function (field, typedthings) {
        if ($scope.afterMapSelection) {
            $scope.afterMapSelection = false;
            return;
        }

        $scope.equalFormAndTravelFields[field] = Utils.fastCompareObjects($scope.formTravel[field], $scope.travel[field]);
        if ($scope.equalFormAndTravelFields[field]) {
            return;
        } else {
            if (!!$scope.formTravel[field]['address']) {
                $scope.formTravel[field]['address'] = '';
            }
            if (!!$scope.formTravel[field]['latitude']) {
                $scope.formTravel[field]['latitude'] = null;
            }
            if (!!$scope.formTravel[field]['longitude']) {
                $scope.formTravel[field]['longitude'] = null;
            }
        }

        var newPlaces = PlanSrv.getTypedPlaces(typedthings);
        newPlaces.then(function (data) {
            //merge with favorites and check no double values
            $scope.places[field].names = data;
            $scope.places[field].coordinates = PlanSrv.getNames();
        });
    };

    $scope.typingFrom = function (typedthings) {
        typing('from', typedthings);
    };

    $scope.typingTo = function (typedthings) {
        typing('to', typedthings);
    };

    var setLocation = function (field, name) {
        $scope.formTravel[field].name = name;
        $scope.formTravel[field].address = name;
        var coordinates = $scope.places[field].coordinates[name].latlng.split(',');
        $scope.formTravel[field].latitude = parseFloat(coordinates[0]);
        $scope.formTravel[field].longitude = parseFloat(coordinates[1]);

        $scope.travel = angular.copy($scope.formTravel);

        $scope.places[field] = {
            'names': [],
            'coordinates': {}
        };
    };

    $scope.setLocationFrom = function (name) {
        setLocation('from', name);
    };

    $scope.setLocationTo = function (name) {
        setLocation('to', name);
    };

    /*
     * Map stuff
     */
    var mapId = 'modalMap';
    var selectedField = null;

    $scope.modalMap = null;

    angular.extend($scope, {
        center: {
            lat: Config.getLat(),
            lng: Config.getLon(),
            zoom: Config.getZoom()
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
                            $scope.formTravel[selectedField].name = name;
                            $scope.formTravel[selectedField].address = name;
                            var splittedCoords = coordinates.split(',');
                            $scope.formTravel[selectedField].latitude = parseFloat(splittedCoords[0]);
                            $scope.formTravel[selectedField].longitude = parseFloat(splittedCoords[1]);
                            $scope.afterMapSelection = true;

                            $scope.travel = angular.copy($scope.formTravel);

                            $scope.equalFormAndTravelFields[selectedField] = Utils.fastCompareObjects($scope.formTravel[selectedField], $scope.travel[selectedField]);
                        }
                        $scope.hideModalMap(selectedField);
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
        //todayButtonType: 'button-carpooling',
        todayButtonType: 'ng-hide',
        closeButtonType: '',
        templateType: 'popup',
        showTodayButton: 'false',
        modalHeaderColor: '',
        modalFooterColor: '',
        from: today,
        to: new Date(2019, 12, 31, 23, 59, 59),
        inputDate: today,
        weekDaysList: Utils.getSDoWList(),
        monthList: Utils.getMonthList(),
        mondayFirst: true,
        disableDates: null,
        callback: function (val) { // Mandatory
            if (typeof (val) === 'undefined') {
                console.error('[datepicker] Date not selected');
            } else {
                /*console.log('Selected date is : ', val);
                console.log('Selected time is : ', $scope.timepickerObj.inputEpochTime);
                var total = angular.copy(val);
                total.setSeconds(total.getSeconds() + $scope.timepickerObj.inputEpochTime);
                console.log('Total date/time: ' + total);*/
                $scope.datepickerObj.inputDate = val;
            }
        },
    };

    /* Time Picker */
    var now = new Date();
    $scope.timepickerObj = {
        inputEpochTime: (now.getHours() * 60 * 60) + ((now.getMinutes() - (now.getMinutes() % Config.getClockStep()) + Config.getClockStep()) * 60),
        step: Config.getClockStep(),
        format: 24,
        titleLabel: $filter('translate')('popup_timepicker_title'),
        setLabel: $filter('translate')('ok'),
        closeLabel: $filter('translate')('cancel'),
        setButtonType: 'button-carpooling',
        closeButtonType: '',
        callback: function (val) { //Mandatory
            if (typeof (val) === 'undefined') {
                console.error('[timepicker] Time not selected');
            } else {
                /*console.log('Selected date is : ', $scope.datepickerObj.inputDate);
                console.log('Selected time is : ', val);
                var total = angular.copy($scope.datepickerObj.inputDate);
                total.setSeconds(total.getSeconds() + val);
                console.log('Total date/time: ' + total);*/
                $scope.timepickerObj.inputEpochTime = val;
            }
        }
    };

    /*
     * Recurrency popup stuff
     */
    $scope.hideModalMap = function (field) {
        $scope.modalMap.hide();

      $scope.places[field] = {
          'names': [],
          'coordinates': {}
      };
    };

    $scope.getDoW = function () {
        return Utils.getSDoWList();
    };

    $scope.DoWselection = {};
    $scope.selectAllDoW = function () {
        for (var i = 0; i < $scope.recurrencyPopupDoW.length; i++) {
            $scope.recurrencyPopupDoW[i].checked = $scope.DoWselection.all;
        }
    };

    $scope.deselectDoW = function () {
        for (var i = 0; i < $scope.recurrencyPopupDoW.length; i++) {
            if (!$scope.recurrencyPopupDoW[i].checked) {
                $scope.DoWselection.all = false;
            }
        }
    };

    $scope.getArray = function (num) {
        var array = new Array(num);
        for (var i = 0; i < num; i++) {
            array[i] = i + 1;
        }
        return array;
    }

    $scope.recurrency = {
        isRecurrent: false,
        recurrencyType: 'w',
        recurrencyD: '1',
        recurrencyDoW: [],
        recurrencyDoWstring: ''
    };

    $scope.recurrencyPopupDoW = [
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

    $scope.updateRecurrency = function () {
        // update recurrenceDoW and recurrenceDoWstring
        $scope.recurrency.recurrencyDoW = [];
        $scope.recurrency.recurrencyDoWstring = '';

        for (var i = 0; i < $scope.recurrencyPopupDoW.length; i++) {
            var dow = $scope.recurrencyPopupDoW[i];
            if (dow.checked) {
                $scope.recurrency.recurrencyDoW.push(dow.value);
                if (!!$scope.recurrency.recurrencyDoWstring) {
                    $scope.recurrency.recurrencyDoWstring = $scope.recurrency.recurrencyDoWstring + ', ';
                }
                $scope.recurrency.recurrencyDoWstring = $scope.recurrency.recurrencyDoWstring + $filter('translate')(dow.name);
            }
        }

        $scope.recurrency.isRecurrent = $scope.recurrency.recurrencyDoW.length > 0;
    };

    var recurrencyPopup = {
        templateUrl: 'templates/popup_offri.html',
        title: $filter('translate')('title_setrecurrency'),
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

    $scope.showRecurrencyPopup = function () {
        $ionicPopup.show(recurrencyPopup).then(
            function (res) {
                $scope.updateRecurrency();
            }
        );
    };

    $scope.$watch('recurrency.isRecurrent', function (newValue, oldValue) {
        if (newValue !== oldValue && !!newValue) {
            $scope.showRecurrencyPopup();
        }
    });

    $scope.selectCommunitiesPopup = function () {
        var communitiesPopup = $ionicPopup.show({
            templateUrl: 'templates/popup_communities.html',
            title: $filter('translate')('lbl_communities'),
            scope: $scope,
            buttons: [
                /*{
                    text: $filter('translate')('cancel'),
                    type: 'button-stable',
                },*/
                {
                    text: $filter('translate')('ok'),
                    type: 'button-carpooling'
                }
            ]
        });
    };

    $scope.getCommunitiesString = function (communityIds) {
        if (!communityIds) {
            return null;
        }

        var cnames = '';
        for (var i = 0; i < $scope.communities.length; i++) {
            var community = $scope.communities[i];
            if (communityIds.indexOf(community.id) > -1) {
                cnames += community.name + ', ';
            }
        }
        cnames = cnames.substring(0, cnames.length - 2);
        return cnames;
    };

    $scope.updateTravelCommunityIds = function (community) {
        if (community.checked) {
            if ($scope.travel.communityIds.indexOf(community.id) == -1) {
                $scope.travel.communityIds.push(community.id);
            }
        } else {
            if ($scope.travel.communityIds.indexOf(community.id) != -1) {
                $scope.travel.communityIds.splice($scope.travel.communityIds.indexOf(community.id), 1);
            }
        }
    };

    $scope.offer = function () {
        // NOTE: 'from', 'to' and 'intermediateStops' are already on $scope.travel
        if (!!$stateParams['communityId']) {
            $scope.travel['communityIds'] = [$stateParams['communityId']];
            console.log($scope.travel);
        }
        var auto = StorageSrv.getUser().auto;

        if (!!auto && auto.posts > 0) {
            $scope.travel['places'] = auto.posts;
            var selectedDateTime = angular.copy($scope.datepickerObj.inputDate);
            selectedDateTime.setSeconds(selectedDateTime.getSeconds() + $scope.timepickerObj.inputEpochTime);
            $scope.travel['when'] = selectedDateTime.getTime();

            if (selectedDateTime.getTime() < (new Date().getTime() - 5*60*1000)) {
              Utils.toast(($filter('translate')('toast_time_invalid')));
              return;
            }

            if ($scope.recurrency.isRecurrent) {
                $scope.travel['recurrency'] = {
                    time: selectedDateTime.getHours(),
                    days: $scope.recurrency.recurrencyDoW
                }
            }

            Utils.loading();

            var creationSuccess = function (savedTravel) {
                Utils.loaded();
                CacheSrv.setReloadDriverTrips(true);
                $state.go('app.home');
                Utils.toast(($filter('translate')('toast_trip_offered')));
            };

            var creationError = function (error) {
                Utils.loaded();
                Utils.toast(Utils.getErrorMsg(error));
            };

            if (!!$scope.travel.recurrency) {
                DriverSrv.createRecurrentTrip($scope.travel).then(creationSuccess, creationError);
            } else {
                DriverSrv.createTrip($scope.travel).then(creationSuccess, creationError);
            }
        } else {
            Utils.toast(($filter('translate')('toast_auto_disabled')));
        }
    };

    $scope.$on('$ionicView.beforeEnter', function () {
        MapSrv.refresh('modalMap');
    });
});
