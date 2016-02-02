package im.actor.server.enrich

import akka.actor.ActorSystem
import im.actor.api.rpc.Implicits._
import im.actor.api.rpc.messaging.{ ApiMessage, UpdateMessageContentChanged }
import im.actor.server.model.{ Peer, PeerType }
import im.actor.server.persist.{ GroupUserRepo, HistoryMessageRepo }
import im.actor.server.sequence.SeqState
import im.actor.server.user.UserExtension
import slick.dbio._

object UpdateHandler {
  def getHandler(fromPeer: Peer, toPeer: Peer, randomId: Long)(implicit system: ActorSystem): UpdateHandler =
    toPeer.typ match {
      case PeerType.Group   ⇒ new GroupHandler(toPeer, randomId)
      case PeerType.Private ⇒ new PrivateHandler(fromPeer, toPeer, randomId)
      case unknown          ⇒ throw new RuntimeException(s"Unknown peer type $unknown")
    }
}

abstract class UpdateHandler(val randomId: Long) {
  def handleDbUpdate(message: ApiMessage): DBIO[Int]

  def handleUpdate(message: ApiMessage): DBIO[Seq[SeqState]]
}

class PrivateHandler(fromPeer: Peer, toPeer: Peer, randomId: Long)(implicit system: ActorSystem) extends UpdateHandler(randomId) {
  import system.dispatcher

  require(fromPeer.typ == PeerType.Private
    && toPeer.typ == PeerType.Private, "Peers must be private")

  def handleUpdate(message: ApiMessage): DBIO[Seq[SeqState]] =
    DBIO.from(for {
      fromUpdate ← UserExtension(system).broadcastUserUpdate(
        fromPeer.id,
        UpdateMessageContentChanged(toPeer.asStruct, randomId, message), None, false, reduceKey = None, deliveryId = Some(s"msgcontent_$randomId")
      )
      toUpdate ← UserExtension(system).broadcastUserUpdate(
        toPeer.id,
        UpdateMessageContentChanged(fromPeer.asStruct, randomId, message), None, false, reduceKey = None, deliveryId = Some(s"msgcontent_$randomId")
      )
    } yield Seq(fromUpdate, toUpdate))

  def handleDbUpdate(message: ApiMessage): DBIO[Int] = HistoryMessageRepo.updateContentAll(
    userIds = Set(fromPeer.id, toPeer.id),
    randomId = randomId,
    peerType = PeerType.Private,
    peerIds = Set(fromPeer.id, toPeer.id),
    messageContentHeader = message.header,
    messageContentData = message.toByteArray
  )
}

class GroupHandler(groupPeer: Peer, randomId: Long)(implicit system: ActorSystem) extends UpdateHandler(randomId) {
  import system.dispatcher

  require(groupPeer.typ == PeerType.Group, "Peer must be a group")

  def handleUpdate(message: ApiMessage): DBIO[Seq[SeqState]] = {
    val update = UpdateMessageContentChanged(groupPeer.asStruct, randomId, message)
    for {
      usersIds ← GroupUserRepo.findUserIds(groupPeer.id)
      seqstate ← DBIO.from(UserExtension(system).broadcastUsersUpdate(usersIds.toSet, update, None, false, deliveryId = Some(s"msgcontent_${randomId}")))
    } yield seqstate
  }

  def handleDbUpdate(message: ApiMessage): DBIO[Int] =
    for {
      usersIds ← GroupUserRepo.findUserIds(groupPeer.id)
      result ← HistoryMessageRepo.updateContentAll(
        userIds = usersIds.toSet,
        randomId = randomId,
        peerType = PeerType.Group,
        peerIds = Set(groupPeer.id),
        messageContentHeader = message.header,
        messageContentData = message.toByteArray
      )
    } yield result

}