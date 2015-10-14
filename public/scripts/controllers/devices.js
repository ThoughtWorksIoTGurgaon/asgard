'use strict';

angular.module('cloudStoreClient')
  .controller('DevicesCtrl', function ($scope,$http) {
    $scope.status=true;
    $scope.oneAtATime = true;
    $scope.loadDevices = function(){
        $http.get("/devices").then(function(response) {
            console.log(response);
            $scope.devices = response.data.devices;
        }, function(response) {
            $scope.devices = [];
        });
    };
    $scope.changeState = function($service) {
        var request = {
            address: $service.address,
            request: ($service.status === "on" ? "switch-on" : "switch-off"),
            data: ""
        };

        var postData = JSON.stringify(request);
        console.log("Switch--->" + postData);
        $http({
            method: 'POST',
            url: '/service',
            data: postData
        });
    };
});
