package model
import play.api.libs.json._

case class Service(
	id: Option[String], 
	label: String, 
	address: String, 
	status: String, 
	requests: List[String] 
){
  @Override
  def equals(that: Service) = {
    if ((id isDefined) && (that.id.isDefined)) id.get == that.id.get else false
  }
}

object Service {
  implicit val serviceFormat = Json.format[Service]
}
