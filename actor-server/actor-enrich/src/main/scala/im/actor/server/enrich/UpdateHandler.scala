package im.actor.server.enrich

import scala.concurrent.ExecutionContext

import akka.util.Timeout
import slick.dbio._

import im.actor.api.rpc.Implicits._
import im.actor.api.rpc.messaging.{ ApiMessage, UpdateMessageContentChanged }
import im.actor.server.models.{ Peer, PeerType }
import im.actor.server.persist
import im.actor.server.sequence.SeqUpdatesExtension
import im.actor.server.sequence.SeqState
import im.actor.server.user.{ UserOffice, UserViewRegion }

object UpdateHandler {
  def getHandler(fromPeer: Peer, toPeer: Peer, randomId: Long)(
    implicit
    ec:             ExecutionContext,
    timeout:        Timeout,
    userViewRegion: UserViewRegion,
    seqUpdExt:      SeqUpdatesExtension
  ): UpdateHandler =
    toPeer.typ match {
      case PeerType.Group   ⇒ new GroupHandler(toPeer, randomId)
      case PeerType.Private ⇒ new PrivateHandler(fromPeer, toPeer, randomId)
    }
}

abstract class UpdateHandler(val randomId: Long) {
  def handleDbUpdate(message: ApiMessage): DBIO[Int]

  def handleUpdate(message: ApiMessage): DBIO[Seq[SeqState]]
}

class PrivateHandler(fromPeer: Peer, toPeer: Peer, randomId: Long)(
  implicit
  ec:             ExecutionContext,
  timeout:        Timeout,
  userViewRegion: UserViewRegion,
  seqUpdExt:      SeqUpdatesExtension
) extends UpdateHandler(randomId) {
  require(fromPeer.typ == PeerType.Private
    && toPeer.typ == PeerType.Private, "Peers must be private")

  def handleUpdate(message: ApiMessage): DBIO[Seq[SeqState]] =
    DBIO.from(for {
      fromUpdate ← UserOffice.broadcastUserUpdate(
        fromPeer.id,
        UpdateMessageContentChanged(toPeer.asStruct, randomId, message), None, false, deliveryId = Some(s"msgcontent_${randomId}")
      )
      toUpdate ← UserOffice.broadcastUserUpdate(
        toPeer.id,
        UpdateMessageContentChanged(fromPeer.asStruct, randomId, message), None, false, deliveryId = Some(s"msgcontent_${randomId}")
      )
    } yield Seq(fromUpdate, toUpdate).flatten)

  def handleDbUpdate(message: ApiMessage): DBIO[Int] = persist.HistoryMessage.updateContentAll(
    userIds = Set(fromPeer.id, toPeer.id),
    randomId = randomId,
    peerType = PeerType.Private,
    peerIds = Set(fromPeer.id, toPeer.id),
    messageContentHeader = message.header,
    messageContentData = message.toByteArray
  )
}

class GroupHandler(groupPeer: Peer, randomId: Long)(
  implicit
  ec:             ExecutionContext,
  timeout:        Timeout,
  userViewRegion: UserViewRegion,
  seqUpdExt:      SeqUpdatesExtension
) extends UpdateHandler(randomId) {
  require(groupPeer.typ == PeerType.Group, "Peer must be a group")

  def handleUpdate(message: ApiMessage): DBIO[Seq[SeqState]] = {
    val update = UpdateMessageContentChanged(groupPeer.asStruct, randomId, message)
    for {
      usersIds ← persist.GroupUser.findUserIds(groupPeer.id)
      seqstate ← DBIO.from(UserOffice.broadcastUsersUpdate(usersIds.toSet, update, None, false, deliveryId = Some(s"msgcontent_${randomId}")))
    } yield seqstate
  }

  def handleDbUpdate(message: ApiMessage): DBIO[Int] =
    for {
      usersIds ← persist.GroupUser.findUserIds(groupPeer.id)
      result ← persist.HistoryMessage.updateContentAll(
        userIds = usersIds.toSet,
        randomId = randomId,
        peerType = PeerType.Group,
        peerIds = Set(groupPeer.id),
        messageContentHeader = message.header,
        messageContentData = message.toByteArray
      )
    } yield result

}