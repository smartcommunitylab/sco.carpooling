angular.module('carpooling.controllers.notifications', [])

.controller('NotificationsCtrl', function ($scope, $filter, $state, $timeout, $location, Utils, UserSrv, $ionicScrollDelegate) {
    $scope.dateTimeMask = 'dd MMMM yyyy - HH:mm';

    $scope.notificationType = {
        Chat: {
            label: 'notif_chat',
            image: 'ion-android-chat'
        },
        TripAvailability: {
            label: 'notif_avail',
            image: 'ion-android-search'
        },
        ParticipationRequest: {
            label: 'notif_participate_request',
            image: 'ion-android-car'
        },
        ParticipationResponse: {
            label: 'notif_participate_response',
            image: 'ion-android-car'
        },
        RatingRequest: {
            label: 'notif_eval',
            image: 'ion-android-star'
        }
    };

    var shortText = function (notific) {
        switch (notific.type) {
            case 'Chat':
                return $filter('translate')('notif_short_chat', {
                    name: notific.data.senderFullName
                });
            case 'TripAvailability':
                return $filter('translate')('notif_short_avail');
            case 'ParticipationRequest':
                return $filter('translate')('notif_short_request', {
                    name: notific.data.senderFullName
                });
            case 'ParticipationResponse':
                return $filter('translate')(notific.data.status + '' == '1' ? 'notif_short_response_ok' : 'notif_short_response_ko');
            case 'RatingRequest':
                return $filter('translate')('notif_short_eval');
            default:
                return '';
        };
        return '';
    };

    $scope.notifications = null;
    $scope.start = 0;
    $scope.all = 10;
    $scope.end_reached = false;

    var correctNotificsShortText = function (list) {
        list.forEach(function (m) {
            m.short_text = shortText(m);
        });
        return list;
    };

    $scope.loadMoreNotifications = function () {
        UserSrv.readNotifications($scope.start, $scope.all).then(function (notifics) {
            notifics = correctNotificsShortText(notifics);
            $scope.notifications = $scope.notifications != null ? $scope.notifications.concat(notifics) : notifics;

            if (notifics.length >= $scope.all) {
                $scope.$broadcast('scroll.infiniteScrollComplete');
                $scope.start += 1;
                $scope.end_reached = false;
            } else {
                $scope.end_reached = true;
            }
            //alert('location object ' + JSON.stringify($location));
        }, function (err) {
            console.error(err);
            $scope.$broadcast('scroll.infiniteScrollComplete');
            $scope.end_reached = true;
        });
    };

    $scope.canWeLoadMoreNotifics = function () {
        return !$scope.end_reached;
    };

    $scope.markANotification = function (id) {
        UserSrv.markNotification(id).then(
            function () {},
            function (err) {
                Utils.loaded();
                Utils.toast(Utils.getErrorMsg(err));
            }
        );
    };

    $scope.deleteANotification = function (id) {
        UserSrv.deleteNotification(id).then(
            function () {
                Utils.toast(($filter('translate')('toast_notification_deleted')));
            },
            function (err) {
                Utils.loaded();
                Utils.toast(Utils.getErrorMsg(err));
            }
        );
    };

    //    var notifications = [
    //        {
    //            id: '1',
    //            type: 'Chat',
    //            data: {
    //              senderId: '5',
    //              senderFullName: 'Mario Rossi',
    //              message: 'New test message'
    //            },
    //            travelId: '5669a7fce4b0c10934dc5389',
    //            timestamp: '1447865802692'
    //        },
    //        {
    //            id: '2',
    //            type: 'ParticipationRequest',
    ////            short_text: 'Giulia Bianchi chiede di partecipare al tuo viaggio Trento - Rovereto',
    //            data: {
    //              senderId: '54',
    //              senderFullName: 'Mario Rossi'
    //            },
    //            travelId: '5669a7fce4b0c10934dc5389',
    //            timestamp: '1447865802692'
    //        },
    //        {
    //            id: '3',
    //            type: 'TripAvailability',
    ////            short_text: 'Trovato un viaggio Trento - Pergine',
    //            data: {
    //            },
    //            travelId: '5669a7fce4b0c10934dc5389',
    //            timestamp: '1447918789919'
    //        },
    //        {
    //            id: '4',
    //            type: 'ParticipationResponse',
    ////            short_text: 'Viaggio confermato',
    //            data: {
    //              status: 'false'
    //            },
    //            travelId: '5669a7fce4b0c10934dc5389',
    //            timestamp: '1447918789919'
    //        }
    //    ];

    $scope.showNotification = function (notific) {
        $scope.markANotification(notific.id);
        switch (notific.type) {
            case 'Chat':
                // messages - to chat
                //$scope.deleteANotification(notific.id);
                $state.go('app.chat', {
                    travelId: notific.travelId,
                    personId: notific.data.senderId,
                    senderName: notific.data.senderFullName
                });
                break;
            case 'TripAvailability':
                // trip request - to mytrip
                $state.go('app.viaggio', {
                    travelId: notific.travelId
                });
                break;
            case 'ParticipationRequest':
                // trip response - to trip
                $state.go('app.viaggio', {
                    travelId: notific.travelId
                });
                break;
            case 'ParticipationResponse':
                // driver rating - to driver profile (trip data)
                $state.go('app.viaggio', {
                    travelId: notific.travelId
                });
                break;
            case 'RatingRequest':
                $state.go('app.viaggio', {
                    travelId: notific.travelId
                });
                break;
            default:
                break;
        };
    };
})

