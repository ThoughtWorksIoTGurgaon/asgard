package controller
import model._
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.actor.Props
import actor.QueenBeeSupervisor
import actor.QueenBeeSupervisor.UpdateDeviceState

/**
 * @author syedatifakhtar
 */

object MainController extends Controller {

  val queenBeeSupervisor = Akka.system.actorOf(Props[QueenBeeSupervisor],"QueenBeeSupervisor")

  def device() = Action(parse.json) {
    request =>
      println(s"Got some request!! ${request.body}")
      request.body.validate[Device].map {
        case device @ Device(label, state, id) =>
          queenBeeSupervisor ! UpdateDeviceState(device)
          Ok(s"Received device: $device").withHeaders(
            ACCESS_CONTROL_ALLOW_ORIGIN -> "*")
      }.recoverTotal {
        e =>
          println(s"Bad request!:$e")
          BadRequest(s"$e").withHeaders(
            ACCESS_CONTROL_ALLOW_ORIGIN -> "*")
      }
  }

  def options(path: String) = Action {
    Ok("").withHeaders(
      "Access-Control-Allow-Origin" -> "*",
      "Access-Control-Allow-Methods" -> "GET, POST, PUT, DELETE, OPTIONS",
      "Access-Control-Allow-Headers" -> "Accept, Origin, Content-type, X-Json, X-Prototype-Version, X-Requested-With",
      "Access-Control-Allow-Credentials" -> "true",
      "Access-Control-Max-Age" -> (0).toString)
  }

  def test = Action {
    request =>
      Ok("Publishing message to mosquitto")
  }

}