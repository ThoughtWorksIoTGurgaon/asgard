'use strict';

/**
 * @ngdoc function
 * @name theGymApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the theGymApp
 */
angular.module('cloudStoreClient')
    .controller('MainCtrl', function () {
	this.awesomeThings = [
	    'HTML5 Boilerplate',
	    'AngularJS',
	    'Karma'
	];
    })
    .controller('HeaderController', function($scope,$location){
	$scope.isActive = function(viewLocation){
	    return viewLocation === $location.path();
	}
    });
