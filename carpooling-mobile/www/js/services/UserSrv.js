angular.module('carpooling.services.user', [])

.factory('UserSrv', function ($http, $q, Config, StorageSrv) {
    var user = null;

    var userService = {};

    userService.getUser = function (userId) {
        var deferred = $q.defer();

        $http.get(Config.getServerURL() + '/api/read/user/' + userId, Config.getHTTPConfig())

        .success(function (data) {
            if (data[0] == '<') {
                deferred.reject();
            } else {
                if (StorageSrv.getUserId() === userId) {
                    // It's-a-me!
                    StorageSrv.saveUser(data.data).then(
                        function (data) {
                            deferred.resolve(data.data);
                        },
                        function (err) {
                            deferred.reject(err);
                        }
                    );

                    // My communities
                    userService.getCommunities().then(
                        function (data) {
                            data.data.forEach(function (community) {
                                delete community['color'];
                                delete community['zone'];
                                delete community['cars'];
                                delete community['users'];
                                delete community['userObjs'];
                            });
                            StorageSrv.saveCommunities(data.data);
                            deferred.resolve(data.data);
                        }
                    );
                } else {
                    deferred.resolve(data.data);
                }
            }
        })

        .error(function (err) {
            deferred.reject(err);
        });

        return deferred.promise;
    };

    userService.getTravelProfile = function () {
        var deferred = $q.defer();

        $http.get(Config.getServerURL() + '/api/read/profile', Config.getHTTPConfig())

        .success(function (data) {
            if (data[0] == '<') {
                deferred.reject();
            } else {
                deferred.resolve(data);
            }
        })

        .error(function (err) {
            deferred.reject(err);
        });

        return deferred.promise;
    };

    userService.saveTravelProfile = function (travelProfile) {
        var deferred = $q.defer();

        if (!travelProfile) {
            deferred.reject('Invalid travelProfile');
        } else {
            $http.post(Config.getServerURL() + '/api/save/profile', travelProfile, Config.getHTTPConfig())

            .success(function (data) {
                if (data[0] == '<') {
                    deferred.reject();
                } else {
                    deferred.resolve(data);
                }
            })

            .error(function (err) {
                deferred.reject(err);
            });
        }

        return deferred.promise;
    };

    userService.saveAuto = function (auto) {
        var deferred = $q.defer();

        $http.post(Config.getServerURL() + '/api/save/autoInfo', auto, Config.getHTTPConfig())

        .success(function (data) {
            if (data[0] == '<') {
                deferred.reject();
            } else {
                deferred.resolve(data);
            }
        })

        .error(function (err) {
            deferred.reject(err);
        });

        return deferred.promise;
    };

    userService.getCommunities = function () {
        var deferred = $q.defer();

        $http.get(Config.getServerURL() + '/api/read/communities', Config.getHTTPConfig())

        .success(function (data) {
            if (data[0] == '<') {
                deferred.reject();
            } else {
                deferred.resolve(data);
            }
        })

        .error(function (err) {
            deferred.reject(err);
        });

        return deferred.promise;
    };

    userService.getCommunitiesDetails = function () {
        var deferred = $q.defer();

        $http.get(Config.getServerURL() + '/api/read/communities/details', Config.getHTTPConfig())

        .success(function (data) {
            if (data[0] == '<') {
                deferred.reject();
            } else {
                deferred.resolve(data.data);
            }
        })

        .error(function (err) {
            deferred.reject(err);
        });

        return deferred.promise;
    };

    userService.getCommunityTravels = function (communityId, timeinmillis) {
        var deferred = $q.defer();

        $http.get(Config.getServerURL() + '/api/community/' + communityId + '/' + timeinmillis + '/travel', Config.getHTTPConfig())

        .success(function (data) {
            if (data[0] == '<') {
                deferred.reject();
            } else {
                deferred.resolve(data.data);
            }
            deferred.resolve(data.data);
        })

        .error(function (err) {
            deferred.reject(err);
        });

        return deferred.promise;
    };

    userService.getDiscussion = function (travelId, targetUserId) {
        var deferred = $q.defer();

        $http.get(Config.getServerURL() + '/api/read/' + travelId + '/' + targetUserId + '/discussion', Config.getHTTPConfig())

        .success(function (data) {
            if (data[0] == '<') {
                deferred.reject();
            } else {
                deferred.resolve(data.data);
            }
        })

        .error(function (err) {
            deferred.reject(err);
        });

        return deferred.promise;
    };

    userService.sendMessage = function (travelId, message) {
        var deferred = $q.defer();

        if (!travelId) {
            deferred.reject('Invalid travelId');
        } else if (!message || !message.userId || !message.timestamp || !message.message || !message.targetUserId) {
            deferred.reject('Invalid message');
        } else {
            $http.post(Config.getServerURL() + '/api/message/' + travelId + '/send', message, Config.getHTTPConfig())

            .success(function (data) {
                if (data[0] == '<') {
                    deferred.reject();
                } else {
                    deferred.resolve(data);
                }
            })

            .error(function (err) {
                deferred.reject(err);
            });
        }

        return deferred.promise;
    }

    userService.readNotifications = function (start, count) {
        var deferred = $q.defer();

        //        $http.get(Config.getServerURL() + '/api/read/' + travelId + '/' + targetUserId + '/discussion', Config.getHTTPConfig())
        //
        //        .success(function (data) {
        //            deferred.resolve(data.data);
        //        })
        //
        //        .error(function (err) {
        //            deferred.reject(err);
        //        });
        //
        //        return deferred.promise;
        if (start == null || start < 0) {
            deferred.reject('Invalid start position');
        } else if (!count) {
            deferred.reject('Invalid count');
        } else {
            $http.get(Config.getServerURL() + '/api/read/notifications/' + start + '/' + count, Config.getHTTPConfig())

            .success(function (data) {
                if (data[0] == '<') {
                    deferred.reject();
                } else {
                    deferred.resolve(data.data);
                }
            })

            .error(function (err) {
                deferred.reject(err);
            });
        }

        return deferred.promise;
    }

    userService.markNotification = function (id) {
        var deferred = $q.defer();

        if (!id) {
            deferred.reject('Invalid notification id');
        } else {
            $http.post(Config.getServerURL() + '/api/mark/read/notification/' + id, id, Config.getHTTPConfig())

            .success(function (data) {
                if (data[0] == '<') {
                    deferred.reject();
                } else {
                    deferred.resolve(data);
                }
            })

            .error(function (err) {
                deferred.reject(err);
            });
        }

        return deferred.promise;
    }

    userService.deleteNotification = function (id) {
        var deferred = $q.defer();

        if (!id) {
            deferred.reject('Invalid notification id');
        } else {
            $http.delete(Config.getServerURL() + '/api/delete/notification/' + id, id, Config.getHTTPConfig())

            .success(function (data) {
                if (data[0] == '<') {
                    deferred.reject();
                } else {
                    deferred.resolve(data);
                }
            })

            .error(function (err) {
                deferred.reject(err);
            });
        }

        return deferred.promise;
    }

    return userService;
});
