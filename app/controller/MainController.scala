package controller
import model.Device
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.actor.Props
import actor.DeviceLoggerSupervisor


/**
 * @author syedatifakhtar
 */

object MainController extends Controller {

  val deviceLoggerSupervisor = Akka.system.actorOf(Props[DeviceLoggerSupervisor],"DeviceSupervisor")
  
  def status(deviceId: String) = Action(parse.json){
  request=> request.body.validate[Device].map{
    case device@Device(id,status)=> 
      deviceLoggerSupervisor ! device
    Ok(deviceId)
    
    }.recoverTotal {
      e=>BadRequest(s"$e")
    }
  }


}