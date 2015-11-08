package im.actor.server

import akka.actor.{ ActorRef, ActorSystem }
import akka.stream.Materializer
import im.actor.server.session.{ SessionConfig, Session, SessionRegion }

trait ImplicitSessionRegion {
  protected implicit val system: ActorSystem
  protected implicit val materializer: Materializer

  private implicit lazy val config = SessionConfig.load(system.settings.config.getConfig("session"))
  protected implicit lazy val sessionRegion: SessionRegion = Session.startRegion(Session.props)
}
