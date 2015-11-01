package actor

import java.net.{ InetAddress, InetSocketAddress }
import actor.DiscoverySupervisor._
import akka.actor.ActorRef
import com.typesafe.config.{ Config, ConfigFactory }
import model._
import model.profile._
import net.sigusr.mqtt.api.{ Connect, Connected, ConnectionFailure, Manager, Publish }
import play.api.Logger
import play.api.libs.json.Json
import net.ceedubs.ficus.Ficus._
import scala.concurrent.duration._
import akka.persistence.{PersistentActor, RecoveryCompleted, SnapshotOffer}
import java.security.MessageDigest
import net.sigusr.mqtt.api._

case object DiscoveryState {
  val allServices = Map[String, Service](
    "address:myid1" -> new Service("address:myid1","Fan","on", SpeedProfile.widget) with SpeedProfile
    , "address:myid2"-> new Service("address:myid2","Tubelight","on", SwitchProfile.widget) with SwitchProfile
    , "address:myid3" -> new Service("address:myid3","Monitor","on", SwitchProfile.widget) with SwitchProfile
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
  allServices: Map[String, Service] = DiscoveryState.allServices,
  untaggedServices: Set[Service] = DiscoveryState.dummyUntaggedServices
) {
  def updated(evt: DiscoveryEvent): DiscoveryState = {
    evt match {
      case ApplianceAdded(appliance) =>
        val uniqueId = appliance.services.foldLeft(""){(a,switch) => a + switch.address}

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

      case ServiceDiscovered(services) =>
        val newServices = services.filter(service => !(allServices contains service.address))

        val servicesMap = newServices.map{
          service => (service.address, service)
        }.toMap[String, Service]

        copy(appliances, allServices ++ servicesMap, untaggedServices ++ newServices)
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

    case GetAllServices =>
      val allServices = state.allServices.values.toList
      log.debug(s"${self.path.name} - Replying with all services : $allServices")
      sender ! allServices

    case GetAppliances =>
      log.debug(s"${self.path.name} - Replying with list of appliances : ${state.appliances}")
      sender ! state.appliances

    case Connected ⇒
      log.debug(s"Succesfully connected to MQTT at $mqttHost:$mqttPort")
      log.debug(s"Ready to publish to topic [ $localPublisher ]")
      sender() ! Subscribe(Vector(Tuple2("/service/+/data", {AtMostOnce})), 1)
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

    case GetAllServices =>
      val allServices = state.allServices.values
      log.debug(s"${self.path.name} - Replying with all services : $allServices")
      sender ! allServices

    case GetAppliances =>
      log.debug(s"${self.path.name} - Replying with list of appliances : ${state.appliances}")
      sender ! state.appliances

    case Snap => saveSnapshot(state)

    case UpdateDeviceState(widgetStatus) ⇒
      val service = state.allServices.get(widgetStatus.address).get
      val serviceRequest = service.processWidget(widgetStatus)

      log.debug(s"Sending service ${serviceRequest.address}, ${serviceRequest.request}, ${serviceRequest.data}")

      mqttManager !
        Publish(
          s"/service/${serviceRequest.address}/cmd",
          Json.toJson(serviceRequest).toString().getBytes("UTF-8").to[Vector])

    case Message(topic, payload) ⇒
      val message = new String(payload.to[Array], "UTF-8")
      println(s"[$topic] $message")

      val addressParserRegex = """^/service/([^/]+)/data$""".r

      topic match {
        case addressParserRegex(address) =>
          Json.parse(message).validate[ServiceResponse].map{
            serviceResponse =>
              val service =
                if(state.allServices contains address){
                  state.allServices get address get
                } else if (serviceResponse.response == "discover-service") {
                  val deviceService = new Service(address, "Device", "some", "device") with DeviceProfile
                  updateState(new ServiceDiscovered(deviceService +: List.empty))
                  deviceService
                }

              service match {
                case _:DeviceProfile =>
                  service.asInstanceOf[DeviceProfile].processResponse(
                    serviceResponse,
                    newServices => updateState(new ServiceDiscovered(newServices.asInstanceOf[List[Service]]))
                  )
                case _:Profile =>
                  service.asInstanceOf[Profile].processResponse(serviceResponse)
              }
          }
      }
  }
}