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
		        "Title": "Master Bedroom",
		        "Description": "Controls the master bedroom",
		        "Id": "ABCD12345",
		        "Status": "UP",
		        "Temperature": "34",
		        "Switches": [
		            {
		                "Label": "Fan",
		                "State": true
		            },
		            {
		                "Label": "AC",
		                "State": true
		            }
		        ]
		    },
		   {
		        "Title": "Master Kitchen",
		        "Description": "Controls the chef",
		        "Id": "ABCD1233",
		        "Status": "UP",
		        "Temperature": "34",
		        "Switches": [
		            {
		                "Label": "Fan",
		                "State": false
		            },
		            {
		                "Label": "Fridge",
		                "State": true
		            }
		        ]
		    }
			];		
		  });
