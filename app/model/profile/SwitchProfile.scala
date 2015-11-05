package model.profile

import model.{ServiceResponse, Profile, ServiceRequest, WidgetStatus}

trait SwitchProfile extends Profile{
  override def processWidget(widgetStatus: WidgetStatus): ServiceRequest = {
    createServiceRequest(if (widgetStatus.value == "on") "switch-on" else  "switch-off", "")
  }

  override def processResponse(serviceResponse: ServiceResponse, callback: (AnyRef) => Unit): WidgetStatus = {
    val value = serviceResponse.data
    updateValue(value)
    createWidgetStatus(value)
  }
}

object SwitchProfile{
  val id = "SWH"
  val widget = "toggle-button"
}