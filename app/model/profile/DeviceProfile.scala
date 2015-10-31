package model.profile

import model.{Service, WidgetStatus, ServiceResponse, Profile}

import play.api.libs.json.Json

case class ServiceProfile(
  address: String,
  profileId: String
)

object ServiceProfile {
  implicit val serviceProfileFormat = Json.format[ServiceProfile]
}

trait DeviceProfile extends Profile{
  override def processResponse(serviceResponse: ServiceResponse, callback: (AnyRef) => Unit): WidgetStatus = {
    val serviceProfiles = Json.parse(serviceResponse.data).validate[List[ServiceProfile]].get
    val newServices = serviceProfiles.map{
      serviceProfile => serviceProfile.profileId match {
        case SwitchProfile.id =>
          new Service(serviceProfile.address, serviceProfile.profileId, "", SwitchProfile.widget)
            with SwitchProfile

        case SpeedProfile.id =>
          new Service(serviceProfile.address, serviceProfile.profileId, "", SpeedProfile.widget)
            with SpeedProfile
      }
    }

    callback.apply(newServices)
    createWidgetStatus("New services add")
  }
}

object DeviceProfile