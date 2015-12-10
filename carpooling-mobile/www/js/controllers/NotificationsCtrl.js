angular.module('carpooling.controllers.notifications', [])

.controller('NotificationsCtrl', function ($scope, $filter, $state, $timeout, $ionicScrollDelegate) {

    var viewScroll = $ionicScrollDelegate.$getByHandle('userMessageScroll');

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
        },
        {
            id: '4',
            userId: 1,
            text: 'Provo a scrivere ancora per vedere se poi mi mette la scrollbar quando la pagina dei messaggi inizia ad allungarsi',
            timestamp: '1447865802692',
            userId_target: 2
        },
        {
            id: '5',
            userId: 2,
            text: 'Ciao Stefano, nessun problema. Tu continua pure a scrivere che poi vediamo se scoppia tutto o se funziona...',
            timestamp: '1447918789919',
            userId_target: 1
        },
        {
            id: '6',
            userId: 1,
            text: 'Speriamo in bene, tu incrocia le dita e vediamo cosa succede.',
            timestamp: '1447918789919',
            userId_target: 1
        }
    ];

    $scope.loadAllMsg = function(){
        viewScroll.scrollBottom();
    };

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

    $scope.chatExtraLength = function(chat){
        if(chat != null){
            if(chat.length > 30){
                return true;
            } else {
                return false;
            }
        }
    };

    $scope.inputUp = function() {
        //if (isIOS) $scope.data.keyboardHeight = 216;
        $timeout(function() {
            viewScroll.resize();
            viewScroll.scrollBottom(true);
        }, 500);
    };

    $scope.inputDown = function() {
        //if (isIOS) $scope.data.keyboardHeight = 0;
        $timeout(function() {
            viewScroll.resize();
        }, 500);
    };

    $scope.sendMessage = function(value){
        if(value != null && value != ""){
            var now = new Date();
            var msg_timestamp = now.getTime();
            console.log("Msg value " + value);
            var msg_length = $scope.messages.length + 1;
            var new_m = {
                id: msg_length + '',
                userId: 1,
                text: value,
                timestamp: msg_timestamp+'',
                userId_target: 2
            }
            $scope.messages.push(new_m);
        }
        viewScroll.scrollBottom(true);
        $scope.new_message = "";
    };
});
