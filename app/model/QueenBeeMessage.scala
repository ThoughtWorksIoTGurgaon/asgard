package model
import play.api.libs.json._

case class QueenBeeMessage(address: String, request: String, data: String)
object QueenBeeMessage{
  implicit val queenBeeMessageFormat = Json.format[QueenBeeMessage]
}
