var app = angular.module('kyori', ['pascalprecht.translate', 'ngMaterial']);


app.config( function ($translateProvider,$mdIconProvider) {
    $mdIconProvider.defaultIconSet("./assets/svg/avatars.svg", 128).icon("menu", "./assets/svg/menu.svg", 24).icon("share", "./assets/svg/share.svg", 24).icon("google_plus", "./assets/svg/google_plus.svg", 512).icon("hangouts", "./assets/svg/hangouts.svg", 512).icon("twitter", "./assets/svg/twitter.svg", 512).icon("phone", "./assets/svg/phone.svg", 512);


    $translateProvider.translations('en', {
        'kyori.title': 'Kyori Photo Challenge',
        'login': 'login',
        'wip': 'Kyori - Project currently under heavy work - stay tuned'
    });

    $translateProvider.translations('fr', {
        'kyori.title': 'Kyori Concours Photo',
        'login': 'connexion',
        'wip': 'Kyori - Project en construction - revenez bientot'

    });

    $translateProvider.preferredLanguage('en');
});