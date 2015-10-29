package im.actor.server.sequence

import java.nio.ByteBuffer

import akka.actor._
import akka.pattern.ask
import akka.serialization.Serialization
import akka.util.Timeout
import com.google.protobuf.ByteString
import im.actor.api.rpc.messaging.{ ApiDialogGroup, UpdateMessage }
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.api.{ rpc ⇒ api }
import im.actor.serialization.ActorSerializer
import im.actor.server.db.DbExtension
import im.actor.server.models.sequence
import im.actor.server.{ models, persist ⇒ p }
import slick.dbio.DBIO

import scala.annotation.tailrec
import scala.concurrent._
import scala.concurrent.duration._

object SeqUpdatesManager {

  import SeqUpdatesManagerMessages._

  type Sequence = Int

  // TODO: configurable
  private implicit val OperationTimeout = Timeout(30.seconds)

  def register(): Unit = {
    ActorSerializer.register(60001, classOf[SeqState])
    ActorSerializer.register(60002, classOf[SeqStateDate])
  }

  def getSeqState(authId: Long)(implicit system: ActorSystem): Future[SeqState] = {
    val ext = SeqUpdatesExtension(system)
    ext.region.ref.ask(GetSeqState(authId))(OperationTimeout).mapTo[SeqState]
  }

  def persistAndPushUpdate(
    authId:     Long,
    update:     api.Update,
    pushText:   Option[String],
    isFat:      Boolean        = false,
    deliveryId: Option[String] = None
  )(implicit system: ActorSystem): Future[SeqState] = {
    val header = update.header
    val serializedData = update.toByteArray

    persistAndPushUpdate(authId, header, serializedData, updateRefs(update), pushText, getOriginPeer(update), isFat, deliveryId)
  }

  def persistAndPushUpdate(
    authId:         Long,
    header:         Int,
    serializedData: Array[Byte],
    refs:           UpdateRefs,
    pushText:       Option[String],
    originPeer:     Option[ApiPeer],
    isFat:          Boolean,
    deliveryId:     Option[String]
  )(implicit system: ActorSystem): Future[SeqState] =
    pushUpdateGetSeqState(authId, header, serializedData, refs, pushText, originPeer, isFat, deliveryId)

  def persistAndPushUpdates(
    authIds:    Set[Long],
    update:     api.Update,
    pushText:   Option[String],
    isFat:      Boolean        = false,
    deliveryId: Option[String] = None
  )(implicit system: ActorSystem): Future[Seq[SeqState]] = {
    val header = update.header
    val serializedData = update.toByteArray

    persistAndPushUpdates(authIds, header, serializedData, updateRefs(update), pushText, getOriginPeer(update), isFat, deliveryId)
  }

  def persistAndPushUpdates(
    authIds:        Set[Long],
    header:         Int,
    serializedData: Array[Byte],
    refs:           UpdateRefs,
    pushText:       Option[String],
    originPeer:     Option[ApiPeer],
    isFat:          Boolean,
    deliveryId:     Option[String]
  )(implicit system: ActorSystem): Future[Seq[SeqState]] = {
    import system.dispatcher
    Future.sequence(authIds.toSeq map { authId ⇒
      persistAndPushUpdate(authId, header, serializedData, refs, pushText, originPeer, isFat, deliveryId)
    })
  }

  def setPushCredentials(
    authId: Long,
    creds:  models.push.PushCredentials
  )(implicit system: ActorSystem): Unit = {
    val ext = SeqUpdatesExtension(system)
    val msg = creds match {
      case c: models.push.GooglePushCredentials ⇒
        PushCredentialsUpdated(authId).withGoogle(GooglePushCredentials(c.projectId, c.regId))
      case c: models.push.ApplePushCredentials ⇒
        PushCredentialsUpdated(authId).withApple(ApplePushCredentials(c.apnsKey, ByteString.copyFrom(c.token)))
    }

    ext.region.ref ! msg
  }

  def deletePushCredentials(authId: Long)(implicit system: ActorSystem): Unit = {
    val ext = SeqUpdatesExtension(system)
    ext.region.ref ! PushCredentialsDeleted(authId)
  }

  def deleteApplePushToken(token: Array[Byte])(implicit system: ActorSystem): Unit = {
    import system.dispatcher

    val ext = SeqUpdatesExtension(system)

    DbExtension(system).db.run(p.push.ApplePushCredentialsRepo.findByToken(token)) foreach { creds ⇒
      creds foreach { c ⇒
        ext.region.ref ! PushCredentialsDeleted(c.authId)
      }
    }
  }

