package model.profile

import model.{Profile, ServiceRequest, WidgetStatus}

trait SpeedProfile extends Profile{
  override def processWidget(widgetStatus: WidgetStatus): ServiceRequest = {
    createServiceRequest("change-speed", widgetStatus.value)
  }
}

object SpeedProfile{
  val id = "SPE"
  val widget = "range-slider"
}
