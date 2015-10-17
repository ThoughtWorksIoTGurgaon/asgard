package model

import play.api.libs.json.Json

case class ServiceRequest(
  address: String,
  request: String,
  data: String
)

object ServiceRequest {
  implicit val serviceRequestFormat = Json.format[ServiceRequest]
}