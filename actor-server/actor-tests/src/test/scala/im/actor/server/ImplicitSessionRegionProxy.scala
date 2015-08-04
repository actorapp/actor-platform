package im.actor.server

import akka.actor.{ ActorSystem, ActorRef }

import im.actor.server.session.{ SessionRegion, Session }

trait ImplicitSessionRegionProxy {
  protected implicit val system: ActorSystem

  protected val mediator: ActorRef

  protected implicit lazy val sessionRegion: SessionRegion = Session.startRegionProxy()
}
