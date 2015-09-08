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
import actor.QueenBeeSupervisor._

/**
 * @author syedatifakhtar
 */
class QueenBeePubSubActor(queue: String,_supervisor: ActorRef) extends Actor {
  import context.dispatcher

  private val localSubscriber = queue
  private val localPublisher = queue
  private val supervisor = _supervisor
  
  context.actorOf(Manager.props(new InetSocketAddress(1883))) ! Connect(localSubscriber)


  def receive: Receive = {
    case Connected ⇒
      println("Successfully connected to localhost:1883")
      println(s"Ready to publish to topic [ $localPublisher ]")
      context become ready(sender())
      supervisor ! ConnectedToMQTT
    case ConnectionFailure(reason) ⇒
      println(s"Connection to localhost:1883 failed [$reason]")
      supervisor ! WaitingReconnect
  }

  def ready(mqttManager: ActorRef): Receive = {
    case UpdateDeviceState(device) ⇒
      println(s"Updating device $device")
      mqttManager ! Publish(localPublisher, "Hello!".getBytes("UTF-8").to[Vector])
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