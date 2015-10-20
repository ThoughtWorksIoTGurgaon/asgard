package actor

import java.net.{InetAddress, InetSocketAddress}
import actor.DiscoverySupervisor._
import akka.actor.{ActorLogging, Actor, ActorRef}
import com.typesafe.config.{Config, ConfigFactory}
import net.sigusr.mqtt.api.{Connect, Connected, ConnectionFailure, Manager, Publish}
import play.api.{Logger, Play}
import play.api.libs.json.Json
import net.ceedubs.ficus.Ficus._
import scala.concurrent.duration._


class DiscoveryActor(queue: String, _supervisor: ActorRef) extends Actor {
  import context.dispatcher

  val config: Config = ConfigFactory.load
  val localPublisher = "SCALA_DISCOVERY_ACTOR"
  val mqttHost = config.as[String]("mqtt.host")
  val mqttPort: Int = config.as[Int]("mqtt.port")
  val supervisor = _supervisor
  val mqttAutoReconnectIntervalDuration = config.as[FiniteDuration]("mqtt.autoreconnect.intervalduration")
  val log = Logger.logger
  
  var mqttManager = context.actorOf(
    Manager.props(new InetSocketAddress(InetAddress.getByName(mqttHost),mqttPort))
  )

  override def preStart() = {
    log.debug(
      s"------------------------------------------------------" +
      s"Starting ${self.path.name} with the following configuration: " +
        s"\nmqttHost: $mqttHost" +
        s"\nmqttPort: $mqttPort" +
        s"\nreconnectInterval: $mqttAutoReconnectIntervalDuration" +
        s"------------------------------------------------------")
  }
  context.system.scheduler.scheduleOnce(0 minutes, mqttManager, Connect(localPublisher))


  def receive: Receive = {
    case Connected ⇒
      log.debug(s"Succesfully connected to MQTT at $mqttHost:$mqttPort")
      log.debug(s"Ready to publish to topic [ $localPublisher ]")
      supervisor ! ConnectedToMQTT
      context become ready(sender())

    case ConnectionFailure(reason) ⇒
      log.debug(s"Connection to $mqttHost:$mqttPort [$reason] - trying reconnect in $mqttAutoReconnectIntervalDuration")
      context.system.scheduler.scheduleOnce(mqttAutoReconnectIntervalDuration,mqttManager,Connect(localPublisher))
      supervisor ! WaitingReconnect
  }

  def ready(mqttManager: ActorRef): Receive = {
    case UpdateDeviceState(serviceRequest) ⇒
      log.debug(s"Sending service ${serviceRequest.address}, ${serviceRequest.request}, ${serviceRequest.data}")
      
      mqttManager ! 
      Publish(
        s"/service/${serviceRequest.address}/cmd",
        Json.toJson(serviceRequest).toString.getBytes("UTF-8").to[Vector]
      )
  }
  
}