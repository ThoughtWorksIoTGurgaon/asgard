package controller

import actor.DiscoverySupervisor
import actor.DiscoverySupervisor.{ApplianceConfigured, ApplianceAdded, UpdateDeviceState}
import akka.actor.ActorRef
import akka.pattern._
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import model._
import play.api.libs.json._
import play.api.mvc.{Action, Controller}


class MainController(val discoverySupervisor: ActorRef) extends Controller {

  implicit val akkaTimeout = akka.util.Timeout(1 minute)

  def getUnassignedServicesAction = Action.async {
    val getUntaggedServices = discoverySupervisor.ask(DiscoverySupervisor.GetUntaggedServices).mapTo[List[SwitchService]]
    getUntaggedServices.map {
      services =>
        Ok(Json.obj("services" -> Json.toJson(services)))
    }
  }


  def getAllAppliancesAction = Action.async {
    val getAppliances = discoverySupervisor.ask(DiscoverySupervisor.GetAppliances).mapTo[List[Appliance]]
    getAppliances.map {
      appliances =>
        Ok(Json.obj("appliances" -> Json.toJson(appliances)))
    }
  }


  def addApplianceAction() = Action(parse.json) {
    request =>
      println(s"Got some request!! ${request.body}")
      request.body.validate[Appliance].map {
        appliance =>
          discoverySupervisor ! ApplianceAdded(appliance)
          Ok("")
      }.recoverTotal(e=>BadRequest(s"Bad request $e"))
  }


  def updateApplianceAction() = Action(parse.json) {
    request =>
      println(s"Got some request!! ${request.body}")
      request.body.validate[Appliance].map {
        appliance =>
          discoverySupervisor ! ApplianceConfigured(appliance)
          Ok("")
      }.recoverTotal(e=>BadRequest(s"Bad request $e"))
  }
}