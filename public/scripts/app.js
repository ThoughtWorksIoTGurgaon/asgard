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
        templateUrl: 'views/dashboard.html',
        controller: 'DashboardCtrl',
        controllerAs: 'dashboardCtrl'
      })
      .when('/manage-appliance', {
        templateUrl: 'views/mange-appliance.html',
        controller: 'ManageApplianceCtrl',
        controllerAs: 'manageApplianceCtrl'
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
