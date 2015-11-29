package model.profile

import model.{ServiceResponse, Profile, WidgetStatus}

trait DistanceProfile extends Profile{
  override def processResponse(serviceResponse: ServiceResponse, callback: (AnyRef) => Unit): WidgetStatus = {
    val value = serviceResponse.data
    updateValue(value)
    createWidgetStatus(value)
  }
}

object DistanceProfile{
  val id = "DST"
  val widget = "text-label"
}
