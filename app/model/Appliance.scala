package model

import play.api.libs.json.Json

case class Appliance(
  val id: Option[String],
  val title: Option[String],
  val description: Option[String],
  val services: List[SwitchService]
)

object Appliance {
  implicit val applianceFormat = Json.format[Appliance]
}