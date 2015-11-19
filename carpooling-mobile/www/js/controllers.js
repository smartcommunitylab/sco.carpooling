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

.controller('OffriCtrl', function ($scope, $filter, $ionicPopup, Config) {
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
        recurrenceType: 'd',
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
        // update $scope.recurrence.recurrenceDoW
        // TODO: update $scope.recurrence.recurrenceDoWstring
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
                    console.log($scope.recurrence.recurrenceD);
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

.controller('CercaViaggioCtrl', function ($scope, Config, $q, $http, $ionicModal, $ionicLoading, $filter, $state, $window, planService, GeoLocate) {

    $scope.title = $filter('translate')('plan_map_title');
    $scope.place = null;
    $scope.placesandcoordinates = null;
    $scope.planParams = {
        from: {
            name: '',
            lat: '',
            long: ''
        },
        to: {
            name: '',
            lat: '',
            long: ''
        },
        routeType: '',
        transportTypes: [],
        departureTime: '',
        date: ''
    };

    $scope.plan = function () {


        if (setAndCheckPlanParams()) {
            planService.planJourney($scope.planParams).then(function (value) {
                //if ok let's go to visualization
                $state.go('app.planlist')
            }, function (error) {
                //error then pop up some problem
                $scope.showErrorServer()
            });
        } else {
            //message something is missing
        }
    }
    var selectPlace = function (placeSelected) {
        if ($scope.place == 'from') {

            $scope.fromName = placeSelected;
            $scope.planParams.from.name = placeSelected;
            $scope.planParams.from.lat = planService.getPosition($scope.place).latitude;
            $scope.planParams.from.long = planService.getPosition($scope.place).longitude;
        } else if ($scope.place == 'to') {
            $scope.toName = placeSelected;
            $scope.planParams.to.name = placeSelected;
            $scope.planParams.to.lat = planService.getPosition($scope.place).latitude;
            $scope.planParams.to.long = planService.getPosition($scope.place).longitude;
        }
        console.log(placeSelected);
        /*close map*/
    }
    $scope.favoriteSelect = function (newplace) {
        $scope.closeFavorites();
        planService.setPosition($scope.place, newplace.lat, newplace.long);
        planService.setName($scope.place, newplace.name);
        selectPlace(newplace.name);
    }

    $scope.locateMe = function () {
        /*$ionicLoading.show()*/
        ;
        // if ($window.navigator.geolocation) {
        // $window.navigator.geolocation.getCurrentPosition(function (position) {
        GeoLocate.locate().then(function (position) {
                //                $scope.$apply(function () {
                $scope.position = position;
                var placedata = $q.defer();
                var places = {};
                var url = Config.getGeocoderURL() + '/location?latlng=' + position[0] + ',' + position[1];

                //add timeout
                $http.get(encodeURI(url), {
                    timeout: 5000
                }).
                success(function (data, status, headers, config) {
                    //                         planService.setName($scope.place, data.response.docs[0]);

                    places = data.response.docs;
                    name = '';
                    if (data.response.docs[0]) {
                        $scope.place = 'from';
                        planService.setPosition($scope.place, position[0], position[1]);
                        planService.setName($scope.place, data.response.docs[0]);
                        selectPlace(name);
                    }
                    $ionicLoading.hide();
                }).
                error(function (data, status, headers, config) {
                    //temporary
                    $ionicLoading.hide();
                    $scope.showNoConnection();
                });

            }

        );

    };

    $scope.detail = function (view) {
        window.location.assign(view);
    }

    $scope.typePlace = function (typedthings) {
        $scope.result = typedthings;
        $scope.newplaces = planService.getTypedPlaces(typedthings);
        $scope.newplaces.then(function (data) {
            //merge with favorites and check no double values
            $scope.places = data;
            $scope.placesandcoordinates = planService.getnames();

        });
    }
    $scope.select = function (suggestion) {
        console.log("select");
    }
    $scope.setPlaceById = function (id) {
        console.log(id);
    }

    $scope.changeStringFrom = function (suggestion) {
        console.log("changestringfrom");
        $scope.place = 'from';
        planService.setPosition($scope.place, $scope.placesandcoordinates[suggestion].latlong.split(',')[0], $scope.placesandcoordinates[suggestion].latlong.split(',')[1]);
        planService.setName($scope.place, suggestion);
        selectPlace(suggestion);
    }
    $scope.changeStringTo = function (suggestion) {
        console.log("changestringto");
        $scope.place = 'to';
        planService.setPosition($scope.place, $scope.placesandcoordinates[suggestion].latlong.split(',')[0], $scope.placesandcoordinates[suggestion].latlong.split(',')[1]);
        planService.setName($scope.place, suggestion);
        selectPlace(suggestion);
    }

    $scope.locateMe();

    $scope.test = function () {
        alert(" Latitude: " + $scope.planParams.from.lat + "\n Longitude: " + $scope.planParams.from.long);
    }

});