.controller('ChatCtrl', function ($scope, $rootScope, $interval, $stateParams, $filter, $state, $timeout, $ionicScrollDelegate, $location, $anchorScroll, Utils, UserSrv, StorageSrv) {
    var viewScroll = $ionicScrollDelegate.$getByHandle('userMessageScroll');
    $scope.messages = [];
    $scope.updatesMsg = [];
    $scope.id = StorageSrv.getUserId();
    $scope.oldMsgPresent = false;
    $scope.dateDayMask = 'dd MMMM yyyy';
    $scope.isToday = function (time) {
        var start = new Date();
        var end = new Date();
        start.setHours(0, 0, 0, 0);
        end.setHours(23, 59, 59, 999);
        start = start.getTime();
        end = end.getTime();
        if (time >= start && time <= end) {
            return true;
        }
    };

    var init = function () {
        $scope.personId = $stateParams.personId;
        $scope.travelId = $stateParams.travelId;
        $scope.personName = $stateParams.senderName;
        /*Utils.loading();
        UserSrv.getDiscussion($scope.travelId, $scope.personId).then(function (discussion) {
            $scope.messages = discussion.messages ? discussion.messages : [];
            if ($scope.messages.length > 10) {
                $scope.oldMsgPresent = true;
            }
            $scope.personName = discussion.personName;
            viewScroll.scrollBottom();
            Utils.loaded();
        }, function (err) {
            Utils.loaded();
            console.error(err);
            Utils.toast(Utils.getErrorMsg(err));
        });*/
        Utils.loading();
        UserSrv.readNotificationsByTravelId($scope.travelId, $scope.personId).then(function (discussion) {
            $scope.updatesMsg = discussion ? discussion : [];
            viewScroll.scrollBottom();
            Utils.loaded();
        }, function (err) {
            Utils.loaded();
            console.error(err);
            Utils.toast(Utils.getErrorMsg(err));
        });
    };

    init();

    $scope.loadOldChat = function () {
        Utils.loading();
        var old_msg = [];
        UserSrv.getDiscussion($scope.travelId, $scope.personId).then(function (discussion) {
            old_msg = discussion.messages ? discussion.messages : [];
            var last_msg = $scope.messages[0];
            var pos = '' + last_msg.timestamp;
            for (var i = old_msg.length - 1; i >= 0; i--) {
                // --- for test, to be removed ---
                old_msg[i].message = 'old ' + old_msg[i].message;
                old_msg[i].timestamp = old_msg[i].timestamp + (1000 * 60 * 60 * 24);
                // -------------------------------
                $scope.messages.splice(0, 0, old_msg[i]);
            }
            $timeout(function () {
                $location.hash(pos);
                $anchorScroll();
            }, 500);
            Utils.loaded();
            //var y_pos = viewScroll.getScrollPosition();
            //alert('position ' + JSON.stringify(y_pos));
            //viewScroll.scrollTo(0, y_pos.top, false);
        }, function (err) {
            Utils.loaded();
            Utils.toast(Utils.getErrorMsg(err));
        });
    };

    $scope.isMe = function (id) {
        return id == $scope.id;
    };

    $scope.chatExtraLength = function (chat) {
        if (chat != null) {
            if (chat.length > 30) {
                return true;
            } else {
                return false;
            }
        }
    };

    $scope.inputUp = function () {
        //if (isIOS) $scope.data.keyboardHeight = 216;
        $timeout(function () {
            viewScroll.resize();
            viewScroll.scrollBottom(true);
        }, 500);
    };

    $scope.inputDown = function () {
        //if (isIOS) $scope.data.keyboardHeight = 0;
        $timeout(function () {
            viewScroll.resize();
        }, 500);
    };

    $scope.sendMessage = function (value) {
        if (value != null && value != '') {
            var now = new Date();
            var msg_timestamp = now.getTime();
            var new_m = {
                userId: $scope.id,
                message: value,
                timestamp: msg_timestamp,
                targetUserId: $scope.personId
            }

            Utils.loading();
            UserSrv.sendMessage($scope.travelId, new_m).then(
                function () {
                    init();
                },
                function (err) {
                    Utils.loaded();
                    Utils.toast(Utils.getErrorMsg(err));
                }
            );
        }

        viewScroll.scrollBottom(true);
        $scope.new_message = '';
    };

    var updateChat = function () {
        UserSrv.readNotificationsByTravelId($scope.travelId, $scope.personId).then(function (discussion) {
            $scope.updatesMsg = discussion ? discussion : [];
            viewScroll.scrollBottom();
            Utils.loaded();
        }, function (err) {
            Utils.loaded();
            console.error(err);
            Utils.toast(Utils.getErrorMsg(err));
        });
    }

    $scope.$on('$ionicView.enter', function () {
        if (!window.ParsePushPlugin) {
            $scope.interval = $interval(updateChat, 10000);
        }
    });
    /*
     * exit
     */
    $scope.$on('$ionicView.leave', function () {
        if ($scope.interval) $interval.cancel($scope.interval);
    });
});
