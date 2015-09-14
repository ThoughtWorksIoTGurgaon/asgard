package model
import play.api.libs.json._
/*
 * @author syedatifakhtar
 */

case class Device(label: String,state: Boolean,id: String,data: String,serviceAddress: String)
object Device {
  implicit val deviceFormat = Json.format[Device]
}
