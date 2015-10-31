package model.profile

import model.{Profile, ServiceRequest, WidgetStatus}

trait SwitchProfile extends Profile{
  override def processWidget(widgetStatus: WidgetStatus): ServiceRequest = {
    createServiceRequest(if (widgetStatus.value == "on") "switch-on" else  "switch-off", "")
  }
}

object SwitchProfile{
  val id = "SWH"
  val widget = "toggle-button"
}