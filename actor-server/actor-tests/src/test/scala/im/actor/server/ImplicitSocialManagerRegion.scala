package im.actor.server

import akka.actor.ActorSystem
import slick.driver.PostgresDriver.api.Database

import im.actor.server.social.{ SocialManager, SocialManagerRegion }

trait ImplicitSocialManagerRegion {
  protected implicit val system: ActorSystem
  protected implicit val db: Database

  protected implicit lazy val socialManagerRegion: SocialManagerRegion = SocialManager.startRegion()
}
