package im.actor.server.push

import java.nio.ByteBuffer

import scala.annotation.tailrec
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.{ Tag ⇒ KryoTag }
import com.github.tototoshi.slick.PostgresJodaSupport._
import com.google.android.gcm.server.{ Sender ⇒ GCMSender }
import slick.dbio.DBIO

import im.actor.api.rpc.UpdateBox
import im.actor.api.rpc.messaging.UpdateMessage
import im.actor.api.rpc.peers.Peer
import im.actor.api.{ rpc ⇒ api }
import im.actor.server.models.sequence
import im.actor.server.{ models, persist ⇒ p }

object SeqUpdatesManager {

  @SerialVersionUID(1L)
  private[push] case class Envelope(authId: Long, payload: Message)

  private[push] sealed trait Message

  @SerialVersionUID(1L)
  private[push] case object GetSequenceState extends Message

  @SerialVersionUID(1L)
  private[push] case class PushUpdate(
    header:         Int,
    serializedData: Array[Byte],
    userIds:        Set[Int],
    groupIds:       Set[Int],
    pushText:       Option[String],
    originPeer:     Option[Peer],
    isFat:          Boolean
  ) extends Message

  @SerialVersionUID(1L)
  private[push] case class PushUpdateGetSequenceState(
    header:         Int,
    serializedData: Array[Byte],
    userIds:        Set[Int],
    groupIds:       Set[Int],
    pushText:       Option[String],
    originPeer:     Option[Peer],
    isFat:          Boolean
  ) extends Message

  @SerialVersionUID(1L)
  private[push] case class Subscribe(consumer: ActorRef) extends Message

  @SerialVersionUID(1L)
  private[push] case class SubscribeAck(consumer: ActorRef) extends Message

  @SerialVersionUID(1L)
  private[push] case class PushCredentialsUpdated(credsOpt: Option[models.push.PushCredentials]) extends Message

  @SerialVersionUID(1L)
  case class UpdateReceived(update: UpdateBox)

  type Sequence = Int
  type SequenceState = (Int, Array[Byte])
  type SequenceStateDate = (SequenceState, Long)

  // TODO: configurable
  private val OperationTimeout = Timeout(5.seconds)
  private val MaxDifferenceUpdates = 100

  def getSeqState(authId: Long)(implicit region: SeqUpdatesManagerRegion, ec: ExecutionContext): DBIO[(Sequence, Array[Byte])] = {
    for {
      seqstate ← DBIO.from(region.ref.ask(Envelope(authId, GetSequenceState))(OperationTimeout).mapTo[SequenceState])
    } yield seqstate
  }

  def persistAndPushUpdate(
    authId:         Long,
    header:         Int,
    serializedData: Array[Byte],
    userIds:        Set[Int],
    groupIds:       Set[Int],
    pushText:       Option[String],
    originPeer:     Option[Peer],
    isFat:          Boolean
  )(implicit region: SeqUpdatesManagerRegion, ec: ExecutionContext): DBIO[SequenceState] = {
    DBIO.from(pushUpdateGetSeqState(authId, header, serializedData, userIds, groupIds, pushText, originPeer, isFat))
  }

  def persistAndPushUpdate(authId: Long, update: api.Update, pushText: Option[String], isFat: Boolean = false)(implicit region: SeqUpdatesManagerRegion, ec: ExecutionContext): DBIO[SequenceState] = {
    val header = update.header
    val serializedData = update.toByteArray

    val (userIds, groupIds) = updateRefs(update)

    persistAndPushUpdate(authId, header, serializedData, userIds, groupIds, pushText, getOriginPeer(update), isFat)
  }

  def persistAndPushUpdates(authIds: Set[Long], update: api.Update, pushText: Option[String], isFat: Boolean = false)(implicit region: SeqUpdatesManagerRegion, ec: ExecutionContext): DBIO[Seq[SequenceState]] = {
    val header = update.header
    val serializedData = update.toByteArray

    val (userIds, groupIds) = updateRefs(update)

    persistAndPushUpdates(authIds, header, serializedData, userIds, groupIds, pushText, getOriginPeer(update), isFat)
  }

