var app = angular.module('kyori', ['pascalprecht.translate', 'ngMaterial', 'ngMessages', 'ngCookies']);


app.factory('authHttpResponseInterceptor', function ($q, $location, $rootScope) {
    return {
        response: function (response) {
            if (response.status === 401) {
                console.log("Response 401");
            }
            return response || $q.when(response);
        },
        responseError: function (rejection) {
            if (rejection.status === 401) {
                console.log("Response Error " + rejection.status, rejection);
                $rootScope.$broadcast('user:loginForm');
            }
            return $q.reject(rejection);
        }
    }
}).factory('TokenService', function ($cookies, $rootScope, $http) {
    var tokenService = {
        httpProvider: null
    };

    tokenService.readToken = function () {
        //get cookie
        var token = $cookies.get('token');
        if (token != null) {
            // $http.defaults.headers.common['Authorization'] = token;
            $rootScope.userInfo.userExist = true;
            getUserName();
        } else {
            $rootScope.userInfo.userExist = false;
        }

    };

    tokenService.logout = function(){
        $cookies.remove('token');
        $rootScope.userInfo = {
            userExist: false,
            username: null,
            role: null
        };
    };

    tokenService.registerToken = function (token) {
        if (token != null) {
            $cookies.put('token', token);
            //  $http.defaults.headers.common['Authorization'] = token;
            $rootScope.userInfo.userExist = true;
            getUserName();
        }
    };

    function getUserName() {
        $http({
            method: 'GET',
            url: '/rest/user'
        }).then(function successCallback(response) {
            $rootScope.userInfo.username = response.data.username
        }, function errorCallback(response) {

        });
    }

    return tokenService;
}).factory('Notif', function ($mdToast, $filter) {

    var notif = {};

    notif.message = function (message) {
        var toastLoginOk = $mdToast.simple()
            .textContent($filter('translate')(message))
            .action($filter('translate')('close'))
            .position('bottom right');
        $mdToast.show(toastLoginOk);
    };

    notif.success = function (message) {
        var toastLoginOk = $mdToast.simple()
            .textContent($filter('translate')(message))
            .action($filter('translate')('close'))
            .toastClass('sucess')
            .position('bottom right');
        $mdToast.show(toastLoginOk);
    };

    notif.alert = function (message) {
        var toastLoginOk = $mdToast.simple()
            .textContent($filter('translate')(message))
            .action($filter('translate')('close'))
            .toastClass('error')
            .hideDelay(20000)
            .position('bottom right');
        $mdToast.show(toastLoginOk);
    }

    return notif;
}).config(function ($translateProvider, $httpProvider, $compileProvider) {
    $compileProvider.debugInfoEnabled(false);

    $translateProvider.useUrlLoader('/rest/app').determinePreferredLanguage();

    $httpProvider.interceptors.push('authHttpResponseInterceptor');

}).run(function ($rootScope) {
    $rootScope.userInfo = {
        userExist: false,
        username: null,
        role: null
    }
})
    .controller('AppController', function ($rootScope, $scope, $mdDialog, TokenService) {
        TokenService.readToken();

        $scope.$on('user:loginForm', function () {
            $scope.loginOrRegister();
        });

        $scope.logout = function(){
            TokenService.logout();
        };

        $scope.loginOrRegister = function (ev) {

            $mdDialog.show({
                controller: 'LoginController',
                templateUrl: './js/login/view/login.tmpl.html',
                parent: angular.element(document.body),
                targetEvent: ev,
                clickOutsideToClose: true,
                fullscreen: true
            })
                .then(function (answer) {
                    $scope.status = 'You said the information was "' + answer + '".';
                }, function () {
                    $scope.status = 'You cancelled the dialog.';
                });

        };
    });