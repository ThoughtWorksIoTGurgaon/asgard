package actor

import akka.actor.{ActorRef, Props}
import play.api.libs.concurrent.Akka
import play.api.Play.current
import com.softwaremill.macwire._


trait GlobalActorsModule {


  lazy val discoverySupervisor: ActorRef =
    Akka.system.actorOf(Props(wire[DiscoverySupervisor]), "DiscoverySupervisor")
}
