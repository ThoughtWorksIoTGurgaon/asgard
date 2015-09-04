'use strict';

angular.module('cloudStoreClient')
  .controller('DevicesCTRL',function DeviceCTRL($scope) {
    
    $scope.sortableOptions = {
      disabled: false
    };
    $scope.status=false;
    $scope.oneAtATime = true;
    $scope.devices = [
      {
        "title": "Master Bedroom",
        "description": "Controls the master bedroom",
        "id": "ABCD12345",
        "status": "UP",
        "temperature": "34",
        "switches": [
            {
                "label": "Fan",
                "state": true,
                "id" : "1234"
            },
            {
                "label": "AC",
                "state": true,
                "id": "abc"
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
    $scope.changeState = function() {
      console.log("Hello!!!!!");
    };   
});
