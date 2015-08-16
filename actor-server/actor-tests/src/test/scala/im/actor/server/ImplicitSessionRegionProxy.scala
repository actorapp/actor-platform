package im.actor.server

import akka.actor.{ ActorSystem, ActorRef }

import im.actor.server.session.{ SessionRegion, Session }

//todo: maybe extend ActorSerializerPrepare
trait ImplicitSessionRegionProxy {
  protected implicit val system: ActorSystem

  protected val mediator: ActorRef

  protected implicit lazy val sessionRegion: SessionRegion = Session.startRegionProxy()
}
