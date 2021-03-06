'use strict';

angular.module('cloudStoreClient')
    .controller('ManageServiceCtrl', function ($scope,$http,$modal, $interval) {
        $scope.loadAllServices = function() {
            $http.get('/services').then(function(response) {
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
                scope: $scope,
                resolve: {
                    selectedService : function(){return selectedService}
                }
            });
        };

        $scope.$on("service-configured", function(event, service) {
            $scope.loadAllServices();
        });

        var intervalPromise = $interval($scope.loadAllServices, 2000);
        $scope.$on('$destroy', function () { $interval.cancel(intervalPromise); });
    });


angular.module('cloudStoreClient')
    .controller('ServiceEditModalCtrl', function ($scope, $modalInstance, $http, selectedService) {

        $scope.service = selectedService;

        $scope.submit = function(service) {
            $modalInstance.close();

            var serviceUrl = '/service/update';

            $http({
                method: 'POST',
                url: serviceUrl,
                data: JSON.stringify(service)
            }).then(function(){
                $scope.$emit("service-configured", service);
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    });
