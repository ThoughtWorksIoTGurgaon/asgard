package model

import play.api.libs.json.Json

case class Service(
	address: String,
	label: String,
	value: String,
	widget: String
) extends Profile{
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
}

object Service{
	implicit val serviceFormat = Json.format[Service]
}
