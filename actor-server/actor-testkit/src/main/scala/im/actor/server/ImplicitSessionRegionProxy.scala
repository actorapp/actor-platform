package im.actor.server

import akka.actor.{ ActorRef, ActorSystem }
import im.actor.server.session.{ Session, SessionRegion }

//todo: maybe extend ActorSerializerPrepare
trait ImplicitSessionRegionProxy {
  protected implicit val system: ActorSystem

  protected val mediator: ActorRef

  protected implicit lazy val sessionRegion: SessionRegion = Session.startRegionProxy()
}
