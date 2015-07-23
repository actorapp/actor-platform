package im.actor.server.enrich

import scala.concurrent.ExecutionContext

import slick.dbio._

import im.actor.api.rpc.Implicits._
import im.actor.api.rpc.messaging.{ Message, UpdateMessageContentChanged }
import im.actor.server.models.{ Peer, PeerType }
import im.actor.server.persist
import im.actor.server.push.SeqUpdatesManager._
import im.actor.server.push.SeqUpdatesManagerRegion
import im.actor.server.sequence.SeqState

object UpdateHandler {
  def getHandler(fromPeer: Peer, toPeer: Peer, randomId: Long)(
    implicit
    ec:                  ExecutionContext,
    seqUpdManagerRegion: SeqUpdatesManagerRegion
  ): UpdateHandler =
    toPeer.typ match {
      case PeerType.Group   ⇒ new GroupHandler(toPeer, randomId)
      case PeerType.Private ⇒ new PrivateHandler(fromPeer, toPeer, randomId)
    }
}

abstract class UpdateHandler(val randomId: Long) {
  def handleDbUpdate(message: Message): DBIO[Int]

  def handleUpdate(message: Message): DBIO[Seq[SeqState]]
}

class PrivateHandler(fromPeer: Peer, toPeer: Peer, randomId: Long)(
  implicit
  ec:                  ExecutionContext,
  seqUpdManagerRegion: SeqUpdatesManagerRegion
) extends UpdateHandler(randomId) {
  require(fromPeer.typ == PeerType.Private
    && toPeer.typ == PeerType.Private, "Peers must be private")

  def handleUpdate(message: Message): DBIO[Seq[SeqState]] =
    for {
      fromUpdate ← broadcastUserUpdate(
        fromPeer.id,
        UpdateMessageContentChanged(toPeer.asStruct, randomId, message), None
      )
      toUpdate ← broadcastUserUpdate(
        toPeer.id,
        UpdateMessageContentChanged(fromPeer.asStruct, randomId, message), None
      )
    } yield Seq(fromUpdate, toUpdate).flatten

  def handleDbUpdate(message: Message): DBIO[Int] = persist.HistoryMessage.updateContentAll(
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
  ec:                  ExecutionContext,
  seqUpdManagerRegion: SeqUpdatesManagerRegion
) extends UpdateHandler(randomId) {
  require(groupPeer.typ == PeerType.Group, "Peer must be a group")

  def handleUpdate(message: Message): DBIO[Seq[SeqState]] = {
    val update = UpdateMessageContentChanged(groupPeer.asStruct, randomId, message)
    for {
      usersIds ← persist.GroupUser.findUserIds(groupPeer.id)
      seqstate ← broadcastUsersUpdate(usersIds.toSet, update, None)
    } yield seqstate
  }

  def handleDbUpdate(message: Message): DBIO[Int] =
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