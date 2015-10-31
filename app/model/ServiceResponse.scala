package model

import play.api.libs.json.Json

case class ServiceProfile(
  address: String,
  profileId: String
)

object ServiceProfile {
  implicit val serviceProfileFormat = Json.format[ServiceProfile]
}

case class ServiceResponse(
  response: String,
  data: Array[ServiceProfile]
) {

}

object ServiceResponse {
  implicit val serviceResponseFormat = Json.format[ServiceResponse]
}