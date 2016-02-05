package im.actor.server.persist.dialog

import com.github.tototoshi.slick.PostgresJodaSupport._
import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model.{ UserDialog, PeerType, Peer }

import org.joda.time.DateTime

import scala.concurrent.ExecutionContext

object UserDialogRepo {
  val userDialogs = TableQuery[UserDialogTable]

  val byPKC = Compiled(byPK _)
  val idByPeerTypeC = Compiled(idByPeerType _)

  val notArchived = userDialogs.filter(_.archivedAt.isEmpty)

  val notArchivedVisible = notArchived.filter(_.shownAt.isDefined)

  private def byPK(userId: Rep[Int], peerType: Rep[Int], peerId: Rep[Int]) =
    userDialogs.filter(u ⇒ u.userId === userId && u.peerType === peerType && u.peerId === peerId)

  private def byPeerType(userId: Rep[Int], peerType: Rep[Int]) =
    userDialogs.filter(u ⇒ u.userId === userId && u.peerType === peerType)

  private def idByPeerType(userId: Rep[Int], peerType: Rep[Int]) =
    byPeerType(userId, peerType).map(_.peerId)
}

trait UserDialogOperations {
  import UserDialogRepo._

  def findUsersVisible(userId: Rep[Int]) = notArchivedVisible.filter(_.userId === userId)

  def findGroupIds(userId: Int) =
    idByPeerTypeC((userId, PeerType.Group.value)).result

  def findUsers(userId: Int, peer: Peer): DBIO[Option[UserDialog]] =
    byPKC.applied((userId, peer.typ.value, peer.id)).result.headOption

  def usersExists(userId: Int, peer: Peer) =
    byPKC.applied((userId, peer.typ.value, peer.id)).exists.result

  def show(userId: Int, peer: Peer) =
    byPKC.applied((userId, peer.typ.value, peer.id)).map(d ⇒ (d.shownAt, d.archivedAt)).update((Some(new DateTime), None))

  def favourite(userId: Int, peer: Peer) =
    byPKC.applied((userId, peer.typ.value, peer.id)).map(_.isFavourite).update(true)

  def unfavourite(userId: Int, peer: Peer) =
    byPKC.applied((userId, peer.typ.value, peer.id)).map(_.isFavourite).update(false)

  def updateOwnerLastReceivedAt(userId: Int, peer: Peer, ownerLastReceivedAt: DateTime)(implicit ec: ExecutionContext) =
    byPKC.applied((userId, peer.typ.value, peer.id)).map(_.ownerLastReceivedAt).update(ownerLastReceivedAt)

  def updateOwnerLastReadAt(userId: Int, peer: Peer, ownerLastReadAt: DateTime)(implicit ec: ExecutionContext) =
    byPKC.applied((userId, peer.typ.value, peer.id)).map(_.ownerLastReadAt).update(ownerLastReadAt)

  def archive(userId: Int, peer: Peer) =
    byPKC.applied((userId, peer.typ.value, peer.id)).map(_.archivedAt).update(Some(new DateTime))

  def delete(userId: Int, peer: Peer) =
    byPKC.applied((userId, peer.typ.value, peer.id)).delete

}
