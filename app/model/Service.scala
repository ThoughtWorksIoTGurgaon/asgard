package model

import model.profile._
import play.api.libs.json.Json

case class Service(
	address: String,
	var label: String,
	var value: String,
	widget: String
) extends Profile{
	def updateLabel(service: Service): Service = {
		this.label = service.label
		this
	}

	override def processWidget(widgetStatus: WidgetStatus): ServiceRequest = {
		createServiceRequest("default", widgetStatus.value)
	}
	override protected def createServiceRequest(request: String, data: String) : ServiceRequest = {
		new ServiceRequest(address, request, data)
	}

	override def processResponse(serviceResponse: ServiceResponse, callback: AnyRef => Unit = (_) => Unit): WidgetStatus = {
		callback.apply(serviceResponse)
		createWidgetStatus(serviceResponse.data)
	}

	override protected def createWidgetStatus(value: String) : WidgetStatus = {
		new WidgetStatus(address, value)
	}

	override protected def updateValue(value: String): Unit = {
		this.value = value
	}

	override def hashCode(): Int = address.hashCode()

	override def equals(obj: scala.Any): Boolean = {
		if (!obj.isInstanceOf[Service]){
			return false
		}

		this.address.equals(obj.asInstanceOf[Service].address)
	}
}

object Service{
	implicit val serviceFormat = Json.format[Service]

	def createService(profileId: String, address: String): Service ={
		profileId match {
			case DeviceProfile.id =>
				new Service(address, profileId, "10", DeviceProfile.widget) with DeviceProfile

			case SwitchProfile.id =>
				new Service(address, profileId, "off", SwitchProfile.widget) with SwitchProfile

			case SpeedProfile.id =>
				new Service(address, profileId, "10", SpeedProfile.widget) with SpeedProfile

			case DistanceProfile.id =>
				new Service(address, profileId, "10", DistanceProfile.widget) with DistanceProfile
		}
	}

	def createService(serviceProfile: ServiceProfile): Service ={
		createService(serviceProfile.profileId, serviceProfile.address)
	}
}
