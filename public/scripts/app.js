'use strict';

/**
 * @ngdoc overview
 * @name cloudStoreClient
 * @description
 * # cloudStoreClient
 *
 * Main module of the application.
 */
angular
  .module('cloudStoreClient', [
    'ngAnimate',
    'ngCookies',
    'ngResource',
    'ngRoute',
    'ngSanitize',
	'ui.sortable',
	'ui.bootstrap',
    'ngTouch'
  ])
  .config(function ($routeProvider) {
    $routeProvider
      .when('/', {
        templateUrl: 'views/main.html',
        controller: 'MainCtrl',
        controllerAs: 'main'
      })
      .when('/about', {
        templateUrl: 'views/about.html',
        controller: 'AboutCtrl',
        controllerAs: 'about'
      })
      .when('/add-device', {
        templateUrl: 'views/add_device.html',
        controller: 'AddDeviceCtrl',
        controllerAs: 'addDevice'
      })
      .otherwise({
        redirectTo: '/'
      });
  });
