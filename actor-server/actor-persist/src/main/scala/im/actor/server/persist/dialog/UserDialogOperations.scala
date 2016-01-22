package im.actor.server.persist.dialog

import com.github.tototoshi.slick.PostgresJodaSupport._
import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model.{ UserDialog, PeerType, Peer }
import im.actor.server.persist.GroupRepo

import org.joda.time.DateTime

import scala.concurrent.ExecutionContext

object UserDialogRepo {
  val userDialogs = TableQuery[UserDialogTable]

  val byPKC = Compiled(byPK _)
  val idByPeerTypeC = Compiled(idByPeerType _)

  val notArchived = userDialogs joinLeft GroupRepo.groups on (_.peerId === _.id) filter {
    case (usedDialog, groupOpt) ⇒ usedDialog.isArchived === false && groupOpt.map(!_.isHidden).getOrElse(true)
  } map (_._1)

  val notHiddenNotArchived = notArchived.filter(_.shownAt.isDefined)

  private def byPK(userId: Rep[Int], peerType: Rep[Int], peerId: Rep[Int]) =
    userDialogs.filter(u ⇒ u.userId === userId && u.peerType === peerType && u.peerId === peerId)

  private def byPeerType(userId: Rep[Int], peerType: Rep[Int]) =
    userDialogs.filter(u ⇒ u.userId === userId && u.peerType === peerType)

  private def idByPeerType(userId: Rep[Int], peerType: Rep[Int]) =
    byPeerType(userId, peerType).map(_.peerId)
}

trait UserDialogOperations {
  import UserDialogRepo._

  def findUsersVisible(userId: Rep[Int]) = notHiddenNotArchived.filter(_.userId === userId)

  def findGroupIds(userId: Int) =
    idByPeerTypeC((userId, PeerType.Group.value)).result

  def findUsers(userId: Int, peer: Peer): DBIO[Option[UserDialog]] =
    byPKC.applied((userId, peer.typ.value, peer.id)).result.headOption

  def usersExists(userId: Int, peer: Peer) =
    byPKC.applied((userId, peer.typ.value, peer.id)).exists.result

  def hide(userId: Int, peer: Peer) =
    byPKC.applied((userId, peer.typ.value, peer.id)).map(_.shownAt).update(None)

  def show(userId: Int, peer: Peer) =
    byPKC.applied((userId, peer.typ.value, peer.id)).map(_.shownAt).update(Some(new DateTime))

  def favourite(userId: Int, peer: Peer) =
    byPKC.applied((userId, peer.typ.value, peer.id)).map(_.isFavourite).update(true)

  def unfavourite(userId: Int, peer: Peer) =
    byPKC.applied((userId, peer.typ.value, peer.id)).map(_.isFavourite).update(false)

  def updateOwnerLastReceivedAt(userId: Int, peer: Peer, ownerLastReceivedAt: DateTime)(implicit ec: ExecutionContext) =
    byPKC.applied((userId, peer.typ.value, peer.id)).map(_.ownerLastReceivedAt).update(ownerLastReceivedAt)

  def updateOwnerLastReadAt(userId: Int, peer: Peer, ownerLastReadAt: DateTime)(implicit ec: ExecutionContext) =
    byPKC.applied((userId, peer.typ.value, peer.id)).map(_.ownerLastReadAt).update(ownerLastReadAt)

  def makeArchived(userId: Int, peer: Peer) =
    byPKC.applied((userId, peer.typ.value, peer.id)).map(_.isArchived).update(true)

  def delete(userId: Int, peer: Peer) =
    byPKC.applied((userId, peer.typ.value, peer.id)).delete

}
