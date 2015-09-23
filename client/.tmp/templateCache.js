angular.module('cloudStoreClient').run(['$templateCache', function($templateCache) {
  'use strict';

  $templateCache.put('views/about.html',
    "<p>This is the about view.</p>"
  );


  $templateCache.put('views/add_device.html',
    "<div ng-init=\"hello()\"> <div ng-show=\"services.length > 0\"> <h3>Click to map a service to device</h3> <script type=\"text/ng-template\" id=\"myModalContent.html\"><div class=\"modal-header\">\n" +
    "        <h3 class=\"modal-title\">Map {{selected_service_address}}</h3>\n" +
    "      </div>\n" +
    "      <div class=\"modal-body\">\n" +
    "      <form class=\"form-horizontal\">\n" +
    "        <fieldset>\n" +
    "          <div class=\"form-group\">\n" +
    "            <label for=\"deviceName\" class=\"col-lg-3 control-label\">Device Name</label>\n" +
    "            <div class=\"col-lg-9\">\n" +
    "              <input type=\"text\" class=\"form-control\" id=\"deviceName\" placeholder=\"Device name\" ng-model=\"device.name\">\n" +
    "            </div>\n" +
    "          </div>\n" +
    "          <div class=\"form-group\">\n" +
    "            <label for=\"deviceDescription\" class=\"col-lg-3 control-label\">Device Description</label>\n" +
    "            <div class=\"col-lg-9\">\n" +
    "              <input type=\"text\" class=\"form-control\" id=\"deviceDescription\" placeholder=\"Device description\" ng-model=\"device.description\">\n" +
    "            </div>\n" +
    "          </div>\n" +
    "          <div class=\"form-group\">\n" +
    "            <label for=\"deviceLocation\" class=\"col-lg-3 control-label\">Device Location</label>\n" +
    "            <div class=\"col-lg-9\">\n" +
    "              <input type=\"text\" class=\"form-control\" id=\"deviceLocation\" placeholder=\"Device location\" ng-model=\"device.location\">\n" +
    "            </div>\n" +
    "          </div>\n" +
    "        </fieldset>\n" +
    "      </form>\n" +
    "      </div>\n" +
    "      <div class=\"modal-footer\">\n" +
    "        <button class=\"btn btn-primary\" type=\"button\" ng-click=\"submit(device)\">Submit</button>\n" +
    "        <button class=\"btn btn-warning\" type=\"button\" ng-click=\"cancel()\">Cancel</button>\n" +
    "      </div></script> <div class=\"panel panel-default\" ng-repeat=\"service in services\"> <div class=\"panel-body\"> <h4 class=\"inline\">{{service.address}}</h4> <a href=\"javascript:;\"><i class=\"pull-right glyphicon glyphicon-pencil vertical-middle\" ng-click=\"open('{{service.address}}')\"></i></a> </div> </div> </div> <div ng-hide=\"services.length > 0\"> No services found </div> </div>"
  );


  $templateCache.put('views/main.html',
    "<div class=\"container\"> <div ng-controller=\"DevicesCtrl\" class=\"list-unstyled\"> <div class=\"row-fluid\"> <div class=\"span4\"> <h1>Devices</h1> </div> <div class=\"span8\" style=\"height:60px\"> <button class=\"btn btn-primary btn-fab btn-fab-mini btn-raised mdi-content-add pull-right btn-xs\"></button> <button type=\"button\" class=\"btn btn-default btn-fab btn-fab-mini pull-right btn-success mdi-action-dashboard btn-xs\"> </button> </div> </div> <div class=\"row-fluid\"> <div class=\"span4\"> <accordion class=\"accordion\" close-others=\"oneAtATime\"> <div ui-sortable=\"sortableOptions\" ng-model=\"devices\" class=\"accordion\"> <div accordion-group ng-repeat=\"device in devices\" class=\"indent panel\" is-open=\"status.open\"> <accordion-heading> {{device.title}} <i class=\"pull-right glyphicon\" ng-class=\"{'glyphicon-chevron-down': status.open, 'glyphicon-chevron-right': !status.open}\"></i> </accordion-heading> <div> <div>Description: {{device.description}}</div> <div>Status: {{device.dtatus}}</div> <div class=\"indent\"> <h3>Status</h3> <h3>Peripherals</h3> <ul class=\"list-unstyled indent\"> <li ng-repeat=\"switch in device.switches\"> <div class=\"togglebutton\" id=\"{{switch.id}}\"> <label> <input type=\"checkbox\" ng-model=\"switch.data\" ng-change=\"changeState(switch)\" ng-true-value=\"'on'\" ng-false-value=\"'off'\"> <span class=\"toggle\"></span> {{switch.label}} </label> </div> </li> </ul> </div> </div> </div> </div> </accordion> </div> </div> <div class=\"row-fluid\"> </div> </div> </div>"
  );

}]);