  def persistAndPushUpdates(
    authIds:        Set[Long],
    header:         Int,
    serializedData: Array[Byte],
    userIds:        Set[Int],
    groupIds:       Set[Int],
    pushText:       Option[String],
    originPeer:     Option[Peer],
    isFat:          Boolean
  )(implicit region: SeqUpdatesManagerRegion, ec: ExecutionContext): DBIO[Seq[SequenceState]] =
    DBIO.sequence(authIds.toSeq map (persistAndPushUpdate(_, header, serializedData, userIds, groupIds, pushText, originPeer, isFat)))

  def broadcastClientAndUsersUpdate(
    userIds:  Set[Int],
    update:   api.Update,
    pushText: Option[String],
    isFat:    Boolean        = false
  )(implicit
    region: SeqUpdatesManagerRegion,
    ec:     ExecutionContext,
    client: api.AuthorizedClientData): DBIO[(SequenceState, Seq[SequenceState])] =
    broadcastClientAndUsersUpdate(client.userId, client.authId, userIds, update, pushText, isFat)

  def broadcastClientAndUsersUpdate(
    clientUserId: Int,
    clientAuthId: Long,
    userIds:      Set[Int],
    update:       api.Update,
    pushText:     Option[String],
    isFat:        Boolean
  )(implicit
    region: SeqUpdatesManagerRegion,
    ec: ExecutionContext): DBIO[(SequenceState, Seq[SequenceState])] = {
    val header = update.header
    val serializedData = update.toByteArray
    val (refUserIds, refGroupIds) = updateRefs(update)

    val originPeer = getOriginPeer(update)

    for {
      authIds ← p.AuthId.findIdByUserIds(userIds + clientUserId)
      seqstates ← DBIO.sequence(
        authIds.view
          .filterNot(_ == clientAuthId)
          .map(persistAndPushUpdate(_, header, serializedData, refUserIds, refGroupIds, pushText, originPeer, isFat))
      )
      seqstate ← persistAndPushUpdate(clientAuthId, header, serializedData, refUserIds, refGroupIds, pushText, originPeer, isFat)
    } yield (seqstate, seqstates)
  }

  def broadcastUsersUpdate(
    userIds:  Set[Int],
    update:   api.Update,
    pushText: Option[String],
    isFat:    Boolean        = false
  )(implicit
    region: SeqUpdatesManagerRegion,
    ec: ExecutionContext): DBIO[Seq[SequenceState]] = {
    val header = update.header
    val serializedData = update.toByteArray
    val (refUserIds, refGroupIds) = updateRefs(update)

    val originPeer = getOriginPeer(update)

    for {
      authIds ← p.AuthId.findIdByUserIds(userIds)
      seqstates ← DBIO.sequence(
        authIds.map(persistAndPushUpdate(_, header, serializedData, refUserIds, refGroupIds, pushText, originPeer, isFat))
      )
    } yield seqstates
  }

  def broadcastUserUpdate(
    userId:   Int,
    update:   api.Update,
    pushText: Option[String],
    isFat:    Boolean        = false
  )(implicit
    region: SeqUpdatesManagerRegion,
    ec: ExecutionContext): DBIO[Seq[SequenceState]] = {
    val header = update.header
    val serializedData = update.toByteArray
    val (userIds, groupIds) = updateRefs(update)

    broadcastUserUpdate(userId, header, serializedData, userIds, groupIds, pushText, getOriginPeer(update), isFat)
  }

  def broadcastUserUpdate(
    userId:         Int,
    header:         Int,
    serializedData: Array[Byte],
    userIds:        Set[Int],
    groupIds:       Set[Int],
    pushText:       Option[String],
    originPeer:     Option[Peer],
    isFat:          Boolean
  )(implicit
    region: SeqUpdatesManagerRegion,
    ec: ExecutionContext): DBIO[Seq[SequenceState]] = {
    for {
      authIds ← p.AuthId.findIdByUserId(userId)
      seqstates ← DBIO.sequence(authIds map (persistAndPushUpdate(_, header, serializedData, userIds, groupIds, pushText, originPeer, isFat)))
    } yield seqstates
  }

  def broadcastClientUpdate(update: api.Update, pushText: Option[String], isFat: Boolean = false)(
    implicit
    region: SeqUpdatesManagerRegion,
    client: api.AuthorizedClientData,
    ec:     ExecutionContext
  ): DBIO[SequenceState] = broadcastClientUpdate(client.userId, client.authId, update, pushText, isFat)

