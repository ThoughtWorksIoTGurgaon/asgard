import actor.GlobalActorsModule
import controller.MainController
import com.softwaremill.macwire.wire

object Application extends GlobalActorsModule{

  lazy val mainController = wire[MainController]

}
