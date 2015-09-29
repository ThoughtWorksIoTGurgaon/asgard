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
import scala.concurrent.duration._
import model.QueenBeeMessage


class QueenBeePubSubActor(queue: String, _supervisor: ActorRef) extends Actor {
  import context.dispatcher

  private val localSubscriber = "SCALA_QueenBee_Subscriber"
  private val localPublisher = "SCALA_QueenBee_Publisher"
  private val supervisor = _supervisor
  
  var mqttManager = context.actorOf(Manager.props(new InetSocketAddress(1883)))
  context.system.scheduler.scheduleOnce(0 minutes,mqttManager,Connect(localPublisher))


  def receive: Receive = {
    case Connected ⇒
      println("Successfully connected to localhost:1883")
      println(s"Ready to publish to topic [ $localPublisher ]")
      supervisor ! ConnectedToMQTT
      context become ready(sender())

    case ConnectionFailure(reason) ⇒
      println(s"Connection to localhost:1883 failed [$reason] - trying reconnect in 5 minutes")
        context.system.scheduler.scheduleOnce(5 minutes,mqttManager,Connect(localPublisher))
      supervisor ! WaitingReconnect
  }

  def ready(mqttManager: ActorRef): Receive = {
    case UpdateDeviceState(qbMessage) ⇒
      println(s"Sending service $qbMessage.address data $qbMessage.request with data $qbMessage.data")
      
      mqttManager ! 
      Publish(
        s"/service/${qbMessage.address}/cmd", 
        Json.toJson(qbMessage).toString.getBytes("UTF-8").to[Vector]
      )
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