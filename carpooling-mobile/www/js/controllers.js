angular.module('carpooling.controllers', [])

.controller('AppCtrl', function ($scope) {})

.controller('HomeCtrl', function ($scope) {})

.controller('PartecipoCtrl', function ($scope, UserSrv) {
    $scope.travelProfile = 'empty';

    $scope.getTravelProfile = function () {
        UserSrv.getTravelProfile().then(function (data) {
            $scope.travelProfile = data;
        });
    }
})

.controller('OffroCtrl', function ($scope) {})

.controller('OffriCtrl', function ($scope, $filter, $ionicModal, $ionicPopup, Config, MapSrv) {
    var mapId = 'modalMap';

    // NOTE: to be removed
    $scope.temp = function () {
        console.log('ciao');
    };

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
                // TODO: strings, button styles, actions
                var confirmPopup = $ionicPopup.confirm({
                    title: 'TITOLO',
                    template: 'LAT:' + args.leafletEvent.latlng.lat.toString().substring(0, 7) + ' LON:' + args.leafletEvent.latlng.lng.toString().substring(0, 7),
                    buttons: [
                        {
                            text: $filter('translate')('cancel'),
                            type: 'button-positive'
                        },
                        {
                            text: $filter('translate')('ok'),
                            type: 'button-positive',
                            onTap: function () {
                                //$scope.result = args.leafletEvent.latlng;
                                //return selectPlace(args.leafletEvent.latlng)
                                console.log(args.leafletEvent.latlng);
                            }
                        }
                    ]
                });



                /*
                $ionicLoading.show();
                planService.setPosition($scope.place, args.leafletEvent.latlng.lat, args.leafletEvent.latlng.lng);
                var placedata = $q.defer();
                var url = Config.getGeocoderURL() + '/location?latlng=' + args.leafletEvent.latlng.lat + ',' + args.leafletEvent.latlng.lng;

                $http.get(encodeURI(url), {
                    timeout: 5000
                })

                .success(function (data, status, headers, config) {
                    $ionicLoading.hide();
                    name = '';
                    if (data.response.docs[0]) {
                        planService.setName($scope.place, data.response.docs[0]);
                        $scope.showConfirm(name, $filter('translate')("popup_address"), function () {
                            //$scope.result = name;
                            return selectPlace(name)
                        });
                    } else {
                        $scope.showConfirm($filter('translate')("popup_lat") + args.leafletEvent.latlng.lat.toString().substring(0, 7) + " " + $filter('translate')("popup_long") + args.leafletEvent.latlng.lng.toString().substring(0, 7), $filter('translate')("popup_no_address"), function () {
                            //$scope.result = args.leafletEvent.latlng;
                            return selectPlace(args.leafletEvent.latlng)
                        });
                    }
                })

                .error(function (data, status, headers, config) {
                    $ionicLoading.hide();
                    $scope.showNoConnection();
                });
                */
            });
        });
    };

    $scope.showModalMap = function () {
        $scope.modalMap.show().then(function () {
            var modalMapElement = document.getElementById('modal-map-container');
            if (modalMapElement != null) {
                MapSrv.resizeElementHeight(modalMapElement, mapId);
                MapSrv.refresh(mapId);
            }
        });
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
                type: 'button-carpooling',
                onTap: function (e) {
                    // don't allow the user to close unless he enters wifi password
                    //e.preventDefault();
                    $scope.updateRecurrence();
                    return true;
                }
            }
        ]
    };

    $scope.$watch('recurrence.isRecurrent', function (newValue, oldValue) {
        if (newValue !== oldValue && !!newValue) {
            $ionicPopup.show(recurrentPopup).then(
                function (res) {
                    console.log($scope.recurrence.isRecurrent);
                    console.log($scope.recurrence.recurrenceType);
                    console.log($scope.recurrence.recurrenceDoW);
                    console.log($scope.recurrence.recurrenceDoWstring);
                    if (!!!res) {
                        $scope.recurrence.isRecurrent = false;
                    }
                }
            );
        }
    });
})

.controller('CercaViaggioCtrl', function ($scope, $filter) {
    $scope.date = $filter("date")(Date.now(), 'yyyy-MM-dd');
    $scope.time = '10:30';
});
