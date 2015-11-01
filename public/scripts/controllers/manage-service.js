'use strict';

angular.module('cloudStoreClient')
    .controller('ManageServiceCtrl', function ($scope,$http,$modal) {
        $scope.loadAllServices = function() {
            $http.get('/services/all').then(function(response) {
                $scope.services = response.data.services;
            }, function(response) {
                $scope.services = [];
            });
            $scope.animationsEnabled = true;
        };

        $scope.editServiceModal = function(selectedService){
            var modalInstance = $modal.open({
                animation: $scope.animationsEnabled,
                templateUrl: 'serviceEditModal.html',
                controller: 'ServiceEditModalCtrl',
                resolve: {
                    selectedService : function(){return selectedService}
                }
            });
        };
    });


angular.module('cloudStoreClient')
    .controller('ServiceEditModalCtrl', function ($scope, $modalInstance, $http, selectedService) {

        $scope.service = selectedService;

        $scope.submit = function(service) {
            $modalInstance.close();

            var serviceUrl = selectedService ? '/appliances/update' : '/appliances/add';

            $http({
                method: 'POST',
                url: serviceUrl,
                data: JSON.stringify(service)
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    });
