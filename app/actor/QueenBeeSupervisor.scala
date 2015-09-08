package actor

import akka.actor.Actor
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy._
import scala.concurrent.duration._
import akka.actor.Props
import model.Device
import actor.QueenBeeSupervisor._
import play.api.Logger


/**
 * @author syedatifakhtar
 */
class QueenBeeSupervisor extends Actor{
  
   override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10,withinTimeRange = 10 minutes){
   case _:Exception =>
     Restart
 }
   
   val queenBeeWorker = context.actorOf(Props(new QueenBeePubSubActor("/devices/+/",self)))
   
   def receive = {
     case ConnectedToMQTT=>
          context become liveConsume
          Logger.debug("Connected to MQTT Broker,ready to receive!")
     case WaitingReconnect=>
          context become waitingReconnect
   }
   
   def liveConsume: Receive = {
     case cmd:Command =>
       queenBeeWorker ! cmd
     case WaitingReconnect=>
     context become waitingReconnect
       
   }
   
   def waitingReconnect: Receive = {
     case ConnectedToMQTT=>
          context become liveConsume
   }


}

object QueenBeeSupervisor {
  
  trait MQTTActorMessage
  case object ConnectedToMQTT extends MQTTActorMessage
  case object WaitingReconnect extends MQTTActorMessage
  
  trait Command
  case class UpdateDeviceState(device: Device) extends Command
}