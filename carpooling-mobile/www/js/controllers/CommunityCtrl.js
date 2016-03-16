angular.module('carpooling.controllers.communities', [])

.controller('CommunityCtrl', function ($scope, $rootScope, $state, StorageSrv, UserSrv, Utils) {
    $scope.communities = null;
    Utils.loading();

    UserSrv.getCommunitiesDetails().then(
        function (communities) {
            Utils.loaded();
            $scope.communities = communities;
        },
        function (error) {
            Utils.loaded();
            Utils.toast();
            $scope.communities = [];
        }
    );

    $scope.selectCommunity = function (index) {
        var community = $scope.communities[index];
        $state.go('app.comunitainfo', {
            'community': community
        });
    };

    $scope.searchCommunity = function () {
        var myCommunities = $scope.communities;
        $state.go('app.cercacomunita', {
            'myCommunities': myCommunities
        });
    };
})

.controller('FindCommunityCtrl', function ($scope, $rootScope, $state, $stateParams, $filter, $ionicModal, $ionicPopup, $ionicLoading, Utils, StorageSrv, UserSrv, MapSrv, GeoSrv, PlanSrv) {
    // FUTURE use that if search communities will be implemented
    $scope.communitiesToFilter = $stateParams['myCommunities'];
    var filteredCommunities = [];

    $scope.search = {
        searchText: null,
        location: {
            address: null,
            coordinates: null
        }
    };

    /*
     * Map stuff
     */
    var mapId = 'modalMap';

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
                        $scope.search.location.address = name;
                        $scope.search.location.coordinates = coordinates;
                        $scope.afterMapSelection = true;
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

    $scope.showModalMap = function () {
        $scope.modalMap.show().then(function () {
            // resize map!
            var modalMapElement = document.getElementById('modal-map-container');
            if (modalMapElement != null) {
                MapSrv.resizeElementHeight(modalMapElement, mapId);
                MapSrv.refresh(mapId);
            }
        });
    };

    $scope.hideModalMap = function () {
        $scope.modalMap.hide();
    };

    $scope.findCommunity = function () {
        var coords = $scope.search.location ? $scope.search.location.coordinates : null;
        UserSrv.searchCommunities(coords, $scope.search.searchText).then(
            function (communities) {
                // TODO process and assign results
            },
            function (reason) {}
        );
    };
});