  def broadcastClientUpdate(clientUserId: Int, clientAuthId: Long, update: api.Update, pushText: Option[String], isFat: Boolean)(
    implicit
    region: SeqUpdatesManagerRegion,
    ec:     ExecutionContext
  ): DBIO[SequenceState] = {
    val header = update.header
    val serializedData = update.toByteArray
    val (userIds, groupIds) = updateRefs(update)

    val originPeer = getOriginPeer(update)

    for {
      otherAuthIds ← p.AuthId.findIdByUserId(clientUserId).map(_.filter(_ != clientAuthId))
      _ ← DBIO.sequence(otherAuthIds map (authId ⇒ persistAndPushUpdate(authId, header, serializedData, userIds, groupIds, pushText, originPeer, isFat)))
      ownseqstate ← persistAndPushUpdate(clientAuthId, header, serializedData, userIds, groupIds, pushText, originPeer, isFat)
    } yield ownseqstate
  }

  def broadcastOtherDevicesUpdate(userId: Int, currentAuthId: Long, update: api.Update, pushText: Option[String], isFat: Boolean = false)(
    implicit
    region: SeqUpdatesManagerRegion,
    ec:     ExecutionContext
  ): DBIO[SequenceState] = {
    val header = update.header
    val serializedData = update.toByteArray
    val (userIds, groupIds) = updateRefs(update)

    val originPeer = getOriginPeer(update)

    for {
      otherAuthIds ← p.AuthId.findIdByUserId(userId).map(_.view.filter(_ != currentAuthId))
      _ ← DBIO.sequence(otherAuthIds map (authId ⇒ persistAndPushUpdate(authId, header, serializedData, userIds, groupIds, pushText, originPeer, isFat)))
      ownseqstate ← persistAndPushUpdate(currentAuthId, header, serializedData, userIds, groupIds, pushText, originPeer, isFat)
    } yield ownseqstate
  }

  def notifyUserUpdate(userId: Int, exceptAuthId: Long, update: api.Update, pushText: Option[String], isFat: Boolean = false)(
    implicit
    region: SeqUpdatesManagerRegion,
    ec:     ExecutionContext
  ): DBIO[Seq[SequenceState]] = {
    val header = update.header
    val serializedData = update.toByteArray
    val (userIds, groupIds) = updateRefs(update)

    val originPeer = getOriginPeer(update)

    notifyUserUpdate(userId, exceptAuthId, header, serializedData, userIds, groupIds, pushText, originPeer, isFat)
  }

  def notifyUserUpdate(
    userId:         Int,
    exceptAuthId:   Long,
    header:         Int,
    serializedData: Array[Byte],
    userIds:        Set[Int],
    groupIds:       Set[Int],
    pushText:       Option[String],
    originPeer:     Option[Peer],
    isFat:          Boolean
  )(implicit
    region: SeqUpdatesManagerRegion,
    ec: ExecutionContext) = {
    for {
      otherAuthIds ← p.AuthId.findIdByUserId(userId).map(_.view.filter(_ != exceptAuthId))
      seqstates ← DBIO.sequence(otherAuthIds map (authId ⇒ persistAndPushUpdate(authId, header, serializedData, userIds, groupIds, pushText, originPeer, isFat)))
    } yield seqstates
  }

  def notifyClientUpdate(update: api.Update, pushText: Option[String], isFat: Boolean = false)(
    implicit
    region: SeqUpdatesManagerRegion,
    client: api.AuthorizedClientData,
    ec:     ExecutionContext
  ): DBIO[Seq[SequenceState]] = {
    val header = update.header
    val serializedData = update.toByteArray
    val (userIds, groupIds) = updateRefs(update)

    val originPeer = getOriginPeer(update)

    notifyClientUpdate(header, serializedData, userIds, groupIds, pushText, originPeer, isFat)
  }

  def notifyClientUpdate(
    header:         Int,
    serializedData: Array[Byte],
    userIds:        Set[Int],
    groupIds:       Set[Int],
    pushText:       Option[String],
    originPeer:     Option[Peer],
    isFat:          Boolean
  )(implicit
    region: SeqUpdatesManagerRegion,
    client: api.AuthorizedClientData,
    ec:     ExecutionContext) = {
    notifyUserUpdate(client.userId, client.authId, header, serializedData, userIds, groupIds, pushText, originPeer, isFat)
  }

  def setPushCredentials(authId: Long, creds: models.push.PushCredentials)(implicit region: SeqUpdatesManagerRegion): Unit = {
    region.ref ! Envelope(authId, PushCredentialsUpdated(Some(creds)))
  }

