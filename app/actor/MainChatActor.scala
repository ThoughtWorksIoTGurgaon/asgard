package actor

import akka.actor.Actor

/**
 * @author syedatifakhtar
 */
class MainChatActor extends Actor{
   def receive = {
     case _ =>
   }
}

case class Message(message: String)
