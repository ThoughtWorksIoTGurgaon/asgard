'use strict';

var updateServiceValue = function($service) {
    var postData = JSON.stringify({address: $service.address, value: $service.value});
    console.log("Switch--->" + postData);
    return $.ajax({
        type: "POST",
        url: '/service/value',
        data: postData,
        dataType: "json",
        contentType: "application/json; charset=utf-8"
    });
};

var pollService = function($scope, $http, $interval, milliSeconds){
    var intervalPromise = $interval(function(){
        $http.get("/service/" + $scope.service.address)
            .then(function(response) {
                $scope.service.value = response.data.service.value;
            });
    }, milliSeconds);
    $scope.$on('$destroy', function () { $interval.cancel(intervalPromise); });
};

angular.module('cloudStoreClient')
  .controller('DashboardCtrl', function ($scope,$http) {
    $scope.status=true;
    $scope.oneAtATime = true;
    $scope.loadAppliances = function(){
        $http.get("/appliances").then(function(response) {
            $scope.appliances = response.data.appliances;
        }, function(response) {
            $scope.appliances = [];
        });
    };
  })
  .directive('toggleButton', function () {
      return {
          scope: {
              service: "=service"
          },
          restrict: 'E',
          templateUrl: '/widget-templates/toggle-button.html',
          controller: function($scope, $http, $interval) {
              $scope.changeState = updateServiceValue;
              pollService($scope, $http, $interval, 5000);
          }
      }
  })
  .directive('textLabel', function () {
      return {
          scope: {
              service: "=service"
          },
          restrict: 'E',
          templateUrl: '/widget-templates/text-label.html',
          controller: function($scope, $http, $interval) {
              pollService($scope, $http, $interval, 500);
          }
      }
  })
  .directive('rangeSlider', function () {
      return {
          scope: {
              service: "=service"
          },
          restrict: 'E',
          templateUrl: '/widget-templates/range-slider.html',
          link: function(scope, element){
              var sliderElement = element.find(".slider")[0];
              noUiSlider.create(
                  sliderElement,
                  {
                      start: 50,
                      connect: "lower",
                      step: 10,
                      range: {
                          min: 0,
                          max: 100
                      }
                  }
              );

              sliderElement.noUiSlider.set(parseInt(scope.service.value));

              sliderElement.noUiSlider.on('change', function(){
                  scope.service.value = sliderElement.noUiSlider.get();
                  updateServiceValue(scope.service);
              });
          }
      }
  })
;
