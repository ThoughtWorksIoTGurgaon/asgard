package model

trait Profile{
  def processWidget(widgetStatus: WidgetStatus): ServiceRequest
  protected def createServiceRequest(request: String, data: String) : ServiceRequest

  def processResponse(serviceResponse: ServiceResponse, callback: AnyRef => Unit = (_) => Unit) : WidgetStatus
  protected def createWidgetStatus(value: String) : WidgetStatus

  protected def updateValue(value: String)
}