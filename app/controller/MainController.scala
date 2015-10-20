package controller
import actor.QueenBeeSupervisor.UpdateDeviceState
import akka.actor.ActorRef
import model._
import play.api.libs.json._
import play.api.mvc.{Action, Controller}

import scala.util.Random

class MainController(val queenBeeSupervisor: ActorRef) extends Controller {


  var allServicesMap = Map(
    "my-device-id:1" ->
      SwitchService(
        "1234",
        "my-device-id:1",
        "Switch",
        "on"
      ),
    "my-device-id:2" ->
      SwitchService(
        "1235",
        "my-device-id:2",
        "Switch",
        "off"
      ),
    "my-device-id:3" ->
      SwitchService(
        "1236",
        "my-device-id:3",
        "Switch",
        "on"
      )
  )

  var appliancesMap = Map[String, Appliance]()

  var unassignedServicesSet = Set(
    allServicesMap("my-device-id:1"),
    allServicesMap("my-device-id:2"),
    allServicesMap("my-device-id:3")
  )

  def getUnassignedServicesAction = Action {
    Ok(Json.obj("services" -> unassignedServicesSet)).withHeaders(
      ACCESS_CONTROL_ALLOW_ORIGIN -> "*")
  }

  def updateServiceValue() = Action(parse.json) {
    request =>
      println(s"Got some request!! ${request.body}")
      request.body.validate[WidgetStatus].map {
        case widgetRequest @ WidgetStatus(address, status) =>
          val serviceRequest = allServicesMap(address).updateStatus(status)
          queenBeeSupervisor ! UpdateDeviceState(serviceRequest)

          Ok(s"Received widgetRequest: $widgetRequest").withHeaders(
            ACCESS_CONTROL_ALLOW_ORIGIN -> "*")
      }.recoverTotal {
        e =>
          println(s"Bad request!:$e")
          BadRequest(s"$e").withHeaders(
            ACCESS_CONTROL_ALLOW_ORIGIN -> "*")
      }
  }

  def getAllAppliancesAction = Action {
    Ok(Json.obj("appliances" -> appliancesMap.values)).withHeaders(
      ACCESS_CONTROL_ALLOW_ORIGIN -> "*")
  }

  def updateAppliancesAction() = Action(parse.json) {
    request =>
      println(s"Got some request!! ${request.body}")
      request.body.validate[Appliance].map {
        case appliance @ Appliance(id, _, _, _) =>
          var applianceId = id
          val newServices = appliance.services

          if (applianceId != None && appliancesMap.contains(id.get)) {
            val oldServices = appliancesMap(id.get).services
            unassignedServicesSet ++= oldServices
          } else {
            applianceId = Option(Random.alphanumeric.take(5).mkString)
          }

          unassignedServicesSet --= newServices
          appliancesMap = appliancesMap updated(applianceId.get, appliance)

          Ok(s"Appliance updated: $appliance").withHeaders(
            ACCESS_CONTROL_ALLOW_ORIGIN -> "*")
      }.recoverTotal {
        e =>
          println(s"Bad request!:$e")
          BadRequest(s"$e").withHeaders(
            ACCESS_CONTROL_ALLOW_ORIGIN -> "*")
      }
  }
}