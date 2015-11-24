angular.module('carpooling.services.plan', [])

.factory('PlanSrv', function ($q, $http, $filter, Config) {
    var planService = {};
    var position = {};
    var planJourneyResults = {};
    var planConfigure = {};
    var selectedjourney = {};
    var geoCoderPlaces = {};
    var fromOrTo = "";
    var tripId = null;
    var getNameFromComplex = function (data) {
        name = '';
        if (data) {
            if (data.name) {
                name = name + data.name;
            }
            if (data.street && (data.name != data.street)) {
                if (name)
                    name = name + ', ';
                name = name + data.street;
            }
            if (data.housenumber) {
                if (name)
                    name = name + ', ';
                name = name + data.housenumber;
            }
            if (data.city) {
                if (name)
                    name = name + ', ';
                name = name + data.city;
            }
            return name;
        }
    }

    planService.setFromOrTo = function (value) {
        fromOrTo = value;
    };

    planService.getFromOrTo = function () {
        return fromOrTo;
    };

    planService.setTripId = function (id) {
        tripId = id;
    };

    planService.getTripId = function () {
        return tripId;
    };

    planService.setName = function (place, complexName) {
        if (place == 'from') {
            if (!position.nameFrom) {
                position.nameFrom = '';
            }
            if (typeof complexName === 'string' || complexName instanceof String) {
                position.nameFrom = complexName;
            } else { //get name from complexName
                position.nameFrom = getNameFromComplex(complexName);
            }
        } else {
            if (!position.nameTo) {
                position.nameTo = '';
            }
            if (typeof complexName === 'string' || complexName instanceof String) {
                position.nameTo = complexName;
            } else {
                //get name from complexName
                position.nameTo = getNameFromComplex(complexName);
            }
        }
    };

    planService.getName = function (place) {
        if (place == 'from') {
            return position.nameFrom;
        } else {
            return position.nameTo;
        }
    };

    planService.setPosition = function (place, latitude, longitude) {
        if (place == 'from') {
            if (!position.positionFrom) {
                position.positionFrom = {};
            }
            position.positionFrom.latitude = latitude;
            position.positionFrom.longitude = longitude;
        } else {
            if (!position.positionTo) {
                position.positionTo = {};
            }
            position.positionTo.latitude = latitude;
            position.positionTo.longitude = longitude;
        }
    };

    planService.getPosition = function (place) {
        if (place == 'from') {
            return position.positionFrom;
        } else {
            return position.positionTo;
        }

    };

    planService.getLength = function (it) {
        if (!it.leg && it.length) {
            return (it.length / 1000).toFixed(2);
        }
        var l = 0;
        for (var i = 0; i < it.leg.length; i++) {
            l += it.leg[i].length;
        }
        return (l / 1000).toFixed(2);
    };

    var getLength = function (it) {
        if (!it.leg && it.length) {
            return (it.length / 1000).toFixed(2);
        }
        var l = 0;
        for (var i = 0; i < it.leg.length; i++) {
            l += it.leg[i].length;
        }
        return (l / 1000).toFixed(2);
    };

    var getLegCost = function (plan, i) {
        var fareMap = {};
        var total = 0;
        if (plan.leg[i].extra) {
            var fare = plan.leg[i].extra.fare;
            var fareIdx = plan.leg[i].extra.fareIndex;
            if (fare && fareMap[fareIdx] == null) {
                fareMap[fareIdx] = fare;
                total += fare.cents / 100;
            }
        }
        return total;
    };

    planService.getItineraryCost = function (plan) {
        var fareMap = {};
        var total = 0;
        for (var i = 0; i < plan.leg.length; i++) {
            if (plan.leg[i].extra) {
                var fare = plan.leg[i].extra.fare;
                var fareIdx = plan.leg[i].extra.fareIndex;
                if (fare && fareMap[fareIdx] == null) {
                    fareMap[fareIdx] = fare;
                    total += fare.cents / 100;
                }
            }
        }
        return total;
    };

    planService.getLegCost = function (plan, i) {
        var fareMap = {};
        var total = 0;
        if (plan.leg[i].extra) {
            var fare = plan.leg[i].extra.fare;
            var fareIdx = plan.leg[i].extra.fareIndex;
            if (fare && fareMap[fareIdx] == null) {
                fareMap[fareIdx] = fare;
                total += fare.cents / 100;
            }
        }
        return total;
    };

    planService.getNames = function (i) {
        return geoCoderPlaces;
    };

    planService.getTypedPlaces = function (i) {
        var placedata = $q.defer();
        var names = [];
        i = i.replace(/\ /g, "+");
        var url = Config.getGeocoderURL() + '/address?latlng=' + Config.getLat() + ',' + Config.getLon() + '&distance=' + Config.getDistanceForAutocomplete() + '&address=' + i;

        $http.get(url, {
            timeout: 5000
        })

        .success(function (data, status, headers, config) {
            geoCoderPlaces = [];
            //places = data.response.docs;
            // store the data, return the labels
            k = 0;
            for (var i = 0; i < data.response.docs.length; i++) {
                temp = planService.generatePlaceString(data.response.docs[i]);

                //check se presente
                if (!geoCoderPlaces[temp]) {
                    //se non presente
                    names[k] = temp;
                    k++;
                    geoCoderPlaces[temp] = {
                        latlng: data.response.docs[i].coordinate
                    }
                }
            }
            placedata.resolve(names);
        })

        .error(function (data, status, headers, config) {
            //$scope.error = true;
        });

        return placedata.promise;
    };

    planService.saveTrip = function (tripId, trip, name, requestedFrom, requestedTo) {
        var deferred = $q.defer();
        if (!!!tripId) {
            tripId = new Date().getTime();
        }
        var tripToSave = {
            'tripId': tripId,
            'data': {
                'originalFrom': {
                    'name': requestedFrom,
                    'lat': trip.from.lat,
                    'lon': trip.from.lon
                },
                'originalTo': {
                    'name': requestedTo,
                    'lat': trip.to.lat,
                    'lon': trip.to.lon
                },
                'monitor': true,
                'name': name,
                'data': trip
            }
        };
        var savedTrips = JSON.parse(localStorage.getItem(Config.getAppId() + "_savedTrips"));
        if (!!!savedTrips) {
            savedTrips = {};
        }
        savedTrips[tripId] = tripToSave;
        localStorage.setItem(Config.getAppId() + "_savedTrips", JSON.stringify(savedTrips));
        deferred.resolve(tripToSave);
        return deferred.promise;
    };

    planService.generatePlaceString = function (place) {
        var temp = '';

        if (!!place) {
            if (place.name) {
                temp = temp + place.name;
            }
            if (place.street != place.name) {
                if (place.street) {
                    if (temp) {
                        temp = temp + ', ';
                    }
                    temp = temp + place.street;
                }
            }
            if (place.housenumber) {
                if (temp) {
                    temp = temp + ', ';
                }
                temp = temp + place.housenumber;
            }
            if (place.city) {
                if (temp) {
                    temp = temp + ', ';
                }
                temp = temp + place.city;
            }
        }

        return temp;
    };

    return planService;
});
