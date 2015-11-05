package model.profile

import model.{ServiceResponse, Profile, ServiceRequest, WidgetStatus}

trait SpeedProfile extends Profile{
  override def processWidget(widgetStatus: WidgetStatus): ServiceRequest = {
    createServiceRequest("change-speed", widgetStatus.value)
  }

  override def processResponse(serviceResponse: ServiceResponse, callback: (AnyRef) => Unit): WidgetStatus = {
    val value = serviceResponse.data
    updateValue(value)
    createWidgetStatus(value)
  }
}

object SpeedProfile{
  val id = "SPE"
  val widget = "range-slider"
}
