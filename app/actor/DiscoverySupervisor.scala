package actor

import akka.actor.Actor
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy.Restart
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import akka.actor.Props
import akka.pattern.{ ask, pipe }
import model.{WidgetStatus, Service, Appliance, ServiceRequest}
import actor.DiscoverySupervisor._
import play.api.Logger

class DiscoverySupervisor extends Actor{

  implicit val timeout = akka.util.Timeout(1 minute)
   override val supervisorStrategy =
     OneForOneStrategy(maxNrOfRetries = 10,withinTimeRange = 10 minutes){
       case _:Exception =>
         Restart
     }
  
  var queryBuffer : List[GetResource] = List.empty
  val log = Logger.logger
   
   val discoveryActor = context.actorOf(
    Props(new DiscoveryActor("/services/#", self)),
    "discoveryActor"
   )
   
   self.path.name
   def receive = {
     case ConnectedToMQTT=>
          context become liveConsume
          log.debug("Connected to MQTT Broker,ready to receive!")
     case cmd:Command =>
       log.info("MQTT Connection lost! Message will be relayed once service is resumed")
       queryBuffer.map(discoveryActor ! _)
       queryBuffer = List.empty
     case query:GetResource =>
       discoveryActor ask query pipeTo sender
     case WaitingReconnect=>
              log.debug(s"\n\n\n\n\n------------------Waiting to reconnect-------------")
          context become waitingReconnect
      
   }
   
   def liveConsume: Receive = {
     case cmd:Command =>
       log.debug("In live consume")
       discoveryActor ! cmd
     case query:GetResource =>
       discoveryActor ask query pipeTo sender
     case WaitingReconnect=>
       log.debug(s"\n\n\n\n\n------------------Waiting to reconnect-------------")
     context become waitingReconnect
       
   }
   
   def waitingReconnect: Receive = {
     case cmd:Command =>
       log.debug("In live consume")
       discoveryActor ! cmd
     case query:GetResource =>
       discoveryActor ask query pipeTo sender
     case ConnectedToMQTT=>
           log.debug("Waiting to reconnect")
          context become liveConsume
   }
}

object DiscoverySupervisor {
  
  trait MQTTActorMessage
  case object ConnectedToMQTT extends MQTTActorMessage
  case object WaitingReconnect extends MQTTActorMessage

  trait Command
  trait DiscoveryEvent
  case class ApplianceAdded(appliance: Appliance) extends DiscoveryEvent with Command
  case class ApplianceConfigured(appliance: Appliance) extends DiscoveryEvent with Command 
  case class ApplianceDeleted(appliance: Appliance) extends DiscoveryEvent with Command 
  case class ServiceDiscovered(service: Service) extends DiscoveryEvent


  trait GetResource
  case object GetUntaggedServices extends GetResource
  case object GetAppliances extends GetResource


 
  case class UpdateDeviceState(widgetStatus: WidgetStatus) extends Command
}