  def getDifference(authId: Long, timestamp: Long, maxSizeInBytes: Long)(implicit ec: ExecutionContext): DBIO[(Vector[models.sequence.SeqUpdate], Boolean)] = {
    def run(state: Long, acc: Vector[models.sequence.SeqUpdate], currentSize: Long): DBIO[(Vector[models.sequence.SeqUpdate], Boolean)] = {
      p.sequence.SeqUpdateRepo.findAfter(authId, state).flatMap { updates ⇒
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
          if (newSize > maxSizeInBytes && acc.nonEmpty) {
            (acc, currSize, false)
          } else {
            run(t, acc :+ h, newSize)
          }
        case Vector() ⇒ (acc, currSize, true)
      }
    }
    run(updates, updateAcc, currentSize)
  }

  def peerRefs(peer: api.peers.ApiPeer): UpdateRefs = {
    if (peer.`type` == api.peers.ApiPeerType.Private) {
      UpdateRefs(Seq(peer.id), Seq.empty)
    } else {
      UpdateRefs(Seq.empty, Seq(peer.id))
    }
  }

  def peerRefs(dialogGroups: Seq[api.messaging.ApiDialogGroup]): UpdateRefs = {
    dialogGroups.view.flatMap(_.dialogs).foldLeft(UpdateRefs(Seq.empty, Seq.empty)) {
      case (UpdateRefs(userIds, groupIds), dialog) ⇒
        dialog.peer.`type` match {
          case ApiPeerType.Private ⇒ UpdateRefs(userIds :+ dialog.peer.id, groupIds)
          case ApiPeerType.Group   ⇒ UpdateRefs(userIds, groupIds :+ dialog.peer.id)
        }
    }
  }

