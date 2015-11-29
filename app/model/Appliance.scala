package model

import play.api.libs.json.Json

case class Appliance(
  id: Option[String],
  title: Option[String],
  description: Option[String],
  services: Set[Service]
)

object Appliance {
  implicit val applianceFormat = Json.format[Appliance]
}