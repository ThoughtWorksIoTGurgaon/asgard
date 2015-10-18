package controller
import model._
import play.api.libs.json._
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.actor.Props
import actor.QueenBeeSupervisor
import actor.QueenBeeSupervisor.UpdateDeviceState

object MainController extends Controller {

  val queenBeeSupervisor = Akka.system.actorOf(Props[QueenBeeSupervisor], "QueenBeeSupervisor")

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

  var appliancesMap = Map[String, Appliance](
    "ABCD12355" -> Appliance(
        "ABCD12355",
        "Fan",
        "Controls bedroom fan",
        List(
          allServicesMap("my-device-id:1")
        )
      )
  )

  var unassignedServicesSet = Set(
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

          val newServices = appliance.services
          val oldServices = appliancesMap(id).services

          unassignedServicesSet ++= oldServices
          unassignedServicesSet --= newServices

          appliancesMap = appliancesMap updated (id, appliance)

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