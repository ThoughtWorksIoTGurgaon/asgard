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

  val queenBeeSupervisor = Akka.system.actorOf(Props[QueenBeeSupervisor],"QueenBeeSupervisor")

  def service() = Action(parse.json) {
    request =>
      println(s"Got some request!! ${request.body}")
      request.body.validate[QueenBeeMessage].map {
        case queenBeeMessage @ QueenBeeMessage(address, request, data) =>
          queenBeeSupervisor ! UpdateDeviceState(queenBeeMessage)
          Ok(s"Received service: $queenBeeMessage").withHeaders(
            ACCESS_CONTROL_ALLOW_ORIGIN -> "*")
      }.recoverTotal {
        e =>
          println(s"Bad request!:$e")
          BadRequest(s"$e").withHeaders(
            ACCESS_CONTROL_ALLOW_ORIGIN -> "*")
      }
  }

  def devices() = Action {
    var devicesList = List(
      Device(
        "ABCD12355",
        "Master Bedroom",
        "Controls the master bedroom",
        List(
          Service(
            Some("1234"),
            "Fan", 
            "my-device-id:1", 
            "off", 
            List(
              "switch-on", 
              "switch-off"
            )
          )
        )
      )
    )
    Ok(Json.obj("devices" -> devicesList)).withHeaders(
      ACCESS_CONTROL_ALLOW_ORIGIN -> "*")
  }
}