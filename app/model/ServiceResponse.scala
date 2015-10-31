package model

import play.api.libs.json.Json

case class ServiceResponse(
  response: String,
  data: String
)

object ServiceResponse {
  implicit val serviceResponseFormat = Json.format[ServiceResponse]
}