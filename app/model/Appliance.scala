package model

import play.api.libs.json.Json

case class Appliance(
  id: Option[String],
  title: String,
  description: String,
  services: List[SwitchService]
)

object Appliance {
  implicit val applianceFormat = Json.format[Appliance]
}