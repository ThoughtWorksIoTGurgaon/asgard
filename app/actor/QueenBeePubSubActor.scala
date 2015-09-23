package actor

import java.net.InetAddress
import java.net.InetSocketAddress
import actor.QueenBeeSupervisor._
import akka.actor.Actor
import akka.actor.ActorRef
import net.sigusr.mqtt.api.Connected
import net.sigusr.mqtt.api.ConnectionFailure
import net.sigusr.mqtt.api.Publish
import java.net.Inet4Address
import net.sigusr.mqtt.api.Manager
import net.sigusr.mqtt.api.Connect
import play.api.libs.json.Json

/**
 * @author syedatifakhtar
 */
class QueenBeePubSubActor(queue: String,_supervisor: ActorRef) extends Actor {
  import context.dispatcher

  private val localSubscriber = "Harry Potter"
  private val localPublisher = "Harry Potter"
  private val supervisor = _supervisor
  
  context.actorOf(Manager.props(new InetSocketAddress(1883))) ! Connect(localSubscriber)


  def receive: Receive = {
    case Connected ⇒
      println("Successfully connected to localhost:1883")
      println(s"Ready to publish to topic [ $localPublisher ]")
      supervisor ! ConnectedToMQTT
      context become ready(sender())

    case ConnectionFailure(reason) ⇒
      println(s"Connection to localhost:1883 failed [$reason]")
      supervisor ! WaitingReconnect
  }

  def ready(mqttManager: ActorRef): Receive = {
    case UpdateDeviceState(device) ⇒
      println(s"Updating device $device")
      mqttManager ! Publish("/service/sdFGHDjv7w6fdsF:0/cmd", Json.toJson(device).toString.getBytes("UTF-8").to[Vector])
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