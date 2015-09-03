package actor

import scala.concurrent.duration._

import akka.actor.Actor
import akka.actor.OneForOneStrategy
import akka.actor.Props
import akka.actor.SupervisorStrategy._
import model.CreateLog

/**
 * @author syedatifakhtar
 */


class DeviceLoggerSupervisor extends Actor{
  
 val deviceLogger = context.actorOf(Props[DeviceLogger]) 
 
 override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10,withinTimeRange = 10 minutes){
   case _:Exception =>
     Restart
 }
  def receive = {
    case messageToLog:CreateLog =>
      deviceLogger ! messageToLog
  }
}