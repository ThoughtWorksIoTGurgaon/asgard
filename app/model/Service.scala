package model
import play.api.libs.json._

case class Service(address: String)
object Service {
  implicit val serviceFormat = Json.format[Service]
}