  def updateRefs(update: api.Update): UpdateRefs = {

    val empty = UpdateRefs(Seq.empty, Seq.empty)
    def singleUser(userId: Int) = UpdateRefs(Seq(userId), Seq.empty)
    def singleGroup(groupId: Int) = UpdateRefs(Seq.empty, Seq(groupId))
    def userAndGroup(userId: Int, groupId: Int) = UpdateRefs(Seq(userId), Seq(groupId))
    def users(userIds: Set[Int]) = UpdateRefs(userIds.toSeq, Seq.empty)

    update match {
      case _: api.misc.UpdateConfig              ⇒ empty
      case _: api.configs.UpdateParameterChanged ⇒ empty
      case api.messaging.UpdateChatClear(peer) ⇒
        peer.`type` match {
          case ApiPeerType.Private ⇒ singleUser(peer.id)
          case ApiPeerType.Group   ⇒ singleGroup(peer.id)
        }
      case api.messaging.UpdateChatDelete(peer) ⇒
        peer.`type` match {
          case ApiPeerType.Private ⇒ singleUser(peer.id)
          case ApiPeerType.Group   ⇒ singleGroup(peer.id)
        }
      case api.messaging.UpdateMessage(peer, senderUserId, _, _, _) ⇒
        val refs = peerRefs(peer)
        refs.copy(userIds = refs.userIds :+ senderUserId)
      case api.messaging.UpdateMessageDelete(peer, _)                              ⇒ peerRefs(peer)
      case api.messaging.UpdateMessageRead(peer, _, _)                             ⇒ peerRefs(peer)
      case api.messaging.UpdateMessageReadByMe(peer, _)                            ⇒ peerRefs(peer)
      case api.messaging.UpdateMessageReceived(peer, _, _)                         ⇒ peerRefs(peer)
      case api.messaging.UpdateMessageSent(peer, _, _)                             ⇒ peerRefs(peer)
      case api.messaging.UpdateMessageContentChanged(peer, _, _)                   ⇒ peerRefs(peer)
      case api.messaging.UpdateMessageDateChanged(peer, _, _)                      ⇒ peerRefs(peer)
      case api.messaging.UpdateChatGroupsChanged(dialogs)                          ⇒ peerRefs(dialogs)
      case api.groups.UpdateGroupAvatarChanged(groupId, userId, _, _, _)           ⇒ userAndGroup(userId, groupId)
      case api.groups.UpdateGroupInvite(groupId, inviteUserId, _, _)               ⇒ userAndGroup(inviteUserId, groupId)
      case api.groups.UpdateGroupMembersUpdate(groupId, members)                   ⇒ UpdateRefs((members.map(_.userId).toSet ++ members.map(_.inviterUserId).toSet).toSeq, Seq(groupId)) // TODO: #perf use foldLeft
      case api.groups.UpdateGroupTitleChanged(groupId, userId, _, _, _)            ⇒ userAndGroup(userId, groupId)
      case api.groups.UpdateGroupUserInvited(groupId, userId, inviterUserId, _, _) ⇒ UpdateRefs(Seq(userId, inviterUserId), Seq(groupId))
      case api.groups.UpdateGroupUserKick(groupId, userId, kickerUserId, _, _)     ⇒ UpdateRefs(Seq(userId, kickerUserId), Seq(groupId))
      case api.groups.UpdateGroupUserLeave(groupId, userId, _, _)                  ⇒ UpdateRefs(Seq(userId), Seq(groupId))
      case api.groups.UpdateGroupAboutChanged(groupId, _)                          ⇒ singleGroup(groupId)
      case api.groups.UpdateGroupTopicChanged(groupId, _, userId, _, _)            ⇒ userAndGroup(userId, groupId)
      case api.contacts.UpdateContactRegistered(userId, _, _, _)                   ⇒ singleUser(userId)
      case api.contacts.UpdateContactsAdded(userIds)                               ⇒ users(userIds.toSet)
      case api.contacts.UpdateContactsRemoved(userIds)                             ⇒ users(userIds.toSet)
      case api.users.UpdateUserAvatarChanged(userId, _)                            ⇒ singleUser(userId)
      case api.users.UpdateUserContactsChanged(userId, _)                          ⇒ singleUser(userId)
      case api.users.UpdateUserLocalNameChanged(userId, _)                         ⇒ singleUser(userId)
      case api.users.UpdateUserNameChanged(userId, _)                              ⇒ singleUser(userId)
      case api.users.UpdateUserNickChanged(userId, _)                              ⇒ singleUser(userId)
      case api.users.UpdateUserAboutChanged(userId, _)                             ⇒ singleUser(userId)
      case api.users.UpdateUserPreferredLanguagesChanged(userId, _)                ⇒ singleUser(userId)
      case api.users.UpdateUserTimeZoneChanged(userId, _)                          ⇒ singleUser(userId)
      case api.weak.UpdateGroupOnline(groupId, _)                                  ⇒ singleGroup(groupId)
      case api.weak.UpdateTyping(peer, userId, _) ⇒
        val refs = peerRefs(peer)
        refs.copy(userIds = refs.userIds :+ userId)
      case api.weak.UpdateUserLastSeen(userId, _) ⇒ singleUser(userId)
      case api.weak.UpdateUserOffline(userId)     ⇒ singleUser(userId)
      case api.weak.UpdateUserOnline(userId)      ⇒ singleUser(userId)
      case api.counters.UpdateCountersChanged(_)  ⇒ empty
    }
  }

  def getFatMetaData(update: api.Update): FatMetaData = {
    val UpdateRefs(userIds, groupIds) = updateRefs(update)
    FatMetaData(userIds, groupIds)
  }

  def subscribe(authId: Long, consumer: ActorRef)(implicit ec: ExecutionContext, ext: SeqUpdatesExtension): Future[Unit] = {
    ext.region.ref.ask(Subscribe(authId, Serialization.serializedActorPath(consumer))).mapTo[SubscribeAck].map(_ ⇒ ())
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

  //todo: call seqUpdates extension here maybe?
  private def pushUpdateGetSeqState(
    authId:         Long,
    header:         Int,
    serializedData: Array[Byte],
    refs:           UpdateRefs,
    pushText:       Option[String],
    originPeer:     Option[ApiPeer],
    isFat:          Boolean,
    deliveryId:     Option[String]
  )(implicit system: ActorSystem): Future[SeqState] = {
    val ext = SeqUpdatesExtension(system)
    ext.region.ref.ask(
      PushUpdate(
        authId, deliveryId, header, ByteString.copyFrom(serializedData), refs, isFat, pushText, originPeer
      )
    ).mapTo[SeqState]
  }

  def getOriginPeer(update: api.Update): Option[ApiPeer] = {
    update match {
      case u: UpdateMessage ⇒ Some(u.peer)
      case _                ⇒ None
    }
  }
}
