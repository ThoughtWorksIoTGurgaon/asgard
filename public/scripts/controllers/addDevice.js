'use strict';

angular.module('cloudStoreClient')
    .controller('AddDeviceCtrl', function ($scope,$http,$modal) {
  $scope.loadAvailableServices = function() {
    $http.get('/appliances').then(function(response) {
      $scope.appliances = response.data.appliances;
    }, function(response) {
      $scope.appliances = [];
    });
    $scope.animationsEnabled = true;   
  };

  $scope.openAddApplianceModal = function(selectedAppliance){
    var modalInstance = $modal.open({
        animation: $scope.animationsEnabled,
        templateUrl: 'myModalContent.html',
        controller: 'ModalInstanceCtrl',
        resolve: {
            appliance : function(){return selectedAppliance}
        }
    });
  };
});


angular.module('cloudStoreClient')
    .controller('ModalInstanceCtrl', function ($scope, $modalInstance, $http, appliance) {

  $scope.allServices = appliance && appliance.services || [];
  $scope.appliance = appliance;

  $scope.newServices = {};
  $scope.allServices.forEach(function(service){
      $scope.newServices[service.address] = true;
  });

  $http.get('/services/unassigned').then(function(response) {
      $scope.allServices = $scope.allServices.concat(response.data.services);
  });

  $scope.submit = function(appliance, newServices) {
    $modalInstance.close();

    appliance.services = $scope.allServices.filter(function(service){
        return newServices[service.address];
    });

    var applianceServiceUrl = $scope.appliance ? '/appliances/update' : '/appliances/add';

    $http({
        method: 'POST',
        url: applianceServiceUrl,
        data: JSON.stringify(appliance)
    });
  };

  $scope.cancel = function () {
    $modalInstance.dismiss('cancel');
  };
});
