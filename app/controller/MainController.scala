package controller

import actor.DiscoverySupervisor
import actor.DiscoverySupervisor._
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
    val getUntaggedServices = discoverySupervisor
      .ask(DiscoverySupervisor.GetUntaggedServices)
      .mapTo[List[Service]]
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
      println(s"addApplianceAction got some request!! ${request.body}")
      request.body.validate[Appliance].map {
        appliance =>
          discoverySupervisor ! ApplianceAdded(appliance)
          Ok("")
      }.recoverTotal(e=>BadRequest(s"Bad request $e"))
  }

  def deleteApplianceAction() = Action(parse.json) {
    request =>
      println(s"deleteApplianceAction got some request!! ${request.body}")
      request.body.validate[Appliance].map {
        appliance =>
          discoverySupervisor ! ApplianceDeleted(appliance)
          Ok("")
      }.recoverTotal(e=>BadRequest(s"Bad request $e"))
  }


  def updateApplianceAction() = Action(parse.json) {
    request =>
      println(s"updateApplianceAction got some request!! ${request.body}")
      request.body.validate[Appliance].map {
        appliance =>
          discoverySupervisor ! ApplianceConfigured(appliance)
          Ok("")
      }.recoverTotal(e=>BadRequest(s"Bad request $e"))
  }


  def updateServiceAction() = Action(parse.json) {
    request =>
      println(s"updateServiceAction got some request!! ${request.body}")
      request.body.validate[Service].map {
        service =>
          discoverySupervisor ! ServiceConfigured(service)
          Ok("")
      }.recoverTotal(e=>BadRequest(s"Bad request $e"))
  }

  def updateServiceValueAction() = Action(parse.json) {
    request =>
      println(s"updateServiceValue got some request!! ${request.body}")
      request.body.validate[WidgetStatus].map {
        widgetStatus =>
          discoverySupervisor ! UpdateDeviceState(widgetStatus)
          Ok("")
      }.recoverTotal(e=>BadRequest(s"Bad request $e"))
  }

  def getAllServicesAction = Action.async {
    val getAllServices = discoverySupervisor
      .ask(DiscoverySupervisor.GetAllServices)
      .mapTo[List[Service]]
    getAllServices.map {
      services =>
        Ok(Json.obj("services" -> Json.toJson(services)))
    }
  }
}