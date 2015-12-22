package im.actor.server.api.rpc.service.messaging

import akka.actor._
import im.actor.api.rpc.messaging._
import im.actor.server.db.DbExtension
import im.actor.server.dialog.DialogExtension
import im.actor.server.group.GroupExtension
import im.actor.server.social.{ SocialExtension, SocialManagerRegion }
import im.actor.server.user.UserExtension
import slick.driver.PostgresDriver.api._

object MessagingServiceImpl {
  def apply()(implicit actorSystem: ActorSystem): MessagingServiceImpl = new MessagingServiceImpl
}

final class MessagingServiceImpl(implicit protected val actorSystem: ActorSystem)
  extends MessagingService
  with MessagingHandlers
  with HistoryHandlers
  with ReactionsHandlers
  with FavouritesHandlers {
  protected val db: Database = DbExtension(actorSystem).db
  protected val userExt = UserExtension(actorSystem)
  protected val groupExt = GroupExtension(actorSystem)
  protected val dialogExt = DialogExtension(actorSystem)
  protected implicit val socialRegion: SocialManagerRegion = SocialExtension(actorSystem).region

}
