var app = angular.module('photoServ', ['pascalprecht.translate', 'ngMaterial','users']);


app.config(function ($mdThemingProvider, $mdIconProvider) {

    $mdIconProvider
        .defaultIconSet("./assets/svg/avatars.svg", 128)
        .icon("menu", "./assets/svg/menu.svg", 24)
        .icon("share", "./assets/svg/share.svg", 24)
        .icon("google_plus", "./assets/svg/google_plus.svg", 512)
        .icon("hangouts", "./assets/svg/hangouts.svg", 512)
        .icon("twitter", "./assets/svg/twitter.svg", 512)
        .icon("phone", "./assets/svg/phone.svg", 512);

    $mdThemingProvider.theme('default')
        .primaryPalette('brown')
        .accentPalette('red');

});

app.config(['$translateProvider', function ($translateProvider) {
    $translateProvider.translations('en', {
        'TITLE': 'Hello',
        'FOO': 'This is a paragraph'
    });

    $translateProvider.translations('de', {
        'TITLE': 'Hallo',
        'FOO': 'Dies ist ein Absatz'
    });

    $translateProvider.preferredLanguage('fr');
}]);