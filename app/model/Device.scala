package model
import play.api.libs.json._
/*
 * @author syedatifakhtar
 */



trait CreateLog
case class Device(id: String,status: String) extends CreateLog
object Device {
  implicit val deviceFormat = Json.format[Device]
}