  def deletePushCredentials(authId: Long)(implicit region: SeqUpdatesManagerRegion): Unit = {
    region.ref ! Envelope(authId, PushCredentialsUpdated(None))
  }

  def getDifference(authId: Long, timestamp: Long, maxSizeInBytes: Long)(implicit ec: ExecutionContext): DBIO[(Vector[models.sequence.SeqUpdate], Boolean)] = {
    def run(state: Long, acc: Vector[models.sequence.SeqUpdate], currentSize: Long): DBIO[(Vector[models.sequence.SeqUpdate], Boolean)] = {
      p.sequence.SeqUpdate.findAfter(authId, state).flatMap { updates ⇒
        if (updates.isEmpty) {
          DBIO.successful(acc → false)
        } else {
          val (newAcc, newSize, allFit) = append(updates.toVector, currentSize, maxSizeInBytes, acc)
          if (allFit) {
            newAcc.lastOption match {
              case Some(u) ⇒ run(u.timestamp, newAcc, newSize)
              case None    ⇒ DBIO.successful(acc → false)
            }
          } else {
            DBIO.successful(newAcc → true)
          }
        }
      }
    }
    run(timestamp, Vector.empty[sequence.SeqUpdate], 0L)
  }

  private def append(updates: Vector[sequence.SeqUpdate], currentSize: Long, maxSizeInBytes: Long, updateAcc: Vector[sequence.SeqUpdate]): (Vector[sequence.SeqUpdate], Long, Boolean) = {
    @tailrec
    def run(updLeft: Vector[sequence.SeqUpdate], acc: Vector[sequence.SeqUpdate], currSize: Long): (Vector[sequence.SeqUpdate], Long, Boolean) = {
      updLeft match {
        case h +: t ⇒
          val newSize = currSize + h.serializedData.length
          if (newSize > maxSizeInBytes) {
            (acc, currSize, false)
          } else {
            run(t, acc :+ h, newSize)
          }
        case Vector() ⇒ (acc, currSize, true)
      }
    }
    run(updates, updateAcc, currentSize)
  }

