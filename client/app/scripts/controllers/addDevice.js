'use strict';

angular.module('cloudStoreClient')
  .controller('AddDeviceCtrl', function ($scope,$http) {
    $scope.hello = function() {
      $scope.serviceAddresses = ['1234', 'ABCD', 'WXYZ']
      console.log("hi")
    };   
});
