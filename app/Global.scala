import actor.GlobalActorsModule
import play.api.GlobalSettings
import com.softwaremill.macwire.wiredInModule


object Global extends GlobalSettings with GlobalActorsModule{

  lazy val wired = wiredInModule(Application)
  override def getControllerInstance[A](controllerClass: Class[A]) = wired.lookupSingleOrThrow(controllerClass)

}