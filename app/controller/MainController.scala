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

  var services = Map(
    "my-device-id:1" ->
      SwitchService(
        "1234",
        "my-device-id:1",
        "Switch",
        "on"
      )
  )

  var appliancesList = List[Appliance](
    Appliance(
        "ABCD12355",
        "Fan",
        "Controls bedroom fan",
        List(
          services("my-device-id:1")
        )
      )
  )

  def service = Action(parse.json) {
    request =>
      println(s"Got some request!! ${request.body}")
      request.body.validate[WidgetStatus].map {
        case widgetRequest @ WidgetStatus(address, status) =>
          val serviceRequest = services(address).updateStatus(status)
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

  def appliances = Action {
    Ok(Json.obj("appliances" -> appliancesList)).withHeaders(
      ACCESS_CONTROL_ALLOW_ORIGIN -> "*")
  }
}