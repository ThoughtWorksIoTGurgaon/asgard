package model

import play.api.libs.json.Json

trait Profile{
	def updateStatus(request: String, data: String) : ServiceRequest
}

trait SwitchProfile extends Profile{
	val widget = "radio-button"

	def processValue(value: String): ServiceRequest = {
		updateStatus(if (value == "on") "switch-on" else  "switch-off", "")
	}
}


trait SpeedProfile extends Profile{
	val widget = "number-field"

	def processValue(value: String): ServiceRequest = {
		updateStatus("change-speed", value)
	}
}

case class Service(
	address: String,
	label: String,
	value: String
) extends Profile{
	override def updateStatus(request: String, data: String) : ServiceRequest = {
		new ServiceRequest(address, "switch-off", "")
	}
}

object Service{
	implicit val serviceFormat = Json.format[Service]
}
