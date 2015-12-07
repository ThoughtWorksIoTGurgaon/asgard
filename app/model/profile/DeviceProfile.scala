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
    val newServices = serviceProfiles.map(serviceProfile => Service.createService(serviceProfile))

    callback.apply(newServices)
    createWidgetStatus("New services add")
  }
}

object DeviceProfile{
  val id = "DEV"
  val widget = "device-widget"
}