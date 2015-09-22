'use strict';

angular.module('cloudStoreClient')
    .controller('AddDeviceCtrl', function ($scope,$http,$modal) {
  $scope.hello = function() {
    $http.get('http://localhost:8080/services').then(function(response) {
      $scope.services = response.data.services;
    }, function(response) {
      $scope.services = [];
    });
    $scope.animationsEnabled = true;   
  };   

  $scope.open = function(selected_service){
    var modalInstance = $modal.open({
        animation: $scope.animationsEnabled,
        templateUrl: 'myModalContent.html',
        controller: 'ModalInstanceCtrl',
        selected_service: selected_service,
        resolve: {
      selected_service_address: function () {
          return selected_service;
      }
        }
    });    
  }
});


angular.module('cloudStoreClient').controller('ModalInstanceCtrl', function ($scope, $modalInstance, selected_service_address) {
  $scope.selected_service_address = selected_service_address;

  $scope.submit = function(device) {
    $modalInstance.close($scope.selected_service_address);
    console.log(device);
  };

  $scope.cancel = function () {
    $modalInstance.dismiss('cancel');
  };
});
