'use strict';

angular.module('cloudStoreClient')
  .controller('DevicesCtrl', function ($scope,$http) {
    $scope.status=true;
    $scope.oneAtATime = true;
    $scope.loadAppliances = function(){
        $http.get("/appliances").then(function(response) {
            console.log(response);
            $scope.appliances = response.data.appliances;
        }, function(response) {
            $scope.appliances = [];
        });
    };
    $scope.changeState = function($service) {
        var postData = JSON.stringify({address: $service.address, value: $service.value});
        //var postData = JSON.stringify($service);
        console.log("Switch--->" + postData);
        $http({
            method: 'POST',
            url: '/service',
            data: postData
        });
    };
});
