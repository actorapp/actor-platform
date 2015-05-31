package im.actor.server.api.rpc.service.messaging

import scala.concurrent.{ ExecutionContext, Future }

import akka.actor.{ Status, ActorRef, ActorSystem, Props }
import akka.contrib.pattern.{ ClusterSharding, ShardRegion }
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import im.actor.api.PeersImplicits
import im.actor.api.rpc.messaging.{ Message ⇒ ApiMessage, _ }
import im.actor.api.rpc.peers.{ Peer, PeerType }
import im.actor.server.push.{ SeqUpdatesManager, SeqUpdatesManagerRegion }
import im.actor.server.util.{ HistoryUtils, UserUtils }
import im.actor.server.{ models, persist }

case class GroupPeerManagerRegion(ref: ActorRef)

object GroupPeerManager {
  import PeerManager._

  private val idExtractor: ShardRegion.IdExtractor = {
    case env @ Envelope(groupId, payload) ⇒ (groupId.toString, env)
  }

  private val shardResolver: ShardRegion.ShardResolver = msg ⇒ msg match {
    case Envelope(groupId, _) ⇒ (groupId % 100).toString // TODO: configurable
  }

  private def startRegion(props: Option[Props])(implicit system: ActorSystem): GroupPeerManagerRegion =
    GroupPeerManagerRegion(ClusterSharding(system).start(
      typeName = "GroupPeerManager",
      entryProps = props,
      idExtractor = idExtractor,
      shardResolver = shardResolver
    ))

  def startRegion()(
    implicit
    system:              ActorSystem,
    db:                  Database,
    seqUpdManagerRegion: SeqUpdatesManagerRegion
  ): GroupPeerManagerRegion =
    startRegion(Some(props))

  def startRegionProxy()(implicit system: ActorSystem): GroupPeerManagerRegion =
    startRegion(None)

  def props(
    implicit
    db:                  Database,
    seqUpdManagerRegion: SeqUpdatesManagerRegion
  ): Props =
    Props(classOf[GroupPeerManager], db, seqUpdManagerRegion)

  def sendMessage(userId: Int, senderUserId: Int, senderAuthId: Long, randomId: Long, date: DateTime, message: ApiMessage)(
    implicit
    peerManagerRegion: GroupPeerManagerRegion,
    timeout:           Timeout,
    ec:                ExecutionContext
  ): Future[SeqUpdatesManager.SequenceState] = {
    (peerManagerRegion.ref ? Envelope(userId, SendMessage(senderUserId, senderAuthId, randomId, date, message))).mapTo[SeqUpdatesManager.SequenceState]
  }

  def messageReceived(groupId: Int, receiverUserId: Int, date: Long, receivedDate: Long)(implicit peerManagerRegion: GroupPeerManagerRegion): Unit = {
    peerManagerRegion.ref ! Envelope(groupId, MessageReceived(receiverUserId, date, receivedDate))
  }

  def messageRead(groupId: Int, readerUserId: Int, date: Long, readDate: Long)(implicit peerManagerRegion: GroupPeerManagerRegion): Unit = {
    peerManagerRegion.ref ! Envelope(groupId, MessageRead(readerUserId, date, readDate))
  }
}

class GroupPeerManager(
  implicit
  db:                  Database,
  seqUpdManagerRegion: SeqUpdatesManagerRegion
) extends PeerManager with PeersImplicits {
  import PeerManager._
  import HistoryUtils._
  import SeqUpdatesManager._
  import UserUtils._

  implicit private val ec: ExecutionContext = context.dispatcher

  def receive = {
    case Envelope(groupId, SendMessage(senderUserId, senderAuthId, randomId, date, message)) ⇒
      val replyTo = sender()

      // TODO: create once #perf
      val groupPeer = Peer(PeerType.Group, groupId)

      val outUpdate = UpdateMessage(
        peer = groupPeer,
        senderUserId = senderUserId,
        date = date.getMillis,
        randomId = randomId,
        message = message
      )

      val clientUpdate = UpdateMessageSent(groupPeer, randomId, date.getMillis)

      db.run(for {
        _ ← broadcastGroupMessage(senderUserId, senderAuthId, groupId, outUpdate)
        seqstate ← persistAndPushUpdate(senderAuthId, clientUpdate, None)
      } yield {
        db.run(writeHistoryMessage(models.Peer.privat(senderUserId), groupPeer.asModel, date, randomId, message.header, message.toByteArray))
        seqstate
      }) pipeTo replyTo onFailure {
        case e ⇒
          replyTo ! Status.Failure(e)
          log.error(e, "Failed to send message")
      }
    case Envelope(groupId, MessageReceived(receiverUserId, date, receivedDate)) ⇒
      val update = UpdateMessageReceived(Peer(PeerType.Group, groupId), date, receivedDate)

      // TODO: #perf cache user ids

      db.run(for {
        otherGroupUserIds ← persist.GroupUser.findUserIds(groupId).map(_.filterNot(_ == receiverUserId).toSet)
        otherAuthIds ← persist.AuthId.findIdByUserIds(otherGroupUserIds).map(_.toSet)
        _ ← persistAndPushUpdates(otherAuthIds, update, None)
      } yield {
        db.run(markMessagesReceived(models.Peer.privat(receiverUserId), models.Peer.group(groupId), new DateTime(date)))
      }) onFailure {
        case e ⇒
          log.error(e, "Failed to mark messages received")
      }
    case Envelope(groupId, MessageRead(readerUserId, date, readDate)) ⇒
      val groupPeer = Peer(PeerType.Group, groupId)
      val update = UpdateMessageRead(groupPeer, date, readDate)
      val readerUpdate = UpdateMessageReadByMe(groupPeer, date)

      db.run(for {
        otherGroupUserIds ← persist.GroupUser.findUserIds(groupId).map(_.filterNot(_ == readerUserId).toSet)
        otherAuthIds ← persist.AuthId.findIdByUserIds(otherGroupUserIds).map(_.toSet)
        _ ← persistAndPushUpdates(otherAuthIds, update, None)
        _ ← broadcastUserUpdate(readerUserId, readerUpdate, None)
      } yield {
        // TODO: report errors
        db.run(markMessagesRead(models.Peer.privat(readerUserId), models.Peer.group(groupId), new DateTime(date)))
      }) onFailure {
        case e ⇒
          log.error(e, "Failed to mark messages read")
      }
  }

  private def broadcastGroupMessage(senderUserId: Int, senderAuthId: Long, groupId: Int, update: UpdateMessage) = {
    val updateHeader = update.header
    val updateData = update.toByteArray
    val (updateUserIds, updateGroupIds) = updateRefs(update)

    for {
      userIds ← persist.GroupUser.findUserIds(groupId)
      clientUser ← getUserUnsafe(senderUserId)
      seqstates ← DBIO.sequence(userIds.view.filterNot(_ == senderUserId) map { userId ⇒
        for {
          pushText ← getPushText(update.message, clientUser, userId)
          seqstates ← broadcastUserUpdate(userId, updateHeader, updateData, updateUserIds, updateGroupIds, Some(pushText), Some(Peer(PeerType.Group, groupId)), isFat = false)
        } yield seqstates
      }) map (_.flatten)
      selfseqstates ← notifyUserUpdate(senderUserId, senderAuthId, updateHeader, updateData, updateUserIds, updateGroupIds, None, None, isFat = false)
    } yield seqstates ++ selfseqstates
  }

}