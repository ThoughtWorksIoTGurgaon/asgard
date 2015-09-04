package actor

import akka.actor.Actor

/**
 * @author syedatifakhtar
 */
class ChatActor extends Actor{
  
  def receive = {
    case message@_=>
      println(s"Received ${message} from ${sender.path.name}")
     sender ! "Hi"
  }
  
}