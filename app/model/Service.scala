package model

import play.api.libs.json.Json

trait Profile{
	def processWidget(widgetStatus: WidgetStatus): ServiceRequest
	protected def updateStatus(address: String, request: String, data: String) : ServiceRequest = {
		new ServiceRequest(address, request, data)
	}
}

trait SwitchProfile extends Profile{
	override def processWidget(widgetStatus: WidgetStatus): ServiceRequest = {
		updateStatus(widgetStatus.address, if (widgetStatus.value == "on") "switch-on" else  "switch-off", "")
	}
}

object SwitchProfile{
	val id = "SWH"
	val widget = "toggle-button"
}

trait SpeedProfile extends Profile{
	override def processWidget(widgetStatus: WidgetStatus): ServiceRequest = {
		updateStatus(widgetStatus.address, "change-speed", widgetStatus.value)
	}
}

object SpeedProfile{
	val id = "SPE"
	val widget = "range-slider"
}

case class Service(
	address: String,
	label: String,
	value: String,
	widget: String
) extends Profile{
	override def processWidget(widgetStatus: WidgetStatus): ServiceRequest = {
		updateStatus(widgetStatus.address, "default", widgetStatus.value)
	}
}

object Service{
	implicit val serviceFormat = Json.format[Service]
}
