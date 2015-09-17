'use strict';

angular.module('cloudStoreClient')
    .controller('AddDeviceCtrl', function ($scope,$http,$modal) {
	$scope.hello = function() {
	    $scope.serviceAddresses = ['1234', 'ABCD', 'WXYZ'];
	    $scope.animationsEnabled = true;
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
		
		console.log($modal);
	    }
	};   
    });


angular.module('cloudStoreClient').controller('ModalInstanceCtrl', function ($scope, $modalInstance, selected_service_address) {

    $scope.selected_service_address = selected_service_address;

    $scope.ok = function () {
	$modalInstance.close($scope.selected_service_address);
    };

    $scope.cancel = function () {
	$modalInstance.dismiss('cancel');
    };
});