  def updateRefs(update: api.Update): (Set[Int], Set[Int]) = {
    def peerRefs(peer: api.peers.Peer): (Set[Int], Set[Int]) = {
      if (peer.`type` == api.peers.PeerType.Private) {
        (Set(peer.id), Set.empty)
      } else {
        (Set.empty, Set(peer.id))
      }
    }

    val empty = (Set.empty[Int], Set.empty[Int])
    def singleUser(userId: Int): (Set[Int], Set[Int]) = (Set(userId), Set.empty)
    def singleGroup(groupId: Int): (Set[Int], Set[Int]) = (Set.empty, Set(groupId))
    def users(userIds: Seq[Int]): (Set[Int], Set[Int]) = (userIds.toSet, Set.empty)

    update match {
      case _: api.misc.UpdateConfig              ⇒ empty
      case _: api.configs.UpdateParameterChanged ⇒ empty
      case api.messaging.UpdateChatClear(peer)   ⇒ (Set.empty, Set(peer.id))
      case api.messaging.UpdateChatDelete(peer)  ⇒ (Set.empty, Set(peer.id))
      case api.messaging.UpdateMessage(peer, senderUserId, _, _, _) ⇒
        val refs = peerRefs(peer)
        refs.copy(_1 = refs._1 + senderUserId)
      case api.messaging.UpdateMessageDelete(peer, _)                              ⇒ peerRefs(peer)
      case api.messaging.UpdateMessageRead(peer, _, _)                             ⇒ peerRefs(peer)
      case api.messaging.UpdateMessageReadByMe(peer, _)                            ⇒ peerRefs(peer)
      case api.messaging.UpdateMessageReceived(peer, _, _)                         ⇒ peerRefs(peer)
      case api.messaging.UpdateMessageSent(peer, _, _)                             ⇒ peerRefs(peer)
      case api.messaging.UpdateMessageContentChanged(peer, _, _)                   ⇒ peerRefs(peer)
      case api.messaging.UpdateMessageDateChanged(peer, _, _)                      ⇒ peerRefs(peer)
      case api.groups.UpdateGroupAvatarChanged(groupId, userId, _, _, _)           ⇒ (Set(userId), Set(groupId))
      case api.groups.UpdateGroupInvite(groupId, inviteUserId, _, _)               ⇒ (Set(inviteUserId), Set(groupId))
      case api.groups.UpdateGroupMembersUpdate(groupId, members)                   ⇒ (members.map(_.userId).toSet ++ members.map(_.inviterUserId).toSet, Set(groupId)) // TODO: #perf use foldLeft
      case api.groups.UpdateGroupTitleChanged(groupId, userId, _, _, _)            ⇒ (Set(userId), Set(groupId))
      case api.groups.UpdateGroupUserInvited(groupId, userId, inviterUserId, _, _) ⇒ (Set(userId, inviterUserId), Set(groupId))
      case api.groups.UpdateGroupUserKick(groupId, userId, kickerUserId, _, _)     ⇒ (Set(userId, kickerUserId), Set(groupId))
      case api.groups.UpdateGroupUserLeave(groupId, userId, _, _)                  ⇒ (Set(userId), Set(groupId))
      case api.contacts.UpdateContactRegistered(userId, _, _, _)                   ⇒ singleUser(userId)
      case api.contacts.UpdateContactsAdded(userIds)                               ⇒ users(userIds)
      case api.contacts.UpdateContactsRemoved(userIds)                             ⇒ users(userIds)
      case api.users.UpdateUserAvatarChanged(userId, _)                            ⇒ singleUser(userId)
      case api.users.UpdateUserContactsChanged(userId, _)                          ⇒ singleUser(userId)
      case api.users.UpdateUserLocalNameChanged(userId, _)                         ⇒ singleUser(userId)
      case api.users.UpdateUserNameChanged(userId, _)                              ⇒ singleUser(userId)
      case api.weak.UpdateGroupOnline(groupId, _)                                  ⇒ singleGroup(groupId)
      case api.weak.UpdateTyping(peer, userId, _) ⇒
        val refs = peerRefs(peer)
        refs.copy(_1 = refs._1 + userId)
      case api.weak.UpdateUserLastSeen(userId, _) ⇒ singleUser(userId)
      case api.weak.UpdateUserOffline(userId)     ⇒ singleUser(userId)
      case api.weak.UpdateUserOnline(userId)      ⇒ singleUser(userId)
      case api.calls.UpdateCallRing(user, _)      ⇒ singleUser(user.id)
      case api.calls.UpdateCallEnd(_)             ⇒ empty
    }
  }

  def bytesToTimestamp(bytes: Array[Byte]): Long = {
    if (bytes.isEmpty) {
      0L
    } else {
      ByteBuffer.wrap(bytes).getLong
    }
  }

  def timestampToBytes(timestamp: Long): Array[Byte] = {
    ByteBuffer.allocate(java.lang.Long.BYTES).putLong(timestamp).array()
  }

  private[push] def subscribe(authId: Long, consumer: ActorRef)(implicit region: SeqUpdatesManagerRegion, ec: ExecutionContext, timeout: Timeout): Future[Unit] = {
    region.ref.ask(Envelope(authId, Subscribe(consumer))).mapTo[SubscribeAck].map(_ ⇒ ())
  }

  private def pushUpdateGetSeqState(
    authId:         Long,
    header:         Int,
    serializedData: Array[Byte],
    userIds:        Set[Int],
    groupIds:       Set[Int],
    pushText:       Option[String],
    originPeer:     Option[Peer],
    isFat:          Boolean
  )(implicit region: SeqUpdatesManagerRegion): Future[SequenceState] = {
    region.ref.ask(Envelope(authId, PushUpdateGetSequenceState(header, serializedData, userIds, groupIds, pushText, originPeer, isFat)))(OperationTimeout).mapTo[SequenceState]
  }

  private def pushUpdate(
    authId:         Long,
    header:         Int,
    serializedData: Array[Byte],
    userIds:        Set[Int],
    groupIds:       Set[Int],
    pushText:       Option[String],
    originPeer:     Option[Peer],
    isFat:          Boolean
  )(implicit region: SeqUpdatesManagerRegion): Unit = {
    region.ref ! Envelope(authId, PushUpdate(header, serializedData, userIds, groupIds, pushText, originPeer, isFat))
  }

  private def getOriginPeer(update: api.Update): Option[Peer] = {
    update match {
      case u: UpdateMessage ⇒ Some(u.peer)
      case _                ⇒ None
    }
  }
}
