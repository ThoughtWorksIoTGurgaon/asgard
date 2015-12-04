package actor

import java.security.MessageDigest

import actor.DiscoverySupervisor._
import model.{Appliance, Service}
import model.profile.{DistanceProfile, SwitchProfile, SpeedProfile}

case object DiscoveryState {
  val service1 = Service.createService(SwitchProfile.id, "address:myid1")
  val service2 = Service.createService(DistanceProfile.id, "address:myid2")
  val service3 = Service.createService(SpeedProfile.id, "address:myid3")

  val allServices = Map[String, Service](
//    service1.address -> service1
//    , service2.address -> service2
//    , service3.address -> service3
  )

  val untaggedServices = Set[Service](
//    service2
  )

  val dummyAppliances = List[Appliance](
//    Appliance(
//      Some("1"),
//      Some("Fan"),
//      Some("Bed room"),
//      Set(service1, service3)
//    )
  )
}

case class DiscoveryState(
   appliances: List[Appliance] = DiscoveryState.dummyAppliances,
   allServices: Map[String, Service] = DiscoveryState.allServices,
   untaggedServices: Set[Service] = DiscoveryState.untaggedServices
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
          appliances.filter(_.id != updatedAppliance.id) :+ updatedAppliance,
          allServices,
          (untaggedServices ++ applianceToUpdate.services) -- updatedAppliance.services
        )

      case ApplianceDeleted(appliance) =>
        copy(appliances.filter(_.id != appliance.id), allServices, untaggedServices ++ appliance.services)

      case ServiceConfigured(service) =>
        val updatedService = allServices.get(service.address).get.updateLabel(service)

        copy(
          appliances,
          allServices + (updatedService.address -> updatedService),
          untaggedServices
        )

      case ServiceDiscovered(services) =>
        val newServices = services.toSet[Service] -- allServices.map(s => s._2).toSet[Service]

        copy(
          appliances,
          allServices ++ newServices.map(service => service.address -> service).toMap,
          untaggedServices ++ newServices
        )
    }
  }

  def sha(s: String) = {
    MessageDigest.getInstance("SHA-256").digest(s.getBytes).toString
  }

  def unAssginedServices:List[Service] = {
    allServices.filter(serviceKV => untaggedServices.contains(serviceKV._2)).map(s => s._2).toList
  }
}