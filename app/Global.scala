import actor.GlobalActorsModule
import play.api.GlobalSettings
import com.softwaremill.macwire._

object Global extends GlobalSettings with GlobalActorsModule{

  val wired = wiredInModule(Application)
  override def getControllerInstance[A](controllerClass: Class[A]) = wired.lookupSingleOrThrow(controllerClass)

}