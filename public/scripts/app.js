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
        templateUrl: 'views/devices.html',
        controller: 'DevicesCtrl',
        controllerAs: 'devicesCtrl'
      })
      .when('/about', {
        templateUrl: 'views/about.html',
        controller: 'AboutCtrl',
        controllerAs: 'about'
      })
      .when('/manage-appliance', {
        templateUrl: 'views/add_device.html',
        controller: 'AddDeviceCtrl',
        controllerAs: 'addDeviceCtrl'
      })
      .otherwise({
        redirectTo: '/'
      });
  })
  .controller('HeaderController', function($scope,$location){
    $scope.isActive = function(viewLocation){
      return viewLocation === $location.path();
    };
  });
