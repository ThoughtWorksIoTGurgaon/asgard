package model

import play.api.libs.json.{Writes, JsPath, Reads, Json}

import scalaz.Alpha.A


abstract class Service{
	def updateStatus(value: String) : ServiceRequest
	def id: String
}

case class SwitchService(
	val id: String,
	val address: String,
	val label: String,
	val value: String
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
	val id: String,
	val address: String,
	val label: String,
	val value: String
) extends Service{
	val widget = "number-field"

	override def updateStatus(value: String): ServiceRequest = {
		new ServiceRequest(address, "change-speed", value)
	}
}