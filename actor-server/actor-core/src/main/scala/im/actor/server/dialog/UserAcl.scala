package im.actor.server.dialog

import akka.actor.ActorSystem
import im.actor.server.db.DbExtension
import im.actor.server.model.Peer
import im.actor.server.persist.contact.UserContactRepo

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
      isBlocked ← DbExtension(system).db.run(UserContactRepo.isBlocked(contactOwnerUserId, contactUserId))
      result ← if (isBlocked) failed else default
    } yield result
  }

}