package actor

import akka.actor.Actor
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration.FiniteDuration
import net.sigusr.mqtt.api.ConnectionFailure
import java.net.InetSocketAddress
import net.sigusr.mqtt.api.Manager
import scala.util.Random
import net.sigusr.mqtt.api.Connect
import akka.actor.ActorRef
import net.sigusr.mqtt.api.Connected
import net.sigusr.mqtt.api.Publish
import scala.concurrent.duration._

/**
 * @author syedatifakhtar
 */
class QueenBeePubSubActor extends Actor {
  import context.dispatcher

  private val localSubscriber = "/device/hello"
  private val localPublisher = "/device/hello"
  
  context.actorOf(Manager.props(new InetSocketAddress(1883))) ! Connect(localSubscriber)


  def receive: Receive = {
    case Connected ⇒
      println("Successfully connected to localhost:1883")
      println(s"Ready to publish to topic [ $localPublisher ]")
      context become ready(sender())
      self ! "Hi from scala"
    case ConnectionFailure(reason) ⇒
      println(s"Connection to localhost:1883 failed [$reason]")
  }

  def ready(mqttManager: ActorRef): Receive = {
    case m: String ⇒
      println(s"Publishing [ $m ]")
      mqttManager ! Publish(localPublisher, m.getBytes("UTF-8").to[Vector])
  }
}

object LocalPublisher {

  val config =
    """akka {
         loglevel = INFO
         actor {
            debug {
              receive = off
              autoreceive = off
              lifecycle = off
            }
         }
       }
    """
}