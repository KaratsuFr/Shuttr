app.controller('LoginController', function ($scope, $mdDialog, $rootScope, $http,TokenService,Notif) {

    $scope.userExist = $rootScope.userInfo.userExist;

    $scope.userInfo = {
        username: '',
        password: '',
        mail: ''
    };

    $scope.toogleAccess = function () {
        $scope.userExist = !$scope.userExist;
        $rootScope.userInfo.userExist = $scope.userExist ;
        dynamicText();
    };

    function dynamicText() {
        if($scope.userExist){
            $scope.description = '.loginText';
            $scope.toggleBtLabel = '.register';
            $scope.submitLabel = '.login';
        }else{
            $scope.description = '.registerText'
            $scope.toggleBtLabel = '.login';
            $scope.submitLabel = '.register';
        }
    }

    dynamicText();

    $scope.hide = function () {
        $mdDialog.hide();
    };

    $scope.cancel = function () {
        $mdDialog.cancel();
    };

    $scope.submit = function () {

        if( $scope.userExist) {
            $http({
                method: 'POST',
                url: '/rest/user/login',
                data: $scope.userInfo
            }).then(function successCallback(response) {

                TokenService.registerToken(response.data.token);
                Notif.success('loginSuccess');
                $mdDialog.hide();
            }, function errorCallback(response) {
                Notif.alert('loginFail' );
            });
        }else{
            $http({
                method: 'POST',
                url: '/rest/user/register',
                data: $scope.userInfo
            }).then(function successCallback(response) {
                TokenService.registerToken(response.data.token);

                Notif.success('loginSuccess');
                $mdDialog.hide();
            }, function errorCallback(response) {
                Notif.alert('registerFail');
            });
        }
    };

});