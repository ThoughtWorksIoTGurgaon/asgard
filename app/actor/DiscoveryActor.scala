package actor

import java.net.{ InetAddress, InetSocketAddress }
import actor.DiscoverySupervisor._
import akka.actor.ActorRef
import com.typesafe.config.{ Config, ConfigFactory }
import model.{SwitchService, Service, Appliance}
import net.sigusr.mqtt.api.{ Connect, Connected, ConnectionFailure, Manager, Publish }
import play.api.{ Logger, Play }
import play.api.libs.json.Json
import net.ceedubs.ficus.Ficus._
import scala.concurrent.duration._
import akka.persistence.{SnapshotMetadata, PersistentActor, RecoveryCompleted, SnapshotOffer}
import java.security.MessageDigest


case object DiscoveryState {
  val dummyUntaggedServices = List(
    SwitchService("1","address:myid1","Fan","on")
    ,SwitchService("2","address:myid2","Tubelight","on")
    ,SwitchService("3","address:myid3","Monitor","on"))
  val dummyUntaggedServices2 = List(
    SwitchService("5","address:myid1","awesome","on")
    ,SwitchService("6","address:myid2","awesomer","on")
    ,SwitchService("7","address:myid3","awesomest","off"))
  val dummyAppliances = List(
  Appliance(Some("1"),Some("AwesomeAppliance"),Some("AwesomeDescription"),dummyUntaggedServices2)
  )
}

case class DiscoveryState(appliances: List[Appliance] = DiscoveryState.dummyAppliances, untaggedServices: List[Service] = DiscoveryState.dummyUntaggedServices) {
  def updated(evt: DiscoveryEvent): DiscoveryState = {
    evt match {
      case ApplianceAdded(appliance) =>
        val uniqueId = appliance.services.foldLeft(""){(a,switch) => a + switch.id}
        copy(appliance.copy(id = Some(sha(uniqueId))) :: appliances, untaggedServices.diff(appliance.services))
      case ApplianceConfigured(newAppliance) =>
        copy(
          appliances
            .filter(_.id != newAppliance.id) :+ newAppliance
          ,untaggedServices
            .filter(s => newAppliance.services.exists(_.id == s.id)))
      case ApplianceDeleted(app) =>
        copy(appliances.filter(_.id == app.id), untaggedServices ::: app.services)
      case ServiceDiscovered(service) =>
        copy(appliances, untaggedServices :+ service)
    }
  }

  def sha(s: String) = {
    MessageDigest.getInstance("SHA-256").digest(s.getBytes).toString
  }
}

class DiscoveryActor(queue: String, _supervisor: ActorRef) extends PersistentActor {
  import context.dispatcher

  val persistenceId = ""
  val config: Config = ConfigFactory.load
  val localPublisher = "SCALA_DISCOVERY_ACTOR"
  val mqttHost = config.as[String]("mqtt.host")
  val mqttPort: Int = config.as[Int]("mqtt.port")
  val supervisor = _supervisor
  val mqttAutoReconnectIntervalDuration = config.as[FiniteDuration]("mqtt.autoreconnect.intervalduration")
  val log = Logger.logger
  var state = DiscoveryState()
  case object Snap

  val mqttManager = context.actorOf(
    Manager.props(new InetSocketAddress(InetAddress.getByName(mqttHost), mqttPort)))
  val scheduleRepeatedMQTTConnect = context.system.scheduler.scheduleOnce(0 minutes,mqttManager, Connect(localPublisher))
  val scheduleAutoSnapShot = context.system.scheduler.schedule(0 minutes,2 minutes,self,Snap)


  def recoveryCompleted() = {
    log.debug(s"${self.path.name} - Recovery completed\nState: ${state}")
    init
  }
  
  def updateState(event: DiscoveryEvent) = {
    state = state.updated(event)
  }

  def init() = {
    log.debug(
      s"------------------------------------------------------" +
        s"Starting ${self.path.name} with the following configuration: " +
        s"\nmqttHost: $mqttHost" +
        s"\nmqttPort: $mqttPort" +
        s"\nreconnectInterval: $mqttAutoReconnectIntervalDuration" +
        s"------------------------------------------------------")
    
  }
  
  def receiveRecover:Receive = {
    case RecoveryCompleted => recoveryCompleted()
    case discoveryEvent: DiscoveryEvent =>
      updateState(discoveryEvent)
    case SnapshotOffer(_,snapshot: DiscoveryState) =>
      log.debug(s"{self.path.name} - Updating from snapshot")
      state = snapshot
      recoveryCompleted()
  }

  override def receiveCommand: Receive = {
    case GetUntaggedServices =>
      log.debug(s"${self.path.name} - Replying with untagged services : ${state.untaggedServices}")
      sender ! state.untaggedServices
    case GetAppliances =>
      log.debug(s"${self.path.name} - Replying with list of appliances : ${state.appliances}")
      sender ! state.appliances
    case Connected ⇒
      log.debug(s"Succesfully connected to MQTT at $mqttHost:$mqttPort")
      log.debug(s"Ready to publish to topic [ $localPublisher ]")
      supervisor ! ConnectedToMQTT
      context become ready(sender)
    case ConnectionFailure(reason) ⇒
      log.debug(s"Connection to $mqttHost:$mqttPort [$reason] - trying reconnect in $mqttAutoReconnectIntervalDuration")
      context.system.scheduler.scheduleOnce(mqttAutoReconnectIntervalDuration, mqttManager, Connect(localPublisher))
      supervisor ! WaitingReconnect

    case Snap =>
      log.debug(s"${self.path.name} - Saving snapshot")
      saveSnapshot(state)
  }

  def ready(mqttManager: ActorRef): Receive = {
    case event:DiscoveryEvent =>
      log.debug(s"${self.path.name} - Got discovery event")
      persist(event)(updateState)
    case GetUntaggedServices =>
      log.debug(s"${self.path.name} - Replying with untagged services : ${state.untaggedServices}")
      sender ! state.untaggedServices
    case GetAppliances =>
      log.debug(s"${self.path.name} - Replying with list of appliances : ${state.appliances}")
      sender ! state.appliances
    case Snap => saveSnapshot(state)
    case UpdateDeviceState(serviceRequest) ⇒
      log.debug(s"Sending service ${serviceRequest.address}, ${serviceRequest.request}, ${serviceRequest.data}")

      mqttManager !
        Publish(
          s"/service/${serviceRequest.address}/cmd",
          Json.toJson(serviceRequest).toString.getBytes("UTF-8").to[Vector])
  }

}