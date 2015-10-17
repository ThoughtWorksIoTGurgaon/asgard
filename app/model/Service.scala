package model

import play.api.libs.json.Json

trait Service{
	def updateStatus(value: String) : ServiceRequest

}
case class SwitchService(
	id: String,
	address: String,
	label: String,
	value: String
) extends Service{
	val widget = "radio-button"

	override def updateStatus(value: String): ServiceRequest = {
		if (value == "on")
			return new ServiceRequest(address, "switch-on", "")

		new ServiceRequest(address, "switch-off", "")
	}
}

object SwitchService{
	implicit val switchServiceFormat = Json.format[SwitchService]
}

case class SpeedService(
	id: String,
	address: String,
	label: String,
	value: String
) extends Service{
	val widget = "number-field"

	override def updateStatus(value: String): ServiceRequest = {
		new ServiceRequest(address, "change-speed", value)
	}
}