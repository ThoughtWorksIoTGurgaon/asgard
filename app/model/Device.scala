package model
import play.api.libs.json._

case class Device(id: String, title: String, description: String, services: List[Service])

object Device {
  implicit val deviceFormat = Json.format[Device]
}
