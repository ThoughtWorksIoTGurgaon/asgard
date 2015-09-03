package actor

import akka.actor.Actor
import model.CreateLog

class DeviceLogger extends Actor{
  
  def receive = {
     case messageToLog:CreateLog =>
      println(s"Received object: $messageToLog")
  }
}