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
  val allServices = Map[String, SwitchService](
    "address:myid1" -> SwitchService("1","address:myid1","Fan","on")
    , "address:myid2"-> SwitchService("2","address:myid2","Tubelight","on")
    , "address:myid3" -> SwitchService("3","address:myid3","Monitor","on")
  )

  val dummyUntaggedServices = Set(
    allServices("address:myid2")
  )

  val dummyAppliances = List(
    Appliance(
      Some("1"),
      Some("AwesomeAppliance"),
      Some("AwesomeDescription"),
      List(
        allServices("address:myid1")
      , allServices("address:myid3")
      )
    )
  )
}

case class DiscoveryState(
  appliances: List[Appliance] = DiscoveryState.dummyAppliances,
  allServices: Map[String, SwitchService] = DiscoveryState.allServices,
  untaggedServices: Set[SwitchService] = DiscoveryState.dummyUntaggedServices
) {
  def updated(evt: DiscoveryEvent): DiscoveryState = {
    evt match {
      case ApplianceAdded(appliance) =>
        val uniqueId = appliance.services.foldLeft(""){(a,switch) => a + switch.id}

        copy(
          appliance.copy(id = Some(sha(uniqueId))) :: appliances,
          allServices,
          untaggedServices -- appliance.services
        )

      case ApplianceConfigured(updatedAppliance) =>
        val applianceToUpdate = appliances.find(_.id == updatedAppliance.id).get
        copy(
          appliances
            .filter(_.id != updatedAppliance.id) :+ updatedAppliance,
          allServices,
          (untaggedServices ++ applianceToUpdate.services) -- updatedAppliance.services
        )
      case ApplianceDeleted(app) =>
        copy(appliances.filter(_.id == app.id), allServices, untaggedServices ++ app.services)
      case ServiceDiscovered(service) =>
        copy(appliances, allServices, untaggedServices + service)
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
        s"\nStarting ${self.path.name} with the following configuration: " +
        s"\nmqttHost: $mqttHost" +
        s"\nmqttPort: $mqttPort" +
        s"\nreconnectInterval: $mqttAutoReconnectIntervalDuration" +
        s"\n------------------------------------------------------")
    
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
      val untaggedServices = state.untaggedServices.toList
      log.debug(s"${self.path.name} - Replying with untagged services : $untaggedServices")
      sender ! untaggedServices
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
      val untaggedServices = state.untaggedServices.toList
      log.debug(s"${self.path.name} - Replying with untagged services : $untaggedServices")
      sender ! untaggedServices
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