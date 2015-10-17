package model

import play.api.libs.json.Json

case class WidgetStatus(
  address: String,
  value: String
)

object WidgetStatus {
  implicit val widgetStatusFormat = Json.format[WidgetStatus]
}