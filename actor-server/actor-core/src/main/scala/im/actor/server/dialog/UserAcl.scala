package im.actor.server.dialog

import akka.actor.ActorSystem
import im.actor.server.db.DbExtension
import im.actor.server.model.Peer
import im.actor.server.persist.social.RelationRepo

import scala.concurrent.Future

trait UserAcl {

  protected val system: ActorSystem

  protected def withNonBlockedPeer[A](
    contactUserId: Int,
    peer:          Peer
  )(default: ⇒ Future[A], failed: ⇒ Future[A]): Future[A] = {
    if (peer.`type`.isGroup) {
      default
    } else {
      withNonBlockedUser(contactUserId, peer.id)(default, failed)
    }
  }

  protected def withNonBlockedUser[A](
    contactUserId:      Int,
    contactOwnerUserId: Int
  )(default: ⇒ Future[A], failed: ⇒ Future[A]): Future[A] = {
    import system.dispatcher
    for {
      isBlocked ← checkIsBlocked(contactUserId, contactOwnerUserId)
      result ← if (isBlocked) failed else default
    } yield result
  }

  protected def checkIsBlocked(contactUserId: Int, contactOwnerUserId: Int): Future[Boolean] =
    DbExtension(system).db.run(RelationRepo.isBlocked(contactOwnerUserId, contactUserId))
}