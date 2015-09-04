package controller
import model.Device
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.actor.Props
import actor.DeviceLoggerSupervisor
import actor.QueenBeePubSubActor


/**
 * @author syedatifakhtar
 */

object MainController extends Controller {

  val queenBeeActor = Akka.system.actorOf(Props[QueenBeePubSubActor],"QueenBeePubActor")
  
  def status(deviceId: String) = Action(parse.json){
  request=> request.body.validate[Device].map{
    case device@Device(id,status)=> 
      queenBeeActor ! device
    Ok(deviceId)
    
    }.recoverTotal {
      e=>BadRequest(s"$e")
    }
  }
  
  def test = Action {
    request=>
      queenBeeActor ! "Hello from scala!"
      Ok("Publishing message to mosquitto")
  }


}