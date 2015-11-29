'use strict';

angular.module('cloudStoreClient')
    .controller('ManageApplianceCtrl', function ($scope,$http,$modal) {
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
        scope: $scope,
        resolve: {
            selectedAppliance : function(){return selectedAppliance}
        }
    });
  };

  $scope.deleteAppliance = function(selectedAppliance){
      $http({
          method: 'POST',
          url: "/appliances/delete",
          data: JSON.stringify(selectedAppliance)
      }).then(function(response){
          $scope.loadAvailableServices();
      });
  };

  $scope.$on("appliance-added", function(event, appliance) {
      $scope.loadAvailableServices();
  });
});


angular.module('cloudStoreClient')
    .controller('ModalInstanceCtrl', function ($scope, $modalInstance, $http, selectedAppliance) {

  $scope.allServices = selectedAppliance && selectedAppliance.services || [];
  $scope.appliance = selectedAppliance;

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

    var applianceServiceUrl = selectedAppliance ? '/appliances/update' : '/appliances/add';

    $http({
        method: 'POST',
        url: applianceServiceUrl,
        data: JSON.stringify(appliance)
    }).then(function(){
        $scope.$emit("appliance-added", appliance);
    });
  };

  $scope.cancel = function () {
    $modalInstance.dismiss('cancel');
  };
});
