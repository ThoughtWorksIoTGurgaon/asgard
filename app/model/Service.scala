package model

import play.api.libs.json.Json

trait Profile{
	def updateStatus(request: String, data: String) : ServiceRequest
}

trait SwitchProfile extends Profile{
	def processValue(value: String): ServiceRequest = {
		updateStatus(if (value == "on") "switch-on" else  "switch-off", "")
	}
}

object SwitchProfile{
	val widget = "toggle-button"
}

trait SpeedProfile extends Profile{
	def processValue(value: String): ServiceRequest = {
		updateStatus("change-speed", value)
	}
}

object SpeedProfile{
	val widget = "range-slider"
}

case class Service(
	address: String,
	label: String,
	value: String,
	widget: String
) extends Profile{
	override def updateStatus(request: String, data: String) : ServiceRequest = {
		new ServiceRequest(address, request, data)
	}
}

object Service{
	implicit val serviceFormat = Json.format[Service]
}
