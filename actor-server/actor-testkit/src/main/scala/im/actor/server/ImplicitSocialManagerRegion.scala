package im.actor.server

import akka.actor.ActorSystem
import im.actor.server.db.DbExtension
import im.actor.server.social.{ SocialManager, SocialManagerRegion }

trait ImplicitSocialManagerRegion {
  protected implicit val system: ActorSystem

  protected implicit lazy val socialManagerRegion: SocialManagerRegion = SocialManager.startRegion()(system, DbExtension(system).db)
}
