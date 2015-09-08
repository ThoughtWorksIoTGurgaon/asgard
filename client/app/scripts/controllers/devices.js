'use strict';

angular.module('cloudStoreClient')
  .controller('DevicesCTRL',function DeviceCTRL($scope,$http) {
    
    $scope.sortableOptions = {
      disabled: false
    };
    $scope.status=false;
    $scope.oneAtATime = true;
    $scope.devices = [
      {
        "title": "Master Bedroom",
        "description": "Controls the master bedroom",
        "id": "ABCD12356",
        "status": "UP",
        "temperature": "34",
        "switches": [
            {
                "label": "Fan",
                "state": true,
                "id" : "my-device-id:0"
            },
            {
                "label": "AC",
                "state": true,
                "id": "my-device-id:1"
            }
        ]
      },
     {
        "title": "Master Kitchen",
        "description": "Controls the chef",
        "id": "ABCD1233",
        "status": "UP",
        "temperature": "34",
        "switches": [
            {
                "label": "Fan",
                "state": false,
                "id": "xyz"
            },
            {
                "label": "Fridge",
                "state": true,
                "id": "def"
            }
        ]
      }
    ]; 
    $scope.changeState = function($switch) {
    	
    	var postData = JSON.stringify($switch);
    	console.log("Switch--->" + postData);
    	$http({
    	    method: 'POST',
    	    url: 'http://localhost:8080/device',
    	    data: postData
    	});
    };   
});
